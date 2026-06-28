package io.apitomy.umg.pipe.java.method;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.apitomy.umg.logging.Logger;

public class BodyBuilder {

    private StringBuilder str = new StringBuilder();
    private Map<String, String> context = new HashMap<>();

    public static BodyBuilder create() {
        return new BodyBuilder();
    }

    public BodyBuilder c(String name, String value) {
        addContext(name, value);
        return this;
    }

    public void addContext(String name, String value) {
        if (value == null) {
            Logger.warn("[BodyBuilder] Adding null value to BodyBuilder for: " + name);
        } else {
            context.put(name, value);
        }
    }

    public void clearContext() {
        context.clear();
    }

    public void addContext(Map<String, String> values) {
        values.forEach(this::addContext);
    }

    public BodyBuilder a(String line) {
        append(line);
        return this;
    }

    public void append(String line) {
        String resolved = resolveVars(line);
        str.append(resolved);
        str.append("\n");
        if (resolved.contains("${")) {
            Logger.warn("[BodyBuilder] 'append' detected unresolved variables: " + resolved);
        }
    }

    /**
     * Append a multi-line text block with ${var} substitution.
     * Each line in the block is resolved and appended separately.
     * Leading/trailing blank lines from text block formatting are trimmed.
     */
    public void appendBlock(String textBlock) {
        String[] lines = textBlock.split("\n", -1);
        int start = 0;
        int end = lines.length;
        while (start < end && lines[start].isBlank()) start++;
        while (end > start && lines[end - 1].isBlank()) end--;
        for (int i = start; i < end; i++) {
            append(lines[i]);
        }
    }

    /**
     * Conditional code generation. Appends the result of the matching supplier.
     * Suppliers can perform side effects (e.g., adding imports) and return
     * a code string (single or multi-line) to append.
     */
    public void ifElse(boolean condition, Supplier<String> thenBlock, Supplier<String> elseBlock) {
        String result = condition ? thenBlock.get() : elseBlock.get();
        if (result != null && !result.isEmpty()) {
            appendBlock(result);
        }
    }

    /**
     * Conditional code generation without else branch.
     */
    public void ifTrue(boolean condition, Supplier<String> thenBlock) {
        if (condition) {
            String result = thenBlock.get();
            if (result != null && !result.isEmpty()) {
                appendBlock(result);
            }
        }
    }

    /**
     * Loop code generation with scoped context. For each item, the callback
     * receives a LoopContext (with loop-local variable scope) and the item.
     * The callback returns a code string to append for that iteration.
     *
     * @param items      collection to iterate
     * @param callback   receives (loopContext, item, isFirst) and returns code string
     */
    public <T> void forEach(Iterable<T> items, LoopCallback<T> callback) {
        Map<String, String> savedContext = new HashMap<>(context);
        boolean isFirst = true;
        for (T item : items) {
            LoopContext loopCtx = new LoopContext(this);
            String result = callback.generate(loopCtx, item, isFirst);
            if (result != null && !result.isEmpty()) {
                appendBlock(result);
            }
            // Restore parent context — loop-local additions are discarded
            context = new HashMap<>(savedContext);
            isFirst = false;
        }
    }

    @FunctionalInterface
    public interface LoopCallback<T> {
        String generate(LoopContext ctx, T item, boolean isFirst);
    }

    /**
     * Loop-scoped context. Inherits parent context variables but additions
     * are discarded after each iteration.
     */
    public static class LoopContext {
        private final BodyBuilder body;

        LoopContext(BodyBuilder body) {
            this.body = body;
        }

        public LoopContext set(String name, String value) {
            body.addContext(name, value);
            return this;
        }
    }

    private String resolveVars(String line) {
        String resolved = line;
        for (Entry<String, String> entry : context.entrySet()) {
            resolved = resolved.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }

    @Override
    public String toString() {
        return str.toString();
    }

}
