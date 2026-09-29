package io.apitomy.datamodels.openapi.compat.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * The set of concrete addresses (server origin + path template) an
 * interaction is reachable at, expanded from its {@link EffectiveServer}
 * templates where that expansion is actually finite.
 * <p>
 * A server variable with a declared {@code enum} is a finite choice: every
 * combination of every variable's enum values is expanded into a distinct
 * concrete origin. A server variable with only a {@code default} (no
 * {@code enum}) is open-ended: the address set can only represent it
 * structurally (the template string itself), never enumerate the unbounded
 * domain it stands for. A server variable in the template with neither an
 * {@code enum} nor a resolvable value is unresolved, and the whole entry is
 * recorded as such rather than guessed.
 */
public final class AddressSet {

    /** One address this set contains: either a concrete origin, or an open/unresolved template, paired with the interaction's path template. */
    public static final class Address {
        private final String origin;
        private final boolean open;
        private final boolean unresolved;
        private final String pathTemplate;

        Address(String origin, boolean open, boolean unresolved, String pathTemplate) {
            this.origin = origin;
            this.open = open;
            this.unresolved = unresolved;
            this.pathTemplate = pathTemplate;
        }

        /** The concrete origin (e.g. {@code https://api.example.com}), or the raw template string when {@link #isOpen()} or {@link #isUnresolved()}. */
        public String getOrigin() {
            return origin;
        }

        /** True if this origin came from a server variable with no declared {@code enum} -- structural comparison only, never enumerated. */
        public boolean isOpen() {
            return open;
        }

        /** True if this origin's template contains a variable this checker could not resolve any value for at all. */
        public boolean isUnresolved() {
            return unresolved;
        }

        /** The path template (with its own placeholders, if any, left intact) reachable at {@link #getOrigin()}. */
        public String getPathTemplate() {
            return pathTemplate;
        }

        /** True if this address is exactly the same origin and path template as {@code other} (a concrete-to-concrete or open-template-to-open-template comparison; never true across an open/concrete mismatch). */
        public boolean matches(Address other) {
            if (open != other.open || unresolved != other.unresolved) {
                return false;
            }
            return origin.equals(other.origin) && pathTemplate.equals(other.pathTemplate);
        }
    }

    private final List<Address> addresses;

    private AddressSet(List<Address> addresses) {
        this.addresses = addresses;
    }

    /** Expands every server in {@code servers} against {@code pathTemplate}, producing one address per concrete variable-value combination (or one open/unresolved address per server when expansion is not finite). */
    public static AddressSet of(List<EffectiveServer> servers, String pathTemplate) {
        List<Address> addresses = new ArrayList<Address>();
        if (servers != null) {
            for (int i = 0; i < servers.size(); i++) {
                expandServer(servers.get(i), pathTemplate, addresses);
            }
        }
        return new AddressSet(addresses);
    }

    private static void expandServer(EffectiveServer server, String pathTemplate, List<Address> out) {
        List<String> variableNames = templateVariables(server.getUrlTemplate());
        if (variableNames.isEmpty()) {
            out.add(new Address(server.getUrlTemplate(), false, false, pathTemplate));
            return;
        }
        Map<String, List<String>> enums = server.getVariableEnums();
        Map<String, String> defaults = server.getVariableDefaults();
        boolean allFinite = true;
        boolean anyUnresolved = false;
        for (int i = 0; i < variableNames.size(); i++) {
            String name = variableNames.get(i);
            if (!enums.containsKey(name)) {
                allFinite = false;
                if (!defaults.containsKey(name)) {
                    anyUnresolved = true;
                }
            }
        }
        if (anyUnresolved) {
            out.add(new Address(server.getUrlTemplate(), false, true, pathTemplate));
            return;
        }
        if (!allFinite) {
            out.add(new Address(server.getUrlTemplate(), true, false, pathTemplate));
            return;
        }
        List<String> expansions = new ArrayList<String>();
        expansions.add(server.getUrlTemplate());
        for (int i = 0; i < variableNames.size(); i++) {
            String name = variableNames.get(i);
            List<String> values = enums.get(name);
            List<String> next = new ArrayList<String>();
            for (int e = 0; e < expansions.size(); e++) {
                String partial = expansions.get(e);
                for (int v = 0; v < values.size(); v++) {
                    next.add(partial.replace("{" + name + "}", values.get(v)));
                }
            }
            expansions = next;
        }
        for (int i = 0; i < expansions.size(); i++) {
            out.add(new Address(expansions.get(i), false, false, pathTemplate));
        }
    }

    /** The variable names ({@code {name}}) appearing in {@code template}, in order of first appearance, without duplicates. */
    static List<String> templateVariables(String template) {
        List<String> names = new ArrayList<String>();
        int i = 0;
        while (i < template.length()) {
            int open = template.indexOf('{', i);
            if (open < 0) {
                break;
            }
            int close = template.indexOf('}', open + 1);
            if (close < 0) {
                break;
            }
            String name = template.substring(open + 1, close);
            if (!names.contains(name)) {
                names.add(name);
            }
            i = close + 1;
        }
        return names;
    }

    public List<Address> getAddresses() {
        return CollectionUtil.copyOfList(addresses);
    }

    /**
     * True if every one of this set's addresses is matched by some address in
     * {@code other} (an exact origin+path match for a concrete or open
     * template). An unresolved address, or a concrete address with no
     * matching counterpart, makes this false -- callers distinguish those two
     * cases via {@link #findUnmatched} for diagnostics.
     */
    public boolean isCoveredBy(AddressSet other) {
        return findUnmatched(other).isEmpty();
    }

    /** Every address in this set with no matching counterpart in {@code other}. */
    public List<Address> findUnmatched(AddressSet other) {
        List<Address> unmatched = new ArrayList<Address>();
        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            boolean found = false;
            for (int j = 0; j < other.addresses.size(); j++) {
                if (address.matches(other.addresses.get(j))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unmatched.add(address);
            }
        }
        return unmatched;
    }
}
