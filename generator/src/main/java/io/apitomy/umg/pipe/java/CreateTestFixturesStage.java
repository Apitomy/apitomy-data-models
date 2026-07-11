package io.apitomy.umg.pipe.java;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.curiousoddman.rgxgen.RgxGen;

import com.github.curiousoddman.rgxgen.config.RgxGenProperties;
import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.NamespaceModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.PropertyType;
import io.apitomy.umg.pipe.AbstractStage;

public class CreateTestFixturesStage extends AbstractStage {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new SecureRandom();

    public static ObjectNode objectNode() {
        return factory.objectNode();
    }
    public static ArrayNode arrayNode() {
        return factory.arrayNode();
    }
    public static NullNode nullNode() {
        return factory.nullNode();
    }

    @Override
    protected void doProcess() {
        if (getState().getConfig().isGenerateTestFixtures()) {
            getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
                getState().getConceptIndex().findEntities(specVersion.getNamespace()).stream().filter(entity -> entity.isRoot()).forEach(entity -> {
                    generateIOFixture(specVersion, entity);
                });
            });
        } else {
            info("Skipping generation of test fixtures.");
        }
    }

    private void generateIOFixture(SpecificationVersion specVersion, EntityModel entity) {
        String filename = specVersion.getPrefix().toLowerCase() + "-full.json";
        File fixtureFile = new File(getState().getConfig().getTestOutputDirectory(), filename);
        Stack<String> entityStack = new Stack<>();
        ObjectNode allJson = generateEntityFixture(entity, entityStack);
        writeIOFixture(allJson, fixtureFile);
    }

    private ObjectNode generateEntityFixture(EntityModel entity, Stack<String> entityStack) {
        ObjectNode json = objectNode();
        if (entityStack.search(entity.getName()) != -1) {
            return json;
        } else {
            entityStack.push(entity.getName());
        }
        getState().getConceptIndex().getAllEntityProperties(entity).forEach(propertyWithOrigin -> {
            PropertyModel property = propertyWithOrigin.getProperty();
            JsonNode value = generatePropertyValue(entity, property, entityStack);
            if (value != null) {
                if (isStarProperty(property) || isRegexProperty(property)) {
                    ObjectNode wrapperObject = (ObjectNode) value;
                    wrapperObject.fieldNames().forEachRemaining((fieldName) -> {
                        json.set(fieldName, wrapperObject.get(fieldName));
                    });
                } else {
                    json.set(property.getName(), value);
                }
            }
        });
        entityStack.pop();
        return json;
    }

    private JsonNode generatePropertyValue(EntityModel entity, PropertyModel property, Stack<String> entityStack) {
        if (isStarProperty(property)) {
            PropertyType mappedType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            PropertyModel itemsProperty = PropertyModel.builder().name("_items").type(mappedType).build();
            return generatePropertyValue(entity, itemsProperty, entityStack);
        } else if (isRegexProperty(property)) {
            PropertyType mappedType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            PropertyModel itemsProperty = PropertyModel.builder().name(property.getCollection()).collection(property.getName()).type(mappedType).build();
            return generatePropertyValue(entity, itemsProperty, entityStack);
        } else if (isPrimitive(property)) {
            return generatePrimitiveValue(property);
        } else if (isPrimitiveList(property)) {
            return generatePrimitiveListValue(property);
        } else if (isPrimitiveMap(property)) {
            return generatePrimitiveMapValue(property);
        } else if (isEntity(property)) {
            EntityModel propertyEntity = resolveEntity(entity.getNamespace(), property.getType().getSimpleType());
            if (propertyEntity != null) {
                return generateEntityFixture(propertyEntity, entityStack);
            }
        } else if (isEntityList(property)) {
            PropertyType listEntityType = property.getType().getNested().iterator().next();
            EntityModel propertyEntity = resolveEntity(entity.getNamespace(), listEntityType.getSimpleType());
            if (propertyEntity != null) {
                return generateListValue(() -> {
                    ObjectNode object = generateEntityFixture(propertyEntity, entityStack);
                    return object;
                });
            }
        } else if (isEntityMap(property)) {
            PropertyType mapEntityType = property.getType().getNested().iterator().next();
            EntityModel propertyEntity = resolveEntity(entity.getNamespace(), mapEntityType.getSimpleType());
            if (propertyEntity != null) {
                return generateEntityMapValue(property, propertyEntity, entityStack);
            }
        }
        warn("Unhandled property: " + property);
        return null;
    }

    private EntityModel resolveEntity(NamespaceModel namespaceModel, String entityName) {
        String fqn = namespaceModel.fullName() + "." + entityName;
        return getState().getConceptIndex().lookupEntity(fqn);
    }

    private JsonNode generatePrimitiveValue(PropertyModel property) {
        switch (property.getType().getSimpleType()) {
            case "string":
                String val = UUID.randomUUID().toString().substring(10, 18);
                return factory.textNode(property.getName() + "-" + val);
            case "boolean":
                return factory.booleanNode(Boolean.TRUE);
            case "number":
                double value = ((randomInt() % 10000) * 10) + ((randomInt() % 8) + 1);
                value /= 100d;
                return factory.numberNode(value);
            case "integer":
                return factory.numberNode(randomInt() % 10l);
            case "object":
                ObjectNode object = factory.objectNode();
                object.set("foo", factory.textNode("bar"));
                object.set("baz", factory.booleanNode(false));
                return object;
            case "any":
                ArrayNode array = factory.arrayNode();
                array.add("one");
                array.add("two");
                array.add("three");
                return array;
        }
        warn("Property type not handled: " + property.getType());
        return null;
    }

    private JsonNode generatePrimitiveListValue(PropertyModel property) {
        PropertyModel primitiveProperty = PropertyModel.builder().name("_tmp").type(
                property.getType().getNested().iterator().next()).build();
        return generateListValue(() -> {
            return generatePrimitiveValue(primitiveProperty);
        });
    }

    private JsonNode generatePrimitiveMapValue(PropertyModel property) {
        PropertyModel primitiveProperty = PropertyModel.builder().name("_tmp").type(
                property.getType().getNested().iterator().next()).build();
        if (property.getCollection() != null) {
            RgxGen rgxGen = RgxGen.parse(extractRegex(property.getCollection()));
            return generateMapValue((index) -> {
                return rgxGen.generate(random);
            },() -> {
                return generatePrimitiveValue(primitiveProperty);
            });
        } else {
            return generateMapValue(() -> {
                return generatePrimitiveValue(primitiveProperty);
            });
        }
    }

    private JsonNode generateEntityMapValue(PropertyModel property, EntityModel propertyEntity, Stack<String> entityStack) {
        Supplier<JsonNode> supplier = () -> {
            ObjectNode object = generateEntityFixture(propertyEntity, entityStack);
            return object;
        };

        if (property.getCollection() != null) {
            RgxGen rgxGen = RgxGen.parse(extractRegex(property.getCollection()));
            return generateMapValue((index) -> {
                return rgxGen.generate(random);
            }, supplier);
        } else {
            return generateMapValue(supplier);
        }
    }

    private ArrayNode generateListValue(Supplier<JsonNode> supplier) {
        ArrayNode array = arrayNode();
        int numItems = (randomInt() % 5) + 1;
        for (int i = 0; i < numItems; i++) {
            JsonNode item = supplier.get();
            array.add(item);
        }
        return array;
    }

    private ObjectNode generateMapValue(Supplier<JsonNode> valueSupplier) {
        return generateMapValue((index) -> {
            return "key_" + index;
        }, valueSupplier);
    }

    private ObjectNode generateMapValue(Function<Integer, String> keySupplier, Supplier<JsonNode> valueSupplier) {
        ObjectNode object = objectNode();
        int numItems = (randomInt() % 5) + 1;
        for (int i = 0; i < numItems; i++) {
            String key = keySupplier.apply(i);
            JsonNode item = valueSupplier.get();
            object.set(key, item);
        }
        return object;
    }

    private void writeIOFixture(ObjectNode allJson, File fixtureFile) {
        if (!fixtureFile.getParentFile().exists()) {
            fixtureFile.getParentFile().mkdirs();
        }
        try {
            PrettyPrinter pp = new PrettyPrinter();
            mapper.writer(pp).writeValue(fixtureFile, allJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int randomInt() {
        return Math.abs(random.nextInt());
    }

    private static class PrettyPrinter extends MinimalPrettyPrinter {
        private static final long serialVersionUID = -4446121026177697380L;

        private int indentLevel = 0;

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeStartObject(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeStartObject(JsonGenerator g) throws IOException {
            super.writeStartObject(g);
            indentLevel++;
            g.writeRaw("\n");
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeEndObject(com.fasterxml.jackson.core.JsonGenerator, int)
         */
        @Override
        public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
            indentLevel--;
            g.writeRaw("\n");
            writeIndent(g);
            super.writeEndObject(g, nrOfEntries);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeStartArray(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeStartArray(JsonGenerator g) throws IOException {
            super.writeStartArray(g);
            indentLevel++;
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeEndArray(com.fasterxml.jackson.core.JsonGenerator, int)
         */
        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            g.writeRaw("\n");
            indentLevel--;
            writeIndent(g);
            super.writeEndArray(g, nrOfValues);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#beforeObjectEntries(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void beforeObjectEntries(JsonGenerator g) throws IOException {
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#beforeArrayValues(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void beforeArrayValues(JsonGenerator g) throws IOException {
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeArrayValueSeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
            super.writeArrayValueSeparator(g);
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeObjectEntrySeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeObjectEntrySeparator(JsonGenerator g) throws IOException {
            super.writeObjectEntrySeparator(g);
            g.writeRaw("\n");
            writeIndent(g);
        }

        /**
         * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter#writeObjectFieldValueSeparator(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            super.writeObjectFieldValueSeparator(g);
            g.writeRaw(" ");
        }

        private void writeIndent(JsonGenerator g) throws IOException {
            for (int idx = 0; idx < this.indentLevel; idx++) {
                g.writeRaw("    ");
            }
        }
    }

}
