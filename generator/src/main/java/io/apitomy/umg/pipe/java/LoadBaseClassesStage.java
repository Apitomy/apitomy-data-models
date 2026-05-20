package io.apitomy.umg.pipe.java;

import java.io.IOException;
import java.net.URL;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.PackagedSource;

import io.apitomy.umg.pipe.AbstractStage;

/**
 * Creates the i/o reader classes. There is a bespoke reader for each specification version.
 *
 * @author eric.wittmann@gmail.com
 */
public class LoadBaseClassesStage extends AbstractStage {

    @Override
    protected void doProcess() {
        try {
            loadBaseEnums(
                    "io.apitomy.umg.base.visitors.TraversalStepType",
                    "io.apitomy.umg.base.ParentPropertyType"
                    );
            loadBaseClasses(
                    "io.apitomy.umg.base.NodeImpl",
                    "io.apitomy.umg.base.RootNodeImpl",
                    "io.apitomy.umg.base.util.DataModelUtil",
                    "io.apitomy.umg.base.util.JsonUtil",
                    "io.apitomy.umg.base.util.ReaderUtil",
                    "io.apitomy.umg.base.util.WriterUtil",
                    "io.apitomy.umg.base.visitors.AbstractTraverser",
                    "io.apitomy.umg.base.visitors.TraversalStep",
                    "io.apitomy.umg.base.visitors.TraversalContextImpl",
                    "io.apitomy.umg.base.visitors.ReverseTraverser",
                    "io.apitomy.umg.base.union.BooleanUnionValueImpl",
                    "io.apitomy.umg.base.union.ListUnionValueImpl",
                    "io.apitomy.umg.base.union.MapUnionValueImpl",
                    "io.apitomy.umg.base.union.EntityListUnionValueImpl",
                    "io.apitomy.umg.base.union.EntityMapUnionValueImpl",
                    "io.apitomy.umg.base.union.PrimitiveUnionValueImpl",
                    "io.apitomy.umg.base.union.StringListUnionValueImpl",
                    "io.apitomy.umg.base.union.StringUnionValueImpl",
                    "io.apitomy.umg.base.union.ObjectUnionValueImpl",
                    "io.apitomy.umg.base.union.AnyUnionValueImpl",
                    "io.apitomy.umg.base.union.UnionValueImpl"
                    );
            loadBaseInterfaces(
                    "io.apitomy.umg.base.Node",
                    "io.apitomy.umg.base.MappedNode",
                    "io.apitomy.umg.base.RootNode",
                    "io.apitomy.umg.base.Visitable",
                    "io.apitomy.umg.base.visitors.Traverser",
                    "io.apitomy.umg.base.visitors.TraversalContext",
                    "io.apitomy.umg.base.visitors.TraversingVisitor",
                    "io.apitomy.umg.base.io.ModelReader",
                    "io.apitomy.umg.base.io.ModelWriter",
                    "io.apitomy.umg.base.union.BooleanUnionValue",
                    "io.apitomy.umg.base.union.EntityListUnionValue",
                    "io.apitomy.umg.base.union.EntityMapUnionValue",
                    "io.apitomy.umg.base.union.ListUnionValue",
                    "io.apitomy.umg.base.union.MapUnionValue",
                    "io.apitomy.umg.base.union.PrimitiveUnionValue",
                    "io.apitomy.umg.base.union.StringListUnionValue",
                    "io.apitomy.umg.base.union.StringUnionValue",
                    "io.apitomy.umg.base.union.ObjectUnionValue",
                    "io.apitomy.umg.base.union.AnyUnionValue",
                    "io.apitomy.umg.base.union.Union",
                    "io.apitomy.umg.base.union.UnionValue"
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadBaseEnums(String... enums) throws IOException {
        loadBaseSources(JavaEnumSource.class, enums);
    }

    private void loadBaseClasses(String... classes) throws IOException {
        loadBaseSources(JavaClassSource.class, classes);
    }

    private void loadBaseInterfaces(String... interfaces) throws IOException {
        loadBaseSources(JavaInterfaceSource.class, interfaces);
    }

    private <T extends JavaType<?>> void loadBaseSources(final Class<T> type, String... sources) throws IOException {
        for (String _source : sources) {
            debug("Including base source: " + _source);
            URL sourceUrl = getBaseSourceURL(_source);
            T source = Roaster.parse(type, sourceUrl);
            String targetPackageName = source.getPackage().replace("io.apitomy.umg.base", getState().getConfig().getRootNamespace());
            ((PackagedSource<?>) source).setPackage(targetPackageName);
            ((Importer<?>) source).getImports().forEach(_import -> {
                if (_import.getPackage().contains("io.apitomy.umg.base")) {
                    String newPackage = _import.getPackage().replace("io.apitomy.umg.base", getState().getConfig().getRootNamespace());
                    _import.setName(newPackage + "." + _import.getSimpleName());
                }
            });
            if (type.equals(JavaClassSource.class)) {
                getState().getJavaIndex().index((JavaClassSource) source);
            } else if (type.equals(JavaInterfaceSource.class)) {
                getState().getJavaIndex().index((JavaInterfaceSource) source);
            } else if (type.equals(JavaEnumSource.class)) {
                getState().getJavaIndex().index((JavaEnumSource) source);
            }
        }
    }

    private URL getBaseSourceURL(String _class) {
        return getClass().getClassLoader().getResource("base/" + _class.replace('.', '/') + ".java");
    }

}
