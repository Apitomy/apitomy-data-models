import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CompatibilityFinding} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityFinding";
import {CompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityResult";
import {CompatibilityVerdict} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityVerdict";
import {FullCompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/FullCompatibilityResult";
import {OpenApiCompatibilityChecker} from "../src/io/apitomy/datamodels/openapi/compat/OpenApiCompatibilityChecker";
import {OpenApiAsyncCompatibilityChecker} from "../src/io/apitomy/datamodels/openapi/compat/OpenApiAsyncCompatibilityChecker";
import {AsyncResourceLoader} from "../src/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceLoader";
import {ResourceDocument} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceDocument";
import {ResourceRequest} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceRequest";

/**
 * Metamorphic properties (T19/V5), mirroring OpenApiMetamorphicTest.java.
 * See that class's Javadoc for why "extracting an inline schema into an
 * equivalent reference preserves verdicts" is not attempted here either.
 */

const WITH_PARAM_A_THEN_B: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"parameters":['
    + '{"name":"a","in":"query","schema":{"type":"string"}},'
    + '{"name":"b","in":"query","schema":{"type":"string"}}'
    + '],"responses":{"200":{"description":"ok"}}}}}}';

const WITH_PARAM_B_THEN_A: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"parameters":['
    + '{"name":"b","in":"query","schema":{"type":"string"}},'
    + '{"name":"a","in":"query","schema":{"type":"string"}}'
    + '],"responses":{"200":{"description":"ok"}}}}}}';

const ORIGINAL: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"parameters":[{"name":"id","in":"query","required":true,'
    + '"schema":{"type":"string"}}],"responses":{"200":{"description":"ok"}}}}}}';

const UPDATED_BREAKING: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';

function codesAndImpacts(result: CompatibilityResult): string[] {
    return result.getFindings().map((f: CompatibilityFinding) => f.getCode() + ":" + f.getImpact());
}

test("forward of A and B matches backward of B and A", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const forwardAB: CompatibilityResult = checker.checkForwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
    const backwardBA: CompatibilityResult = checker.checkBackwardJson(UPDATED_BREAKING, ORIGINAL, CheckOptions.defaults());

    expect(backwardBA.getVerdict()).toBe(forwardAB.getVerdict());
    expect(codesAndImpacts(backwardBA)).toEqual(codesAndImpacts(forwardAB));
});

test("full preserves the two individual directional verdicts", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const backward: CompatibilityResult = checker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
    const forward: CompatibilityResult = checker.checkForwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());
    const full: FullCompatibilityResult = checker.checkFullJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());

    expect(full.getBackwardResult().getVerdict()).toBe(backward.getVerdict());
    expect(full.getForwardResult().getVerdict()).toBe(forward.getVerdict());
});

test("reordering parameters preserves the effective verdict", () => {
    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: CompatibilityResult = checker.checkBackwardJson(WITH_PARAM_A_THEN_B, WITH_PARAM_B_THEN_A, CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
});

test("adding an unused component preserves the verdict", () => {
    const withUnusedComponent: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
        + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}},'
        + '"components":{"schemas":{"Unused":{"type":"object"}}}}';
    const withoutIt: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
        + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';

    const checker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const result: CompatibilityResult = checker.checkBackwardJson(withoutIt, withUnusedComponent, CheckOptions.defaults());
    expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
    expect(result.getFindings().length).toBe(0);
});

test("sync equals async for the same snapshot", () => {
    const syncChecker: OpenApiCompatibilityChecker = OpenApiCompatibilityChecker.builder().build();
    const syncResult: CompatibilityResult = syncChecker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults());

    const asyncChecker: OpenApiAsyncCompatibilityChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    class NeverCalledLoader implements AsyncResourceLoader {
        public load(request: ResourceRequest): Promise<ResourceDocument> {
            throw new Error("Loader should never be invoked: " + request.getUri());
        }
    }
    return asyncChecker.checkBackwardJson(ORIGINAL, UPDATED_BREAKING, CheckOptions.defaults(), new NeverCalledLoader())
        .then((asyncResult: CompatibilityResult) => {
            expect(asyncResult.getVerdict()).toBe(syncResult.getVerdict());
            expect(codesAndImpacts(asyncResult)).toEqual(codesAndImpacts(syncResult));
        });
});
