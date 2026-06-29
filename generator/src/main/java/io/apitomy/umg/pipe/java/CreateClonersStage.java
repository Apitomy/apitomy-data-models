package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModelWithOrigin;
import io.apitomy.umg.pipe.java.method.BodyBuilder;
import io.apitomy.umg.pipe.java.method.ClonerMethod;
import io.apitomy.umg.pipe.java.method.CodeBlock;
import io.apitomy.umg.pipe.java.method.PropertyCodeGen;
import io.apitomy.umg.pipe.java.method.cloner.CloneEntityPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneListPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.ClonePrimitivePropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneRegexPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneStarPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionListPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionMapPropertyBlock;
import io.apitomy.umg.pipe.java.method.cloner.CloneUnionPropertyBlock;

/**
 * Creates the deep-copy cloner classes. There is a bespoke cloner for each specification
 * version. Each cloner can clone any node in the tree by walking the source node and
 * copying property values directly into a new target node -- avoiding JSON serialization.
 */
public class CreateClonersStage extends AbstractIOStage {

    @Override
    protected String getPackageName(SpecificationVersion specVersion) {
        return getClonerPackageName(specVersion);
    }

    @Override
    protected String getClassName(SpecificationVersion specVersion) {
        return getClonerClassName(specVersion);
    }

    @Override
    protected void addImports(JavaClassSource classSource) {
        classSource.addImport(getState().getConfig().getRootNamespace() + ".util.JsonUtil");
    }

    @Override
    protected void createEntityMethod(SpecificationVersion specVersion,
            JavaClassSource classSource, EntityModel entityModel) {
        String entityFQN = getJavaEntityInterfaceFQN(entityModel);
        String cloneMethodName = ClonerMethod.methodName(entityModel.getName());

        debug("Creating clone method: " + cloneMethodName);

        JavaInterfaceSource javaEntity = getState().getJavaIndex().lookupInterface(entityFQN);
        if (javaEntity == null) {
            warn("Java interface for entity not found: " + entityFQN);
            return;
        }

        classSource.addImport(javaEntity);
        MethodSource<JavaClassSource> methodSource = classSource.addMethod()
                .setName(cloneMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntity.getName(), "source");
        methodSource.addParameter(javaEntity.getName(), "target");

        BodyBuilder body = new BodyBuilder();

        Collection<PropertyModelWithOrigin> allProperties =
                getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            body.clearContext();
            dispatchProperty(body, property, entityModel, classSource);
        });

        appendExtraPropertiesCode(body, classSource);

        methodSource.setBody(body.toString());
    }

    private void appendExtraPropertiesCode(BodyBuilder body, JavaClassSource classSource) {
        classSource.addImport(JsonNode.class);
        classSource.addImport(List.class);
        body.append("{");
        body.append("    List<String> extraPropertyNames = source.getExtraPropertyNames();");
        body.append("    if (extraPropertyNames != null) {");
        body.append("        extraPropertyNames.forEach(name -> {");
        body.append("            JsonNode value = source.getExtraProperty(name);");
        body.append("            if (value != null) {");
        body.append("                target.addExtraProperty(name, JsonUtil.clone(value));");
        body.append("            }");
        body.append("        });");
        body.append("    }");
        body.append("}");
    }

    @Override
    protected CodeBlock createPropertyBlock(PropertyBlockKind kind,
            PropertyCodeGen prop, JavaClassSource classSource) {
        switch (kind) {
            case STAR:       return new CloneStarPropertyBlock(prop, classSource);
            case REGEX:      return new CloneRegexPropertyBlock(prop, classSource);
            case UNION:      return new CloneUnionPropertyBlock(prop, classSource);
            case UNION_LIST: return new CloneUnionListPropertyBlock(prop, classSource);
            case UNION_MAP:  return new CloneUnionMapPropertyBlock(prop, classSource);
            case ENTITY:     return new CloneEntityPropertyBlock(prop, classSource);
            case PRIMITIVE:  return new ClonePrimitivePropertyBlock(prop);
            case LIST:       return new CloneListPropertyBlock(prop, classSource);
            case MAP:        return new CloneMapPropertyBlock(prop, classSource);
            default:         throw new IllegalArgumentException("Unknown block kind: " + kind);
        }
    }
}
