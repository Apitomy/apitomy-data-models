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
     * <p>
     * Keyed by {@code String} specifically (not generic {@code <K, V>}): the
     * transpiler represents a {@code Map<String, V>} as a plain JS object with
     * its entries as direct string-keyed properties, and every operation here
     * (including this method's own {@code keySet()}/{@code get()}/{@code put()})
     * compiles against that representation consistently only when the key
     * type is concretely {@code String} at the call site -- a fully generic
     * {@code <K, V>} key type instead falls back to a distinct, incompatible
     * internal representation, silently losing every entry of a plain-object
     * source map passed through it.
     *
     * @param source the map to copy
     * @return a copy the caller may safely retain
     */
    public static <V> Map<String, V> copyOfMap(Map<String, V> source) {
        Map<String, V> copy = new LinkedHashMap<String, V>();
        if (source == null) {
            return copy;
        }
        for (String key : source.keySet()) {
            copy.put(key, source.get(key));
        }
        return copy;
    }
}
