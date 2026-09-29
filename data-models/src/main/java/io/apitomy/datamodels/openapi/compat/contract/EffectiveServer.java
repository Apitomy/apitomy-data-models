package io.apitomy.datamodels.openapi.compat.contract;

import java.util.Map;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * One effective server entry: its URL template (which may contain
 * {@code {variable}} placeholders), together with each variable's
 * declared allowed values (when the source gave it a finite {@code enum})
 * or default (when it did not) -- the information later route/address
 * comparison (T12) needs to expand a template into a finite address set,
 * or to recognize honestly that it cannot.
 */
public final class EffectiveServer {

    private final String urlTemplate;
    private final Map<String, java.util.List<String>> variableEnums;
    private final Map<String, String> variableDefaults;

    public EffectiveServer(String urlTemplate, Map<String, java.util.List<String>> variableEnums,
            Map<String, String> variableDefaults) {
        if (urlTemplate == null) {
            throw new IllegalArgumentException("urlTemplate must not be null");
        }
        this.urlTemplate = urlTemplate;
        this.variableEnums = CollectionUtil.copyOfMap(variableEnums);
        this.variableDefaults = CollectionUtil.copyOfMap(variableDefaults);
    }

    /** This server's raw URL, with any {@code {variable}} placeholders left unexpanded. */
    public String getUrlTemplate() {
        return urlTemplate;
    }

    /** Each templated variable's declared finite allowed values, or absent if the variable has no {@code enum} (open-ended). */
    public Map<String, java.util.List<String>> getVariableEnums() {
        return CollectionUtil.copyOfMap(variableEnums);
    }

    /** Each templated variable's default value. */
    public Map<String, String> getVariableDefaults() {
        return CollectionUtil.copyOfMap(variableDefaults);
    }
}
