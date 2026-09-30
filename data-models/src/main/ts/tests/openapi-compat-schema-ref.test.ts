import {ContainmentContext} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentContext";
import {ContainmentResult} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentResult";
import {ContainmentVerdict} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/ContainmentVerdict";
import {SchemaContainment} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaContainment";
import {SchemaDialect} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaDialect";
import {SchemaView} from "../src/io/apitomy/datamodels/jsonschema/compat/containment/SchemaView";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";
import {CheckDirection} from "../src/io/apitomy/datamodels/openapi/compat/CheckDirection";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";

/**
 * Schema $ref resolution and cycle-safe recursive traversal, mirroring
 * SchemaContainmentRefTest.java. See that class's Javadoc for the full
 * rationale (the gap this closes, and why a genuinely recursive schema
 * still honestly resolves Unknown rather than a guessed Compatible).
 */

function view(documentRoot: any, pointer: string): SchemaView {
    const node: any = navigate(documentRoot, pointer);
    return new SchemaView(node, SchemaDialect.DRAFT2020_12, "urn:test:resource", pointer, documentRoot);
}

function navigate(root: any, pointer: string): any {
    let current: any = root;
    if (pointer.length === 0) {
        return current;
    }
    pointer.substring(1).split("/").forEach((segment) => {
        current = current[segment];
    });
    return current;
}

function context(): ContainmentContext {
    return ContainmentContext.of(HttpRole.REQUEST, ProviderRole.INPUT, CheckDirection.BACKWARD, CompatibilityPolicy.defaults());
}

test("a top-level ref resolves and compares reflexively", () => {
    const document: any = {
        $defs: {Widget: {type: "object", properties: {name: {type: "string"}}}},
        source: {$ref: "#/$defs/Widget"},
        target: {$ref: "#/$defs/Widget"},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    const result: ContainmentResult = SchemaContainment.compare(source, target, context());
    expect(result.getVerdict()).toBe(ContainmentVerdict.YES);
});

test("different ref targets resolve and compare by their actual content", () => {
    const document: any = {
        $defs: {
            Wide: {type: "string", "enum": ["a", "b", "c"]},
            Narrow: {type: "string", "enum": ["a", "b"]},
        },
        source: {$ref: "#/$defs/Wide"},
        target: {$ref: "#/$defs/Narrow"},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    expect(SchemaContainment.compare(source, target, context()).getVerdict()).toBe(ContainmentVerdict.NO);
    expect(SchemaContainment.compare(target, source, context()).getVerdict()).toBe(ContainmentVerdict.YES);
});

test("a ref nested inside a property resolves", () => {
    const document: any = {
        $defs: {Owner: {type: "object", properties: {id: {type: "string"}}}},
        source: {type: "object", properties: {owner: {$ref: "#/$defs/Owner"}}},
        target: {type: "object", properties: {owner: {$ref: "#/$defs/Owner"}}},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    expect(SchemaContainment.compare(source, target, context()).getVerdict()).toBe(ContainmentVerdict.YES);
});

test("a self-referential recursive schema is cycle-safe", () => {
    const document: any = {
        $defs: {
            TreeNode: {
                type: "object",
                properties: {
                    value: {type: "integer"},
                    children: {type: "array", items: {$ref: "#/$defs/TreeNode"}},
                },
            },
        },
        source: {$ref: "#/$defs/TreeNode"},
        target: {$ref: "#/$defs/TreeNode"},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    const result: ContainmentResult = SchemaContainment.compare(source, target, context());
    expect(result).not.toBeNull();
    // No stack overflow/infinite loop (the property under test); the verdict
    // is honestly Unknown, not a guessed Compatible -- see the Java test.
    expect(result.getVerdict()).toBe(ContainmentVerdict.UNKNOWN);
});

test("a mutually recursive two-schema cycle is cycle-safe", () => {
    const document: any = {
        $defs: {
            A: {type: "object", properties: {next: {$ref: "#/$defs/B"}}},
            B: {type: "object", properties: {next: {$ref: "#/$defs/A"}}},
        },
        source: {$ref: "#/$defs/A"},
        target: {$ref: "#/$defs/A"},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    const result: ContainmentResult = SchemaContainment.compare(source, target, context());
    expect(result).not.toBeNull();
    expect(result.getVerdict()).toBe(ContainmentVerdict.UNKNOWN);
});

test("an external or unresolvable ref stays unknown exactly as before this feature existed", () => {
    const document: any = {
        source: {$ref: "other-file.json#/Foo"},
        target: {type: "object"},
    };
    const source: SchemaView = view(document, "/source");
    const target: SchemaView = view(document, "/target");

    expect(SchemaContainment.compare(source, target, context()).getVerdict()).toBe(ContainmentVerdict.UNKNOWN);
});

test("a long ref chain and a deep recursive graph complete quickly", () => {
    const chainLength = 8;
    const defs: any = {};
    for (let i = 0; i < chainLength; i++) {
        defs["Link" + i] = i < chainLength - 1 ? {$ref: "#/$defs/Link" + (i + 1)} : {type: "string"};
    }
    const chainDocument: any = {$defs: defs, source: {$ref: "#/$defs/Link0"}, target: {$ref: "#/$defs/Link0"}};
    const chainSource: SchemaView = view(chainDocument, "/source");
    const chainTarget: SchemaView = view(chainDocument, "/target");
    const chainResult: ContainmentResult = SchemaContainment.compare(chainSource, chainTarget, context());
    expect(chainResult.getVerdict()).toBe(ContainmentVerdict.YES);

    const listDocument: any = {
        $defs: {
            Node: {
                type: "object",
                properties: {value: {type: "integer"}, next: {$ref: "#/$defs/Node"}},
            },
        },
        source: {$ref: "#/$defs/Node"},
        target: {$ref: "#/$defs/Node"},
    };
    const listSource: SchemaView = view(listDocument, "/source");
    const listTarget: SchemaView = view(listDocument, "/target");
    SchemaContainment.compare(listSource, listTarget, context());
});
