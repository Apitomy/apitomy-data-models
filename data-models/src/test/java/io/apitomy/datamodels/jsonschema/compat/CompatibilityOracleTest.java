package io.apitomy.datamodels.jsonschema.compat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the checker's verdicts against a JSON Schema validator: a document valid against the
 * original schema and invalid against the updated one disproves a "compatible" verdict.
 * <p>
 * Fails on findings not listed in {@code oracle-known-failures.json}, and on listed entries that
 * no longer match. The approach, the workflow and a worked example are in {@code README.md} next
 * to this class.
 */
public class CompatibilityOracleTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> DRAFTS = Map.of(
            "draft-04", "http://json-schema.org/draft-04/schema#",
            "draft-06", "http://json-schema.org/draft-06/schema#",
            "draft-07", "http://json-schema.org/draft-07/schema#",
            "2019-09", "https://json-schema.org/draft/2019-09/schema",
            "2020-12", "https://json-schema.org/draft/2020-12/schema");

    private static final Path REPORT = Path.of("target", "compatibility-oracle-report.md");

    enum Category {
        /** The checker says compatible, and a document proves otherwise. */
        FALSE_COMPATIBLE,
        /** Adding an unused definition to both schemas changed the verdict. */
        NEUTRAL_EDIT_CHANGED_VERDICT,
        /**
         * The checker threw, or reported unsupported features, which callers are told to treat as
         * "could not be determined".
         */
        NO_VERDICT
    }

    private record Pair(String id, List<String> directions, JsonNode original, JsonNode updated,
                        JsonSchemaCompatibilityChecker checker) {
    }

    private record Finding(Category category, String id, String detail) {
    }

    private record KnownFailure(Category category, String pattern, String issue, Pattern regex) {
    }

    /** Totals for the report. */
    private static final class Stats {
        int pairs;
        int skippedPairs;
        int directions;
        int documents;
        int noValidDocument;
        int confirmedIncompatible;
    }

    @Test
    public void verdictsHoldAgainstAValidator() throws IOException {
        var pairs = new ArrayList<Pair>();
        var support = new CompatCaseSupport();
        cataloguePairs(support, pairs);
        var catalogueCount = pairs.size();
        mutationPairs(support.checkerFor(null, null), pairs);

        var validator = new Validator();
        var stats = new Stats();
        var findings = new ArrayList<Finding>();
        var possiblyOverStrict = new TreeMap<String, List<String>>();
        for (var pair : pairs) {
            evaluate(pair, validator, stats, findings, possiblyOverStrict);
        }

        var known = readKnownFailures();
        var unexpected = new ArrayList<Finding>();
        var matchedBy = new LinkedHashMap<KnownFailure, List<Finding>>();
        known.forEach(k -> matchedBy.put(k, new ArrayList<>()));
        for (var finding : findings) {
            var match = known.stream()
                    .filter(k -> k.category() == finding.category() && k.regex().matcher(finding.id()).matches())
                    .findFirst();
            match.ifPresentOrElse(k -> matchedBy.get(k).add(finding), () -> unexpected.add(finding));
        }
        var stale = known.stream().filter(k -> matchedBy.get(k).isEmpty()).toList();

        writeReport(stats, catalogueCount, pairs.size() - catalogueCount, unexpected, matchedBy,
                possiblyOverStrict);

        var message = new StringBuilder();
        if (!unexpected.isEmpty()) {
            message.append("%d unexpected finding(s):%n".formatted(unexpected.size()));
            unexpected.stream().limit(25).forEach(f -> message.append("  %s  %s%n    %s%n"
                    .formatted(f.category(), f.id(), f.detail().replace("\n", "\n    "))));
        }
        if (!stale.isEmpty()) {
            message.append("%d known failure(s) no longer reproduce; remove them from oracle-known-failures.json:%n"
                    .formatted(stale.size()));
            stale.forEach(k -> message.append("  %s  %s  (%s)%n".formatted(k.category(), k.pattern(), k.issue())));
        }
        message.append("Full report: ").append(REPORT.toAbsolutePath());
        assertTrue(unexpected.isEmpty() && stale.isEmpty(), message.toString());
    }

    // -----------------------------------------------------------------------
    // Pairs
    // -----------------------------------------------------------------------

    private static void cataloguePairs(CompatCaseSupport support, List<Pair> out) {
        for (var testCase : CompatCaseSupport.readCatalog().get("tests")) {
            if (!testCase.path("enabled").asBoolean(true)
                    || testCase.has("externalRefs")
                    || testCase.path("expected").has("error")) {
                continue;
            }
            out.add(new Pair("catalogue: " + testCase.get("id").asText(), List.of("backward", "forward"),
                    testCase.get("original"), testCase.get("updated"),
                    support.checkerFor(testCase.get("config"), null)));
        }
    }

    private static void mutationPairs(JsonSchemaCompatibilityChecker checker, List<Pair> out) throws IOException {
        JsonNode bases;
        try (var in = CompatibilityOracleTest.class.getResourceAsStream("oracle-bases.json")) {
            bases = MAPPER.readTree(in).get("bases");
        }
        for (var base : bases) {
            for (var draft : base.get("drafts")) {
                var schema = MAPPER.createObjectNode();
                schema.put("$schema", DRAFTS.get(draft.asText()));
                schema.setAll((ObjectNode) base.get("schema").deepCopy());
                var baseId = base.get("id").asText() + " @" + draft.asText();
                for (var mutation : OracleSchemaMutator.mutations(schema)) {
                    out.add(new Pair(baseId + " " + mutation.location() + " " + mutation.edit(),
                            List.of("apply", "revert"), mutation.original(), mutation.updated(), checker));
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Evaluation
    // -----------------------------------------------------------------------

    private static void evaluate(Pair pair, Validator validator, Stats stats, List<Finding> findings,
                                 Map<String, List<String>> possiblyOverStrict) {
        var documents = OracleInstanceGenerator.generate(pair.original(), pair.updated());
        boolean[] validOriginal;
        boolean[] validUpdated;
        try {
            validOriginal = validator.validity(pair.original(), documents);
            validUpdated = validator.validity(pair.updated(), documents);
        } catch (RuntimeException e) {
            // The validator rejected the schema itself, e.g. a mutation produced an invalid keyword value.
            stats.skippedPairs++;
            return;
        }
        stats.pairs++;
        stats.documents += documents.size();

        checkDirection(pair, pair.directions().get(0), pair.original(), pair.updated(), documents,
                validOriginal, validUpdated, stats, findings, possiblyOverStrict);
        checkDirection(pair, pair.directions().get(1), pair.updated(), pair.original(), documents,
                validUpdated, validOriginal, stats, findings, possiblyOverStrict);
    }

    private static void checkDirection(Pair pair, String direction, JsonNode from, JsonNode to,
                                       List<JsonNode> documents, boolean[] validFrom, boolean[] validTo,
                                       Stats stats, List<Finding> findings,
                                       Map<String, List<String>> possiblyOverStrict) {
        var id = pair.id() + " " + direction;
        stats.directions++;

        var counterexample = -1;
        var anyValid = false;
        for (var i = 0; i < documents.size(); i++) {
            anyValid |= validFrom[i];
            if (validFrom[i] && !validTo[i]) {
                counterexample = i;
                break;
            }
        }
        if (!anyValid) {
            stats.noValidDocument++;
        }

        var result = verdict(pair.checker(), from, to, id, findings);
        if (result == null) {
            return;
        }

        if (result.isCompatible() && counterexample >= 0) {
            findings.add(new Finding(Category.FALSE_COMPATIBLE, id, describe(from, to)
                    + "\ndocument: " + documents.get(counterexample)
                    + "\ndifferences: " + differences(result)));
        } else if (!result.isCompatible()) {
            if (counterexample >= 0) {
                stats.confirmedIncompatible++;
            } else {
                possiblyOverStrict.computeIfAbsent(differences(result.getIncompatibleDifferences()),
                        k -> new ArrayList<>()).add(id);
            }
        }

        var fromNeutral = withUnusedDefinition(from);
        var toNeutral = withUnusedDefinition(to);
        if (fromNeutral == null || toNeutral == null) {
            return;
        }
        var neutral = verdict(pair.checker(), fromNeutral, toNeutral, id + " with an unused definition", findings);
        if (neutral != null && neutral.isCompatible() != result.isCompatible()) {
            findings.add(new Finding(Category.NEUTRAL_EDIT_CHANGED_VERDICT, id, describe(from, to)
                    + "\nwithout the definition: compatible=" + result.isCompatible() + " " + differences(result)
                    + "\nwith it: compatible=" + neutral.isCompatible() + " " + differences(neutral)));
        }
    }

    /**
     * Runs the checker, or records a {@link Category#NO_VERDICT} finding and returns {@code null}
     * when it throws or reports unsupported features.
     */
    private static CompatibilityCheckResult verdict(JsonSchemaCompatibilityChecker checker, JsonNode from,
                                                    JsonNode to, String id, List<Finding> findings) {
        try {
            var result = checker.checkBackward(from.toString(), to.toString());
            if (result.hasUnsupportedFeatures()) {
                findings.add(new Finding(Category.NO_VERDICT, id, describe(from, to)
                        + "\nunsupported: " + result.getUnsupportedFeatures()));
                return null;
            }
            return result;
        } catch (Exception e) {
            findings.add(new Finding(Category.NO_VERDICT, id, describe(from, to) + "\n" + e));
            return null;
        }
    }

    /**
     * Adds a definition nothing references, under the keyword the schema's draft uses. Returns
     * {@code null} for a boolean schema, which cannot carry one.
     */
    private static JsonNode withUnusedDefinition(JsonNode schema) {
        if (!schema.isObject()) {
            return null;
        }
        var draft = schema.path("$schema").asText();
        var keyword = draft.contains("2019-09") || draft.contains("2020-12") ? "$defs" : "definitions";
        var copy = (ObjectNode) schema.deepCopy();
        var definitions = copy.has(keyword) ? (ObjectNode) copy.get(keyword) : copy.putObject(keyword);
        definitions.putObject("unused").put("type", "string");
        return copy;
    }

    private static String describe(JsonNode from, JsonNode to) {
        return "original: " + from + "\nupdated:  " + to;
    }

    private static String differences(CompatibilityCheckResult result) {
        return differences(result.getDifferences());
    }

    private static String differences(Iterable<Difference> differences) {
        var sorted = new TreeSet<String>();
        differences.forEach(d -> sorted.add(d.getDiffType().name()));
        return sorted.toString();
    }

    // -----------------------------------------------------------------------
    // Validator
    // -----------------------------------------------------------------------

    /**
     * The oracle. Draft 7 is the default dialect, matching the draft the checker assumes for a
     * schema without {@code $schema}.
     */
    private static final class Validator {

        private final SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        private final Map<String, Schema> compiled = new HashMap<>();

        boolean[] validity(JsonNode schema, List<JsonNode> documents) {
            var validator = compiled.computeIfAbsent(schema.toString(), k -> registry.getSchema(schema));
            var result = new boolean[documents.size()];
            for (var i = 0; i < documents.size(); i++) {
                result[i] = validator.validate(documents.get(i)).isEmpty();
            }
            return result;
        }
    }

    // -----------------------------------------------------------------------
    // Known failures and report
    // -----------------------------------------------------------------------

    private static List<KnownFailure> readKnownFailures() throws IOException {
        JsonNode entries;
        try (var in = CompatibilityOracleTest.class.getResourceAsStream("oracle-known-failures.json")) {
            entries = MAPPER.readTree(in).get("knownFailures");
        }
        var out = new ArrayList<KnownFailure>();
        for (var entry : entries) {
            var pattern = entry.get("pattern").asText();
            var regex = Pattern.compile(Pattern.quote(pattern).replace("*", "\\E.*\\Q"));
            out.add(new KnownFailure(Category.valueOf(entry.get("category").asText()), pattern,
                    entry.get("issue").asText(), regex));
        }
        return out;
    }

    private static void writeReport(Stats stats, int cataloguePairs, int mutationPairs, List<Finding> unexpected,
                                    Map<KnownFailure, List<Finding>> known,
                                    Map<String, List<String>> possiblyOverStrict) {
        var knownCount = known.values().stream().mapToInt(List::size).sum();
        var overStrictCount = possiblyOverStrict.values().stream().mapToInt(List::size).sum();
        var report = new StringBuilder("""
                # Compatibility oracle report

                Generated by `CompatibilityOracleTest`. See the `README.md` next to it for what each section means.

                | | |
                |---|---|
                | schema pairs | %d (%d from the catalogue, %d from mutations) |
                | pairs skipped because the validator rejected a schema | %d |
                | directions checked | %d |
                | documents generated | %d |
                | directions with no generated document valid against the original | %d |
                | incompatible verdicts confirmed by a document | %d |
                | incompatible verdicts with no document found (possibly over-strict) | %d |
                | unexpected findings | %d |
                | known findings | %d |

                """.formatted(stats.pairs + stats.skippedPairs, cataloguePairs, mutationPairs, stats.skippedPairs,
                stats.directions, stats.documents, stats.noValidDocument, stats.confirmedIncompatible,
                overStrictCount, unexpected.size(), knownCount));

        report.append("## Unexpected findings\n\n");
        appendFindings(report, unexpected);

        report.append("## Known findings\n\n");
        known.forEach((k, findings) -> {
            report.append("### %s: `%s` (%d)\n\n".formatted(k.issue(), k.pattern(), findings.size()));
            appendFindings(report, findings);
        });

        report.append("""
                ## Possibly over-strict

                Incompatible verdicts for which no generated document tells the two schemas apart,
                grouped by the incompatible difference types reported.

                | difference types | count | example |
                |---|---|---|
                """);
        possiblyOverStrict.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .forEach(e -> report.append("| %s | %d | %s |\n"
                        .formatted(e.getKey(), e.getValue().size(), e.getValue().get(0))));

        try {
            Files.createDirectories(REPORT.getParent());
            Files.writeString(REPORT, report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void appendFindings(StringBuilder report, List<Finding> findings) {
        if (findings.isEmpty()) {
            report.append("None.\n\n");
            return;
        }
        var byCategory = findings.stream().collect(Collectors.groupingBy(Finding::category, TreeMap::new,
                Collectors.toList()));
        byCategory.forEach((category, list) -> {
            report.append("**%s** (%d)\n\n".formatted(category, list.size()));
            for (var finding : list) {
                report.append("- `%s`\n  ```\n  %s\n  ```\n".formatted(finding.id(),
                        finding.detail().replace("\n", "\n  ")));
            }
            report.append('\n');
        });
    }
}
