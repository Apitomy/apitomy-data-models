package io.apitomy.umg.pipe.java;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import io.apitomy.umg.beans.SpecificationVersion;
import io.apitomy.umg.models.concept.EntityModel;
import io.apitomy.umg.models.concept.PropertyModel;
import io.apitomy.umg.models.concept.VisitorModel;
import io.apitomy.umg.pipe.java.method.BodyBuilder;

/**
 * Creates a traverser for each specification visitor interface.  A traverser is a visitor that
 * knows how to traverse the data model.
 * @author eric.wittmann@gmail.com
 */
public class CreateTraversersStage extends AbstractVisitorStage {

    @Override
    protected void doProcess() {
        // Create a visitor adapter for each spec version visitor
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVer -> {
            createTraverser(specVer);
        });
    }

    private void createTraverser(SpecificationVersion specVer) {
        VisitorModel visitor = getState().getConceptIndex().lookupVisitor(specVer.getNamespace());

        String traverserPackageName = getTraverserPackageName(specVer);
        String traverserClassName = getTraverserClassName(specVer);

        debug("Creating traverser: " + traverserClassName);

        // Create the traverser class
        JavaClassSource traverserSource = Roaster.create(JavaClassSource.class)
                .setPackage(traverserPackageName)
                .setName(traverserClassName)
                .setPublic();

        // Extend the AbstractTraverser class
        JavaClassSource abstractTraverserSource = getState().getJavaIndex().lookupClass(getAbstractTraverserFQN());
        traverserSource.addImport(abstractTraverserSource);
        traverserSource.extendSuperType(abstractTraverserSource);

        // Create the constructor
        JavaInterfaceSource rootVisitorJavaInterface = getState().getJavaIndex().lookupInterface(getRootVisitorInterfaceFQN());
        MethodSource<JavaClassSource> constructor = traverserSource.addMethod().setConstructor(true).setPublic();
        constructor.addParameter(rootVisitorJavaInterface, "visitor");
        constructor.setBody("super(visitor);");

        // Determine which visitors this adapter is implementing (only one right now)
        Set<VisitorModel> visitorsToImplement = Collections.singleton(visitor);

        // For each visitor, lookup its Java interface, add it to the list of interfaces implemented,
        // and then collect all methods in the interface (avoiding potential duplicates).
        List<MethodSource<?>> methodsToImplement = new LinkedList<MethodSource<?>>();
        Set<String> methodNames = new HashSet<>();
        for (VisitorModel visitorToImplement : visitorsToImplement) {
            JavaInterfaceSource vtiInterface = lookupJavaVisitor(visitorToImplement);
            if (vtiInterface == null) {
                warn("Visitor interface not found: " + visitorToImplement);
            }

            traverserSource.addImport(vtiInterface);
            traverserSource.addInterface(vtiInterface);

            // Add all methods to the list (but avoid duplicates).
            List<MethodSource<?>> allMethods = getAllMethodsForVisitorInterface(visitorToImplement);
            allMethods.forEach(method -> {
                if (!methodNames.contains(method.getName())) {
                    methodsToImplement.add(method);
                    methodNames.add(method.getName());
                }
            });
        }

        // Now create a traversing implementation for each visit method.
        methodsToImplement.forEach(method -> {
            MethodSource<JavaClassSource> methodSource = traverserSource.addMethod()
                    .setName(method.getName())
                    .setReturnTypeVoid()
                    .setPublic();

            // We know each visit method will have a single parameter.
            ParameterSource<?> param = method.getParameters().get(0);
            traverserSource.addImport(param.getType());
            methodSource.addParameter(param.getType().getSimpleName(), param.getName());
            methodSource.addAnnotation(Override.class);

            String entityNamespace = specVer.getNamespace();
            String entityName = method.getName().replace("visit", "");
            EntityModel entityModel = getState().getConceptIndex().lookupEntity(entityNamespace, entityName);

            String body = createTraversalMethodBody(entityModel, traverserSource);
            methodSource.setBody(body);
        });

        // Index the new class
        getState().getJavaIndex().index(traverserSource);
    }

    private String createTraversalMethodBody(EntityModel entityModel, JavaClassSource traverserSource) {
        JavaInterfaceSource javaEntity = lookupJavaEntity(entityModel);

        BodyBuilder body = new BodyBuilder();
        body.append("node.accept(this.visitor);");

        Collection<PropertyModel> allProperties = getState().getConceptIndex().getAllEntityProperties(entityModel).stream().map(property -> property.getProperty()).filter(property -> {
            return isEntity(property) || isEntityList(property) || isEntityMap(property) || isUnion(property);
        }).collect(Collectors.toList());

        if (!allProperties.isEmpty()) {
            body.addContext("entityType", javaEntity.getName());
            traverserSource.addImport(javaEntity);
            body.append("${entityType} model = (${entityType}) node;");
        }

        allProperties.forEach(_property -> {
            PropertyModel property = _property;

            body.addContext("propertyName", property.getName());
            body.addContext("propertyGetter", getterMethodName(property));

            if (isEntity(property)) {
                if (isStarProperty(_property)) {
                    body.append("this.traverseMappedNode(model);");
                } else if (isRegexProperty(_property)) {
                    body.append("this.traverseMap(null, model.${propertyGetter}());");
                } else {
                    body.append("this.traverseNode(\"${propertyName}\", model.${propertyGetter}());");
                }
            } else if (isEntityList(property)) {
                body.append("this.traverseList(\"${propertyName}\", model.${propertyGetter}());");
            } else if (isEntityMap(property)) {
                body.append("this.traverseMap(\"${propertyName}\", model.${propertyGetter}());");
            } else if (isUnion(property)) {
                body.append("this.traverseUnion(\"${propertyName}\", model.${propertyGetter}());");
            } else {
                warn("Unhandled property in traverser: " + property);
            }
        });

        return body.toString();
    }

}
