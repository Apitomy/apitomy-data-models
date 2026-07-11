import {Library} from "../src/io/apitomy/datamodels/Library";
import {readJSON} from "./util/tutils";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {NodePath} from "../src/io/apitomy/datamodels/paths/NodePath";
import {Node} from "../src/io/apitomy/datamodels/models/Node";


interface TestSpec {
    name: string;
    test: string;
    path: string;
}


let allTests: TestSpec[] = readJSON("tests/fixtures/paths/resolve-tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        // Read the original/source file JSON
        let testPath: string = "tests/fixtures/paths/" + spec.test;
        let json: any = readJSON(testPath);
        expect(json).not.toBeNull();

        // Parse/read the document
        let document: Document = Library.readDocument(json);
        expect(document).not.toBeNull();

        // Parse the test path
        let np: NodePath = NodePath.parse(spec.path);

        // Resolve the path to a node in the source
        let resolvedNode: Node = Library.resolveNodePath(np, document);
        if (resolvedNode === null) {
            throw Error("Node Path failed to resolve: " + np.toString());
        }
        //expect(resolvedNode).not.toBeNull();

        // Compare source path to node path (test generating a node path from a node)
        let createdPath: NodePath = Library.createNodePath(resolvedNode);
        let expectedPath: string = spec.path;
        let actualPath: string = createdPath.toString();
        expect(expectedPath).toEqual(actualPath);

        // Verify that the resolved node is what we expected it to be
        let actual: any = Library.writeNode(resolvedNode);
        let expectedCP: string = "tests/fixtures/paths/" + spec.test + ".expected.json";
        let expected: any = readJSON(expectedCP);
        expect(expected).not.toBeNull();
        expect(actual).toEqual(expected);
    });
});
