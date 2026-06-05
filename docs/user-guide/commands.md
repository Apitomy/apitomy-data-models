# Commands

The library provides a command pattern for document mutations with built-in undo/redo support.
Every change to a document can be represented as an `ICommand` that knows how to execute
itself and reverse its effect.

## Overview

Each command implements the `ICommand` interface:

- `execute(document)` — applies the mutation to the document
- `undo(document)` — reverses the mutation
- `type()` — returns the command type name (e.g., `"ChangePropertyCommand"`)

---

## Creating and Executing Commands

### ChangePropertyCommand

The most common command modifies a simple property (string, boolean, number) on any node.

=== "Java"

    ```java
    import io.apitomy.datamodels.cmd.ICommand;
    import io.apitomy.datamodels.cmd.commands.ChangePropertyCommand;
    import io.apitomy.datamodels.models.Node;

    // Change the title of the info section
    Node infoNode = doc.getInfo();
    ICommand cmd = new ChangePropertyCommand<>(infoNode, "title", "Updated API Title");

    // Execute the command
    cmd.execute(doc);
    System.out.println(doc.getInfo().getTitle()); // "Updated API Title"

    // Undo the command
    cmd.undo(doc);
    System.out.println(doc.getInfo().getTitle()); // original title restored
    ```

=== "TypeScript"

    ```typescript
    import { ICommand, ChangePropertyCommand } from '@apitomy/data-models';

    // Change the title of the info section
    const infoNode = doc.getInfo();
    const cmd: ICommand = new ChangePropertyCommand(infoNode, 'title', 'Updated API Title');

    // Execute the command
    cmd.execute(doc);
    console.log(doc.getInfo().getTitle()); // "Updated API Title"

    // Undo the command
    cmd.undo(doc);
    console.log(doc.getInfo().getTitle()); // original title restored
    ```

---

## CommandFactory

`CommandFactory` provides convenience methods for creating, marshalling, and unmarshalling
commands.

### Creating Commands via Factory

=== "Java"

    ```java
    import io.apitomy.datamodels.cmd.CommandFactory;
    import com.fasterxml.jackson.databind.node.ObjectNode;

    // Create a command to add a schema definition
    ObjectNode schemaJson = mapper.createObjectNode();
    schemaJson.put("type", "object");
    ICommand cmd = CommandFactory.createAddSchemaDefinitionCommand("Pet", schemaJson);
    cmd.execute(doc);
    ```

=== "TypeScript"

    ```typescript
    import { CommandFactory } from '@apitomy/data-models';

    // Create a command to add a schema definition
    const schemaJson = { type: 'object' };
    const cmd = CommandFactory.createAddSchemaDefinitionCommand('Pet', schemaJson);
    cmd.execute(doc);
    ```

### Marshalling Commands to JSON

Commands can be serialized to JSON for storage, transmission, or replay. This is useful for
implementing collaborative editing or change logs.

=== "Java"

    ```java
    // Serialize a command to JSON
    ObjectNode commandJson = CommandFactory.marshall(cmd);

    // Deserialize a command from JSON
    ICommand restored = CommandFactory.unmarshall(commandJson);
    restored.execute(doc);
    ```

=== "TypeScript"

    ```typescript
    // Serialize a command to JSON
    const commandJson = CommandFactory.marshall(cmd);

    // Deserialize a command from JSON
    const restored: ICommand = CommandFactory.unmarshall(commandJson);
    restored.execute(doc);
    ```

### Command JSON Format

Marshalled commands use a `__type` field to identify the command type, and fields prefixed
with `_` for command properties:

```json
{
    "__type": "ChangePropertyCommand",
    "_nodePath": "/info",
    "_property": "title",
    "_newValue": "Updated API Title"
}
```

---

## AggregateCommand

Use `AggregateCommand` to batch multiple commands into a single undoable operation.
Undoing the aggregate undoes all of its child commands in reverse order.

=== "Java"

    ```java
    import io.apitomy.datamodels.cmd.commands.AggregateCommand;

    AggregateCommand batch = new AggregateCommand("update-metadata", doc.getInfo());

    batch.addCommand(new ChangePropertyCommand<>(doc.getInfo(), "title", "New Title"));
    batch.addCommand(new ChangePropertyCommand<>(doc.getInfo(), "version", "2.0.0"));
    batch.addCommand(new ChangePropertyCommand<>(doc.getInfo(), "description", "Updated desc"));

    // Execute all three changes
    batch.execute(doc);

    // Undo all three changes at once
    batch.undo(doc);
    ```

=== "TypeScript"

    ```typescript
    import { AggregateCommand, ChangePropertyCommand } from '@apitomy/data-models';

    const batch = new AggregateCommand('update-metadata', doc.getInfo());

    batch.addCommand(new ChangePropertyCommand(doc.getInfo(), 'title', 'New Title'));
    batch.addCommand(new ChangePropertyCommand(doc.getInfo(), 'version', '2.0.0'));
    batch.addCommand(new ChangePropertyCommand(doc.getInfo(), 'description', 'Updated desc'));

    // Execute all three changes
    batch.execute(doc);

    // Undo all three changes at once
    batch.undo(doc);
    ```

---

## Available Command Categories

The library provides commands for all common document mutations:

| Category | Commands | Description |
|----------|---------|-------------|
| **Add** | `AddPathItemCommand`, `AddOperationCommand`, `AddParameterCommand`, `AddResponseCommand`, `AddSchemaDefinitionCommand`, `AddServerCommand`, `AddSecuritySchemeCommand`, `AddTagCommand`, `AddMediaTypeCommand`, `AddCallbackCommand`, `AddExtensionCommand`, `AddExampleCommand` | Add new elements to the document |
| **Delete** | `DeletePathCommand`, `DeleteOperationCommand`, `DeleteParameterCommand`, `DeleteResponseCommand`, `DeleteSchemaCommand`, `DeleteServerCommand`, `DeleteSecuritySchemeCommand`, `DeleteTagCommand`, `DeleteMediaTypeCommand`, `DeleteAllPropertiesCommand`, `DeleteAllOperationsCommand` | Remove elements from the document |
| **Change** | `ChangePropertyCommand`, `ChangeDescriptionCommand`, `ChangeTitleCommand`, `ChangeVersionCommand`, `ChangeContactCommand`, `ChangeLicenseCommand`, `ChangeExtensionCommand`, `ChangeMediaTypeSchemaCommand` | Modify existing properties |
| **Replace** | `ReplacePathItemCommand`, `ReplaceOperationCommand`, `ReplaceSchemaDefinitionCommand`, `ReplaceResponseDefinitionCommand`, `ReplaceSecurityRequirementCommand` | Replace entire elements |
| **Refactor** | `RefactorSchemaDefinitionCommand`, `RefactorResponseDefinitionCommand`, `RefactorParameterDefinitionCommand`, `RenameTagCommand` | Restructure or rename elements |
| **Aggregate** | `AggregateCommand` | Batch multiple commands into one |
