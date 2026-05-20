import {readJSON} from "./util/tutils";
import {NodePath} from "../src/io/apitomy/datamodels/paths/NodePath";


interface TestSpec {
    name: string;
    path: string;
    segments: string[];
}


let allTests: TestSpec[] = readJSON("tests/fixtures/paths/io-tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        let nodePath: NodePath = NodePath.parse(spec.path);
        let actual: string = nodePath.toString(true);
        let expected: string = spec.path;
        expect(actual).toEqual(expected);
        let actualSegments: string[] = nodePath.toSegments();
        let expectedSegments: string[] = spec.segments;
        expect(actualSegments).toEqual(expectedSegments);
    });
});
