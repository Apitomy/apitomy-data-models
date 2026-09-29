import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CompatibilityPolicy} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityPolicy";
import {CompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityResult";
import {CompatibilityVerdict} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityVerdict";
import {FindingImpact} from "../src/io/apitomy/datamodels/openapi/compat/FindingImpact";
import {FullCompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/FullCompatibilityResult";
import {OpenApiCompatibilityChecker} from "../src/io/apitomy/datamodels/openapi/compat/OpenApiCompatibilityChecker";
import {Library} from "../src/io/apitomy/datamodels/Library";

const ORIGINAL_30: string = '{"openapi":"3.0.0","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"parameters":[{"name":"limit","in":"query","schema":{"type":"integer"}}],'
    + '"responses":{"200":{"description":"ok"}}}}}}';

const UPDATED_30_BREAKING: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';

function documentFor(version: string): string {
    if (version === "2.0") {
        return '{"swagger":"2.0","info":{"title":"T","version":"1"},'
            + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';
    }
    return '{"openapi":"' + version + '","info":{"title":"T","version":"1"},'
        + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';
}

test("checkBackwardJson detects a removed parameter as incompatible", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder()
        .policy(CompatibilityPolicy.defaults())
        .build();
    const result: CompatibilityResult = checker.checkBackwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
});

test("checkForwardJson of the same pair is compatible", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: CompatibilityResult = checker.checkForwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
});

test("checkFullJson preserves both directions independently", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: FullCompatibilityResult = checker.checkFullJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults());
    expect(result.getBackwardResult().getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
    expect(result.getForwardResult().getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
    expect(result.getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
});

test("model instance entry points match their JSON counterparts", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const original: any = Library.readRootFromJSONString(ORIGINAL_30);
    const updated: any = Library.readRootFromJSONString(UPDATED_30_BREAKING);

    expect(checker.checkBackward(original, updated, CheckOptions.defaults()).getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
    expect(checker.checkForward(original, updated, CheckOptions.defaults()).getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
    expect(checker.checkFull(original, updated, CheckOptions.defaults()).getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
});

test("all four OpenAPI families are accepted", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    ["2.0", "3.0.4", "3.1.0", "3.2.0"].forEach((version) => {
        const doc: string = documentFor(version);
        const result: CompatibilityResult = checker.checkBackwardJson(doc, doc, CheckOptions.defaults());
        expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
    });
});

test("patch version differences are accepted as the same family", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: CompatibilityResult = checker.checkBackwardJson(documentFor("3.0.0"), documentFor("3.0.4"), CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
});

test("cross-family comparison is an explicit error", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    expect(() => checker.checkBackwardJson(documentFor("2.0"), documentFor("3.0.4"), CheckOptions.defaults())).toThrow();
});

test("invalid JSON is an explicit error", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    expect(() => checker.checkBackwardJson("{ not valid json", documentFor("3.0.4"), CheckOptions.defaults())).toThrow();
});

test("an independently unresolved operation does not hide a known break", () => {
    const original: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
        + '"paths":{'
        + '"/known":{"get":{"parameters":[{"name":"id","in":"query","schema":{"type":"integer"}}],'
        + '"responses":{"200":{"description":"ok"}}}},'
        + '"/unknown":{"get":{"parameters":[{"name":"filter","in":"query",'
        + '"schema":{"type":"string","pattern":"^a"}}],"responses":{"200":{"description":"ok"}}}}'
        + '}}';
    const updated: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
        + '"paths":{'
        + '"/known":{"get":{"responses":{"200":{"description":"ok"}}}},'
        + '"/unknown":{"get":{"parameters":[{"name":"filter","in":"query",'
        + '"schema":{"type":"string","pattern":"^b"}}],"responses":{"200":{"description":"ok"}}}}'
        + '}}';
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: CompatibilityResult = checker.checkBackwardJson(original, updated, CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
    const impacts: FindingImpact[] = result.getFindings().map(f => f.getImpact());
    expect(impacts.indexOf(FindingImpact.BREAKING) >= 0).toBe(true);
    expect(impacts.indexOf(FindingImpact.UNRESOLVED) >= 0).toBe(true);
});
