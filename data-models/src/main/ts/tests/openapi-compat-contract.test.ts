import {readJSON} from "./util/tutils";
import {ContractDocument} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractDocument";
import {ContractInterpreter} from "../src/io/apitomy/datamodels/openapi/compat/contract/ContractInterpreter";
import {EffectiveInteraction} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveInteraction";
import {EffectiveParameter} from "../src/io/apitomy/datamodels/openapi/compat/contract/EffectiveParameter";
import {SchemaUsage} from "../src/io/apitomy/datamodels/openapi/compat/contract/SchemaUsage";
import {HttpRole} from "../src/io/apitomy/datamodels/openapi/compat/HttpRole";
import {ProviderRole} from "../src/io/apitomy/datamodels/openapi/compat/ProviderRole";

function fixtureCase(id: string): any {
    const root: any = readJSON("tests/fixtures/openapi-compat/effective-contract.json");
    for (const testCase of root.cases) {
        if (testCase.id === id) {
            return testCase;
        }
    }
    throw new Error("No fixture case named '" + id + "'");
}

function interpret(documentJson: any): ContractDocument {
    return ContractInterpreter.interpret(documentJson, "urn:test:doc");
}

function onlyInteraction(document: ContractDocument): EffectiveInteraction {
    expect(document.getInteractions().length).toBe(1);
    return document.getInteractions()[0];
}

function findParameter(interaction: EffectiveInteraction, name: string, inLocation: string): EffectiveParameter {
    const parameters: EffectiveParameter[] = interaction.getParameters();
    for (const parameter of parameters) {
        if (parameter.getName() === name && parameter.getIn() === inLocation) {
            return parameter;
        }
    }
    return null;
}

test("moving a parameter between operation and path level does not change effective behavior", () => {
    const testCase: any = fixtureCase("parameter-moved-operation-to-path-same-effective-behavior");
    const fromOperation: EffectiveInteraction = onlyInteraction(interpret(testCase.operationLevel));
    const fromPath: EffectiveInteraction = onlyInteraction(interpret(testCase.pathLevel));

    const opLevel: EffectiveParameter = findParameter(fromOperation, "limit", "query");
    const pathLevel: EffectiveParameter = findParameter(fromPath, "limit", "query");
    expect(opLevel).not.toBeNull();
    expect(pathLevel).not.toBeNull();
    expect(opLevel.isRequired()).toBe(pathLevel.isRequired());
    expect(opLevel.getSchema().getNode()["type"]).toBe("integer");
    expect(pathLevel.getSchema().getNode()["type"]).toBe("integer");
});

test("reordering parameters does not change the effective set", () => {
    const testCase: any = fixtureCase("parameter-list-reordering-same-effective-set");
    const fromOperation: EffectiveInteraction = onlyInteraction(interpret(testCase.operationLevel));
    const fromPath: EffectiveInteraction = onlyInteraction(interpret(testCase.pathLevel));

    expect(fromOperation.getParameters().length).toBe(2);
    expect(fromPath.getParameters().length).toBe(2);
    expect(findParameter(fromOperation, "a", "query")).not.toBeNull();
    expect(findParameter(fromOperation, "b", "query")).not.toBeNull();
    expect(findParameter(fromPath, "a", "query")).not.toBeNull();
    expect(findParameter(fromPath, "b", "query")).not.toBeNull();
});

test("a component parameter reference resolves like an inline declaration", () => {
    const testCase: any = fixtureCase("component-extraction-parameter-ref");
    const interaction: EffectiveInteraction = onlyInteraction(interpret(testCase.document));
    const limit: EffectiveParameter = findParameter(interaction, "limit", "query");
    expect(limit).not.toBeNull();
    expect(limit.isRequired()).toBe(true);
});

test("an operation-level parameter overrides the same identity path parameter", () => {
    const testCase: any = fixtureCase("operation-level-override-of-path-parameter");
    const interaction: EffectiveInteraction = onlyInteraction(interpret(testCase.document));
    const limit: EffectiveParameter = findParameter(interaction, "limit", "query");
    expect(limit).not.toBeNull();
    expect(limit.isDeclaredAtOperationLevel()).toBe(true);
    expect(limit.getSchema().getNode()["type"]).toBe("integer");
});

test("absent security inherits root while an explicit empty array means no security", () => {
    const testCase: any = fixtureCase("security-absent-inherits-root-empty-overrides-to-none");
    const document: ContractDocument = interpret(testCase.document);

    let inherits: EffectiveInteraction = null;
    let overridesToNone: EffectiveInteraction = null;
    document.getInteractions().forEach(interaction => {
        if (interaction.getPathTemplate() === "/inherits") {
            inherits = interaction;
        } else if (interaction.getPathTemplate() === "/overrides-to-none") {
            overridesToNone = interaction;
        }
    });
    expect(inherits).not.toBeNull();
    expect(overridesToNone).not.toBeNull();
    expect(inherits.getSecurity().length).toBe(1);
    expect(overridesToNone.getSecurity().length).toBe(0);
});

test("server precedence is operation then path then root", () => {
    const testCase: any = fixtureCase("server-precedence-operation-then-path-then-root");
    const document: ContractDocument = interpret(testCase.document);

    const byPath: any = {};
    document.getInteractions().forEach(interaction => {
        byPath[interaction.getPathTemplate()] = interaction;
    });

    expect(byPath["/root-only"].getServers()[0].getUrlTemplate()).toBe("https://root.example.com");
    expect(byPath["/path-level"].getServers()[0].getUrlTemplate()).toBe("https://path.example.com");
    expect(byPath["/operation-level"].getServers()[0].getUrlTemplate()).toBe("https://operation.example.com");
});

test("legacy consumes/produces override at the operation level", () => {
    const testCase: any = fixtureCase("legacy-consumes-produces-schemes-operation-override");
    const document: ContractDocument = interpret(testCase.document);

    let inherits: EffectiveInteraction = null;
    let overrides: EffectiveInteraction = null;
    document.getInteractions().forEach(interaction => {
        if (interaction.getPathTemplate() === "/inherits") {
            inherits = interaction;
        } else if (interaction.getPathTemplate() === "/overrides") {
            overrides = interaction;
        }
    });
    expect(inherits).not.toBeNull();
    expect(overrides).not.toBeNull();
    const inheritsContent: any = inherits.getRequestBody().getContentByMediaType();
    const overridesContent: any = overrides.getRequestBody().getContentByMediaType();
    expect("application/json" in inheritsContent).toBe(true);
    expect("application/xml" in inheritsContent).toBe(false);
    expect("application/xml" in overridesContent).toBe(true);
    expect("application/json" in overridesContent).toBe(false);
});

test("a component-only document is an empty API surface with a coverage note", () => {
    const testCase: any = fixtureCase("component-only-document-empty-surface");
    const document: ContractDocument = interpret(testCase.document);

    expect(document.isComponentOnly()).toBe(true);
    expect(document.getInteractions().length).toBe(0);
    expect(document.getCoverageNote()).not.toBeNull();
});

test("a duplicate parameter declaration is recognized and the first wins", () => {
    const testCase: any = fixtureCase("duplicate-parameter-declaration-recognized");
    const document: ContractDocument = interpret(testCase.document);
    const interaction: EffectiveInteraction = onlyInteraction(document);

    expect(document.getProblems().length > 0).toBe(true);
    expect(interaction.getParameters().length).toBe(1);
    const id: EffectiveParameter = findParameter(interaction, "id", "query");
    expect(id).not.toBeNull();
    expect(id.getSchema().getNode()["type"]).toBe("string");
});

test("unreferenced root definitions are not globally applied", () => {
    const testCase: any = fixtureCase("unreferenced-root-definitions-not-globally-applied");
    const document: ContractDocument = interpret(testCase.document);
    const interaction: EffectiveInteraction = onlyInteraction(document);

    expect(document.isComponentOnly()).toBe(false);
    expect(interaction.getParameters().length).toBe(0);
});

test("webhook usages get opposite provider roles from ordinary usages for the same schema", () => {
    const testCase: any = fixtureCase("webhook-request-and-response-roles-independent-of-ordinary");
    const document: ContractDocument = interpret(testCase.document);

    let ordinary: EffectiveInteraction = null;
    let webhook: EffectiveInteraction = null;
    document.getInteractions().forEach(interaction => {
        if (interaction.isWebhook()) {
            webhook = interaction;
        } else {
            ordinary = interaction;
        }
    });
    expect(ordinary).not.toBeNull();
    expect(webhook).not.toBeNull();
    expect(ordinary.getWebhookName()).toBeNull();
    expect(webhook.getWebhookName()).toBe("widgetCreated");
    expect(webhook.getPathTemplate()).toBeNull();

    assertRequestResponseRoles(ordinary.getSchemaUsages(), ProviderRole.INPUT, ProviderRole.OUTPUT);
    assertRequestResponseRoles(webhook.getSchemaUsages(), ProviderRole.OUTPUT, ProviderRole.INPUT);
});

function assertRequestResponseRoles(usages: SchemaUsage[], expectedRequestRole: ProviderRole, expectedResponseRole: ProviderRole): void {
    let sawRequest: boolean = false;
    let sawResponse: boolean = false;
    usages.forEach(usage => {
        if (usage.getHttpRole() === HttpRole.REQUEST) {
            expect(usage.getProviderRole()).toBe(expectedRequestRole);
            sawRequest = true;
        } else {
            expect(usage.getProviderRole()).toBe(expectedResponseRole);
            sawResponse = true;
        }
    });
    expect(sawRequest).toBe(true);
    expect(sawResponse).toBe(true);
}
