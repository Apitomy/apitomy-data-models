import {Library} from "../src/io/apitomy/datamodels/Library";
import {readJSON} from "./util/tutils";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {ICommand} from "../src/io/apitomy/datamodels/cmd/ICommand";
import {CommandFactory} from "../src/io/apitomy/datamodels/cmd/CommandFactory";


interface TestSpec {
    name: string;
    test: string;
    commands?: string;
}

let allTests: TestSpec[] = readJSON("tests/fixtures/cmd/tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        console.info("------------------------");

        let beforePath: string = "tests/fixtures/cmd/" + spec.test + ".before.json";
        let afterPath: string = "tests/fixtures/cmd/" + spec.test + ".after.json";
        let commandsPath: string = "tests/fixtures/cmd/" + spec.test + ".commands.json";

        if (spec.commands) {
            commandsPath = "tests/fixtures/cmd/" + spec.test + spec.commands;
        }

        let beforeJs: any = readJSON(beforePath);
        let afterJs: any = readJSON(afterPath);
        let commandsJs: any = readJSON(commandsPath);

        expect(beforeJs).not.toBeNull();
        expect(afterJs).not.toBeNull();
        expect(commandsJs).not.toBeNull();

        // Parse/read the document
        let document: Document = Library.readDocument(beforeJs);
        expect(document).not.toBeNull();

        // Load all the commands to apply.
        let cjs: any[] = commandsJs;
        let commands: ICommand[] = cjs.map(commandNode => CommandFactory.unmarshall(commandNode));

        // Apply all the commands to the Document (modifying the document).
        commands.forEach(command => {
            command.execute(document);
        });

        // Check that the resulting (modified) document is what we expected.
        let actual: any = Library.writeNode(document);
        let expected: any = afterJs;
        assertJsonEquals(actual, expected, document);

        // If there was only ONE command, then undo it and make sure
        // that results in the original document.
        if (commands.length == 1) {
            commands[0].undo(document);

            actual = Library.writeNode(document);
            expected = beforeJs;

            assertJsonEquals(actual, expected, document);
        }
    });
});

function assertJsonEquals(actual: any, expected: any, actualDoc: Document) {
    expect(actual).toEqual(expected);
    // If the object equality assertion passes, then the two JSON objects are strictly equivalent, though may have different
    // ordering. Round tripping the expected JSON with the same pretty printer used for the actual document allows for
    // a reasonable exact string comparison to be made. 
    let expectedString = Library.writeDocumentToJSONString(Library.readDocument(expected));
    let actualString = Library.writeDocumentToJSONString(actualDoc);
    expect(actualString).toEqual(expectedString);
}

