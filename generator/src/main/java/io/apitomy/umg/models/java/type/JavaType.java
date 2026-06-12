package io.apitomy.umg.models.java.type;

import io.apitomy.umg.models.concept.type.Type;
import org.jboss.forge.roaster.model.source.Importer;

/**
 * Represents a resolved Java type for code generation.
 * <p>
 * Each implementation wraps a concept {@link Type} and provides Java-specific
 * operations: type string generation, import management, and entity resolution.
 * <p>
 * Created by {@link JavaTypeFactory} from concept Types.
 */
public interface JavaType {

    /**
     * The underlying concept type.
     */
    Type getConceptType();

    /**
     * The Java type string for use in generated code (e.g., "String", "List&lt;Widget&gt;",
     * "BooleanWidgetUnion").
     */
    String toJavaTypeString();

    /**
     * Add necessary imports to the given Java source.
     */
    void addImportsTo(Importer<?> importer);

    /**
     * The simple name of this type (for method names, field names, etc.).
     * For entities: "Widget". For primitives: "String". For lists: "WidgetList".
     */
    String getSimpleName();
}
