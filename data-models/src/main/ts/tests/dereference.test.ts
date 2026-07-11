import {Library} from "../src/io/apitomy/datamodels/Library";
import {readJSON} from "./util/tutils";
import {IReferenceResolver} from "../src/io/apitomy/datamodels/refs/IReferenceResolver";
import {ResolvedReference} from "../src/io/apitomy/datamodels/refs/ResolvedReference";
import {Node} from "../src/io/apitomy/datamodels/models/Node";
import {Document} from "../src/io/apitomy/datamodels/models/Document";


interface TestSpec {
    name: string;
    input: string;
    expected: string;
    strict: boolean;
}


const refs: any = readJSON("tests/fixtures/dereference/tests.refs.json");
class DereferenceTestReferenceResolver implements IReferenceResolver {

    public resolveRef(reference: string, from: Node): ResolvedReference {
        if (refs[reference]) {
            let ref = refs[reference];
            if (typeof ref === "object" && ref["type"] === "record") {
                return ResolvedReference.fromJson(ref, "application/vnd.apache.avro+json");
            } else if (typeof ref === "object") {
                // Object content - convert to Node
                let node: Node = this.toModel(ref, from);
                return ResolvedReference.fromNode(node);
            } else if (typeof ref === "string") {
                // Text content - return as text with appropriate media type
                let textContent: string = ref;
                let mediaType: string = this.detectMediaType(reference, textContent);
                return ResolvedReference.fromText(textContent, mediaType);
            }
        }
        return null;
    }

    private detectMediaType(reference: string, textContent: string): string {
        // Detect based on file extension in reference
        if (reference.endsWith(".proto")) {
            return "application/vnd.google.protobuf;version=3";
        } else if (reference.endsWith(".avsc")) {
            return "application/vnd.apache.avro+json;version=1.12.0";
        } else if (reference.endsWith(".graphql") || reference.endsWith(".gql")) {
            return "application/graphql";
        }

        // Fallback to plain text
        return "text/plain";
    }

    private toModel(jsonNode: any, from: Node): Node {
        let rval: Node = from.emptyClone();
        rval.attach(from.parent());
        return Library.readNode(jsonNode, rval);
    }

}
Library.addReferenceResolver(new DereferenceTestReferenceResolver());


let allTests: TestSpec[] = readJSON("tests/fixtures/dereference/tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        // Read the original/source file JSON
        let testPath: string = "tests/fixtures/dereference/" + spec.input;
        let originalJson: any = readJSON(testPath);
        expect(originalJson).not.toBeNull();

        // Parse/read the document
        let sourceDoc: Document = Library.readDocument(originalJson);
        expect(sourceDoc).not.toBeNull();

        // Dereference the document
        let dereferencedDoc: Document = Library.dereferenceDocument(sourceDoc, spec.strict === undefined ? true : spec.strict);

        // Serialize/write the document
        let actual: any = Library.writeNode(dereferencedDoc);
        expect(actual).not.toBeNull();

        let expectedPath: string = "tests/fixtures/dereference/" + spec.expected;
        let expected: any = readJSON(expectedPath);
        expect(expected).not.toBeNull();

        // Compare what we got with what we expected
        expect(actual).toEqual(expected);
    });
});
