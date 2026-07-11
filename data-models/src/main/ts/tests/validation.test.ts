
import {Library} from "../src/io/apitomy/datamodels/Library";
import {readJSON} from "./util/tutils";
import {readTXT} from "./util/tutils";
import {formatProblems} from "./util/tutils";
import {readSeverity} from "./util/tutils";
import {IValidationSeverityRegistry} from "../src/io/apitomy/datamodels/validation/IValidationSeverityRegistry";
import {ValidationProblemSeverity} from "../src/io/apitomy/datamodels/validation/ValidationProblemSeverity";
import {IReferenceResolver} from "../src/io/apitomy/datamodels/refs/IReferenceResolver";
import {ResolvedReference} from "../src/io/apitomy/datamodels/refs/ResolvedReference";
import {ReferenceUtil} from "../src/io/apitomy/datamodels/refs/ReferenceUtil";
import {Node} from "../src/io/apitomy/datamodels/models/Node";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {ValidationProblem} from "../src/io/apitomy/datamodels/validation/ValidationProblem";


interface TestSpec {
    name: string;
    test: string;
    severity?: string;
}

class CustomSeverityRegistry implements IValidationSeverityRegistry {

    constructor(private severity: ValidationProblemSeverity) {}

    public lookupSeverity(): ValidationProblemSeverity {
        return this.severity;
    }

}

class ValidationTestReferenceResolver implements IReferenceResolver {

    public resolveRef(reference: string, from: Node): ResolvedReference {
        if (!reference) {
            return null;
        }
        if (reference.indexOf("test:") != 0) {
            return null;
        }

        let colonIdx: number = reference.indexOf(":");
        let hashIdx: number = reference.indexOf("#");
        let resourceName: string = reference.substring(colonIdx + 1, hashIdx);
        if (!resourceName) {
            throw "Invalid resource name from reference: " + reference;
        }
        let fragment: string = reference.substring(hashIdx + 1);
        if (!fragment) {
            throw "Invalid fragment from reference: " + reference;
        }
        let content: any = readJSON("tests/fixtures/validation/shared/" + resourceName);
        if (!resourceName) {
            throw "Failed to load JSON from reference: " + reference;
        }
        let resolvedContent: any = ReferenceUtil.resolveFragmentFromJS(content, fragment);
        if (!resourceName) {
            throw "Failed to resolve content for reference: " + reference;
        }

        // Check if this is an Avro JSON schema
        if (resolvedContent && typeof resolvedContent === "object" &&
            resolvedContent.type && resolvedContent.type === "record") {
            return ResolvedReference.fromJson(resolvedContent, "application/vnd.apache.avro+json");
        }

        // Otherwise convert to Node
        let node: Node = this.toModel(resolvedContent, from);
        return ResolvedReference.fromNode(node);
    }

    private toModel(jsonNode: any, from: Node): Node {
        let rval: Node = from.emptyClone();
        rval.attach(from.parent());
        return Library.readNode(jsonNode, rval);
    }

}
Library.addReferenceResolver(new ValidationTestReferenceResolver());


let allTests: TestSpec[] = readJSON("tests/fixtures/validation/tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        // Read the source JSON file
        let testPath: string = "tests/fixtures/validation/" + spec.test;
        let json: any = readJSON(testPath);
        expect(json).not.toBeNull();
        
        // Parse/read the document
        let document: Document = Library.readDocument(json);

        // Validate the document
        let severityRegistry: IValidationSeverityRegistry = null;
        if (spec.severity) {
            severityRegistry = new CustomSeverityRegistry(readSeverity(spec.severity));
        }
        let problems: ValidationProblem[] = Library.validate(document, severityRegistry);
        
        // Format the list of problems into a string for comparison with the expected value
        let actual: string[] = formatProblems(problems);

        // Load the expected result and compare with actual
        let expectedStr: string = readTXT(testPath + ".expected");
        expect(expectedStr).not.toBeNull();
        let expected: string[] = [];
        if (expectedStr) {
            expected = expectedStr.split(/\r?\n/);
            expected = expected.filter(val => val != null && val != undefined && val != "");
        }
        actual.sort();
        expected.sort();
        expect(actual).toEqual(expected);
    });
});
