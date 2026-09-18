package io.apitomy.datamodels.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defensive copies of collections, for getters and constructors that must not
 * hand out — or retain — a reference the caller can mutate.
 * <p>
 * These exist as a single helper rather than being inlined at each call site
 * because the pattern is constrained by transpilation. This code is compiled to
 * TypeScript by JSweet, whose runtime provides neither {@code List.copyOf} and
 * friends nor {@code java.util.Collections}, so the obvious spellings of "give
 * me an immutable copy" do not survive the TS build. Routing every such copy
 * through here keeps that constraint in one place: if the runtime later gains a
 * way to express immutability, only these methods change.
 * <p>
 * The three methods carry distinct names rather than being overloads, because
 * JSweet does not support overloading by parameter type — same-named overloads
 * collapse into one function and the call sites stop type-checking.
 * <p>
 * <b>The returned collections are copies, not immutable views.</b> Mutating a
 * returned collection cannot affect the object it came from, which is the
 * property callers actually depend on — but it is not rejected either.
 * Iteration order of the source is preserved.
 */
public final class CollectionUtil {

    private CollectionUtil() {
    }

    /**
     * A copy of the given list, or an empty list when it is {@code null}.
     *
     * @param source the list to copy
     * @return a copy the caller may safely retain
     */
    public static <T> List<T> copyOfList(List<T> source) {
        if (source == null) {
            return new ArrayList<T>();
        }
        return new ArrayList<T>(source);
    }

    /**
     * A copy of the given set, preserving iteration order, or an empty set when
     * it is {@code null}.
     *
     * @param source the set to copy
     * @return a copy the caller may safely retain
     */
    public static <T> Set<T> copyOfSet(Set<T> source) {
        Set<T> copy = new LinkedHashSet<T>();
        if (source == null) {
            return copy;
        }
        // Populated element by element rather than through the copy constructor:
        // the transpiler maps a set to a JS array, whose constructor takes a length
        // or a list of items, so the copy-constructor form does not survive.
        for (T element : source) {
            copy.add(element);
        }
        return copy;
    }

    /**
     * A copy of the given map, preserving iteration order, or an empty map when
     * it is {@code null}.
     *
     * @param source the map to copy
     * @return a copy the caller may safely retain
     */
    public static <K, V> Map<K, V> copyOfMap(Map<K, V> source) {
        if (source == null) {
            return new LinkedHashMap<K, V>();
        }
        return new LinkedHashMap<K, V>(source);
    }
}
