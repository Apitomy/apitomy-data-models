import {readCatalog, validateCatalog} from "./util/openapiCompatCases";

test("bundled catalog is well-formed", () => {
    const root = readCatalog();
    const problems = validateCatalog(root);
    expect(problems).toEqual([]);
});

test("rejects empty case list", () => {
    const problems = validateCatalog({cases: []});
    expect(problems).toEqual(["catalog must contain a non-empty 'cases' array"]);
});

test("rejects missing cases array", () => {
    const problems = validateCatalog({});
    expect(problems).toEqual(["catalog must contain a non-empty 'cases' array"]);
});

function minimalCase(id: string): any {
    return {
        id: id, family: "3.0", original: {}, updated: {}, reason: "x",
        expected: {backward: {verdict: "COMPATIBLE", breakingCodes: [], unresolvedCodes: []}},
    };
}

test("rejects duplicate ids", () => {
    const problems = validateCatalog({cases: [minimalCase("dup"), minimalCase("dup")]});
    expect(problems.some(p => p.indexOf("duplicate case id: dup") >= 0)).toBe(true);
});

test("rejects missing expectations", () => {
    const problems = validateCatalog({
        cases: [{
            id: "no-expectations", family: "3.0", original: {}, updated: {}, reason: "x",
            expected: {},
        }],
    });
    expect(problems.some(p => p.indexOf("at least one of") >= 0)).toBe(true);
});

test("rejects unknown finding codes", () => {
    const problems = validateCatalog({
        cases: [{
            id: "bad-code", family: "3.0", original: {}, updated: {}, reason: "x",
            expected: {
                backward: {
                    verdict: "COMPATIBLE",
                    breakingCodes: ["NOT_A_REAL_CODE"],
                    unresolvedCodes: [],
                },
            },
        }],
    });
    expect(problems.some(p => p.indexOf("unknown finding code") >= 0)).toBe(true);
});

test("rejects unknown verdict", () => {
    const problems = validateCatalog({
        cases: [{
            id: "bad-verdict", family: "3.0", original: {}, updated: {}, reason: "x",
            expected: {backward: {verdict: "MAYBE", breakingCodes: [], unresolvedCodes: []}},
        }],
    });
    expect(problems.some(p => p.indexOf("'verdict' must be one of") >= 0)).toBe(true);
});

test("rejects unknown family", () => {
    const problems = validateCatalog({
        cases: [{
            id: "bad-family", family: "4.0", original: {}, updated: {}, reason: "x",
            expected: {backward: {verdict: "COMPATIBLE", breakingCodes: [], unresolvedCodes: []}},
        }],
    });
    expect(problems.some(p => p.indexOf("'family' must be one of") >= 0)).toBe(true);
});
