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
