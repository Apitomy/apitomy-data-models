import {CheckOptions} from "../src/io/apitomy/datamodels/openapi/compat/CheckOptions";
import {CompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityResult";
import {CompatibilityVerdict} from "../src/io/apitomy/datamodels/openapi/compat/CompatibilityVerdict";
import {FullCompatibilityResult} from "../src/io/apitomy/datamodels/openapi/compat/FullCompatibilityResult";
import {OpenApiAsyncCompatibilityChecker} from "../src/io/apitomy/datamodels/openapi/compat/OpenApiAsyncCompatibilityChecker";
import {AsyncResourceLoader} from "../src/io/apitomy/datamodels/openapi/compat/resource/AsyncResourceLoader";
import {ResourceDocument} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceDocument";
import {ResourceRequest} from "../src/io/apitomy/datamodels/openapi/compat/resource/ResourceRequest";

const ORIGINAL_30: string = '{"openapi":"3.0.0","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"parameters":[{"name":"limit","in":"query","schema":{"type":"integer"}}],'
    + '"responses":{"200":{"description":"ok"}}}}}}';

const UPDATED_30_BREAKING: string = '{"openapi":"3.0.4","info":{"title":"T","version":"1"},'
    + '"paths":{"/widgets":{"get":{"responses":{"200":{"description":"ok"}}}}}}';

class NeverCalledLoader implements AsyncResourceLoader {
    public load(request: ResourceRequest): Promise<ResourceDocument> {
        throw new Error("Loader should never be invoked for a document with no external references: " + request.getUri());
    }
}

test("checkBackwardJson matches the synchronous result and never invokes the loader", () => {
    const checker: OpenApiAsyncCompatibilityChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    return checker.checkBackwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults(), new NeverCalledLoader())
        .then((result: CompatibilityResult) => {
            expect(result.getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
        });
});

test("checkForwardJson matches the synchronous result", () => {
    const checker: OpenApiAsyncCompatibilityChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    return checker.checkForwardJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults(), new NeverCalledLoader())
        .then((result: CompatibilityResult) => {
            expect(result.getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
        });
});

test("checkFullJson preserves both directions", () => {
    const checker: OpenApiAsyncCompatibilityChecker = OpenApiAsyncCompatibilityChecker.builder().build();
    return checker.checkFullJson(ORIGINAL_30, UPDATED_30_BREAKING, CheckOptions.defaults(), new NeverCalledLoader())
        .then((result: FullCompatibilityResult) => {
            expect(result.getBackwardResult().getVerdict()).toBe(CompatibilityVerdict.INCOMPATIBLE);
            expect(result.getForwardResult().getVerdict()).toBe(CompatibilityVerdict.COMPATIBLE);
        });
});
