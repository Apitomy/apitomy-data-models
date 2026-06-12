package io.apitomy.umg.models.concept.type;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses and represents a raw type string from the spec YAML (e.g., "{@code [Widget]}", "{@code Foo|Bar}").
 * <p>
 * Unlike {@link Type}, this is a pure parse tree with no semantic resolution —
 * entity names are just strings, not references to {@link io.apitomy.umg.models.concept.EntityModel}.
 * <p>
 * Compared to {@link io.apitomy.umg.models.concept.PropertyType}, this uses ordered {@code List}
 * instead of unordered {@code Set} for nested types, and sorts union variants alphabetically
 * for deterministic output.
 */
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RawType {

    public static RawType parse(String type) {
        Deque<RawType> stack = new ArrayDeque<>();
        RawType last = null;
        var charArray = type.toCharArray();
        readNextChar:
        for (var i = 0; i < charArray.length; i++) {
            var c = charArray[i];
            var current = stack.peek();
            switch (c) {
                case '{', '[' -> {
                    if (current != null && current.simple) {
                        throw new ParserException(type, i, "Unexpected token '" + c + "'");
                    }
                    var nested = c == '{' ? RawType.builder().map(true).build()
                            : RawType.builder().list(true).build();
                    stack.push(nested);
                }
                case '}', ']' -> {
                    while (!stack.isEmpty()) {
                        current = stack.pop();
                        if (current.union) {
                            current.nested.add(last);
                        }
                        if ((c == '}' && current.map) || (c == ']' && current.list)) {
                            current.nested.add(last);
                            last = current;
                            continue readNextChar;
                        }
                        last = current;
                    }
                    throw new ParserException(type, i, "Unexpected token '" + c + "'");
                }
                case '|' -> {
                    if (current == null || current.map || current.list) {
                        var union = RawType.builder().union(true).build();
                        union.nested.add(last);
                        stack.push(union);
                    } else if (current.union) {
                        current.nested.add(last);
                    } else if (current.simple) {
                        current = stack.pop();
                        last = current;
                        var after = stack.peek();
                        if (after != null && after.union) {
                            after.nested.add(current);
                        } else {
                            var union = RawType.builder().union(true).build();
                            union.nested.add(current);
                            stack.push(union);
                        }
                    }
                }
                default -> {
                    if (current == null || current.union || current.map || current.list) {
                        var simple = RawType.builder().simple(true).build();
                        simple.simpleType = "" + c;
                        stack.push(simple);
                    } else if (current.simple) {
                        current.simpleType += c;
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            var current = stack.pop();
            if (current.union) {
                current.nested.add(last);
            } else if (!current.simple) {
                throw new ParserException(type, type.length(), "Unexpected end of string");
            }
            last = current;
        }
        sort(last);
        return last;
    }

    private static void sort(RawType type) {
        type.getNested().forEach(RawType::sort);
        type.getNested().sort(Comparator.comparing(RawType::asRawType));
    }

    @Builder.Default
    @Getter
    private final List<RawType> nested = new ArrayList<>();

    @Builder.Default
    @Getter
    private String simpleType = "";

    @Getter
    private boolean list;

    @Getter
    private boolean map;

    @Getter
    private boolean union;

    private boolean simple;

    public boolean isSimple() { return simple; }

    public boolean isPrimitiveType() {
        return simple && PrimitiveType.isPrimitive(simpleType);
    }

    public boolean isEntityType() {
        return simple && !isPrimitiveType();
    }

    @EqualsAndHashCode.Include
    public String asRawType() {
        if (simple) return simpleType;
        if (list) return "[" + nested.get(0).asRawType() + "]";
        if (map) return "{" + nested.get(0).asRawType() + "}";
        if (union) return nested.stream().map(RawType::asRawType).collect(Collectors.joining("|"));
        throw new IllegalStateException("Unknown raw type structure");
    }

    @Override
    public String toString() {
        return asRawType();
    }

    public static class ParserException extends RuntimeException {

        public ParserException(String type, int position, String message) {
            super("Parser error at:\n" + type + "\n" + " ".repeat(position) + "^\n" + message);
        }
    }
}
