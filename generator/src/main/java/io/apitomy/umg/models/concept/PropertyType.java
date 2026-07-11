package io.apitomy.umg.models.concept;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.apitomy.umg.pipe.java.Util;
import lombok.Builder;
import lombok.Data;

/**
 * Models the type of a property.
 */
@Builder
@Data
public class PropertyType {

    public static PropertyType parse(String type) {
        Deque<PropertyType> stack = new ArrayDeque<>();
        PropertyType last = null;
        char[] charArray = type.toCharArray();
        readNextChar:
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                var current = stack.peek();
                switch (c) {
                    case '{':
                    case '[': {
                        if (current != null && current.simple) {
                            throw new ParserException(type, i, "Unexpected token '" + c + "'");
                        }
                        PropertyType nested = null;
                        if (c == '{') {
                            nested = PropertyType.builder().map(true).build();
                        }
                        if (c == '[') {
                            nested = PropertyType.builder().list(true).build();
                        }
                        stack.push(nested);
                        break;
                    }
                    case '}':
                    case ']': {
                        while (!stack.isEmpty()) {
                            current = stack.pop();
                            if (current.union) {
                                // {foo|bar}
                                //         ^
                                current.nested.add(last);
                            }
                            if ((c == '}' && current.map) || (c == ']' && current.list)) {
                                current.nested.add(last);
                                last = current;
                                continue readNextChar; // TODO refactor
                            }
                            last = current;
                        }
                        throw new ParserException(type, i, "Unexpected token '" + c + "'");
                    }
                    case '|': {
                        if (current == null || current.map || current.list) {
                            // {foo}|bar
                            //      ^
                            // {[foo]|
                            //       ^
                            var union = PropertyType.builder().union(true).build();
                            union.nested.add(last);
                            stack.push(union);
                        } else if (current.union) {
                            // There was a nested item
                            // {foo|{bar}|baz}
                            //           ^
                            current.nested.add(last);
                        } else if (current.simple) {
                            current = stack.pop();
                            last = current;
                            var after = stack.peek();
                            if (after != null && after.union) {
                                // There is already a union
                                // foo|bar|baz
                                //        ^
                                after.nested.add(current);
                            } else {
                                // foo|bar
                                //    ^
                                var union = PropertyType.builder().union(true).build();
                                union.nested.add(current);
                                stack.push(union);
                            }
                        }
                        break;
                    }
                    default: {
                        if (current == null || current.union || current.map || current.list) {
                            // foo
                            // ^
                            // foo|bar
                            //     ^
                            // {foo
                            //  ^
                            // [foo
                            //  ^
                            var simple = PropertyType.builder().simple(true).build();
                            simple.simpleType = "" + c;
                            stack.push(simple);
                        } else if (current.simple) {
                            // foo
                            //  ^
                            current.simpleType += c;
                        }
                        break;
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
        return last;
    }

    public static class ParserException extends RuntimeException {

        private static final long serialVersionUID = -3740161884930199054L;

        private static String message(String type, int position, String message) {
            var msg = "Parser error at:\n";
            msg += type + "\n";
            msg += " ".repeat(position) + "^\n";
            msg += message;
            return msg;
        }

        public ParserException(String type, int position, String message) {
            super(message(type, position, message));
        }
    }

    @Builder.Default
    private final Set<PropertyType> nested = new HashSet<>();

    @Builder.Default
    private String simpleType = "";

    private boolean list;
    private boolean map;
    private boolean union;

    // Simple type == not a container
    // TODO:
    // Do we want to add a distinction between:
    // - (simple) primitive type
    // - (simple) entity type
    // ?
    private boolean simple;

    public boolean isPrimitiveType() {
        return isSimple() && Util.PRIMITIVE_TYPE_MAP.containsKey(simpleType);
    }

    public boolean isEntityType() {
        return isSimple() && !isPrimitiveType();
    }

    public String asRawType() {
        if (isSimple()) {
            return simpleType;
        }
        if (isList()) {
            return "[" + this.getNested().iterator().next().asRawType() + "]";
        }
        if (isMap()) {
            return "{" + this.getNested().iterator().next().asRawType() + "}";
        }
        if (isUnion()) {
            return this.getNested().stream().map(nested -> nested.asRawType()).collect(Collectors.joining("|"));
        }
        throw new RuntimeException("Unsupported raw type: " + this);
    }

}
