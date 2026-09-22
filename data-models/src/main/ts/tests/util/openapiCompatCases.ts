import {readJSON} from "./tutils";

const KNOWN_FAMILIES: string[] = ["2.0", "3.0", "3.1", "3.2"];
const KNOWN_VERDICTS: string[] = ["COMPATIBLE", "INCOMPATIBLE", "INDETERMINATE"];

// Kept in sync by hand with the known FindingCode constants in
// io/apitomy/datamodels/openapi/compat/FindingCode.java. There is no portable way
// to enumerate a JSweet-transpiled TypeScript enum's member names generically, so
// this list is intentionally duplicated rather than derived.
const KNOWN_FINDING_CODES: string[] = [
    "RESOURCE_UNRESOLVED", "RESOURCE_INVALID", "DIALECT_UNSUPPORTED", "FEATURE_UNANALYZED",
    "NUMERIC_PRECISION_UNCERTAIN", "ANALYSIS_LIMIT_REACHED", "MATCH_AMBIGUOUS",
    "OPERATION_REMOVED", "OPERATION_ADDED", "ADDRESS_REMOVED", "ROUTE_SHADOWING",
    "PARAMETER_REMOVED", "PARAMETER_REQUIRED_ADDED", "REQUEST_BODY_REQUIRED_ADDED",
    "REQUEST_BODY_REMOVED",
    "SERIALIZATION_CHANGED", "REQUEST_MEDIA_TYPE_REMOVED", "RESPONSE_MEDIA_TYPE_REMOVED",
    "RESPONSE_MEDIA_TYPE_ADDED", "RESPONSE_STATUS_ADDED", "RESPONSE_CAPABILITY_REMOVED",
    "RESPONSE_HEADER_GUARANTEE_REMOVED",
    "SCHEMA_INPUT_NARROWED", "SCHEMA_OUTPUT_WIDENED", "SCHEMA_COMPATIBLE", "SCHEMA_UNRESOLVED",
    "READ_WRITE_ENFORCEMENT_UNCERTAIN", "FORMAT_RELATION_UNKNOWN",
    "SECURITY_REQUIREMENT_STRENGTHENED", "SECURITY_MECHANISM_CHANGED", "CREDENTIAL_FLOW_REMOVED",
    "SECURITY_EQUIVALENCE_UNCERTAIN", "REVERSE_INTERACTION_ADDED", "REVERSE_INTERACTION_REMOVED",
    "CALLBACK_DESTINATION_CHANGED", "DISCRIMINATOR_MAPPING_CHANGED", "LINK_TARGET_UNRESOLVED",
    "BEHAVIORAL_DEFAULT_CHANGED", "EXTENSION_SEMANTICS_UNKNOWN", "METADATA_CHANGED",
];

/**
 * Reads and parses the shared OpenAPI compatibility case catalog from the test
 * classpath (copied from {@code data-models/src/test/resources/fixtures/openapi-compat}
 * during the transpilation build).
 */
export function readCatalog(): any {
    return readJSON("tests/fixtures/openapi-compat/cases.json");
}

/**
 * Validates the catalog's shape: a non-empty, duplicate-free {@code cases} array
 * where every case has an expectation for at least one direction, and every
 * expectation names only real finding codes and a known verdict. Mirrors
 * {@code OpenApiCaseSupport.validateCatalog} on the Java side so both suites
 * reject the same malformed catalogs.
 */
export function validateCatalog(root: any): string[] {
    const problems: string[] = [];
    const cases = root ? root.cases : null;
    if (!cases || !Array.isArray(cases) || cases.length === 0) {
        problems.push("catalog must contain a non-empty 'cases' array");
        return problems;
    }

    const seenIds = new Set<string>();
    for (const testCase of cases) {
        const id: string = testCase ? testCase.id : null;
        if (!id || id.trim().length === 0) {
            problems.push("case is missing a non-blank 'id'");
            continue;
        }
        if (seenIds.has(id)) {
            problems.push("duplicate case id: " + id);
        }
        seenIds.add(id);
        validateCase(id, testCase, problems);
    }
    return problems;
}

function validateCase(id: string, testCase: any, problems: string[]): void {
    const family = testCase.family;
    if (!family || KNOWN_FAMILIES.indexOf(family) < 0) {
        problems.push(id + ": 'family' must be one of " + KNOWN_FAMILIES + ", was: " + family);
    }
    if (!testCase.original || typeof testCase.original !== "object") {
        problems.push(id + ": 'original' must be a JSON object");
    }
    if (!testCase.updated || typeof testCase.updated !== "object") {
        problems.push(id + ": 'updated' must be a JSON object");
    }
    const reason = testCase.reason;
    if (!reason || reason.trim().length === 0) {
        problems.push(id + ": 'reason' must be a non-blank explanation");
    }

    const expected = testCase.expected;
    if (!expected || typeof expected !== "object") {
        problems.push(id + ": 'expected' must be an object");
        return;
    }
    const backward = expected.backward;
    const forward = expected.forward;
    if (!backward && !forward) {
        problems.push(id + ": 'expected' must include at least one of 'backward'/'forward'");
    }
    if (backward) {
        validateDirection(id, "backward", backward, problems);
    }
    if (forward) {
        validateDirection(id, "forward", forward, problems);
    }
}

function validateDirection(id: string, direction: string, dirNode: any, problems: string[]): void {
    const verdict = dirNode.verdict;
    if (!verdict || KNOWN_VERDICTS.indexOf(verdict) < 0) {
        problems.push(id + " (" + direction + "): 'verdict' must be one of " + KNOWN_VERDICTS
            + ", was: " + verdict);
    }
    validateCodeList(id, direction, "breakingCodes", dirNode.breakingCodes, problems);
    validateCodeList(id, direction, "unresolvedCodes", dirNode.unresolvedCodes, problems);
}

function validateCodeList(id: string, direction: string, field: string, list: any,
        problems: string[]): void {
    if (list === undefined || list === null) {
        problems.push(id + " (" + direction + "): missing '" + field
            + "' (use an empty array for none)");
        return;
    }
    if (!Array.isArray(list)) {
        problems.push(id + " (" + direction + "): '" + field + "' must be an array");
        return;
    }
    for (const code of list) {
        if (KNOWN_FINDING_CODES.indexOf(code) < 0) {
            problems.push(id + " (" + direction + "): unknown finding code in '" + field + "': " + code);
        }
    }
}

// `assertCase` (dispatching a case by id to the checker under test) is deferred
// until the first rule runner exists; see the note in OpenApiCaseSupport.java.
