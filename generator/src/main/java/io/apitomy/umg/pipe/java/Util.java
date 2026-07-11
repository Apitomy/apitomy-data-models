package io.apitomy.umg.pipe.java;

import static java.util.Map.entry;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Util {

    public static Map<String, Class<?>> PRIMITIVE_TYPE_MAP = Map.ofEntries(
            entry("string", String.class),
            entry("boolean", Boolean.class),
            entry("number", Number.class),
            entry("integer", Integer.class),
            entry("object", ObjectNode.class),
            entry("any", JsonNode.class));

    public static Map<String, String> JAVA_KEYWORD_MAP = Map.ofEntries(
            entry("default", "_default"),
            entry("enum", "_enum"),
            entry("const", "_const"),
            entry("if", "_if"),
            entry("else", "_else"));
}
