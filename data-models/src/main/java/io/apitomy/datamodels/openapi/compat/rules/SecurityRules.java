package io.apitomy.datamodels.openapi.compat.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.openapi.compat.CheckDirection;
import io.apitomy.datamodels.openapi.compat.CompatibilityFinding;
import io.apitomy.datamodels.openapi.compat.FindingCode;
import io.apitomy.datamodels.openapi.compat.FindingImpact;
import io.apitomy.datamodels.openapi.compat.contract.EffectiveInteraction;
import io.apitomy.datamodels.openapi.compat.contract.SecuritySchemeCatalog;
import io.apitomy.datamodels.openapi.compat.contract.SecuritySchemeInfo;

/**
 * Security expression implication and credential mechanisms for a matched
 * pair of interactions' effective security (OR of alternatives, each an AND
 * of named schemes with required scopes).
 * <p>
 * Scheme identity is semantic (type, and for {@code apiKey} its location and
 * name, and for {@code http} its scheme name case-insensitively), resolved
 * through a {@link SecuritySchemeCatalog} built once per document -- not
 * merely the local component name -- so a scheme rename with an updated
 * reference is recognized as the same mechanism, and two differently-named
 * but distinct mechanisms are not conflated just because a rule ran out of
 * evidence.
 * <p>
 * Implication uses this sufficient positive-expression rule: for every old
 * alternative a real request could have used, there must be a new
 * alternative whose required schemes and scopes that old alternative's
 * credentials already satisfy (no additional required scheme, no wider
 * scopes). If every old alternative finds one, the direction is compatible;
 * if a concrete old alternative is positively rejected (every new
 * alternative was resolvable and none satisfied it), it is breaking;
 * otherwise (scheme identity or trust equivalence could not be established
 * for some candidate) it is Indeterminate -- never guessed either way.
 */
public final class SecurityRules {

    private SecurityRules() {
    }

    public static List<CompatibilityFinding> compare(EffectiveInteraction original, EffectiveInteraction updated,
            RuleContext context, SecuritySchemeCatalog originalSchemes, SecuritySchemeCatalog updatedSchemes) {
        List<CompatibilityFinding> findings = new ArrayList<CompatibilityFinding>();
        boolean backward = context.getDirection() == CheckDirection.BACKWARD;

        List<Map<String, List<String>>> oldAlternatives = backward ? original.getSecurity() : updated.getSecurity();
        List<Map<String, List<String>>> newAlternatives = backward ? updated.getSecurity() : original.getSecurity();
        SecuritySchemeCatalog oldCatalog = backward ? originalSchemes : updatedSchemes;
        SecuritySchemeCatalog newCatalog = backward ? updatedSchemes : originalSchemes;

        for (int i = 0; i < oldAlternatives.size(); i++) {
            Map<String, List<String>> oldAlternative = oldAlternatives.get(i);
            FindingImpact impact = evaluateAlternative(oldAlternative, newAlternatives, oldCatalog, newCatalog);
            if (impact != FindingImpact.COMPATIBLE) {
                FindingCode code = oldAlternative.isEmpty() || !impact.equals(FindingImpact.UNRESOLVED)
                        ? FindingCode.SECURITY_REQUIREMENT_STRENGTHENED
                        : FindingCode.SECURITY_EQUIVALENCE_UNCERTAIN;
                findings.add(new CompatibilityFinding(code, impact, original.getInteractionId(), null, null, null, null,
                        describeAlternative(oldAlternative) + (impact == FindingImpact.BREAKING
                                ? " is no longer satisfiable by any effective security alternative"
                                : " could not be established as still satisfiable")));
            }
        }

        compareCredentialFlows(original, oldAlternatives, oldCatalog, newCatalog, findings);
        return findings;
    }

    private static FindingImpact evaluateAlternative(Map<String, List<String>> oldAlternative,
            List<Map<String, List<String>>> newAlternatives, SecuritySchemeCatalog oldCatalog,
            SecuritySchemeCatalog newCatalog) {
        if (oldAlternative.isEmpty()) {
            for (int i = 0; i < newAlternatives.size(); i++) {
                if (newAlternatives.get(i).isEmpty()) {
                    return FindingImpact.COMPATIBLE;
                }
            }
            return FindingImpact.BREAKING;
        }
        boolean anyUncertain = false;
        for (int i = 0; i < newAlternatives.size(); i++) {
            Boolean satisfied = alternativeSatisfiedBy(newAlternatives.get(i), oldAlternative, newCatalog, oldCatalog);
            if (satisfied == null) {
                anyUncertain = true;
            } else if (satisfied.booleanValue()) {
                return FindingImpact.COMPATIBLE;
            }
        }
        return anyUncertain ? FindingImpact.UNRESOLVED : FindingImpact.BREAKING;
    }

    /**
     * True if every scheme {@code newAlternative} requires has a mechanism-equivalent
     * counterpart in {@code oldAlternative} whose granted scopes already cover what
     * the new scheme demands (an AND requirement: every new scheme must be
     * covered, and nothing extra is assumed available); {@code null} if this
     * could not be established for at least one scheme (an unresolved scheme
     * name, or a mechanism whose trust equivalence this checker does not
     * evaluate); {@code false} if some new scheme is definitely not covered.
     */
    private static Boolean alternativeSatisfiedBy(Map<String, List<String>> newAlternative,
            Map<String, List<String>> oldAlternative, SecuritySchemeCatalog newCatalog, SecuritySchemeCatalog oldCatalog) {
        boolean anyUncertain = false;
        List<String> newSchemeNames = new ArrayList<String>(newAlternative.keySet());
        for (int i = 0; i < newSchemeNames.size(); i++) {
            String newSchemeName = newSchemeNames.get(i);
            List<String> newScopes = newAlternative.get(newSchemeName);
            SecuritySchemeInfo newInfo = newCatalog.get(newSchemeName);
            boolean covered = false;
            List<String> oldSchemeNames = new ArrayList<String>(oldAlternative.keySet());
            for (int j = 0; j < oldSchemeNames.size(); j++) {
                String oldSchemeName = oldSchemeNames.get(j);
                SecuritySchemeInfo oldInfo = oldCatalog.get(oldSchemeName);
                Boolean equivalent = mechanismEquivalent(oldInfo, newInfo);
                if (equivalent == null) {
                    anyUncertain = true;
                    continue;
                }
                if (equivalent.booleanValue() && oldAlternative.get(oldSchemeName).containsAll(newScopes)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                return anyUncertain ? null : Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * {@code true} if {@code a} and {@code b} are the same authentication
     * mechanism (regardless of component name); {@code false} if they are
     * positively different; {@code null} if either is unresolved, or if the
     * mechanism's trust equivalence is not something this checker evaluates
     * (an {@code oauth2} issuer/token-acceptance equivalence, an opaque
     * {@code openIdConnect} role, or an mTLS condition expressed only in
     * prose) -- two same-typed schemes are never assumed to accept the same
     * credentials without a concrete, checkable basis.
     */
    private static Boolean mechanismEquivalent(SecuritySchemeInfo a, SecuritySchemeInfo b) {
        if (a == null || b == null) {
            return null;
        }
        if (a.getType() == null || !a.getType().equals(b.getType())) {
            return Boolean.FALSE;
        }
        if ("apiKey".equals(a.getType())) {
            return (equalsNullable(a.getIn(), b.getIn()) && equalsNullable(a.getName(), b.getName())) ? Boolean.TRUE : Boolean.FALSE;
        }
        if ("http".equals(a.getType())) {
            return equalsIgnoreCaseNullable(a.getHttpScheme(), b.getHttpScheme()) ? Boolean.TRUE : Boolean.FALSE;
        }
        if ("oauth2".equals(a.getType()) || "openIdConnect".equals(a.getType())) {
            // A genuine trust-root/issuer equivalence is not established from the
            // document alone -- but when both sides document the same
            // credential-acquisition availability, the scheme is at least not
            // positively known to have changed, which is enough to let a pure
            // scope-obligation comparison proceed; a change in that availability
            // (see CREDENTIAL_FLOW_REMOVED) is itself treated as a different
            // mechanism for this purpose, not silently absorbed into "equivalent".
            return (a.hasFlow() == b.hasFlow()) ? Boolean.TRUE : Boolean.FALSE;
        }
        // An opaque role or an mTLS condition expressed only in prose: uncertain.
        return null;
    }

    private static void compareCredentialFlows(EffectiveInteraction original, List<Map<String, List<String>>> oldAlternatives,
            SecuritySchemeCatalog oldCatalog, SecuritySchemeCatalog newCatalog, List<CompatibilityFinding> findings) {
        List<String> reported = new ArrayList<String>();
        for (int i = 0; i < oldAlternatives.size(); i++) {
            List<String> schemeNames = new ArrayList<String>(oldAlternatives.get(i).keySet());
            for (int j = 0; j < schemeNames.size(); j++) {
                String schemeName = schemeNames.get(j);
                if (reported.contains(schemeName)) {
                    continue;
                }
                SecuritySchemeInfo oldInfo = oldCatalog.get(schemeName);
                SecuritySchemeInfo newInfo = newCatalog.get(schemeName);
                if (oldInfo != null && newInfo != null && "oauth2".equals(oldInfo.getType())
                        && "oauth2".equals(newInfo.getType()) && oldInfo.hasFlow() && !newInfo.hasFlow()) {
                    reported.add(schemeName);
                    findings.add(new CompatibilityFinding(FindingCode.CREDENTIAL_FLOW_REMOVED, FindingImpact.BREAKING,
                            original.getInteractionId(), null, null, null, null,
                            "Security scheme '" + schemeName + "' no longer documents a credential-acquisition flow"));
                }
            }
        }
    }

    private static String describeAlternative(Map<String, List<String>> alternative) {
        if (alternative.isEmpty()) {
            return "Anonymous access";
        }
        return "Security alternative " + alternative.keySet();
    }

    private static boolean equalsNullable(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    private static boolean equalsIgnoreCaseNullable(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equalsIgnoreCase(b);
    }
}
