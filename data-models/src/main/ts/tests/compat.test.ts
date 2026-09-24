import {JsonSchemaCompatibilityChecker} from "../src/io/apitomy/datamodels/jsonschema/compat/JsonSchemaCompatibilityChecker";

describe("Compatibility result tree", () => {
    test("a nested change is reported where it is, in the schema's own keywords", () => {
        const checker = JsonSchemaCompatibilityChecker.builder().build();
        const original = JSON.stringify({
            "$schema": "http://json-schema.org/draft-07/schema#",
            "properties": {"a/b": {"items": [{"maxLength": 10}]}}
        });
        const result = checker.checkBackward(original, original.replace("10", "5"));

        expect(result.isCompatible()).toBe(false);
        const property = result.getRoot().getChildren()[0];
        expect(property.getPathUpdated().toString()).toBe("/properties/a~1b");
        const element = property.getChildren()[0];
        expect(element.getPathUpdated().toString()).toBe("/properties/a~1b/items/0");

        const differences = result.getRoot().flatten();
        expect(differences.length).toBe(1);
        expect(differences[0].getPathUpdated().toString()).toBe("/properties/a~1b/items/0/maxLength");
        expect(differences[0].getShortDescription()).toContain("maxLength");
    });
});
