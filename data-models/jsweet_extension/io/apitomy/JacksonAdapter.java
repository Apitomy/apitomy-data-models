/*
 * Copyright 2021 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apitomy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.jsweet.transpiler.extension.PrinterAdapter;
import org.jsweet.transpiler.model.ExtendedElement;
import org.jsweet.transpiler.model.MethodInvocationElement;
import org.jsweet.transpiler.model.NewClassElement;

/**
 * This adapter provides transpilation from {@link com.fasterxml.jackson.databind.JsonNode} and {@link com.fasterxml.jackson.databind.ObjectNode}
 * to Typescript "any" and "object", respectively.
 */
public class JacksonAdapter extends PrinterAdapter {
    public JacksonAdapter(PrinterAdapter parent) {
        super(parent);
        addTypeMapping("com.fasterxml.jackson.databind.node.ObjectNode", "object");
        addTypeMapping("com.fasterxml.jackson.databind.node.ArrayNode", "Array<any>");
        addTypeMapping("com.fasterxml.jackson.databind.JsonNode", "any");
        addTypeMapping(Predicate.class.getName(), "(any) => boolean");
        addTypeMapping(UnaryOperator.class.getName(), "(any) => any");
    }

    @Override
    public boolean substituteMethodInvocation(MethodInvocationElement invocation) {
        java.util.Set<String> excludedJavaSuperTypes = new java.util.HashSet<>();
        String targetMethodName = invocation.getMethodName();
        String targetClassName = invocation.getMethod().getEnclosingElement().toString();
        org.jsweet.transpiler.model.ExtendedElement targetExpression = invocation.getTargetExpression();
        if (targetExpression != null && targetExpression.getTypeAsElement() != null) {
            targetClassName = targetExpression.getTypeAsElement().toString();
        }
        TypeMirror jdkSuperclass = context.getJdkSuperclass(targetClassName, excludedJavaSuperTypes);
        boolean delegate = jdkSuperclass != null;
        if (delegate) {
            targetClassName = jdkSuperclass.toString();
        }

        TypeMirror targetType = invocation.getTargetType();

        if (targetClassName != null
                && (targetExpression != null || invocation.getMethod().getModifiers().contains(javax.lang.model.element.Modifier.STATIC))) {
            switch (targetClassName) {
            case "com.fasterxml.jackson.databind.node.ObjectNode":
                if ("put".equals(targetMethodName)) {
                    printMacroName(targetMethodName);
                    print(invocation.getTargetExpression().toString());
                    print("[").print(invocation.getArgument(0)).print("] = ").print(invocation.getArgument(1));
                    return true;
                }
                break;
            case "com.fasterxml.jackson.databind.node.ArrayNode":
                if ("add".equals(targetMethodName)) {
                    printMacroName(targetMethodName);
                    print(invocation.getTargetExpression().toString());
                    print(".push(").print(invocation.getArgument(0)).print(")");
                    return true;
                }
                break;
            case "java.util.List":
            case "java.util.Set":
                if ("of".equals(targetMethodName)) {
                    printMacroName(targetMethodName);
                    print("[").printArgList(invocation.getArguments()).print("]");
                    return true;
                }
                break;
            case "io.apitomy.datamodels.util.ResourceUtil":
                // There is no classpath in the TypeScript target, so the resource is
                // read here, at transpile time, and inlined as a string literal. The
                // argument must therefore be a literal path, not a computed one.
                if ("readResourceAsString".equals(targetMethodName)) {
                    printMacroName(targetMethodName);
                    print(inlineResource(invocation.getArgument(0).toString()));
                    return true;
                }
                break;
            case "java.util.function.UnaryOperator":
                if ("identity".equals(targetMethodName)) {
                    printMacroName(targetMethodName);
                    print("(x=>x)");
                    return true;
                }
                break;
            }
        }

        return super.substituteMethodInvocation(invocation);
    }

    /**
     * Reads a bundled resource from the module's resource directory and renders it
     * as a TypeScript string literal.
     *
     * @param literalArgument the call-site argument, including its quotes
     */
    private String inlineResource(String literalArgument) {
        String path = literalArgument.trim();
        if (path.length() < 2 || path.charAt(0) != '"' || path.charAt(path.length() - 1) != '"') {
            throw new RuntimeException(
                    "ResourceUtil.readResourceAsString needs a string literal so the resource can be "
                    + "inlined at transpile time, but got: " + literalArgument);
        }
        path = path.substring(1, path.length() - 1);
        // The transpiler's working directory is the reactor root rather than the module,
        // so the module-relative path is tried as well.
        String[] roots = {"data-models/src/main/resources", "src/main/resources"};
        java.io.File file = null;
        for (String root : roots) {
            java.io.File candidate = new java.io.File(root, path);
            if (candidate.isFile()) {
                file = candidate;
                break;
            }
        }
        if (file == null) {
            throw new RuntimeException("Cannot inline missing resource '" + path
                    + "'; looked under " + java.util.Arrays.toString(roots)
                    + " relative to " + new java.io.File(".").getAbsolutePath());
        }
        String content;
        try {
            content = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Cannot inline resource: " + file.getAbsolutePath(), e);
        }
        StringBuilder out = new StringBuilder(content.length() + 64);
        out.append('"');
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
            case '\\': out.append("\\\\"); break;
            case '"':  out.append("\\\""); break;
            case '\n': out.append("\\n"); break;
            case '\r': out.append("\\r"); break;
            case '\t': out.append("\\t"); break;
            default:
                if (c < 0x20) {
                    out.append(String.format("\\u%04x", (int) c));
                } else {
                    out.append(c);
                }
            }
        }
        out.append('"');
        return out.toString();
    }

    public boolean substituteInstanceof(String exprStr, ExtendedElement expr, TypeMirror type) {
        String typeName = type.toString();
        switch (typeName) {
            case "com.fasterxml.jackson.databind.node.ObjectNode":
                print("typeof ");
                print(exprStr);
                print(" === 'object'");
                return true;
            case "com.fasterxml.jackson.databind.node.ArrayNode":
                print("typeof ");
                print(exprStr);
                print(" === 'array'");
                return true;
        }
        return super.substituteInstanceof(exprStr, expr, type);
    }

}
