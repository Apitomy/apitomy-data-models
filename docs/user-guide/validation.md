# Validation

The library includes a built-in validation engine with hundreds of rules for detecting
specification compliance issues in OpenAPI, AsyncAPI, and OpenRPC documents.

## Basic Usage

Call `Library.validate()` to validate a document. Pass `null` for the severity registry to
use the default severity levels.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.validation.ValidationProblem;
    import java.util.List;

    List<ValidationProblem> problems = Library.validate(doc, null);
    for (ValidationProblem problem : problems) {
        System.out.println("[" + problem.severity + "] " + problem.message);
        System.out.println("  Path: " + problem.nodePath);
        System.out.println("  Code: " + problem.errorCode);
    }
    ```

=== "TypeScript"

    ```typescript
    import { Library, ValidationProblem } from '@apitomy/data-models';

    const problems: ValidationProblem[] = Library.validate(doc, null);
    problems.forEach(problem => {
        console.log(`[${problem.severity}] ${problem.message}`);
        console.log(`  Path: ${problem.nodePath}`);
        console.log(`  Code: ${problem.errorCode}`);
    });
    ```

## ValidationProblem Fields

Each `ValidationProblem` includes the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `errorCode` | `String` | Machine-readable error code (e.g., `INF-001`) |
| `nodePath` | `NodePath` | Path to the node where the problem was found |
| `property` | `String` | The property name that caused the problem |
| `message` | `String` | Human-readable description of the problem |
| `severity` | `ValidationProblemSeverity` | Severity level of the problem |

## Severity Levels

| Severity | Description |
|----------|-------------|
| **Error** | Violations of the specification that must be fixed |
| **Warning** | Potential issues that should be reviewed |
| **Information** | Suggestions for improvement |
| **Hint** | Minor style or convention notes |

## Error Code Prefixes

Validation error codes use an entity-based naming convention. The prefix identifies which
part of the specification the rule applies to:

| Prefix | Entity | Example |
|--------|--------|---------| 
| `INF` | Info | `INF-001` |
| `OP` | Operation | `OP-001` |
| `PAR` | Parameter | `PAR-001` |
| `PATH` | Path Item | `PATH-001` |
| `SCH` | Schema | `SCH-001` |
| `HEAD` | Header | `HEAD-001` |
| `RES` | Response | `RES-001` |
| `RB` | Request Body | `RB-001` |
| `SS` | Security Scheme | `SS-001` |
| `SREQ` | Security Requirement | `SREQ-001` |
| `SRV` | Server | `SRV-001` |
| `SVAR` | Server Variable | `SVAR-001` |
| `TAG` | Tag | `TAG-001` |
| `FLOW` | OAuth Flow | `FLOW-001` |
| `LINK` | Link | `LINK-001` |
| `ENC` | Encoding | `ENC-001` |
| `MT` | Media Type | `MT-001` |
| `DISC` | Discriminator | `DISC-001` |
| `LIC` | License | `LIC-001` |
| `CTC` | Contact | `CTC-001` |
| `XML` | XML | `XML-001` |
| `COMP` | Components | `COMP-001` |
| `EX` | Example | `EX-001` |
| `CHAN` | Channel (AsyncAPI) | `CHAN-001` |
| `AAD`, `AAM`, `AAO` | AsyncAPI Document, Message, Operation | `AAD-001` |
| `ORPC` | OpenRPC | `ORPC-001` |

Codes with a `DEF` suffix (e.g., `SDEF`, `PDEF`, `RDEF`) apply to component definitions
(the reusable entries under `components/`).

---

## Validating a Subtree

You can validate any node in the tree, not just the root document. This validates only that
node and its descendants.

=== "Java"

    ```java
    // Validate just the info section
    List<ValidationProblem> infoProblems = Library.validate(doc.getInfo(), null);
    ```

=== "TypeScript"

    ```typescript
    // Validate just the info section
    const infoProblems = Library.validate(doc.getInfo(), null);
    ```

---

## Custom Severity Registry

You can customize the severity of any validation rule by implementing
`IValidationSeverityRegistry`. This is useful when you want to treat certain warnings as
errors, or suppress rules entirely by downgrading them to hints.

=== "Java"

    ```java
    import io.apitomy.datamodels.validation.IValidationSeverityRegistry;
    import io.apitomy.datamodels.validation.ValidationProblemSeverity;

    class StrictSeverityRegistry implements IValidationSeverityRegistry {
        @Override
        public ValidationProblemSeverity lookupSeverity(
                String ruleCode,
                ValidationProblemSeverity defaultSeverity) {
            // Upgrade all warnings to errors
            if (defaultSeverity == ValidationProblemSeverity.warning) {
                return ValidationProblemSeverity.high;
            }
            return defaultSeverity;
        }
    }

    List<ValidationProblem> problems = Library.validate(doc, new StrictSeverityRegistry());
    ```

=== "TypeScript"

    ```typescript
    import {
        IValidationSeverityRegistry,
        ValidationProblemSeverity
    } from '@apitomy/data-models';

    class StrictSeverityRegistry implements IValidationSeverityRegistry {
        lookupSeverity(
            ruleCode: string,
            defaultSeverity: ValidationProblemSeverity
        ): ValidationProblemSeverity {
            // Upgrade all warnings to errors
            if (defaultSeverity === ValidationProblemSeverity.warning) {
                return ValidationProblemSeverity.high;
            }
            return defaultSeverity;
        }
    }

    const problems = Library.validate(doc, new StrictSeverityRegistry());
    ```

---

## Validation with External References

When your document contains external `$ref` references, the validation engine may need to
resolve them to fully validate the document. Register a reference resolver before validating.

=== "Java"

    ```java
    import io.apitomy.datamodels.refs.IReferenceResolver;
    import io.apitomy.datamodels.refs.ResolvedReference;
    import io.apitomy.datamodels.models.Node;

    // Register a resolver that can fetch external references
    Library.addReferenceResolver(new IReferenceResolver() {
        @Override
        public ResolvedReference resolveRef(String reference, Node from) {
            // Resolve external references (e.g., from a file or URL)
            // Return null for references you can't resolve
            return null;
        }
    });

    // Now validate — external refs will be resolved during validation
    List<ValidationProblem> problems = Library.validate(doc, null);
    ```

=== "TypeScript"

    ```typescript
    import { IReferenceResolver, ResolvedReference, Node } from '@apitomy/data-models';

    // Register a resolver that can fetch external references
    Library.addReferenceResolver({
        resolveRef(reference: string, from: Node): ResolvedReference {
            // Resolve external references (e.g., from a file or URL)
            // Return null for references you can't resolve
            return null;
        }
    });

    // Now validate — external refs will be resolved during validation
    const problems = Library.validate(doc, null);
    ```

See [Dereferencing](dereferencing.md) for more details on implementing custom reference
resolvers.
