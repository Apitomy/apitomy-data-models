package io.apitomy.datamodels.openapi.compat.contract;

import java.util.Map;

/**
 * Resolves which documented response outcome governs a given concrete HTTP
 * status code, in specification-defined precedence order: an exact status
 * code first, then a family-supported range (e.g. {@code "2XX"}), then
 * {@code "default"}.
 */
public final class ResponseSelector {

    private ResponseSelector() {
    }

    /** The effective response for {@code status}, or {@code null} if nothing in {@code interaction} covers it at all (not even {@code default}). */
    public static EffectiveResponse select(EffectiveInteraction interaction, int status) {
        Map<String, EffectiveResponse> responses = interaction.getResponses();
        String exact = String.valueOf(status);
        if (responses.containsKey(exact)) {
            return responses.get(exact);
        }
        String range = rangeKeyFor(status);
        if (range != null && responses.containsKey(range)) {
            return responses.get(range);
        }
        if (responses.containsKey("default")) {
            return responses.get("default");
        }
        return null;
    }

    /** The {@code "NXX"} range key covering {@code status} (e.g. {@code 404} -&gt; {@code "4XX"}), or {@code null} for an out-of-range status. */
    public static String rangeKeyFor(int status) {
        int hundreds = status / 100;
        if (hundreds < 1 || hundreds > 5) {
            return null;
        }
        return hundreds + "XX";
    }
}
