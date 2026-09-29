package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.models.Node;
import io.apitomy.datamodels.models.RootCapable;
import io.apitomy.datamodels.models.openapi.OpenApiSecurityScheme;
import io.apitomy.datamodels.models.openapi.v2x.v20.OpenApi20Document;
import io.apitomy.datamodels.models.openapi.v2x.OpenApi2xSecurityDefinitions;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xComponents;
import io.apitomy.datamodels.models.openapi.v3x.OpenApi3xDocument;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * Every named security scheme a document declares (2.0's {@code securityDefinitions}
 * or 3.x's {@code components.securitySchemes}), resolved to a semantic
 * {@link SecuritySchemeInfo} keyed by its component name -- built once per
 * side and reused for every interaction's security comparison.
 */
public final class SecuritySchemeCatalog {

    private final Map<String, SecuritySchemeInfo> schemes;

    private SecuritySchemeCatalog(Map<String, SecuritySchemeInfo> schemes) {
        this.schemes = schemes;
    }

    /** The resolved scheme named {@code name}, or {@code null} if this document does not declare one under that name. */
    public SecuritySchemeInfo get(String name) {
        return schemes.get(name);
    }

    public static SecuritySchemeCatalog from(RootCapable document) {
        Map<String, SecuritySchemeInfo> result = new LinkedHashMap<String, SecuritySchemeInfo>();
        if (document.modelType() == ModelType.OPENAPI20) {
            OpenApi20Document doc = (OpenApi20Document) document;
            OpenApi2xSecurityDefinitions definitions = doc.getSecurityDefinitions();
            if (definitions != null) {
                List<String> names = new ArrayList<String>(definitions.getItemNames());
                for (int i = 0; i < names.size(); i++) {
                    result.put(names.get(i), toInfo(definitions.getItem(names.get(i))));
                }
            }
        } else {
            Object rootObj = NodeUtil.getNodeProperty((Node) document, "components");
            if (rootObj instanceof OpenApi3xComponents) {
                Object schemesObj = NodeUtil.getNodeProperty((Node) rootObj, "securitySchemes");
                if (schemesObj != null) {
                    List<String> names = new ArrayList<String>(NodeUtil.getMapKeys((Map<String, ?>) schemesObj));
                    for (int i = 0; i < names.size(); i++) {
                        String name = names.get(i);
                        OpenApiSecurityScheme scheme = (OpenApiSecurityScheme) NodeUtil.getMapItem((Map) schemesObj, name);
                        result.put(name, toInfo(scheme));
                    }
                }
            }
        }
        return new SecuritySchemeCatalog(result);
    }

    private static SecuritySchemeInfo toInfo(OpenApiSecurityScheme scheme) {
        String type = (String) NodeUtil.getNodeProperty(scheme, "type");
        String in = (String) NodeUtil.getNodeProperty(scheme, "in");
        String name = (String) NodeUtil.getNodeProperty(scheme, "name");
        String httpScheme = (String) NodeUtil.getNodeProperty(scheme, "scheme");
        boolean hasFlow = false;
        if ("oauth2".equals(type)) {
            Object flow2 = NodeUtil.getNodeProperty(scheme, "flow");
            Object flowsWrapper = NodeUtil.getNodeProperty(scheme, "flows");
            boolean anySubFlow = false;
            if (flowsWrapper instanceof Node) {
                Node flows = (Node) flowsWrapper;
                anySubFlow = NodeUtil.getNodeProperty(flows, "implicit") != null
                        || NodeUtil.getNodeProperty(flows, "password") != null
                        || NodeUtil.getNodeProperty(flows, "clientCredentials") != null
                        || NodeUtil.getNodeProperty(flows, "authorizationCode") != null;
            }
            hasFlow = flow2 != null || anySubFlow;
        }
        return new SecuritySchemeInfo(type, in, name, httpScheme, hasFlow);
    }
}
