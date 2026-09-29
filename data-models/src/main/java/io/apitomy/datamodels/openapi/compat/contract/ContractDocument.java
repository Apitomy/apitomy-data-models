package io.apitomy.datamodels.openapi.compat.contract;

import java.util.List;

import io.apitomy.datamodels.util.CollectionUtil;

/**
 * The read-only, family-normalized effective contract interpreted from one
 * root document, produced by {@link ContractInterpreter}.
 */
public final class ContractDocument {

    private final List<EffectiveInteraction> interactions;
    private final List<String> problems;
    private final boolean componentOnly;
    private final String coverageNote;

    public ContractDocument(List<EffectiveInteraction> interactions, List<String> problems, boolean componentOnly,
            String coverageNote) {
        this.interactions = CollectionUtil.copyOfList(interactions);
        this.problems = CollectionUtil.copyOfList(problems);
        this.componentOnly = componentOnly;
        this.coverageNote = coverageNote;
    }

    /** Every effective interaction (ordinary path operation or webhook operation) reachable from this document. */
    public List<EffectiveInteraction> getInteractions() {
        return CollectionUtil.copyOfList(interactions);
    }

    /**
     * Localized problems recognized while interpreting this document -- an
     * invalid conflicting or duplicate declaration (for example, two
     * effective parameters with the same name and {@code in}) -- described for
     * diagnostic purposes. These are input errors in the source document, not
     * unresolved obligations; interpretation still proceeds using the first
     * declaration encountered for anything ambiguous.
     */
    public List<String> getProblems() {
        return CollectionUtil.copyOfList(problems);
    }

    /**
     * True if this document declares no reachable interaction at all (no
     * paths, and no webhooks): a component-only document publishing a reusable
     * schema/parameter/response library with nothing of its own to check. Its
     * empty interaction list is not itself evidence of a removed API surface
     * when compared against a document that does have interactions --
     * see {@link #getCoverageNote()}.
     */
    public boolean isComponentOnly() {
        return componentOnly;
    }

    /** An explicit note recorded when {@link #isComponentOnly()} is true, rather than silently treating the document as a normal, fully-covered empty API. */
    public String getCoverageNote() {
        return coverageNote;
    }
}
