import {CommandUtil} from "../src/io/apitomy/datamodels/util/CommandUtil";
import {readJSON} from "./util/tutils";
import {ICommand} from "../src/io/apitomy/datamodels/cmd/ICommand";


interface TestSpec {
    name: string;
    test: string;
    commands?: string;
}

let allTests: TestSpec[] = readJSON("tests/fixtures/cmd/tests.json");
allTests.forEach(spec => {
    test("Marshall: " + spec.name, () => {
        let commandsPath: string = "tests/fixtures/cmd/" + spec.test + ".commands.json";
        if (spec.commands) {
            commandsPath = "tests/fixtures/cmd/" + spec.test + spec.commands;
        }

        let commandsJs: any[] = readJSON(commandsPath);
        expect(commandsJs).not.toBeNull();

        commandsJs.forEach((originalNode, index) => {
            // Unmarshall the command from JSON
            let command: ICommand = CommandUtil.unmarshall(originalNode);
            expect(command).not.toBeNull();

            // Marshall the command back to JSON
            let marshalledNode: any = CommandUtil.marshall(command);
            expect(marshalledNode).not.toBeNull();

            // Verify __type is preserved
            expect(marshalledNode["__type"]).toEqual(originalNode["__type"]);

            // Verify that all marshalled fields match the corresponding original values
            Object.keys(marshalledNode).forEach(fieldName => {
                if (originalNode.hasOwnProperty(fieldName)) {
                    expect(marshalledNode[fieldName]).toEqual(originalNode[fieldName]);
                }
            });

            // Verify round-trip consistency: marshall -> unmarshall produces a command
            // that marshalls identically
            let roundTrippedCommand: ICommand = CommandUtil.unmarshall(marshalledNode);
            let secondMarshall: any = CommandUtil.marshall(roundTrippedCommand);
            expect(secondMarshall).toEqual(marshalledNode);
        });
    });
});
