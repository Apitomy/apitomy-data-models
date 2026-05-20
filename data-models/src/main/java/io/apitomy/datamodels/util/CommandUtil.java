package io.apitomy.datamodels.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apitomy.datamodels.cmd.ICommand;
import io.apitomy.datamodels.paths.NodePath;

/**
 * Utility class for marshalling (Command → JSON) and unmarshalling (JSON → Command) of
 * {@link ICommand} instances. Commands are serialized with a {@code __type} field identifying
 * the command class name, and all public fields are included in the JSON output.
 *
 * @author eric.wittmann@gmail.com
 */
public class CommandUtil {

    /**
     * Custom Jackson deserializer for {@link NodePath} fields. Converts a JSON string
     * representation (e.g. "/paths[/foo]/get") into a NodePath instance.
     */
    public static class NodePathDeserializer extends JsonDeserializer<NodePath> {
        @Override
        public NodePath deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            NodePath rval = null;
            String path = p.getText();
            if (path != null) {
                rval = NodePath.parse(path);
            }
            return rval;
        }
    }

    /**
     * Custom Jackson serializer for {@link NodePath} fields. Converts a NodePath instance
     * into its string representation.
     */
    public static class NodePathSerializer extends JsonSerializer<NodePath> {
        @Override
        public void serialize(NodePath value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toString());
            }
        }
    }

    /**
     * Custom Jackson deserializer for {@link ICommand} fields. Reads a JSON object with a
     * {@code __type} field and deserializes it into the appropriate command class. Used for
     * nested commands (e.g. in {@code AggregateCommand._commands}).
     */
    public static class ICommandDeserializer extends JsonDeserializer<ICommand> {
        @Override
        public ICommand deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            ObjectNode node = p.readValueAsTree();
            return unmarshall(node);
        }
    }

    /**
     * ObjectMapper used for unmarshalling (JSON → Command). Includes custom deserializers
     * for NodePath and ICommand types.
     */
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(NodePath.class, new NodePathDeserializer());
        module.addDeserializer(ICommand.class, new ICommandDeserializer());
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * ObjectMapper used for marshalling (Command → JSON). Includes a NodePath serializer
     * but does NOT include an ICommand serializer, to avoid infinite recursion when
     * serializing commands that contain nested commands. Nested ICommand fields are handled
     * explicitly in the {@link #marshall(ICommand)} method.
     */
    private static final ObjectMapper fieldMapper = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(NodePath.class, new NodePathSerializer());
        fieldMapper.registerModule(module);
        fieldMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Creates a new command instance by class name using reflection.
     * @param cmdType the simple class name of the command (e.g. "ChangeTitleCommand")
     * @return a new, empty command instance
     */
    public static ICommand create(String cmdType) {
        try {
            String cmdFQCN = "io.apitomy.datamodels.cmd.commands." + cmdType;
            Class<?> aClass = Class.forName(cmdFQCN);
            return (ICommand) aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unmarshalls a JSON object into an {@link ICommand} instance. The JSON must contain a
     * {@code __type} field identifying the command class name.
     * @param from the JSON object to deserialize
     * @return the deserialized command
     */
    public static ICommand unmarshall(ObjectNode from) {
        String type = from.get("__type").asText();
        if (type == null) {
            throw new RuntimeException("Missing __type from source data.");
        }

        try {
            Class<? extends ICommand> cmdClass = create(type).getClass();
            ICommand cmd = mapper.treeToValue((TreeNode) from, cmdClass);
            return cmd;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error unmarshalling command: " + type, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Missing command from unmarshalling factory: " + type, e);
        }
    }

    /**
     * Marshalls an {@link ICommand} instance into a JSON object. The resulting JSON includes
     * a {@code __type} field identifying the command class name, followed by all public fields.
     * Special field types are handled automatically:
     * <ul>
     *   <li>{@link NodePath} fields are serialized as strings</li>
     *   <li>Nested {@link ICommand} fields (e.g. in AggregateCommand) are recursively marshalled</li>
     * </ul>
     * @param command the command to serialize
     * @return the JSON representation of the command
     */
    public static ObjectNode marshall(ICommand command) {
        ObjectNode result = fieldMapper.createObjectNode();
        result.put("__type", command.type());

        // Serialize the command's fields using the fieldMapper (handles NodePath but not ICommand)
        ObjectNode fields = fieldMapper.valueToTree(command);
        fields.fields().forEachRemaining(entry -> {
            result.set(entry.getKey(), entry.getValue());
        });

        // Post-process any List<ICommand> fields by recursively marshalling each command.
        // This handles AggregateCommand._commands and any future nested command fields.
        marshallCommandListFields(command, result);

        return result;
    }

    /**
     * Scans the command's public fields for {@code List<ICommand>} types and replaces their
     * serialized values in the result with recursively marshalled command JSON objects.
     * @param command the original command instance
     * @param result the JSON node to update
     */
    private static void marshallCommandListFields(ICommand command, ObjectNode result) {
        for (Field field : command.getClass().getFields()) {
            if (List.class.isAssignableFrom(field.getType())) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericType;
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length == 1 && typeArgs[0] == ICommand.class) {
                        try {
                            @SuppressWarnings("unchecked")
                            List<ICommand> commands = (List<ICommand>) field.get(command);
                            if (commands != null) {
                                ArrayNode commandsArray = result.arrayNode();
                                for (ICommand subCommand : commands) {
                                    commandsArray.add(marshall(subCommand));
                                }
                                result.set(field.getName(), commandsArray);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Error marshalling command list field: " + field.getName(), e);
                        }
                    }
                }
            }
        }
    }

}
