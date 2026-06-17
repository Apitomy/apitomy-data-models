# Examples

This page contains end-to-end examples showing common use cases for the Apitomy Data Models
library. Each example is provided in both Java and TypeScript.

---

## Build an OpenAPI Document from Scratch

Create a complete OpenAPI 3.0 document programmatically, including info, paths, operations,
request bodies, responses, and schema definitions.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.ModelType;
    import io.apitomy.datamodels.models.openapi.v3x.v30.OpenApi30Document;

    // Create the document
    OpenApi30Document doc = (OpenApi30Document) Library.createDocument(ModelType.OPENAPI30);

    // Info
    doc.setInfo(doc.createInfo());
    doc.getInfo().setTitle("Pet Store API");
    doc.getInfo().setVersion("1.0.0");
    doc.getInfo().setDescription("A sample API for managing pets");

    // Contact
    doc.getInfo().setContact(doc.getInfo().createContact());
    doc.getInfo().getContact().setName("API Support");
    doc.getInfo().getContact().setEmail("support@example.com");

    // Server
    doc.addServer(doc.createServer());
    doc.getServers().get(0).setUrl("https://api.example.com/v1");

    // Paths
    doc.setPaths(doc.createPaths());

    // POST /pets
    doc.getPaths().addItem("/pets", doc.getPaths().createPathItem());
    var petsPath = doc.getPaths().getItem("/pets");
    petsPath.setPost(petsPath.createOperation());
    var createPet = petsPath.getPost();
    createPet.setOperationId("createPet");
    createPet.setSummary("Create a pet");

    // Request body
    createPet.setRequestBody(createPet.createRequestBody());
    createPet.getRequestBody().setRequired(true);
    createPet.getRequestBody().addMediaType(
        "application/json",
        createPet.getRequestBody().createMediaType()
    );

    // 201 response
    createPet.addResponse("201", createPet.createResponse());
    createPet.getResponse("201").setDescription("Pet created successfully");

    // Components with a Pet schema
    doc.setComponents(doc.createComponents());
    doc.getComponents().addSchema("Pet", doc.getComponents().createSchema());
    var petSchema = doc.getComponents().getSchemas().get("Pet");
    petSchema.setType("object");
    petSchema.addProperty("id", petSchema.createSchema());
    petSchema.getProperties().get("id").setType("integer");
    petSchema.addProperty("name", petSchema.createSchema());
    petSchema.getProperties().get("name").setType("string");

    // Tags
    doc.addTag(doc.createTag());
    doc.getTags().get(0).setName("pets");
    doc.getTags().get(0).setDescription("Pet operations");

    // Write to JSON
    String json = Library.writeDocumentToJSONString(doc);
    System.out.println(json);
    ```

=== "TypeScript"

    ```typescript
    import { Library, ModelType, OpenApi30Document } from '@apitomy/data-models';

    // Create the document
    const doc = Library.createDocument(ModelType.OPENAPI30) as OpenApi30Document;

    // Info
    doc.setInfo(doc.createInfo());
    doc.getInfo().setTitle('Pet Store API');
    doc.getInfo().setVersion('1.0.0');
    doc.getInfo().setDescription('A sample API for managing pets');

    // Contact
    doc.getInfo().setContact(doc.getInfo().createContact());
    doc.getInfo().getContact().setName('API Support');
    doc.getInfo().getContact().setEmail('support@example.com');

    // Server
    doc.addServer(doc.createServer());
    doc.getServers()[0].setUrl('https://api.example.com/v1');

    // Paths
    doc.setPaths(doc.createPaths());

    // POST /pets
    doc.getPaths().addItem('/pets', doc.getPaths().createPathItem());
    const petsPath = doc.getPaths().getItem('/pets');
    petsPath.setPost(petsPath.createOperation());
    const createPet = petsPath.getPost();
    createPet.setOperationId('createPet');
    createPet.setSummary('Create a pet');

    // Request body
    createPet.setRequestBody(createPet.createRequestBody());
    createPet.getRequestBody().setRequired(true);
    createPet.getRequestBody().addMediaType(
        'application/json',
        createPet.getRequestBody().createMediaType()
    );

    // 201 response
    createPet.addResponse('201', createPet.createResponse());
    createPet.getResponse('201').setDescription('Pet created successfully');

    // Components with a Pet schema
    doc.setComponents(doc.createComponents());
    doc.getComponents().addSchema('Pet', doc.getComponents().createSchema());
    const petSchema = doc.getComponents().getSchemas().get('Pet');
    petSchema.setType('object');
    petSchema.addProperty('id', petSchema.createSchema());
    petSchema.getProperties().get('id').setType('integer');
    petSchema.addProperty('name', petSchema.createSchema());
    petSchema.getProperties().get('name').setType('string');

    // Tags
    doc.addTag(doc.createTag());
    doc.getTags()[0].setName('pets');
    doc.getTags()[0].setDescription('Pet operations');

    // Write to JSON
    const json = JSON.stringify(Library.writeNode(doc), null, 2);
    console.log(json);
    ```

---

## List All API Endpoints

Read a document and extract every endpoint (HTTP method + path) along with its operation
details.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.TraverserDirection;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.models.openapi.OpenApiOperation;
    import io.apitomy.datamodels.models.openapi.OpenApiPathItem;
    import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
    import java.util.ArrayList;
    import java.util.List;

    Document doc = Library.readDocumentFromJSONString(json);

    class Endpoint {
        String method;
        String path;
        String operationId;
        String summary;

        Endpoint(String method, String path, String operationId, String summary) {
            this.method = method;
            this.path = path;
            this.operationId = operationId;
            this.summary = summary;
        }
    }

    List<Endpoint> endpoints = new ArrayList<>();

    Library.visitTree(doc, new CombinedVisitorAdapter() {
        @Override
        public void visitOperation(OpenApiOperation node) {
            // The parent is a PathItem — get the HTTP method from the parent property name
            String method = node.parentPropertyName().toUpperCase();
            // The grandparent property name is the path string
            String path = ((OpenApiPathItem) node.parent()).mapPropertyName();
            endpoints.add(new Endpoint(method, path, node.getOperationId(), node.getSummary()));
        }
    }, TraverserDirection.down);

    System.out.println("Found " + endpoints.size() + " endpoints:");
    for (Endpoint ep : endpoints) {
        System.out.printf("  %-6s %-30s %s%n", ep.method, ep.path,
            ep.operationId != null ? ep.operationId : "(no operationId)");
    }
    ```

=== "TypeScript"

    ```typescript
    import {
        Library, TraverserDirection, Document,
        CombinedVisitorAdapter, OpenApiOperation, OpenApiPathItem
    } from '@apitomy/data-models';

    const doc: Document = Library.readDocument(json);

    interface Endpoint {
        method: string;
        path: string;
        operationId: string | null;
        summary: string | null;
    }

    const endpoints: Endpoint[] = [];

    class EndpointCollector extends CombinedVisitorAdapter {
        visitOperation(node: OpenApiOperation): void {
            // The parent is a PathItem — get the HTTP method from the parent property name
            const method = node.parentPropertyName().toUpperCase();
            // The grandparent property name is the path string
            const path = (node.parent() as OpenApiPathItem).mapPropertyName();
            endpoints.push({
                method,
                path,
                operationId: node.getOperationId(),
                summary: node.getSummary(),
            });
        }
    }

    Library.visitTree(doc, new EndpointCollector(), TraverserDirection.down);

    console.log(`Found ${endpoints.length} endpoints:`);
    endpoints.forEach(ep => {
        const id = ep.operationId ?? '(no operationId)';
        console.log(`  ${ep.method.padEnd(6)} ${ep.path.padEnd(30)} ${id}`);
    });
    ```

---

## Validate and Report Problems

Read a document, validate it, and produce a formatted report of all issues found.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.validation.ValidationProblem;
    import io.apitomy.datamodels.validation.ValidationProblemSeverity;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    Document doc = Library.readDocumentFromJSONString(json);
    List<ValidationProblem> problems = Library.validate(doc, null);

    if (problems.isEmpty()) {
        System.out.println("Document is valid.");
    } else {
        // Group by severity
        Map<ValidationProblemSeverity, List<ValidationProblem>> bySeverity =
            problems.stream().collect(Collectors.groupingBy(p -> p.severity));

        // Print summary
        System.out.println("Validation Results:");
        System.out.println("  Errors:   " + bySeverity.getOrDefault(ValidationProblemSeverity.high, List.of()).size());
        System.out.println("  Warnings: " + bySeverity.getOrDefault(ValidationProblemSeverity.medium, List.of()).size());

        // Print details
        System.out.println("\nProblems:");
        for (ValidationProblem p : problems) {
            System.out.printf("  [%s] %s%n", p.severity, p.message);
            System.out.printf("    Path: %s%n", p.nodePath);
            System.out.printf("    Code: %s%n", p.errorCode);
        }
    }
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document, ValidationProblem } from '@apitomy/data-models';

    const doc: Document = Library.readDocument(json);
    const problems: ValidationProblem[] = Library.validate(doc, null);

    if (problems.length === 0) {
        console.log('Document is valid.');
    } else {
        // Group by severity
        const errors = problems.filter(p => p.severity.toString() === 'high');
        const warnings = problems.filter(p => p.severity.toString() === 'medium');

        // Print summary
        console.log('Validation Results:');
        console.log(`  Errors:   ${errors.length}`);
        console.log(`  Warnings: ${warnings.length}`);

        // Print details
        console.log('\nProblems:');
        problems.forEach(p => {
            console.log(`  [${p.severity}] ${p.message}`);
            console.log(`    Path: ${p.nodePath}`);
            console.log(`    Code: ${p.errorCode}`);
        });
    }
    ```

---

## Upgrade OpenAPI 2.0 to 3.0

Read a Swagger 2.0 document, transform it to OpenAPI 3.0, and write out the result.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.models.ModelType;

    // Read the Swagger 2.0 document
    String swaggerJson = """
        {
          "swagger": "2.0",
          "info": { "title": "Legacy API", "version": "1.0" },
          "host": "api.example.com",
          "basePath": "/v1",
          "schemes": ["https"],
          "consumes": ["application/json"],
          "produces": ["application/json"],
          "paths": {
            "/users": {
              "get": {
                "operationId": "listUsers",
                "summary": "List all users",
                "parameters": [
                  {
                    "name": "limit",
                    "in": "query",
                    "type": "integer",
                    "description": "Maximum number of users to return"
                  }
                ],
                "responses": {
                  "200": {
                    "description": "A list of users",
                    "schema": { "$ref": "#/definitions/UserList" }
                  }
                }
              }
            }
          },
          "definitions": {
            "User": {
              "type": "object",
              "properties": {
                "id": { "type": "integer" },
                "name": { "type": "string" }
              }
            },
            "UserList": {
              "type": "array",
              "items": { "$ref": "#/definitions/User" }
            }
          }
        }
        """;

    Document swagger = Library.readDocumentFromJSONString(swaggerJson);
    Document openApi30 = Library.transformDocument(swagger, ModelType.OPENAPI30);

    String result = Library.writeDocumentToJSONString(openApi30);
    System.out.println(result);
    // Output has "openapi": "3.0.3", "servers" array, "components/schemas", etc.
    ```

=== "TypeScript"

    ```typescript
    import { Library, Document, ModelType } from '@apitomy/data-models';

    // Read the Swagger 2.0 document
    const swaggerJson = {
        swagger: '2.0',
        info: { title: 'Legacy API', version: '1.0' },
        host: 'api.example.com',
        basePath: '/v1',
        schemes: ['https'],
        consumes: ['application/json'],
        produces: ['application/json'],
        paths: {
            '/users': {
                get: {
                    operationId: 'listUsers',
                    summary: 'List all users',
                    parameters: [
                        {
                            name: 'limit',
                            in: 'query',
                            type: 'integer',
                            description: 'Maximum number of users to return',
                        },
                    ],
                    responses: {
                        '200': {
                            description: 'A list of users',
                            schema: { $ref: '#/definitions/UserList' },
                        },
                    },
                },
            },
        },
        definitions: {
            User: {
                type: 'object',
                properties: {
                    id: { type: 'integer' },
                    name: { type: 'string' },
                },
            },
            UserList: {
                type: 'array',
                items: { $ref: '#/definitions/User' },
            },
        },
    };

    const swagger: Document = Library.readDocument(swaggerJson);
    const openApi30: Document = Library.transformDocument(swagger, ModelType.OPENAPI30);

    const result = JSON.stringify(Library.writeNode(openApi30), null, 2);
    console.log(result);
    // Output has "openapi": "3.0.3", "servers" array, "components/schemas", etc.
    ```

---

## Read and Validate an AsyncAPI Document

Read an AsyncAPI 2.6 document, inspect its channels and operations, and validate it.

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.TraverserDirection;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.models.asyncapi.v2x.AsyncApi2xDocument;
    import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelItem;
    import io.apitomy.datamodels.models.asyncapi.AsyncApiOperation;
    import io.apitomy.datamodels.models.visitors.CombinedVisitorAdapter;
    import io.apitomy.datamodels.validation.ValidationProblem;
    import java.util.List;

    String json = """
        {
          "asyncapi": "2.6.0",
          "info": {
            "title": "User Events",
            "version": "1.0.0"
          },
          "channels": {
            "user/signedup": {
              "subscribe": {
                "operationId": "onUserSignedUp",
                "message": {
                  "payload": {
                    "type": "object",
                    "properties": {
                      "userId": { "type": "string" },
                      "email": { "type": "string" }
                    }
                  }
                }
              }
            }
          }
        }
        """;

    Document doc = Library.readDocumentFromJSONString(json);
    AsyncApi2xDocument asyncDoc = (AsyncApi2xDocument) doc;
    System.out.println("Title: " + asyncDoc.getInfo().getTitle());

    // Walk the channels and operations
    Library.visitTree(doc, new CombinedVisitorAdapter() {
        @Override
        public void visitChannelItem(AsyncApiChannelItem node) {
            System.out.println("Channel: " + node.mapPropertyName());
        }

        @Override
        public void visitOperation(AsyncApiOperation node) {
            System.out.println("  Operation: " + node.getOperationId());
        }
    }, TraverserDirection.down);

    // Validate
    List<ValidationProblem> problems = Library.validate(doc, null);
    System.out.println("Validation problems: " + problems.size());
    ```

=== "TypeScript"

    ```typescript
    import {
        Library, TraverserDirection, Document,
        AsyncApi2xDocument, AsyncApiChannelItem, AsyncApiOperation,
        CombinedVisitorAdapter, ValidationProblem
    } from '@apitomy/data-models';

    const json = {
        asyncapi: '2.6.0',
        info: {
            title: 'User Events',
            version: '1.0.0',
        },
        channels: {
            'user/signedup': {
                subscribe: {
                    operationId: 'onUserSignedUp',
                    message: {
                        payload: {
                            type: 'object',
                            properties: {
                                userId: { type: 'string' },
                                email: { type: 'string' },
                            },
                        },
                    },
                },
            },
        },
    };

    const doc: Document = Library.readDocument(json);
    const asyncDoc = doc as AsyncApi2xDocument;
    console.log('Title:', asyncDoc.getInfo().getTitle());

    // Walk the channels and operations
    class ChannelPrinter extends CombinedVisitorAdapter {
        visitChannelItem(node: AsyncApiChannelItem): void {
            console.log('Channel:', node.mapPropertyName());
        }
        visitOperation(node: AsyncApiOperation): void {
            console.log('  Operation:', node.getOperationId());
        }
    }

    Library.visitTree(doc, new ChannelPrinter(), TraverserDirection.down);

    // Validate
    const problems: ValidationProblem[] = Library.validate(doc, null);
    console.log('Validation problems:', problems.length);
    ```

---

## Implement a Custom Reference Resolver

Resolve external `$ref` references by fetching content from a map (simulating a file
system or database).

=== "Java"

    ```java
    import io.apitomy.datamodels.Library;
    import io.apitomy.datamodels.models.Document;
    import io.apitomy.datamodels.models.Node;
    import io.apitomy.datamodels.refs.IReferenceResolver;
    import io.apitomy.datamodels.refs.ResolvedReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import java.util.Map;

    // Simulated external schemas
    Map<String, String> externalSchemas = Map.of(
        "https://schemas.example.com/address.json", """
            {
              "type": "object",
              "properties": {
                "street": { "type": "string" },
                "city": { "type": "string" },
                "zipCode": { "type": "string" }
              }
            }
            """,
        "https://schemas.example.com/phone.json", """
            {
              "type": "object",
              "properties": {
                "number": { "type": "string" },
                "type": { "type": "string", "enum": ["home", "work", "mobile"] }
              }
            }
            """
    );

    // Create a resolver that looks up schemas from the map
    IReferenceResolver resolver = new IReferenceResolver() {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public ResolvedReference resolveRef(String reference, Node from) {
            String schemaJson = externalSchemas.get(reference);
            if (schemaJson == null) {
                return null;
            }
            try {
                Object json = mapper.readTree(schemaJson);
                return ResolvedReference.fromJson(json);
            } catch (Exception e) {
                return null;
            }
        }
    };

    // Register and dereference
    Library.addReferenceResolver(resolver);
    Document doc = Library.readDocumentFromJSONString(apiJson);
    Document dereferenced = Library.dereferenceDocument(doc);
    Library.removeReferenceResolver(resolver);

    // The dereferenced document has external $refs replaced with actual content
    String result = Library.writeDocumentToJSONString(dereferenced);
    System.out.println(result);
    ```

=== "TypeScript"

    ```typescript
    import {
        Library, Document, Node,
        IReferenceResolver, ResolvedReference
    } from '@apitomy/data-models';

    // Simulated external schemas
    const externalSchemas: Record<string, any> = {
        'https://schemas.example.com/address.json': {
            type: 'object',
            properties: {
                street: { type: 'string' },
                city: { type: 'string' },
                zipCode: { type: 'string' },
            },
        },
        'https://schemas.example.com/phone.json': {
            type: 'object',
            properties: {
                number: { type: 'string' },
                type: { type: 'string', enum: ['home', 'work', 'mobile'] },
            },
        },
    };

    // Create a resolver that looks up schemas from the map
    class MapReferenceResolver implements IReferenceResolver {
        resolveRef(reference: string, from: Node): ResolvedReference | null {
            const schema = externalSchemas[reference];
            if (!schema) {
                return null;
            }
            return ResolvedReference.fromJson(schema);
        }
    }

    // Register and dereference
    const resolver = new MapReferenceResolver();
    Library.addReferenceResolver(resolver);
    const doc: Document = Library.readDocument(apiJson);
    const dereferenced: Document = Library.dereferenceDocument(doc);
    Library.removeReferenceResolver(resolver);

    // The dereferenced document has external $refs replaced with actual content
    const result = JSON.stringify(Library.writeNode(dereferenced), null, 2);
    console.log(result);
    ```
