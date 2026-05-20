import {Library} from "../src/io/apitomy/datamodels/Library";
import {readJSON} from "./util/tutils";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {ModelType} from "../src/io/apitomy/datamodels/models/ModelType";
import {ModelTypeUtil} from "../src/io/apitomy/datamodels/util/ModelTypeUtil";
import {
    OpenApi20to30TransformationVisitor
} from "../src/io/apitomy/datamodels/transform/OpenApi20to30TransformationVisitor";
import {TraverserDirection} from "../src/io/apitomy/datamodels/TraverserDirection";


interface TestSpec {
    name: string;
    input: string;
    expected: string;
    fromType: string;
    toType: string;
}


let allTests: TestSpec[] = readJSON("tests/fixtures/transformation/tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        // Read the original/source file JSON
        let testPath: string = "tests/fixtures/transformation/" + spec.input;
        let originalJson: any = readJSON(testPath);
        expect(originalJson).not.toBeNull();

        if (!spec.fromType) {
            spec.fromType = "OPENAPI20";
        }
        if (!spec.toType) {
            spec.toType = "OPENAPI30";
        }
        const fromType: ModelType = ModelTypeUtil.fromString(spec.fromType);
        const toType: ModelType = ModelTypeUtil.fromString(spec.toType);

        // Parse/read the document
        let fromDoc: Document = Library.readDocument(originalJson);
        expect(fromDoc.root().modelType()).toEqual(fromType);

        // Transform the document
        let toDoc: Document = Library.transformDocument(fromDoc, toType);
        expect(toDoc.root().modelType()).toEqual(toType);

        // Serialize/write the document
        let actual: any = Library.writeDocument(toDoc);
        expect(actual).not.toBeNull();

        let expectedPath: string = "tests/fixtures/transformation/" + spec.expected;
        let expected: any = readJSON(expectedPath);
        expect(expected).not.toBeNull();

        // Compare what we got with what we expected
        expect(actual).toEqual(expected);
    });
});
