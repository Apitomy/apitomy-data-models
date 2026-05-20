import {ICommand} from '../cmd/ICommand';
import {NodePath} from '../paths/NodePath';
import {NodePathUtil} from "../paths/NodePathUtil";

import {AddCallbackCommand} from "../cmd/commands/AddCallbackCommand";
import {AddChannelItemCommand} from "../cmd/commands/AddChannelItemCommand";
import {AddExampleCommand} from "../cmd/commands/AddExampleCommand";
import {AddExampleDefinitionCommand} from "../cmd/commands/AddExampleDefinitionCommand";
import {AddExtensionCommand} from "../cmd/commands/AddExtensionCommand";
import {AddHeaderDefinitionCommand} from "../cmd/commands/AddHeaderDefinitionCommand";
import {AddLinkCommand} from "../cmd/commands/AddLinkCommand";
import {AddMediaTypeCommand} from "../cmd/commands/AddMediaTypeCommand";
import {AddOperationSecurityRequirementCommand} from "../cmd/commands/AddOperationSecurityRequirementCommand";
import {AddParameterCommand} from "../cmd/commands/AddParameterCommand";
import {AddParameterDefinitionCommand} from "../cmd/commands/AddParameterDefinitionCommand";
import {AddPathItemCommand} from "../cmd/commands/AddPathItemCommand";
import {AddRequestBodyCommand} from "../cmd/commands/AddRequestBodyCommand";
import {AddRequestBodyDefinitionCommand} from "../cmd/commands/AddRequestBodyDefinitionCommand";
import {AddResponseCommand} from "../cmd/commands/AddResponseCommand";
import {AddResponseDefinitionCommand} from "../cmd/commands/AddResponseDefinitionCommand";
import {AddResponseHeaderCommand} from "../cmd/commands/AddResponseHeaderCommand";
import {AddSchemaDefinitionCommand} from "../cmd/commands/AddSchemaDefinitionCommand";
import {AddSchemaPropertyCommand} from "../cmd/commands/AddSchemaPropertyCommand";
import {AddSecurityRequirementCommand} from "../cmd/commands/AddSecurityRequirementCommand";
import {AddSecuritySchemeCommand} from "../cmd/commands/AddSecuritySchemeCommand";
import {AddServerCommand} from "../cmd/commands/AddServerCommand";
import {AddServerVariableCommand} from "../cmd/commands/AddServerVariableCommand";
import {AddTagCommand} from "../cmd/commands/AddTagCommand";

import {ChangeContactCommand} from "../cmd/commands/ChangeContactCommand";
import {ChangeDescriptionCommand} from "../cmd/commands/ChangeDescriptionCommand";
import {ChangeExtensionCommand} from "../cmd/commands/ChangeExtensionCommand";
import {ChangeLicenseCommand} from "../cmd/commands/ChangeLicenseCommand";
import {ChangeMediaTypeSchemaCommand} from "../cmd/commands/ChangeMediaTypeSchemaCommand";
import {ChangePropertyCommand} from "../cmd/commands/ChangePropertyCommand";
import {ChangeTitleCommand} from "../cmd/commands/ChangeTitleCommand";
import {ChangeVersionCommand} from "../cmd/commands/ChangeVersionCommand";

import {CreateOperationCommand} from "../cmd/commands/CreateOperationCommand";
import {CreatePathCommand} from "../cmd/commands/CreatePathCommand";
import {CreateSchemaCommand} from "../cmd/commands/CreateSchemaCommand";

import {DeleteCallbackCommand} from "../cmd/commands/DeleteCallbackCommand";
import {DeleteAllChildSchemasCommand} from "../cmd/commands/DeleteAllChildSchemasCommand";
import {DeleteAllExamplesCommand} from "../cmd/commands/DeleteAllExamplesCommand";
import {DeleteAllExtensionsCommand} from "../cmd/commands/DeleteAllExtensionsCommand";
import {DeleteAllHeadersCommand} from "../cmd/commands/DeleteAllHeadersCommand";
import {DeleteAllOperationsCommand} from "../cmd/commands/DeleteAllOperationsCommand";
import {DeleteAllParametersCommand} from "../cmd/commands/DeleteAllParametersCommand";
import {DeleteAllPropertiesCommand} from "../cmd/commands/DeleteAllPropertiesCommand";
import {DeleteAllResponsesCommand} from "../cmd/commands/DeleteAllResponsesCommand";
import {DeleteAllSecurityRequirementsCommand} from "../cmd/commands/DeleteAllSecurityRequirementsCommand";
import {DeleteAllSecuritySchemesCommand} from "../cmd/commands/DeleteAllSecuritySchemesCommand";
import {DeleteAllServersCommand} from "../cmd/commands/DeleteAllServersCommand";
import {DeleteAllTagsCommand} from "../cmd/commands/DeleteAllTagsCommand";
import {DeleteContactCommand} from "../cmd/commands/DeleteContactCommand";
import {DeleteExtensionCommand} from "../cmd/commands/DeleteExtensionCommand";
import {DeleteLicenseCommand} from "../cmd/commands/DeleteLicenseCommand";
import {DeleteLinkCommand} from "../cmd/commands/DeleteLinkCommand";
import {DeleteMediaTypeCommand} from "../cmd/commands/DeleteMediaTypeCommand";
import {DeleteOperationCommand} from "../cmd/commands/DeleteOperationCommand";
import {DeleteOperationSecurityRequirementCommand} from "../cmd/commands/DeleteOperationSecurityRequirementCommand";
import {DeleteParameterCommand} from "../cmd/commands/DeleteParameterCommand";
import {DeleteExampleDefinitionCommand} from "../cmd/commands/DeleteExampleDefinitionCommand";
import {DeleteHeaderDefinitionCommand} from "../cmd/commands/DeleteHeaderDefinitionCommand";
import {DeleteParameterDefinitionCommand} from "../cmd/commands/DeleteParameterDefinitionCommand";
import {DeleteRequestBodyDefinitionCommand} from "../cmd/commands/DeleteRequestBodyDefinitionCommand";
import {DeletePathCommand} from "../cmd/commands/DeletePathCommand";
import {DeleteRequestBodyCommand} from "../cmd/commands/DeleteRequestBodyCommand";
import {DeleteResponseCommand} from "../cmd/commands/DeleteResponseCommand";
import {DeleteResponseHeaderCommand} from "../cmd/commands/DeleteResponseHeaderCommand";
import {DeleteSchemaCommand} from "../cmd/commands/DeleteSchemaCommand";
import {DeleteSchemaPropertyCommand} from "../cmd/commands/DeleteSchemaPropertyCommand";
import {DeleteSecurityRequirementCommand} from "../cmd/commands/DeleteSecurityRequirementCommand";
import {DeleteSecuritySchemeCommand} from "../cmd/commands/DeleteSecuritySchemeCommand";
import {DeleteServerCommand} from "../cmd/commands/DeleteServerCommand";
import {DeleteServerVariableCommand} from "../cmd/commands/DeleteServerVariableCommand";
import {DeleteTagCommand} from "../cmd/commands/DeleteTagCommand";

import {EnsureChildNodeCommand} from "../cmd/commands/EnsureChildNodeCommand";

import {RefactorCallbackDefinitionCommand} from "../cmd/commands/RefactorCallbackDefinitionCommand";
import {RefactorExampleDefinitionCommand} from "../cmd/commands/RefactorExampleDefinitionCommand";
import {RefactorHeaderDefinitionCommand} from "../cmd/commands/RefactorHeaderDefinitionCommand";
import {RefactorLinkDefinitionCommand} from "../cmd/commands/RefactorLinkDefinitionCommand";
import {RefactorParameterDefinitionCommand} from "../cmd/commands/RefactorParameterDefinitionCommand";
import {RefactorRequestBodyDefinitionCommand} from "../cmd/commands/RefactorRequestBodyDefinitionCommand";
import {RefactorResponseDefinitionCommand} from "../cmd/commands/RefactorResponseDefinitionCommand";
import {RefactorSchemaDefinitionCommand} from "../cmd/commands/RefactorSchemaDefinitionCommand";
import {RenameTagCommand} from "../cmd/commands/RenameTagCommand";

import {ReplaceOperationCommand} from "../cmd/commands/ReplaceOperationCommand";
import {ReplacePathItemCommand} from "../cmd/commands/ReplacePathItemCommand";
import {ReplaceResponseDefinitionCommand} from "../cmd/commands/ReplaceResponseDefinitionCommand";
import {ReplaceSchemaDefinitionCommand} from "../cmd/commands/ReplaceSchemaDefinitionCommand";
import {ReplaceSecurityRequirementCommand} from "../cmd/commands/ReplaceSecurityRequirementCommand";

import {SetExternalDocsCommand} from "../cmd/commands/SetExternalDocsCommand";
import {SetPropertyCommand} from "../cmd/commands/SetPropertyCommand";

import {UpdateNodeCommand} from "../cmd/commands/UpdateNodeCommand";
import {UpdateSecuritySchemeCommand} from "../cmd/commands/UpdateSecuritySchemeCommand";


const pathKeys: string[] = [
    "_mediaTypePath", "_responsePath", "_responsesPath", "_parentPath", "_schemaPath", "_parameterPath", "_operationPath",
    "_propPath", "_propertyPath", "_paramPath", "_nodePath", "_headerPath", "_messagePath", "_pathItemPath", "_serverPath"
];
const pathListKeys: string[] = [ "_references" ];
const cmdListKeys: string[] = [ "_commands" ];

type Supplier = () => ICommand;

const commandSuppliers: { [key: string]: Supplier } = {
    "AddCallbackCommand": () => { return new AddCallbackCommand(); },
    "AddChannelItemCommand": () => { return new AddChannelItemCommand(); },
    "AddExampleCommand": () => { return new AddExampleCommand(); },
    "AddExampleDefinitionCommand": () => { return new AddExampleDefinitionCommand(); },
    "AddExtensionCommand": () => { return new AddExtensionCommand(); },
    "AddHeaderDefinitionCommand": () => { return new AddHeaderDefinitionCommand(); },
    "AddLinkCommand": () => { return new AddLinkCommand(); },
    "AddMediaTypeCommand": () => { return new AddMediaTypeCommand(); },
    "AddOperationSecurityRequirementCommand": () => { return new AddOperationSecurityRequirementCommand(); },
    "AddParameterCommand": () => { return new AddParameterCommand(); },
    "AddParameterDefinitionCommand": () => { return new AddParameterDefinitionCommand(); },
    "AddPathItemCommand": () => { return new AddPathItemCommand(); },
    "AddRequestBodyCommand": () => { return new AddRequestBodyCommand(); },
    "AddRequestBodyDefinitionCommand": () => { return new AddRequestBodyDefinitionCommand(); },
    "AddResponseCommand": () => { return new AddResponseCommand(); },
    "AddResponseDefinitionCommand": () => { return new AddResponseDefinitionCommand(); },
    "AddResponseHeaderCommand": () => { return new AddResponseHeaderCommand(); },
    "AddSchemaDefinitionCommand": () => { return new AddSchemaDefinitionCommand(); },
    "AddSchemaPropertyCommand": () => { return new AddSchemaPropertyCommand(); },
    "AddSecurityRequirementCommand": () => { return new AddSecurityRequirementCommand(); },
    "AddSecuritySchemeCommand": () => { return new AddSecuritySchemeCommand(); },
    "AddServerCommand": () => { return new AddServerCommand(); },
    "AddServerVariableCommand": () => { return new AddServerVariableCommand(); },
    "AddTagCommand": () => { return new AddTagCommand(); },

    "ChangeContactCommand": () => { return new ChangeContactCommand(); },
    "ChangeDescriptionCommand": () => { return new ChangeDescriptionCommand(); },
    "ChangeExtensionCommand": () => { return new ChangeExtensionCommand(); },
    "ChangeLicenseCommand": () => { return new ChangeLicenseCommand(); },
    "ChangeMediaTypeSchemaCommand": () => { return new ChangeMediaTypeSchemaCommand(); },
    "ChangePropertyCommand": () => { return new ChangePropertyCommand(); },
    "ChangeTitleCommand": () => { return new ChangeTitleCommand(); },
    "ChangeVersionCommand": () => { return new ChangeVersionCommand(); },

    "CreateOperationCommand": () => { return new CreateOperationCommand(); },
    "CreatePathCommand": () => { return new CreatePathCommand(); },
    "CreateSchemaCommand": () => { return new CreateSchemaCommand(); },

    "DeleteCallbackCommand": () => { return new DeleteCallbackCommand(); },
    "DeleteAllChildSchemasCommand": () => { return new DeleteAllChildSchemasCommand(); },
    "DeleteAllExamplesCommand": () => { return new DeleteAllExamplesCommand(); },
    "DeleteAllExtensionsCommand": () => { return new DeleteAllExtensionsCommand(); },
    "DeleteAllHeadersCommand": () => { return new DeleteAllHeadersCommand(); },
    "DeleteAllOperationsCommand": () => { return new DeleteAllOperationsCommand(); },
    "DeleteAllParametersCommand": () => { return new DeleteAllParametersCommand(); },
    "DeleteAllPropertiesCommand": () => { return new DeleteAllPropertiesCommand(); },
    "DeleteAllResponsesCommand": () => { return new DeleteAllResponsesCommand(); },
    "DeleteAllSecurityRequirementsCommand": () => { return new DeleteAllSecurityRequirementsCommand(); },
    "DeleteAllSecuritySchemesCommand": () => { return new DeleteAllSecuritySchemesCommand(); },
    "DeleteAllServersCommand": () => { return new DeleteAllServersCommand(); },
    "DeleteAllTagsCommand": () => { return new DeleteAllTagsCommand(); },
    "DeleteContactCommand": () => { return new DeleteContactCommand(); },
    "DeleteExtensionCommand": () => { return new DeleteExtensionCommand(); },
    "DeleteLicenseCommand": () => { return new DeleteLicenseCommand(); },
    "DeleteLinkCommand": () => { return new DeleteLinkCommand(); },
    "DeleteMediaTypeCommand": () => { return new DeleteMediaTypeCommand(); },
    "DeleteOperationCommand": () => { return new DeleteOperationCommand(); },
    "DeleteOperationSecurityRequirementCommand": () => { return new DeleteOperationSecurityRequirementCommand(); },
    "DeleteParameterCommand": () => { return new DeleteParameterCommand(); },
    "DeleteExampleDefinitionCommand": () => { return new DeleteExampleDefinitionCommand(); },
    "DeleteHeaderDefinitionCommand": () => { return new DeleteHeaderDefinitionCommand(); },
    "DeleteParameterDefinitionCommand": () => { return new DeleteParameterDefinitionCommand(); },
    "DeletePathCommand": () => { return new DeletePathCommand(); },
    "DeleteRequestBodyCommand": () => { return new DeleteRequestBodyCommand(); },
    "DeleteRequestBodyDefinitionCommand": () => { return new DeleteRequestBodyDefinitionCommand(); },
    "DeleteResponseCommand": () => { return new DeleteResponseCommand(); },
    "DeleteResponseHeaderCommand": () => { return new DeleteResponseHeaderCommand(); },
    "DeleteSchemaCommand": () => { return new DeleteSchemaCommand(); },
    "DeleteSchemaPropertyCommand": () => { return new DeleteSchemaPropertyCommand(); },
    "DeleteSecurityRequirementCommand": () => { return new DeleteSecurityRequirementCommand(); },
    "DeleteSecuritySchemeCommand": () => { return new DeleteSecuritySchemeCommand(); },
    "DeleteServerCommand": () => { return new DeleteServerCommand(); },
    "DeleteServerVariableCommand": () => { return new DeleteServerVariableCommand(); },
    "DeleteTagCommand": () => { return new DeleteTagCommand(); },

    "EnsureChildNodeCommand": () => { return new EnsureChildNodeCommand(); },

    "RefactorCallbackDefinitionCommand": () => { return new RefactorCallbackDefinitionCommand(); },
    "RefactorExampleDefinitionCommand": () => { return new RefactorExampleDefinitionCommand(); },
    "RefactorHeaderDefinitionCommand": () => { return new RefactorHeaderDefinitionCommand(); },
    "RefactorLinkDefinitionCommand": () => { return new RefactorLinkDefinitionCommand(); },
    "RefactorParameterDefinitionCommand": () => { return new RefactorParameterDefinitionCommand(); },
    "RefactorRequestBodyDefinitionCommand": () => { return new RefactorRequestBodyDefinitionCommand(); },
    "RefactorResponseDefinitionCommand": () => { return new RefactorResponseDefinitionCommand(); },
    "RefactorSchemaDefinitionCommand": () => { return new RefactorSchemaDefinitionCommand(); },
    "RenameTagCommand": () => { return new RenameTagCommand(); },

    "ReplaceOperationCommand": () => { return new ReplaceOperationCommand(); },
    "ReplacePathItemCommand": () => { return new ReplacePathItemCommand(); },
    "ReplaceResponseDefinitionCommand": () => { return new ReplaceResponseDefinitionCommand(); },
    "ReplaceSchemaDefinitionCommand": () => { return new ReplaceSchemaDefinitionCommand(); },
    "ReplaceSecurityRequirementCommand": () => { return new ReplaceSecurityRequirementCommand(); },

    "SetExternalDocsCommand": () => { return new SetExternalDocsCommand(); },
    "SetPropertyCommand": () => { return new SetPropertyCommand(); },

    "UpdateNodeCommand": () => { return new UpdateNodeCommand(); },
    "UpdateSecuritySchemeCommand": () => { return new UpdateSecuritySchemeCommand(); },
};

export class CommandUtil {

    public static create(cmdType: string): ICommand {
        const supplier: Supplier = commandSuppliers[cmdType];
        if (supplier === undefined) {
            throw new Error("Command cannot be created, missing supplier (add to CommandUtil): " + cmdType);
        }
        return supplier();
    }

    /**
     * Marshalls a command into a plain JSON object for storage or transmission.
     * The resulting object includes a `__type` field identifying the command class name,
     * followed by all fields. NodePath fields are serialized as strings, and nested
     * command lists are recursively marshalled.
     * @param command the command to serialize
     * @return the JSON representation of the command
     */
    public static marshall(command: ICommand): object {
        let result: any = {
            "__type": command.type()
        };
        Object.keys(command).filter(key => key !== "__type").forEach(key => {
            let value: any = command[key];
            if (value !== undefined) {
                if (pathKeys.indexOf(key) !== -1 && value) {
                    value = (value as NodePath).toString();
                } else if (pathListKeys.indexOf(key) !== -1 && value) {
                    value = (value as NodePath[]).map(np => np.toString());
                } else if (cmdListKeys.indexOf(key) !== -1 && value) {
                    value = (value as ICommand[]).map(cmd => CommandUtil.marshall(cmd));
                }
            }
            result[key] = value;
        });
        return result;
    }

    public static unmarshall(from: object): ICommand {
        let type: string = from["__type"];
        if (!type) {
            throw Error("Missing __type from source data.");
        }
        let command: ICommand = CommandUtil.create(type);
        Object.keys(from).filter(key => key != "__type").forEach(key => {
            let value: any = from[key];
            if (value) {
                if (pathKeys.indexOf(key) != -1) {
                    value = NodePathUtil.parseNodePath(value);
                } else if (pathListKeys.indexOf(key) != -1 && value) {
                    let newValue: NodePath[] = [];
                    (<string[]>value).forEach(np => {
                        newValue.push(NodePathUtil.parseNodePath(np));
                    });
                    value = newValue;
                    // } else if (typeKeys.indexOf(key) != -1 && value) {
                    //     value = MarshallCompat.unmarshallSimplifiedType(value);
                } else if (cmdListKeys.indexOf(key) != -1 && value) {
                    let newValue: ICommand[] = [];
                    (<any[]>value).forEach(mcmd => {
                        newValue.push(CommandUtil.unmarshall(mcmd));
                    });
                    value = newValue;
                }
            }
            command[key] = value;
        });
        return command;
    }
}
