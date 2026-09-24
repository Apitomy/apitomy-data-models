package io.apitomy.umg.base.union;

import io.apitomy.umg.base.Any;

/**
 * A union value holding a list of entities, or of union values whose variants include entities
 * (for example a list of {@code boolean|Schema}). Only the elements that are nodes are traversed.
 */
public interface EntityListUnionValue<T extends Any> extends ListUnionValue<T> {

}
