# AsyncAPI Validation Rules

This document contains a comprehensive list of validation rules extracted from AsyncAPI specification files
(versions 2.0 through 3.0). These rules should be implemented in the `io.apitomy.datamodels.validation`
package to provide complete validation coverage for AsyncAPI documents.

## Overview

- **Total Rules Identified:** ~150+ distinct validation rules
- **AsyncAPI Versions Covered:** 2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 3.0
- **Rule Categories:** Required Property, Invalid Format, Invalid Value, Invalid Name, Invalid Reference,
  Mutual Exclusivity, Invalid Type, Uniqueness, Version-Specific

## Legend

- **Rule Code:** Unique identifier following the pattern `{ENTITY}-{NUMBER}`
- **Entity:** The AsyncAPI entity type the rule applies to
- **Versions:** AsyncAPI versions where this rule applies
- **Type:** Category of validation rule
- **Spec Mandated:** Whether the rule is required by the specification (Yes) or a best practice (No)
- **Status:** ✓ = Already implemented, ○ = Not yet implemented

## Rule Code Prefixes

- **AAD**: AsyncAPI Document (root)
- **INF**: Info
- **CTC**: Contact
- **LIC**: License
- **SRV**: Server
- **SVAR**: Server Variable
- **CHAN**: Channel
- **AAO**: AsyncAPI Operation
- **AATRT**: AsyncAPI Operation Trait
- **AAREPLY**: AsyncAPI Operation Reply
- **AARADDR**: AsyncAPI Reply Address
- **AAPARAM**: AsyncAPI Parameter
- **AAM**: AsyncAPI Message
- **AAMTRT**: AsyncAPI Message Trait
- **AAMEX**: AsyncAPI Message Example
- **TAG**: Tag
- **AATAG**: AsyncAPI Tag (additional)
- **ED**: External Documentation
- **AAED**: AsyncAPI External Documentation (additional)
- **COMP**: Components
- **SCH**: Schema
- **AASCH**: AsyncAPI Schema (additional)
- **AAMFS**: AsyncAPI Multi-Format Schema
- **SS**: Security Scheme
- **AASS**: AsyncAPI Security Scheme (additional)
- **FLOW**: OAuth Flow
- **AAFLOW**: AsyncAPI OAuth Flow (additional)
- **SREQ**: Security Requirement
- **AASREQ**: AsyncAPI Security Requirement (additional)
- **CID**: Correlation ID
- **AACID**: AsyncAPI Correlation ID (additional)
- **BIND**: Bindings
- **AAREF**: AsyncAPI Reference
- **EXT**: Extensions
- **TRAIT**: Traits

---

## 1. DOCUMENT (Root) ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description                                                               | Versions | Spec Mandated | Status |
|-----------|------|----------|---------------------------------------------------------------------------|----------|---------------|--------|
| AAD-001 | Missing AsyncAPI Property | `asyncapi` | Document must have an 'asyncapi' property specifying the version | All | Yes | ○ |
| AAD-002 | Missing Info Property | `info` | Document must have an 'info' property | All | Yes | ○ |
| AAD-003 | Missing Channels Property | `channels` | Document must have a 'channels' property (required in 2.x,optional in 3.0) | 2.0-2.6 | Yes | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description                                                      | Versions | Spec Mandated | Status |
|-----------|------|----------|------------------------------------------------------------------|----------|---------------|--------|
| AAD-004 | Invalid AsyncAPI Version Format | `asyncapi` | AsyncAPI version must match the pattern (e.g., "2.0.0", "3.0.0") | All | Yes | ○ |
| AAD-005 | Invalid Document ID Format | `id` | Document ID should be a valid URI/identifier if provided | All | Yes | ○ |
| AAD-006 | Invalid Default Content Type | `defaultContentType` | Default content type must be a valid MIME type | All | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAD-007 | Operations Property Only in 3.0 | `operations` | The 'operations' property only exists in AsyncAPI 3.0 | 3.0 | Yes | ○ |

---

## 2. INFO ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| INF-001 | Missing API Title | `title` | Info must have a title | All | Yes | ✓ |
| INF-002 | Missing API Version | `version` | Info must have a version | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| INF-003 | Invalid API Description | `description` | Description format validation | All | Yes | ✓ |
| INF-004 | Invalid Terms of Service URL | `termsOfService` | Terms of service must be a valid URL | All | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description                                          | Versions | Spec Mandated | Status |
|-----------|------|----------|------------------------------------------------------|----------|---------------|--------|
| INF-005 | Tags Property in Info (3.0 Only) | `tags` | In AsyncAPI 3.0, Info can have tags property | 3.0 | Yes | ○ |
| INF-006 | External Docs Property in Info (3.0 Only) | `externalDocs` | In AsyncAPI 3.0, Info can have externalDocs property | 3.0 | Yes | ○ |

---

## 3. CONTACT ENTITY

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CTC-001 | Invalid Contact URL | `url` | Contact URL must be a valid URL | All | Yes | ✓ |
| CTC-002 | Invalid Contact Email | `email` | Contact email must be a valid email format | All | Yes | ✓ |

---

## 4. LICENSE ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| LIC-001 | Missing License Name | `name` | License must have a name | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| LIC-002 | Invalid License URL | `url` | License URL must be a valid URL | All | Yes | ✓ |

---

## 5. SERVER ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SRV-001 | Missing Server URL | `url` | Server must have a URL in AsyncAPI 2.x | 2.0-2.6 | Yes | ✓ |
| SRV-004 | Missing Server Protocol | `protocol` | Server must have a protocol | All | Yes | ✓ |
| SRV-005 | Missing Server Host | `host` | In AsyncAPI 3.0, server must have a host (replaces 'url') | 3.0 | Yes | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SRV-002 | Invalid Server URL | `url` | Server URL format validation | 2.0-2.6 | Yes | ✓ |
| SRV-003 | Invalid Server Description | `description` | Server description format validation | All | Yes | ✓ |
| SRV-006 | Invalid Server Host Format | `host` | Server host must be valid hostname or IP | 3.0 | Yes | ○ |
| SRV-007 | Invalid Server Pathname Format | `pathname` | Server pathname must begin with '/' if provided | 3.0 | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions     | Spec Mandated | Status |
|-----------|------|----------|-------------|--------------|---------------|--------|
| SRV-008 | Invalid Server Reference | `$ref` | Server $ref must point to valid server in components | 2.3-2.6, 3.0 | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description                                                                     | Versions      | Spec Mandated | Status |
|-----------|------|----------|---------------------------------------------------------------------------------|---------------|---------------|---|
| SRV-009 | Server Referenceable Only in 2.3+ | `$ref` | Server gained Referenceable trait starting in 2.3 | 2.3-2.6, 3.0 | Yes | ○ |
| SRV-010 | Server Tags Only in 2.5+ | `tags` | Server can have tags only in 2.5, 2.6, and 3.0 | 2.5, 2.6, 3.0 | Yes | ○ |
| SRV-011 | Server Security Array Changed in 3.0 | `security` | In 3.0, security is type `[SecurityScheme]` not `[SecurityRequirement]` | 3.0 | Yes | ○ |
| SRV-012 | Server Structure Changed in 3.0 | `url` vs `host`/`pathname`/`protocol` | AsyncAPI 3.0 replaced 'url' with separate 'host', 'pathname', 'protocol' fields | 3.0 | Yes | ○ |

---

## 6. SERVER VARIABLE ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SVAR-001 | Missing Server Variable Default Value | `default` | Server variable must have a default value | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description                                                    | Versions | Spec Mandated | Status |
|-----------|------|----------|----------------------------------------------------------------|----------|---------------|--------|
| SVAR-002 | Invalid Server Variable Description | `description` | Server variable description format validation | All | Yes | ✓ |
| SVAR-003 | Server Variable Not Found in Template | Variable name | Server variable must be referenced in server URL/host template | All | Yes | ✓ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SVAR-004 | Server Variable Default Not in Enum | `default` | If enum is defined, default value must be one of the enum values | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description                                                            | Versions | Spec Mandated | Status |
|-----------|------|----------|------------------------------------------------------------------------|----------|---------------|--------|
| SVAR-005 | Invalid Server Variable Reference | `$ref` | Server variable $ref must point to valid server variable in components | 2.4-2.6, 3.0 | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SVAR-006 | Server Variable Referenceable Only in 2.4+ | `$ref` | ServerVariable gained Referenceable trait starting in 2.4 | 2.4-2.6, 3.0 | Yes | ✓ |

---

## 7. CHANNEL ITEM / CHANNEL ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CHAN-001 | Missing Channel Address | `address` | In AsyncAPI 3.0, Channel must have an address | 3.0 | Yes | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CHAN-002 | Invalid Channel Description | `description` | Channel description format validation | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CHAN-003 | Invalid Channel Reference | `$ref` | Channel $ref must point to valid channel definition | All | Yes | ○ |
| CHAN-004 | Invalid Server Reference in Channel | `servers` | Server references must point to valid servers defined in document | 2.2-2.6, 3.0 | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CHAN-005 | Server Not Defined at Document Level | `servers` | Servers referenced in channel.servers must exist in document.servers | 2.2-2.6, 3.0 | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CHAN-006 | Servers Property Only in 2.2+ | `servers` | The 'servers' property in channels only exists starting in 2.2 | 2.2-2.6, 3.0 | Yes | ○ |
| CHAN-007 | Channel Structure Changed in 3.0 | Multiple | AsyncAPI 3.0 channels no longer have subscribe/publish operations; they have messages and address instead | 3.0 | Yes | ○ |
| CHAN-008 | Channel Messages Property (3.0 Only) | `messages` | In 3.0, channels have messages map instead of operations | 3.0 | Yes | ○ |
| CHAN-009 | Channel Operations in 2.x Only | `subscribe`, `publish` | In 2.x, channels have subscribe/publish operations | 2.0-2.6 | Yes | ○ |

---

## 8. OPERATION ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| OP-008 | Missing Operation ID | `operationId` | Operation should have an operationId (best practice) | All | No | ✓ |
| OP-014 | Missing Operation Summary | `summary` | Operation should have a summary (best practice) | All | No | ✓ |
| OP-015 | Missing Operation Description | `description` | Operation should have a description (best practice) | All | No | ✓ |
| AAO-001 | Missing Operation Action | `action` | In AsyncAPI 3.0, Operation must have an action | 3.0 | Yes | ○ |
| AAO-002 | Missing Operation Channel Reference | `channel` | In AsyncAPI 3.0, Operation must reference a channel | 3.0 | Yes | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAO-003 | Invalid Operation Description | `description` | Operation description format validation | All | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAO-004 | Invalid Operation Action Value | `action` | Operation action must be one of: "send", "receive" | 3.0 | Yes | ○ |
| AAO-005 | Operation ID Uniqueness Across Document | `operationId` | Operation IDs must be unique across all operations | All | Yes | ✓ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAO-006 | Invalid Operation Trait Reference | `traits` | Operation trait references must point to valid operation traits | All | Yes | ○ |
| AAO-007 | Invalid Channel Reference | `channel` | Operation channel reference must point to valid channel | 3.0 | Yes | ○ |
| AAO-008 | Invalid Message Reference | `messages` (3.0), `message` (2.x) | Message references must point to valid messages | All | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAO-009 | Security Property Added in 2.4 | `security` | Operation gained security property in 2.4 | 2.4-2.6, 3.0 | Yes | ○ |
| AAO-010 | Operation Referenceable in 3.0 | `$ref` | Operation gained Referenceable trait in 3.0 | 3.0 | Yes | ○ |
| AAO-011 | Operation Reply Only in 3.0 | `reply` | Operation reply property only exists in 3.0 | 3.0 | Yes | ○ |
| AAO-012 | Operation Message vs Messages | `message` vs `messages` | 2.x uses 'message' (singular), 3.0 uses 'messages' (array of references) | All | Yes | ○ |

---

## 9. OPERATION TRAIT ENTITY

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AATRT-001 | Invalid Operation Trait Reference | `$ref` | Operation trait $ref must point to valid trait in components | All | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AATRT-002 | Operation Trait Security in 2.4+ | `security` | Operation trait gained security property in 2.4 | 2.4-2.6, 3.0 | Yes | ○ |
| AATRT-003 | Operation Trait Structure in 3.0 | Multiple | In 3.0, operation trait no longer has operationId | 3.0 | Yes | ○ |

---

## 10. OPERATION REPLY ENTITY (3.0 Only)

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAREPLY-001 | Invalid Reply Channel Reference | `channel` | Reply channel reference must point to valid channel | 3.0 | Yes | ○ |
| AAREPLY-002 | Invalid Reply Message References | `messages` | Reply message references must point to valid messages | 3.0 | Yes | ○ |
| AAREPLY-003 | Invalid Reply Reference | `$ref` | Reply $ref must point to valid reply in components | 3.0 | Yes | ○ |

---

## 11. OPERATION REPLY ADDRESS ENTITY (3.0 Only)

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AARADDR-001 | Invalid Reply Address Reference | `$ref` | Reply address $ref must point to valid reply address in components | 3.0 | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AARADDR-002 | Invalid Reply Address Location | `location` | Reply address location must be a valid runtime expression | 3.0 | Yes | ○ |

---

## 12. PARAMETER ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAPARAM-001 | Missing Parameter Location | `location` | Parameter must have a location (2.x) | 2.0-2.6 | Yes | ○ |
| AAPARAM-002 | Missing Parameter Schema | `schema` | Parameter should have a schema (2.x) | 2.0-2.6 | Yes | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAPARAM-003 | Invalid Parameter Description | `description` | Parameter description format validation | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAPARAM-004 | Invalid Parameter Reference | `$ref` | Parameter $ref must point to valid parameter in components | All | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAPARAM-005 | Parameter Default Not in Enum | `default` | If enum is defined, default value must be one of the enum values | 3.0 | Yes | ○ |
| AAPARAM-006 | Invalid Parameter Location Value | `location` | Parameter location must be a valid runtime expression | 3.0 | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAPARAM-007 | Parameter Structure Changed in 3.0 | Multiple | In 3.0, parameters have enum/default/examples instead of schema | 3.0 | Yes | ○ |

---

## 13. MESSAGE ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-001 | Missing Message Payload or Headers | `payload` or `headers` | Message should have either payload or headers defined | All | No | ○ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-002 | Invalid Message Description | `description` | Message description format validation | All | Yes | ○ |
| AAM-003 | Invalid Message Content Type | `contentType` | Message content type must be valid MIME type | All | Yes | ○ |
| AAM-004 | Invalid Message Schema Format | `schemaFormat` | Schema format must be valid (e.g., "application/vnd.aai.asyncapi;version=2.0.0") | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-005 | Invalid Message Reference | `$ref` | Message $ref must point to valid message in components | All | Yes | ○ |
| AAM-006 | Invalid Message Trait Reference | `traits` | Message trait references must point to valid message traits | All | Yes | ○ |
| AAM-007 | Invalid Correlation ID Reference | `correlationId` | Correlation ID reference must point to valid correlation ID | All | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-008 | Message Name Uniqueness | `name` | Message names should be unique within a channel/operation context | All | No | ○ |

### Mutual Exclusivity Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-009 | Message OneOf Mutually Exclusive | `oneOf` | When oneOf is present, message should not have payload/headers/etc. | 2.0-2.6 | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAM-010 | Message ID Added in 2.4 | `messageId` | Message gained messageId property in 2.4 | 2.4-2.6 | Yes | ○ |
| AAM-011 | Message Examples Type Changed in 2.2 | `examples` | Examples changed from `{any}` to MessageExample type | 2.2-2.6, 3.0 | Yes | ○ |
| AAM-012 | Message Structure in 3.0 | `payload`, `headers` | In 3.0, payload/headers can be MultiFormatSchema or Schema | 3.0 | Yes | ○ |
| AAM-013 | Message No MessageId in 3.0 | `messageId` | In 3.0, message no longer has messageId property | 3.0 | Yes | ✓ |
| AAM-014 | Message MessageId Only in 2.4-2.6 | `messageId` | Message messageId property only allowed in 2.4-2.6, not in 2.0-2.3 | 2.0-2.3 | Yes | ✓ |

---

## 14. MESSAGE TRAIT ENTITY

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAMTRT-001 | Invalid Message Trait Reference | `$ref` | Message trait $ref must point to valid trait in components | All | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAMTRT-002 | Message Trait MessageId in 2.4-2.6 | `messageId` | Message trait messageId only allowed in 2.4-2.6, not in 2.0-2.3 | 2.0-2.3 | Yes | ✓ |
| AAMTRT-003 | Message Trait Examples Type Changed | `examples` | Examples changed from `{any}` to MessageExample type | 2.2-2.6, 3.0 | Yes | ○ |
| AAMTRT-004 | Message Trait No MessageId in 3.0 | `messageId` | In 3.0, message trait no longer has messageId property | 3.0 | Yes | ✓ |

---

## 15. MESSAGE EXAMPLE ENTITY

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAMEX-001 | Message Example Only in 2.2+ | Entity | MessageExample entity only exists starting in 2.2 | 2.2-2.6, 3.0 | Yes | ○ |
| AAMEX-002 | Message Example Payload Type in 3.0 | `payload` | In 3.0, payload is `{any}` instead of `any` | 3.0 | Yes | ○ |

---

## 16. TAG ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| TAG-001 | Missing Tag Name | `name` | Tag must have a name | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| TAG-002 | Invalid Tag Description | `description` | Tag description format validation | All | Yes | ✓ |

### Uniqueness Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| TAG-003 | Duplicate Tag Definition | `name` | Tag names must be unique | All | Yes | ✓ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AATAG-001 | Invalid Tag Reference | `$ref` | Tag $ref must point to valid tag in components | 3.0 | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AATAG-002 | Tag Referenceable in 3.0 | `$ref` | Tag gained Referenceable trait in 3.0 | 3.0 | Yes | ○ |

---

## 17. EXTERNAL DOCUMENTATION ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| ED-001 | Missing External Documentation URL | `url` | External documentation must have a URL | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| ED-002 | Invalid External Documentation Description | `description` | External docs description format validation | All | Yes | ✓ |
| AAED-001 | Invalid External Documentation URL | `url` | External documentation URL must be valid URL format | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAED-002 | Invalid External Documentation Reference | `$ref` | External docs $ref must point to valid external docs in components | 3.0 | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAED-003 | External Docs Referenceable in 3.0 | `$ref` | External documentation gained Referenceable trait in 3.0 | 3.0 | Yes | ○ |

---

## 18. COMPONENTS ENTITY

### Invalid Name Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| COMP-001 | Invalid Component Key Name Pattern | Component keys | Component keys must match regex pattern `^[a-zA-Z0-9\.\-_]+$` | All | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| COMP-002 | Servers and Channels in Components (2.3+) | `servers`, `channels` | Components gained servers and channels in 2.3 | 2.3-2.6, 3.0 | Yes | ○ |
| COMP-003 | Server Variables in Components (2.4+) | `serverVariables` | Components gained serverVariables in 2.4 | 2.4-2.6, 3.0 | Yes | ○ |
| COMP-004 | Operations in Components (3.0) | `operations` | Components gained operations in 3.0 | 3.0 | Yes | ○ |
| COMP-005 | Replies and Reply Addresses in Components (3.0) | `replies`, `replyAddresses` | Components gained replies and replyAddresses in 3.0 | 3.0 | Yes | ○ |
| COMP-006 | External Docs and Tags in Components (3.0) | `externalDocs`, `tags` | Components gained externalDocs and tags in 3.0 | 3.0 | Yes | ○ |

---

## 19. SCHEMA ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SCH-005 | Missing Schema Array Information | `items` | Schema of type array must have items defined | All | Yes | ✓ |

### Invalid Type Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SCH-003 | Invalid Schema Type Value | `type` | Schema type must be valid JSON Schema type | All | Yes | ✓ |
| SCH-004 | Invalid Schema Array Items | `items` | Items property only valid for array type | All | Yes | ✓ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AASCH-001 | Invalid Schema Reference | `$ref` | Schema $ref must point to valid schema in components | All | Yes | ○ |

---

## 20. MULTI-FORMAT SCHEMA ENTITY (3.0 Only)

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAMFS-001 | Missing Schema Format | `schemaFormat` | Multi-format schema must have schemaFormat | 3.0 | Yes | ○ |
| AAMFS-002 | Missing Schema Definition | `schema` | Multi-format schema must have schema defined | 3.0 | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAMFS-003 | Invalid Multi-Format Schema Reference | `$ref` | Multi-format schema $ref must point to valid schema | 3.0 | Yes | ○ |

---

## 21. SECURITY SCHEME ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SS-001 | Missing Security Scheme Type | `type` | Security scheme must have a type | All | Yes | ✓ |
| SS-002 | Missing API-Key Scheme Parameter Name | `name` | API key security scheme must have parameter name | All | Yes | ✓ |
| SS-003 | Missing API-Key Scheme Parameter Location | `in` | API key security scheme must have location | All | Yes | ✓ |
| SS-019 | Missing HTTP Security Scheme Style | `scheme` | HTTP security scheme must have scheme (basic, bearer, etc.) | All | Yes | ✓ |
| SS-020 | Missing OAuth Security Scheme Flows | `flows` | OAuth security scheme must have flows defined | All | Yes | ✓ |
| SS-021 | Missing OID Connect Security Scheme Connect URL | `openIdConnectUrl` | OpenID Connect security scheme must have connect URL | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SS-014 | Invalid OpenID Connect URL | `openIdConnectUrl` | OpenID Connect URL format validation | All | Yes | ✓ |
| SS-015 | Invalid Security Scheme Description | `description` | Security scheme description format validation | All | Yes | ✓ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SS-008 | Unknown Security Scheme Type | `type` | Security scheme type must be valid (userPassword, apiKey, X509, symmetricEncryption, asymmetricEncryption, httpApiKey, http, oauth2, openIdConnect, plain, scramSha256, scramSha512, gssapi) | All | Yes | ✓ |
| SS-009 | Unknown API Key Location | `in` | API key location must be valid (user, password, query, header, cookie) | All | Yes | ✓ |
| SS-016 | Invalid HTTP Security Scheme Type | `scheme` | HTTP scheme must be valid | All | Yes | ✓ |
| SS-017 | Unexpected Usage of 'bearerFormat' | `bearerFormat` | Bearer format only allowed for HTTP bearer scheme | All | Yes | ✓ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AASS-001 | Invalid Security Scheme Reference | `$ref` | Security scheme $ref must point to valid security scheme in components | All | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AASS-002 | Security Scheme Scopes in 3.0 | `scopes` | Security scheme gained scopes property in 3.0 | 3.0 | Yes | ✓ |

---

## 22. OAUTH FLOWS ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| FLOW-006 | Missing OAuth Flow Scopes | `scopes` (2.x), `availableScopes` (3.0) | OAuth flow must have scopes defined | All | Yes | ✓ |
| FLOW-001 | Missing OAuth Flow Authorization URL | `authorizationUrl` | OAuth flow must have authorization URL for applicable flow types | All | Yes | ✓ |
| FLOW-002 | Missing OAuth Flow Token URL | `tokenUrl` | OAuth flow must have token URL for applicable flow types | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| FLOW-003 | Invalid OAuth Authorization URL | `authorizationUrl` | OAuth authorization URL format validation | All | Yes | ✓ |
| FLOW-004 | Invalid OAuth Token URL | `tokenUrl` | OAuth token URL format validation | All | Yes | ✓ |
| FLOW-005 | Invalid OAuth Refresh URL | `refreshUrl` | OAuth refresh URL format validation | All | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAFLOW-001 | OAuth Flow Scopes vs Available Scopes | `scopes` vs `availableScopes` | OAuth flow changed from 'scopes' to 'availableScopes' in 3.0 | All | Yes | ✓ |

---

## 23. SECURITY REQUIREMENT ENTITY

### Uniqueness Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SREQ-005 | Duplicate Security Requirements | Requirement name | Security requirements must be unique | 2.0-2.6 | Yes | ✓ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SREQ-001 | Invalid Security Requirement Name | Requirement name | Security requirement must reference valid security scheme | 2.0-2.6 | Yes | ✓ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| SREQ-002 | Security Requirement Scopes Must Be Empty | Scopes array | For non-OAuth schemes, scopes must be empty array | 2.0-2.6 | Yes | ✓ |
| SREQ-004 | Invalid Security Requirement Scopes | Scopes | Scopes value must be an array | 2.0-2.6 | Yes | ✓ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AASREQ-001 | Security in 3.0 Uses SecurityScheme | `security` | In 3.0, security arrays contain SecurityScheme objects, not SecurityRequirement | 3.0 | Yes | ○ |

---

## 24. CORRELATION ID ENTITY

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| CID-001 | Missing Correlation ID Location | `location` | Correlation ID must have a location | All | Yes | ✓ |

### Invalid Format Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AACID-001 | Invalid Correlation ID Description | `description` | Correlation ID description format validation | All | Yes | ○ |

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AACID-002 | Invalid Correlation ID Location Format | `location` | Correlation ID location must be valid runtime expression | All | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AACID-003 | Invalid Correlation ID Reference | `$ref` | Correlation ID $ref must point to valid correlation ID in components | All | Yes | ○ |

---

## 25. BINDINGS ENTITIES

Binding entities include: ServerBindings, ChannelBindings, OperationBindings, MessageBindings

### Invalid Value Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| BIND-001 | Unknown Binding Protocol | Protocol key | Binding protocol must be recognized (http, ws, kafka, anypointmq, amqp, amqp1, mqtt, mqtt5, nats, jms, sns, solace, sqs, stomp, redis, mercure, ibmmq, googlepubsub, pulsar) | Varies | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| BIND-002 | Invalid Server Binding Reference | `$ref` | Server binding $ref must point to valid server binding in components | 2.1-2.6, 3.0 | Yes | ○ |
| BIND-003 | Invalid Channel Binding Reference | `$ref` | Channel binding $ref must point to valid channel binding in components | 2.1-2.6, 3.0 | Yes | ○ |
| BIND-004 | Invalid Operation Binding Reference | `$ref` | Operation binding $ref must point to valid operation binding in components | 2.1-2.6, 3.0 | Yes | ○ |
| BIND-005 | Invalid Message Binding Reference | `$ref` | Message binding $ref must point to valid message binding in components | 2.1-2.6, 3.0 | Yes | ○ |

### Version-Specific Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| BIND-006 | Bindings Gained Referenceable in 2.1 | `$ref` | All binding types gained Referenceable trait in 2.1 | 2.1-2.6, 3.0 | Yes | ○ |
| BIND-007 | Mercure and IBMMQ Bindings Added in 2.1 | `mercure`, `ibmmq` | Mercure and IBMMQ binding support added in 2.1 | 2.1-2.6, 3.0 | Yes | ○ |
| BIND-008 | Anypointmq Binding Added in 2.2 | `anypointmq` | Anypointmq binding support added in 2.2 | 2.2-2.6, 3.0 | Yes | ○ |
| BIND-009 | Solace Binding Added in 2.3 | `solace` | Solace binding support added in 2.3 | 2.3-2.6, 3.0 | Yes | ○ |
| BIND-010 | Googlepubsub Binding Added in 2.5 | `googlepubsub` | Google Pub/Sub binding support added in 2.5 | 2.5, 2.6, 3.0 | Yes | ○ |
| BIND-011 | Pulsar Binding Added in 2.6 | `pulsar` | Pulsar binding support added in 2.6 | 2.6, 3.0 | Yes | ○ |
| BIND-012 | Mercure Binding Removed from Operation Bindings | `mercure` | Mercure binding removed from OperationBindings starting in 2.2 | 2.2-2.6, 3.0 | Yes | ○ |

---

## 26. REFERENCE ENTITY (3.0 Only)

### Required Property Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAREF-001 | Missing $ref Property | `$ref` | Reference entity must have $ref property | 3.0 | Yes | ○ |

### Invalid Reference Rules

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| AAREF-002 | Invalid Reference Format | `$ref` | $ref must be valid reference format (JSON reference) | 3.0 | Yes | ○ |

---

## 27. CROSS-CUTTING VALIDATION RULES

### Extension Properties

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| EXT-001 | Invalid Extension Property Name | Extension properties | Extension properties must start with 'x-' | All | Yes | ○ |

### Trait Application

| Rule Code | Name | Property | Description | Versions | Spec Mandated | Status |
|-----------|------|----------|-------------|----------|---------------|--------|
| TRAIT-001 | Extensible Trait Applied Consistently | Extensions | All entities with Extensible trait can have x-* properties | All | Yes | ○ |
| TRAIT-002 | Referenceable Trait Applied Consistently | `$ref` | All entities with Referenceable trait can use $ref | All | Yes | ○ |

---

## Summary of Major Version Changes

### AsyncAPI 2.x to 3.0 Changes

1. **Document Structure:**
    - Added `operations` at document level
    - Channels no longer contain operations directly

2. **Server:**
    - Changed from `url` to `host`/`pathname`/`protocol`
    - Security changed from `[SecurityRequirement]` to `[SecurityScheme]`
    - Gained Referenceable trait

3. **Channel:**
    - Added `address` property (required)
    - Added `messages` map
    - Removed `subscribe`/`publish` operations
    - Changed `servers` to array of Reference objects

4. **Operation:**
    - Moved to document-level `operations` section
    - Added `action` property (required, values: "send", "receive")
    - Added `channel` reference (required)
    - Added `reply` property
    - Changed `message` to `messages` (array of references)
    - Gained Referenceable trait

5. **Message:**
    - Removed `messageId` property
    - Changed payload/headers to support MultiFormatSchema union type
    - Changed `examples` structure

6. **Parameter:**
    - Changed structure: enum/default/examples instead of schema

7. **Security:**
    - SecurityScheme gained `scopes` property
    - Security references changed from SecurityRequirement to SecurityScheme

8. **New Entities in 3.0:**
    - Reference
    - OperationReply
    - OperationReplyAddress
    - MultiFormatSchema

9. **Gained Referenceable Trait in 3.0:**
    - Tag
    - ExternalDocumentation
    - Operation

10. **Components Additions in 3.0:**
    - operations, replies, replyAddresses, externalDocs, tags

### Progressive Changes Across 2.x Versions

- **2.1:** Added Referenceable to bindings, added mercure and ibmmq bindings
- **2.2:** Added `servers` to ChannelItem, changed message examples type, added anypointmq binding
- **2.3:** Added Referenceable to Server, added servers/channels to Components, added solace binding
- **2.4:** Added Referenceable to ServerVariable, added security to Operation/OperationTrait, added messageId
  to Message/MessageTrait, added serverVariables to Components
- **2.5:** Added tags to Server, added googlepubsub binding
- **2.6:** Added pulsar binding

---

## Implementation Notes

1. **Shared Rules with OpenAPI:** Many rules (Info, Contact, License, Tag, ExternalDocumentation, Server,
   ServerVariable, SecurityScheme, OAuthFlows, SecurityRequirement, Schema) are shared between OpenAPI and
   AsyncAPI. The existing implementations may already apply to AsyncAPI documents.

2. **AsyncAPI-Specific Rules:** Focus on implementing rules for AsyncAPI-specific entities:
    - Channel (all versions)
    - Operation (AsyncAPI-specific structure)
    - Message and MessageTrait
    - Parameter (different structure than OpenAPI)
    - CorrelationID
    - Bindings (ServerBindings, ChannelBindings, OperationBindings, MessageBindings)
    - OperationReply and OperationReplyAddress (3.0 only)
    - MultiFormatSchema (3.0 only)
    - Reference (3.0 only)

3. **Version Detection:** Rules must check the AsyncAPI version and apply appropriate validation. The
   `ModelType` enum should include all AsyncAPI versions (ASYNCAPI20, ASYNCAPI21, ASYNCAPI22, ASYNCAPI23,
   ASYNCAPI24, ASYNCAPI25, ASYNCAPI26, ASYNCAPI30).

4. **Rule Priority:** Start with:
    - Required property rules (highest impact)
    - Invalid reference rules (critical for document integrity)
    - Invalid value/format rules
    - Version-specific rules
    - Best practice rules

5. **Testing:** Each rule should have comprehensive tests covering:
    - Valid cases (no violation)
    - Invalid cases (violation detected)
    - Version-specific behavior
    - Edge cases

---

## Next Steps

To complete the AsyncAPI validation implementation:

1. **Review Existing Shared Rules:** Verify which rules already exist and apply to AsyncAPI
2. **Implement AsyncAPI-Specific Rules:** Create new rule classes for AsyncAPI-only entities
3. **Update ValidationRuleSet.java:** Register all new rules with appropriate metadata
4. **Create Test Cases:** Write comprehensive tests for each rule
5. **Document Rule Behavior:** Update documentation with AsyncAPI-specific validation behavior
6. **Version Matrix Testing:** Test rules across all AsyncAPI versions to ensure correct application
