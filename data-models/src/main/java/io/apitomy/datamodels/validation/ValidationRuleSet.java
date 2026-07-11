package io.apitomy.datamodels.validation;

import io.apitomy.datamodels.models.ModelType;
import io.apitomy.datamodels.util.ValidationUtil;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidAsyncApiVersionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidChannelDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidCorrelationIdDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidDefaultContentTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidDocumentIdFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidMessageContentTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidMessageDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidMessageSchemaFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidReplyAddressLocationRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidServerHostFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.AaInvalidServerPathnameFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidApiDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidContactEmailRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidContactUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidExternalDocsDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidLicenseUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidParameterDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidServerDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidServerUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidTagDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.InvalidTermsOfServiceUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidApiBasePathRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidApiHostRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidExampleDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidExternalDocsUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OrpcInvalidOpenRpcVersionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidHeaderDefaultValueRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidHeaderDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidLinkDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOAuth2MetadataUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOAuthAuthorizationUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOAuthDeviceAuthorizationUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOAuthRefreshUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOAuthTokenUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOpenIDConnectUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOperationConsumesRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOperationDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidOperationProducesRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidPathItemDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidRequestBodyDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidResponseDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidSchemaItemsDefaultValueRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidSecuritySchemeAuthUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidSecuritySchemeDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidSecuritySchemeTokenUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidServerVariableDescriptionRule;
import io.apitomy.datamodels.validation.rules.invalid.format.OasInvalidXmlNamespaceUrlRule;
import io.apitomy.datamodels.validation.rules.invalid.name.AaInvalidComponentKeyNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasDuplicatePathSegmentRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasEmptyPathSegmentRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasIdenticalPathTemplateRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidCallbackDefinitionNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidExampleDefinitionNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidHeaderDefinitionNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidHttpResponseCodeRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidLinkDefinitionNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidParameterDefNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidPathSegmentRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidRequestBodyDefinitionNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidResponseDefNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidSchemaDefNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidScopeNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasInvalidSecuritySchemeNameRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasUnmatchedEncodingPropertyRule;
import io.apitomy.datamodels.validation.rules.invalid.name.OasUnmatchedExampleTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidChannelReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidCorrelationIdReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidExternalDocsReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidMessageReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidMessageTraitReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidMultiFormatSchemaReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidOperationChannelReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidOperationTraitReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidReplyAddressReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidReplyChannelReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidReplyMessageReferencesRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidReplyReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidServerReferenceInChannelRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidServerReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidServerVariableReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.AaInvalidTagReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.InvalidParameterReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.InvalidSecuritySchemeReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidCallbackReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidExampleReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidHeaderReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidLinkOperationReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidLinkReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidPathItemReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidRequestBodyReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidResponseReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidSchemaReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OasInvalidSecurityRequirementNameRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidContentDescriptorReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidErrorReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidExamplePairingReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidExampleReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidLinkReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidMethodReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidSchemaReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.reference.OrpcInvalidTagReferenceRule;
import io.apitomy.datamodels.validation.rules.invalid.type.OasInvalidSchemaArrayItemsRule;
import io.apitomy.datamodels.validation.rules.invalid.type.OasInvalidSchemaTypeValueRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaChannelMessagesOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaChannelOperationsIn2xOnlyRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaInfoExternalDocsOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaInfoTagsOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaInvalidCorrelationIdLocationFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaInvalidOperationActionValueRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaInvalidParameterLocationValueRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaMessageMessageIdIn24To26Rule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaMessageNoMessageIdIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaMessageTraitMessageIdIn24To26Rule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaMessageTraitNoMessageIdIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOAuthFlowScopesVsAvailableScopesRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationMessageVsMessagesRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationReferenceableOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationReplyOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationSecurityPropertyOnlyIn24PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationTraitOperationIdIn2xOnlyRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationTraitSecurityIn24PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaOperationsOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaParameterDefaultNotInEnumRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaParameterStructureChangedIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaSecuritySchemeScopesOnlyIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServerReferenceableOnlyIn23PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServerStructureChangedIn3xRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServerTagsOnlyIn25PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServerVariableDefaultNotInEnumRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServerVariableReferenceableOnlyIn24PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.AaServersPropertyOnlyIn22PlusRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasAllowReservedNotAllowedForParamRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasAllowReservedNotAllowedRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasEncodingStyleNotAllowedRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasExplodeNotAllowedRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasFormDataParamNotAllowedRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidApiConsumesMTRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidApiProducesMTRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidApiSchemeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidEncodingForMPMTRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidHeaderStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidHttpSecuritySchemeTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidLinkOperationIdRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidOperationIdRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidOperationSchemeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidSecurityReqScopesRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasMissingPathParamDefinitionRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasMissingResponseForOperationRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasOperationSummaryTooLongRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasPathParamNotFoundRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasRequiredParamWithDefaultValueRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasSecurityRequirementScopesMustBeEmptyRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasServerVarNotFoundInTemplateRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedArrayCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedHeaderCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedHeaderUsageRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedNumOfParamMTsRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedNumberOfHeaderMTsRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedParamAllowEmptyValueRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedParamCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedParamMultiRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedRequestBodyRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedSecurityRequirementScopesRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedUsageOfBearerTokenRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedUsageOfDiscriminatorRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnexpectedXmlWrappingRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownApiKeyLocationRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownArrayCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownArrayFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownArrayTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownCookieParamStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownEncodingStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownHeaderCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownHeaderFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownHeaderParamStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownHeaderTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownOauthFlowTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownParamCollectionFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownParamFormatRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownParamLocationRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownParamStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownParamTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownPathParamStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownQueryParamStyleRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasUnknownSecuritySchemeTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasAdditionalOperationsInvalidKeyRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidTagParentRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasInvalidXmlNodeTypeRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OasTagParentCircularRefRule;
import io.apitomy.datamodels.validation.rules.invalid.value.OrpcInvalidMethodParamStructureRule;
import io.apitomy.datamodels.validation.rules.mutex.AaMessageOneOfMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasBodyAndFormDataMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasExampleValueMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasHeaderExamplesMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasHeaderSchemaContentMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasLinkOperationRefMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasMediaTypeExamplesMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasParameterExamplesMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasExampleDataValueMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasExampleSerializedValueMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasParameterSchemaContentMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.mutex.OasXmlNodeTypeMutualExclusivityRule;
import io.apitomy.datamodels.validation.rules.other.OasBodyParameterUniquenessValidationRule;
import io.apitomy.datamodels.validation.rules.other.OasIgnoredContentTypeHeaderRule;
import io.apitomy.datamodels.validation.rules.other.OasIgnoredHeaderParameterRule;
import io.apitomy.datamodels.validation.rules.other.OasOperationIdUniquenessValidationRule;
import io.apitomy.datamodels.validation.rules.other.OasParameterUniquenessValidationRule;
import io.apitomy.datamodels.validation.rules.other.OasUnknownPropertyRule;
import io.apitomy.datamodels.validation.rules.other.OrpcDuplicateMethodNameRule;
import io.apitomy.datamodels.validation.rules.other.SecurityRequirementUniquenessValidationRule;
import io.apitomy.datamodels.validation.rules.other.TagUniquenessValidationRule;
import io.apitomy.datamodels.validation.rules.required.MissingApiInformationRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingAsyncApiPropertyRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingChannelAddressRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingChannelsPropertyRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingCorrelationIdLocationRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingMessagePayloadOrHeadersRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingOperationActionRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingOperationChannelReferenceRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingParameterLocationRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingParameterSchemaRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingSchemaDefinitionRule;
import io.apitomy.datamodels.validation.rules.required.AaMissingServerHostRule;
import io.apitomy.datamodels.validation.rules.required.AasMissingServerProtocolRule;
import io.apitomy.datamodels.validation.rules.required.MissingApiKeySchemeParamLocationRule;
import io.apitomy.datamodels.validation.rules.required.MissingApiKeySchemeParamNameRule;
import io.apitomy.datamodels.validation.rules.required.MissingApiTitleRule;
import io.apitomy.datamodels.validation.rules.required.MissingApiVersionRule;
import io.apitomy.datamodels.validation.rules.required.MissingHttpSecuritySchemeTypeRule;
import io.apitomy.datamodels.validation.rules.required.MissingLicenseNameRule;
import io.apitomy.datamodels.validation.rules.required.MissingOAuthFlowAuthUrlRule;
import io.apitomy.datamodels.validation.rules.required.MissingOAuthFlowRokenUrlRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOAuthFlowDeviceAuthUrlRule;
import io.apitomy.datamodels.validation.rules.required.MissingOAuthFlowScopesRule;
import io.apitomy.datamodels.validation.rules.required.MissingOAuthSecuritySchemeFlowsRule;
import io.apitomy.datamodels.validation.rules.required.MissingOpenIdConnectSecuritySchemeConnectUrlRule;
import io.apitomy.datamodels.validation.rules.required.MissingOperationDescriptionRule;
import io.apitomy.datamodels.validation.rules.required.MissingOperationIdRule;
import io.apitomy.datamodels.validation.rules.required.MissingOperationSummaryRule;
import io.apitomy.datamodels.validation.rules.required.MissingSecuritySchemeTypeRule;
import io.apitomy.datamodels.validation.rules.required.MissingServerTemplateUrlRule;
import io.apitomy.datamodels.validation.rules.required.MissingTagNameRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingApiPathsRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingBodyParameterSchemaRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingDiscriminatorPropertyNameRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingExternalDocumentationUrlRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingHeaderArrayInformationRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingHeaderTypeRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingItemsArrayInformationRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingItemsTypeRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOAuthSchemeAuthUrlRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOAuthSchemeFlowTypeRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOAuthSchemeScopesRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOAuthSchemeTokenUrlRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOpenApiPropertyRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOperationResponsesRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingOperationTagsRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingParameterArrayTypeRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingParameterLocationRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingParameterNameRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingParameterTypeRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingRequestBodyContentRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingResponseDefinitionDescriptionRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingResponseDescriptionRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingSchemaArrayInformationRule;
import io.apitomy.datamodels.validation.rules.required.OasMissingServerVarDefaultValueRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingContentDescriptorNameRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingContentDescriptorSchemaRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingDocumentMethodsRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingErrorCodeRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingErrorMessageRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingExamplePairingNameRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingExamplePairingParamsRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingLinkNameRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingMethodNameRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingMethodParamsRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingOpenRpcPropertyRule;
import io.apitomy.datamodels.validation.rules.required.OrpcMissingServerVariableDefaultRule;
import io.apitomy.datamodels.validation.rules.required.OasPathParamsMustBeRequiredRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The full set of validation rules.
 *
 * @author eric.wittmann@gmail.com
 */
public class ValidationRuleSet {

    public static ValidationRuleSet instance = new ValidationRuleSet();

    public static ValidationRuleMetaData md(String code, String name, String type, String entity,
                                            ModelType[] versions, boolean specMandated, String messageTemplate, Class<?> ruleClass) {
        return new ValidationRuleMetaData(code, name, type, entity, versions, specMandated,
                messageTemplate, ruleClass);
    }

    private final List<ValidationRuleMetaData> rules;

    /**
     * Constructor.
     */
    public ValidationRuleSet() {
        this.rules = new ArrayList<>();

        this.rules.add(md("UNKNOWN-001", "Unknown/Unexpected Property", "Unknown Property", "All", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "An unexpected property \"${'property'}\" was found.  Extension properties should begin with \"x-\".", OasUnknownPropertyRule.class));
        /** Uniqueness **/
        this.rules.add(md("TAG-003", "Duplicate Tag Definition", "Uniqueness", "Tag", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Duplicate tag '${'tagName'}' found (every tag must have a unique name).", TagUniquenessValidationRule.class));
        this.rules.add(md("OP-003", "Duplicate Operation ID", "Uniqueness", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Duplicate operationId '${'operationId'}' found (operation IDs must be unique across all operations in the API).", OasOperationIdUniquenessValidationRule.class));
        this.rules.add(md("PAR-019", "Duplicate Parameter", "Uniqueness", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Duplicate ${'paramIn'} parameter named '${'paramName'}' found (parameters must be unique by name and location).", OasParameterUniquenessValidationRule.class));
        this.rules.add(md("PAR-020", "Duplicate Body Parameter", "Uniqueness", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Operation has multiple \"body\" parameters.", OasBodyParameterUniquenessValidationRule.class));
        this.rules.add(md("SREQ-005", "Duplicate Security Requirements", "Uniqueness", "Security Requirement", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Duplicate Security Requirement '${'securityReq'}' in Security field", SecurityRequirementUniquenessValidationRule.class));
        /** Invalid Property Format **/
        this.rules.add(md("AAD-004", "Invalid AsyncAPI Version Format", "Invalid Property Format", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "AsyncAPI version '${'version'}' is an incorrect format (must be major.minor.patch).", AaInvalidAsyncApiVersionFormatRule.class));
        this.rules.add(md("AAD-005", "Invalid Document ID Format", "Invalid Property Format", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Document ID should be a valid URI.", AaInvalidDocumentIdFormatRule.class));
        this.rules.add(md("AAD-006", "Invalid Default Content Type", "Invalid Property Format", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Default Content Type should be a valid MIME type.", AaInvalidDefaultContentTypeRule.class));
        this.rules.add(md("CHAN-002", "Invalid Channel Description", "Invalid Property Format", "Channel", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Channel description is an incorrect format.", AaInvalidChannelDescriptionRule.class));
        this.rules.add(md("AARADDR-002", "Invalid Reply Address Location", "Invalid Property Format", "Operation Reply Address", new ModelType[]{ModelType.ASYNCAPI30}, true, "Reply address location must be a valid runtime expression.", AaInvalidReplyAddressLocationRule.class));
        this.rules.add(md("AAPARAM-003", "Invalid Parameter Description (AsyncAPI)", "Invalid Property Format", "Parameter", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Parameter description is an incorrect format.", InvalidParameterDescriptionRule.class));
        this.rules.add(md("AAM-002", "Invalid Message Description", "Invalid Property Format", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Message description is an incorrect format.", AaInvalidMessageDescriptionRule.class));
        this.rules.add(md("AAM-003", "Invalid Message Content Type", "Invalid Property Format", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Message content type must be a valid MIME type.", AaInvalidMessageContentTypeRule.class));
        this.rules.add(md("AAM-004", "Invalid Message Schema Format", "Invalid Property Format", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Schema format must be valid (e.g., \\\"application/vnd.aai.asyncapi;version=2.0.0\\\").", AaInvalidMessageSchemaFormatRule.class));
        this.rules.add(md("AACID-001", "Invalid Correlation ID Description", "Invalid Property Format", "Correlation ID", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Correlation ID description is an incorrect format.", AaInvalidCorrelationIdDescriptionRule.class));
        this.rules.add(md("AAD-007", "Operations Property Only in 3.0", "Invalid Property Value", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "The 'operations' property is only valid in AsyncAPI 3.0, not in version '${'version'}'.", AaOperationsOnlyIn3xRule.class));
        this.rules.add(md("INF-005", "Tags Property in Info (3.0 Only)", "Invalid Property Value", "Info", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "The 'tags' property is only allowed in AsyncAPI 3.0, not in 2.x.", AaInfoTagsOnlyIn3xRule.class));
        this.rules.add(md("INF-006", "External Docs Property in Info (3.0 Only)", "Invalid Property Value", "Info", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "The 'externalDocs' property is only allowed in AsyncAPI 3.0, not in 2.x.", AaInfoExternalDocsOnlyIn3xRule.class));
        this.rules.add(md("CHAN-006", "Servers Property Only in 2.2+", "Invalid Property Value", "Channel", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21}, true, "The 'servers' property is only allowed in AsyncAPI 2.2 and later.", AaServersPropertyOnlyIn22PlusRule.class));
        this.rules.add(md("CHAN-008", "Channel Messages Property (3.0 Only)", "Invalid Property Value", "Channel", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "The 'messages' property is only allowed in AsyncAPI 3.0, not in 2.x.", AaChannelMessagesOnlyIn3xRule.class));
        this.rules.add(md("CHAN-009", "Channel Operations in 2.x Only", "Invalid Property Value", "Channel", new ModelType[]{ModelType.ASYNCAPI30}, true, "The '${'property'}' operation is only allowed in AsyncAPI 2.x, not in 3.0.", AaChannelOperationsIn2xOnlyRule.class));
        this.rules.add(md("R-004", "Invalid API Host", "Invalid Property Format", "API", new ModelType[]{ModelType.OPENAPI20}, true, "Host not properly formatted - only the host name (and optionally port) should be specified.", OasInvalidApiHostRule.class));
        this.rules.add(md("R-005", "Invalid API Base Path", "Invalid Property Format", "API", new ModelType[]{ModelType.OPENAPI20}, true, "Base Path should being with a '/' character.", OasInvalidApiBasePathRule.class));
        this.rules.add(md("INF-003", "Invalid API Description", "Invalid Property Format", "Info", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API description is an incorrect format.", InvalidApiDescriptionRule.class));
        this.rules.add(md("INF-004", "Invalid Terms of Service URL", "Invalid Property Format", "Info", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Terms of Service URL is an incorrect format.", InvalidTermsOfServiceUrlRule.class));
        this.rules.add(md("CTC-001", "Invalid Contact URL", "Invalid Property Format", "Contact", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Contact URL is an incorrect format.", InvalidContactUrlRule.class));
        this.rules.add(md("CTC-002", "Invalid Contact Email", "Invalid Property Format", "Contact", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Contact Email is an incorrect format.", InvalidContactEmailRule.class));
        this.rules.add(md("LIC-002", "Invalid License URL", "Invalid Property Format", "License", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "License URL is an incorrect format.", InvalidLicenseUrlRule.class));
        this.rules.add(md("OP-002", "Invalid Operation Description", "Invalid Property Format", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Operation Description is an incorrect format.", OasInvalidOperationDescriptionRule.class));
        this.rules.add(md("OP-005", "Invalid Operation 'Consumes' Type", "Invalid Property Format", "Operation", new ModelType[]{ModelType.OPENAPI20}, true, "Operation \"consumes\" must be a valid mime type.", OasInvalidOperationConsumesRule.class));
        this.rules.add(md("OP-006", "Invalid Operation 'Produces' Type", "Invalid Property Format", "Operation", new ModelType[]{ModelType.OPENAPI20}, true, "Operation \"produces\" must be a valid mime type.", OasInvalidOperationProducesRule.class));
        this.rules.add(md("ED-002", "Invalid External Documentation Description", "Invalid Property Format", "External Documentation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "External Docs Description is an incorrect format.", InvalidExternalDocsDescriptionRule.class));
        this.rules.add(md("ED-003", "Invalid External Documentation URL", "Invalid Property Format", "External Documentation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "External Docs URL is an incorrect format.", OasInvalidExternalDocsUrlRule.class));
        this.rules.add(md("PAR-010", "Invalid Parameter Description", "Invalid Property Format", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter Description is an incorrect format.", InvalidParameterDescriptionRule.class));
        this.rules.add(md("IT-007", "Invalid Schema Items Default Value", "Invalid Property Format", "Shema Items", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema Items default value does not conform to the correct type.", OasInvalidSchemaItemsDefaultValueRule.class));
        this.rules.add(md("HEAD-005", "Invalid Header Default Value", "Invalid Property Format", "Header Items", new ModelType[]{ModelType.OPENAPI20}, true, "The \"default\" property must conform to the \"type\" of the items.", OasInvalidHeaderDefaultValueRule.class));
        this.rules.add(md("TAG-002", "Invalid Tag Description", "Invalid Property Format", "Tag", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Tag Description is an incorrect format.", InvalidTagDescriptionRule.class));
        this.rules.add(md("SS-011", "Invalid Security Scheme Auth URL", "Invalid Property Format", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "Security Scheme Authorization URL is an incorrect format.", OasInvalidSecuritySchemeAuthUrlRule.class));
        this.rules.add(md("SS-012", "Invalid Security Scheme Token URL", "Invalid Property Format", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "Security Scheme Token URL is an incorrect format.", OasInvalidSecuritySchemeTokenUrlRule.class));
        this.rules.add(md("XML-001", "Invalid XML Namespace URL", "Invalid Property Format", "XML", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "XML Namespace URL is an incorrect format.", OasInvalidXmlNamespaceUrlRule.class));
        this.rules.add(md("RES-004", "Invalid Response Description", "Invalid Property Format", "Response", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Response description is an incorrect format.", OasInvalidResponseDescriptionRule.class));
        this.rules.add(md("EX-002", "Invalid Example Description", "Invalid Property Format", "Example", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Example Description is an incorrect format.", OasInvalidExampleDescriptionRule.class));
        this.rules.add(md("LINK-004", "Invalid Link Description", "Invalid Property Format", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Link Description is an incorrect format.", OasInvalidLinkDescriptionRule.class));
        this.rules.add(md("FLOW-003", "Invalid OAuth Authorization URL", "Invalid Property Format", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "OAuth Authorization URL is an incorrect format.", OasInvalidOAuthAuthorizationUrlRule.class));
        this.rules.add(md("FLOW-004", "Invalid OAuth Token URL", "Invalid Property Format", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "OAuth Token URL is an incorrect format.", OasInvalidOAuthTokenUrlRule.class));
        this.rules.add(md("FLOW-005", "Invalid OAuth Refresh URL", "Invalid Property Format", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "OAuth Refresh URL is an incorrect format.", OasInvalidOAuthRefreshUrlRule.class));
        this.rules.add(md("FLOW-008", "Invalid OAuth Device Authorization URL", "Invalid Property Format", "OAuth Flow", new ModelType[]{ModelType.OPENAPI32}, true, "OAuth Device Authorization URL is an incorrect format.", OasInvalidOAuthDeviceAuthorizationUrlRule.class));
        this.rules.add(md("SS-022", "Invalid OAuth2 Metadata URL", "Invalid Property Format", "Security Scheme", new ModelType[]{ModelType.OPENAPI32}, true, "OAuth2 Metadata URL is an incorrect format.", OasInvalidOAuth2MetadataUrlRule.class));
        this.rules.add(md("PATH-008", "Invalid Path Item Description", "Invalid Property Format", "Path Item", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path Item Description is an incorrect format.", OasInvalidPathItemDescriptionRule.class));
        this.rules.add(md("RB-001", "Invalid Request Body Description", "Invalid Property Format", "Request Body", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Request Body Description is an incorrect format.", OasInvalidRequestBodyDescriptionRule.class));
        this.rules.add(md("HEAD-009", "Invalid Header Description", "Invalid Property Format", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header Description is an incorrect format.", OasInvalidHeaderDescriptionRule.class));
        this.rules.add(md("SS-014", "Invalid OpenID Connect URL", "Invalid Property Format", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "OpenID Connect URL is an incorrect format.", OasInvalidOpenIDConnectUrlRule.class));
        this.rules.add(md("SS-015", "Invalid Security Scheme Description", "Invalid Property Format", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Scheme Description is an incorrect format.", OasInvalidSecuritySchemeDescriptionRule.class));
        this.rules.add(md("SRV-003", "Invalid Server Description", "Invalid Property Format", "Server", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Server Description is an incorrect format.", InvalidServerDescriptionRule.class));
        this.rules.add(md("SRV-002", "Invalid Server URL", "Invalid Property Format", "Server", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Server URL is an incorrect format.", InvalidServerUrlRule.class));
        this.rules.add(md("SRV-006", "Invalid Server Host Format", "Invalid Property Format", "Server", new ModelType[]{ModelType.ASYNCAPI30}, true, "Server host must be a valid hostname or IP address.", AaInvalidServerHostFormatRule.class));
        this.rules.add(md("SRV-007", "Invalid Server Pathname Format", "Invalid Property Format", "Server", new ModelType[]{ModelType.ASYNCAPI30}, true, "Server pathname must begin with '/'.", AaInvalidServerPathnameFormatRule.class));
        this.rules.add(md("SVAR-002", "Invalid Server Variable Description", "Invalid Property Format", "Server Variable", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Server Variable Description is an incorrect format.", OasInvalidServerVariableDescriptionRule.class));
        /** Invalid Property Name **/
        this.rules.add(md("PATH-006", "Empty Path Segment", "Invalid Property Name", "Path Item", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path template \"${'path'}\" contains one or more empty segment.", OasEmptyPathSegmentRule.class));
        this.rules.add(md("PATH-007", "Duplicate Path Segment", "Invalid Property Name", "Path Item", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path template \"${'path'}\" contains duplicate variable names (${'duplicates'}).", OasDuplicatePathSegmentRule.class));
        this.rules.add(md("PATH-005", "Invalid Path Segment", "Invalid Property Name", "Path Item", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path template \"${'path'}\" is not valid.", OasInvalidPathSegmentRule.class));
        this.rules.add(md("PATH-009", "Identical Path Template", "Invalid Property Name", "Path Item", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path template \"${'path'}\" is semantically identical to at least one other path.", OasIdenticalPathTemplateRule.class));
        this.rules.add(md("RES-003", "Invalid HTTP Response Code", "Invalid Property Name", "Response", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "\"${'statusCode'}\" is not a valid HTTP response status code.", OasInvalidHttpResponseCodeRule.class));
        this.rules.add(md("EX-001", "Unmatched Example Type", "Invalid Property Name", "Example", new ModelType[]{ModelType.OPENAPI20}, true, "Example '${'contentType'}' must match one of the \"produces\" mime-types.", OasUnmatchedExampleTypeRule.class));
        this.rules.add(md("SDEF-001", "Invalid Schema Definition Name", "Invalid Property Name", "Schema Definition", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema Definition Name is not valid.", OasInvalidSchemaDefNameRule.class));
        this.rules.add(md("PDEF-001", "Invalid Parameter Definition Name", "Invalid Property Name", "Parameter Definition", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter Definition Name is not valid.", OasInvalidParameterDefNameRule.class));
        this.rules.add(md("RDEF-001", "Invalid Response Definition Name", "Invalid Property Name", "Response Definition", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Response Definition Name is not valid.", OasInvalidResponseDefNameRule.class));
        this.rules.add(md("SCPS-001", "Invalid Scope Name", "Invalid Scope Name", "Scopes", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "'${'scope'}' is not a valid scope name.", OasInvalidScopeNameRule.class));
        this.rules.add(md("SS-013", "Invalid Security Scheme Name", "Invalid Property Name", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Scheme Name is not valid.", OasInvalidSecuritySchemeNameRule.class));
        this.rules.add(md("EDEF-001", "Invalid Example Definition Name", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Example Definition Name is not valid.", OasInvalidExampleDefinitionNameRule.class));
        this.rules.add(md("RBDEF-001", "Invalid Request Body Definition Name", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Request Body Definition Name is not valid.", OasInvalidRequestBodyDefinitionNameRule.class));
        this.rules.add(md("HDEF-001", "Invalid Header Definition Name", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header Definition Name is not valid.", OasInvalidHeaderDefinitionNameRule.class));
        this.rules.add(md("LDEF-001", "Invalid Link Definition Name", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Link Definition Name is not valid.", OasInvalidLinkDefinitionNameRule.class));
        this.rules.add(md("CDEF-001", "Invalid Callback Definition Name", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Callback Definition Name is not valid.", OasInvalidCallbackDefinitionNameRule.class));
        this.rules.add(md("ENC-006", "Unmatched Encoding Property", "Invalid Property Name", "Components", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Encoding Property \"${'name'}\" not found in the associated schema.", OasUnmatchedEncodingPropertyRule.class));
        this.rules.add(md("COMP-001", "Invalid Component Key Name Pattern", "Invalid Property Name", "Components", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Component key \"${'key'}\" does not match the required pattern ^[a-zA-Z0-9\\.\\-_]+$", AaInvalidComponentKeyNameRule.class));
        /** Invalid Property Value **/
        this.rules.add(md("R-006", "Invalid API Scheme", "Invalid Property Value", "API", new ModelType[]{ModelType.OPENAPI20}, true, "API scheme \"${'scheme'}\" not allowed.  Must be one of: http, https, ws, wss", OasInvalidApiSchemeRule.class));
        this.rules.add(md("R-007", "Invalid 'Consumes' Mime-Type", "Invalid Property Value", "API", new ModelType[]{ModelType.OPENAPI20}, true, "API \"consumes\" must be a valid mime-type.", OasInvalidApiConsumesMTRule.class));
        this.rules.add(md("R-008", "Invalid 'Produces' Mime-Type", "Invalid Property Value", "API", new ModelType[]{ModelType.OPENAPI20}, true, "API \"produces\" must be a valid mime-type.", OasInvalidApiProducesMTRule.class));
        this.rules.add(md("OP-001", "Operation Summary Too Long", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Operation Summary should be less than 120 characters.", OasOperationSummaryTooLongRule.class));
        this.rules.add(md("OP-004", "Invalid Operation ID", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Operation ID is an invalid format.", OasInvalidOperationIdRule.class));
        this.rules.add(md("AAO-004", "Invalid Operation Action Value", "Invalid Property Value", "Operation", new ModelType[]{ModelType.ASYNCAPI30}, true, "Operation action '${'action'}' is invalid. Must be one of: send, receive.", AaInvalidOperationActionValueRule.class));
        this.rules.add(md("AAO-009", "Security Property Added in 2.4", "Invalid Property Value", "Operation", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23}, true, "The 'security' property is only allowed in AsyncAPI 2.4 and later.", AaOperationSecurityPropertyOnlyIn24PlusRule.class));
        this.rules.add(md("AAO-010", "Operation Referenceable in 3.0", "Invalid Property Value", "Operation", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "The '$ref' property is only allowed for operations in AsyncAPI 3.0.", AaOperationReferenceableOnlyIn3xRule.class));
        this.rules.add(md("AAO-011", "Operation Reply Only in 3.0", "Invalid Property Value", "Operation", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "The 'reply' property is only allowed for operations in AsyncAPI 3.0.", AaOperationReplyOnlyIn3xRule.class));
        this.rules.add(md("AAO-012", "Operation Message vs Messages", "Invalid Property Value", "Operation", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Operation uses incorrect message property for version: AsyncAPI 2.x uses 'message', 3.0 uses 'messages'.", AaOperationMessageVsMessagesRule.class));
        this.rules.add(md("AATRT-002", "Operation Trait Security in 2.4+", "Invalid Property Value", "Operation Trait", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23}, true, "The 'security' property is only allowed for operation traits in AsyncAPI 2.4 and later.", AaOperationTraitSecurityIn24PlusRule.class));
        this.rules.add(md("AATRT-003", "Operation Trait Structure in 3.0", "Invalid Property Value", "Operation Trait", new ModelType[]{ModelType.ASYNCAPI30}, true, "The 'operationId' property is not allowed for operation traits in AsyncAPI 3.0.", AaOperationTraitOperationIdIn2xOnlyRule.class));
        this.rules.add(md("SRV-009", "Server Referenceable Only in 2.3+", "Invalid Property Value", "Server", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22}, true, "Server $ref is only allowed in AsyncAPI 2.3 and later.", AaServerReferenceableOnlyIn23PlusRule.class));
        this.rules.add(md("SRV-010", "Server Tags Only in 2.5+", "Invalid Property Value", "Server", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24}, true, "Server tags are only allowed in AsyncAPI 2.5 and later.", AaServerTagsOnlyIn25PlusRule.class));
        this.rules.add(md("SRV-012", "Server Structure Changed in 3.0", "Invalid Property Value", "Server", new ModelType[]{ModelType.ASYNCAPI30}, true, "In AsyncAPI 3.0, use 'host', 'pathname', and 'protocol' instead of 'url'.", AaServerStructureChangedIn3xRule.class));
        this.rules.add(md("SVAR-004", "Server Variable Default Not in Enum", "Invalid Property Value", "Server Variable", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Server variable default value must be one of the enum values.", AaServerVariableDefaultNotInEnumRule.class));
        this.rules.add(md("SVAR-006", "Server Variable Referenceable Only in 2.4+", "Invalid Property Value", "Server Variable", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23}, true, "Server variable $ref is only allowed in AsyncAPI 2.4 and later.", AaServerVariableReferenceableOnlyIn24PlusRule.class));
        this.rules.add(md("AAPARAM-005", "Parameter Default Not in Enum (AsyncAPI 3.0)", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.ASYNCAPI30}, true, "Parameter default value must be one of the enum values.", AaParameterDefaultNotInEnumRule.class));
        this.rules.add(md("AAPARAM-006", "Invalid Parameter Location Value (AsyncAPI 3.0)", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.ASYNCAPI30}, true, "Parameter location must be a valid runtime expression.", AaInvalidParameterLocationValueRule.class));
        this.rules.add(md("AAPARAM-007", "Parameter Structure Changed in 3.0", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.ASYNCAPI30}, true, "Parameter structure changed in AsyncAPI 3.0 - use direct properties instead of schema.", AaParameterStructureChangedIn3xRule.class));
        this.rules.add(md("AACID-002", "Invalid Correlation ID Location Format", "Invalid Property Value", "Correlation ID", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Correlation ID location must be a valid runtime expression.", AaInvalidCorrelationIdLocationFormatRule.class));
        this.rules.add(md("AAFLOW-001", "OAuth Flow Scopes vs Available Scopes", "Invalid Property Value", "OAuth Flow", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "OAuth flow uses incorrect scopes property: AsyncAPI 2.x uses 'scopes', 3.0 uses 'availableScopes'.", AaOAuthFlowScopesVsAvailableScopesRule.class));
        this.rules.add(md("OP-010", "Invalid Operation Scheme", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI20}, true, "Operation scheme \"${'scheme'}\" not allowed.  Must be one of: http, https, ws, wss", OasInvalidOperationSchemeRule.class));
        this.rules.add(md("PAR-007", "Path Parameter Not Found", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path Parameter \"${'name'}\" not found in path template.", OasPathParamNotFoundRule.class));
        this.rules.add(md("PAR-008", "Form Data Parameter Not Allowed", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Form Data Parameters are only used in 'application/x-www-form-urlencoded' or 'multipart/form-data' requests.", OasFormDataParamNotAllowedRule.class));
        this.rules.add(md("PAR-009", "Unknown Parameter Location", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameters can only be found in one of: ${'options'}", OasUnknownParamLocationRule.class));
        this.rules.add(md("PAR-011", "Unknown Parameter Type", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter types are limited to: string, number, integer, boolean, array, file", OasUnknownParamTypeRule.class));
        this.rules.add(md("PAR-012", "Unknown Parameter Format", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter Format must be one of: int32, int64, float, double, byte, binary, date, date-time, password", OasUnknownParamFormatRule.class));
        this.rules.add(md("PAR-013", "Unexpected Usage of Parameter 'allowEmptyValue'", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Allow Empty Value is not allowed (only for ${'options'} params).", OasUnexpectedParamAllowEmptyValueRule.class));
        this.rules.add(md("PAR-014", "Unexpected Usage of Parameter 'collectionFormat'", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "The \"collectionFormat\" property is only allowed for 'array' type parameters.", OasUnexpectedParamCollectionFormatRule.class));
        this.rules.add(md("PAR-015", "Unknown Parameter Collection Format", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter Collection Format must be one of: csv, ssv, tsv, pipes, multi", OasUnknownParamCollectionFormatRule.class));
        this.rules.add(md("PAR-016", "Unexpected Parameter Usage of 'multi'", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter Collection Format can only be \"multi\" for Query and Form Data parameters.", OasUnexpectedParamMultiRule.class));
        this.rules.add(md("PAR-017", "Required Parameter With Default Value Not Allowed", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Required Parameters can not have a default value.", OasRequiredParamWithDefaultValueRule.class));
        this.rules.add(md("IT-003", "Unknown Array Type", "Invalid Property Value", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Schema Items Type must be one of: string, number, integer, boolean, array", OasUnknownArrayTypeRule.class));
        this.rules.add(md("IT-004", "Unknown Array Format", "Invalid Property Value", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Schema Items Format must be one of: int32, int64, float, double, byte, binary, date, date-time, password", OasUnknownArrayFormatRule.class));
        this.rules.add(md("IT-005", "Unknown Array Collection Format", "Invalid Property Value", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Schema Items Collection Format must be one of: csv, ssv, tsv, pipes", OasUnknownArrayCollectionFormatRule.class));
        this.rules.add(md("IT-006", "Unexpected Usage of Array 'collectionFormat'", "Invalid Property Value", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Schema Items Collection Format is only allowed for Array style parameters.", OasUnexpectedArrayCollectionFormatRule.class));
        this.rules.add(md("HEAD-003", "Unknown Header Type", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header Type must be one of: string, number, integer, boolean, array", OasUnknownHeaderTypeRule.class));
        this.rules.add(md("HEAD-004", "Unknown Header Format", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header Format must be one of: int32, int64, float, double, byte, binary, date, date-time, password", OasUnknownHeaderFormatRule.class));
        this.rules.add(md("HEAD-006", "Unexpected Usage of Header 'collectionFormat'", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header Collection Format is only allowed for \"array\" type headers.", OasUnexpectedHeaderCollectionFormatRule.class));
        this.rules.add(md("HEAD-007", "Unknown Header Collection Format", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header Collection Format must be one of: csv, ssv, tsv, pipes", OasUnknownHeaderCollectionFormatRule.class));
        this.rules.add(md("XML-002", "Unexpected Usage of XML Wrapping", "Invalid Property Value", "XML", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "XML Wrapped elements can only be used for \"array\" properties.", OasUnexpectedXmlWrappingRule.class));
        this.rules.add(md("SS-008", "Unknown Security Scheme Type", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Scheme Type must be one of: ${'options'}", OasUnknownSecuritySchemeTypeRule.class));
        this.rules.add(md("SS-009", "Unknown API Key Location", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "API Key Security Scheme must be located \"in\" one of: ${'options'}", OasUnknownApiKeyLocationRule.class));
        this.rules.add(md("SS-010", "Unknown OAuth Flow Type", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "OAuth Security Scheme \"flow\" must be one of: implicit, password, application, accessCode", OasUnknownOauthFlowTypeRule.class));
        this.rules.add(md("SREQ-002", "Security Requirement Scopes Must Be Empty", "Invalid Property Value", "Security Requirement", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Requirement '${'sname'}' scopes must be an empty array because the referenced Security Definition not ${'options'}.", OasSecurityRequirementScopesMustBeEmptyRule.class));
        this.rules.add(md("SREQ-003", "Unexpected Security Requirement Scope(s)", "Invalid Property Value", "Security Requirement", new ModelType[]{ModelType.OPENAPI20}, true, "Security Requirement '${'sname'}' scopes must be an array of values from the possible scopes defined by the referenced Security Definition.", OasUnexpectedSecurityRequirementScopesRule.class));
        this.rules.add(md("ENC-001", "Unexpected Usage of Headers (Multipart Media Type)", "Invalid Property Value", "Encoding", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Headers are not allowed for \"${'name'}\" media types.", OasUnexpectedHeaderUsageRule.class));
        this.rules.add(md("ENC-002", "Encoding Style Not Allowed for Media Type", "Invalid Property Value", "Encoding", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Encoding Style is not allowed for \"${'name'}\" media types.", OasEncodingStyleNotAllowedRule.class));
        this.rules.add(md("ENC-003", "'Explode' Not Allowed for Media Type", "Invalid Property Value", "Encoding", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "\"Explode\" is not allowed for \"${'name'}\" media types.", OasExplodeNotAllowedRule.class));
        this.rules.add(md("ENC-004", "'Allow Reserved' Not Allowed for Media Type", "Invalid Property Value", "Encoding", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "\"Allow Reserved\" is not allowed for \"${'name'}\" media types.", OasAllowReservedNotAllowedRule.class));
        this.rules.add(md("ENC-005", "Unknown Encoding Style", "Invalid Property Value", "Encoding", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Encoding Style is an invalid value.", OasUnknownEncodingStyleRule.class));
        this.rules.add(md("HEAD-010", "InvalidHeaderStyle", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header Style must be \"simple\".", OasInvalidHeaderStyleRule.class));
        this.rules.add(md("HEAD-011", "Unexpected Number of Header Media Types", "Invalid Property Value", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header content cannot have multiple media types.", OasUnexpectedNumberOfHeaderMTsRule.class));
        this.rules.add(md("LINK-002", "Invalid Link OperationID Reference", "Invalid Reference", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "The Operation ID does not refer to an existing Operation.", OasInvalidLinkOperationIdRule.class));
        this.rules.add(md("MT-003", "Invalid Encoding For Multi-Part Media Type", "Invalid Property Value", "Media Type", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Encoding is not allowed for \"${'name'}\" media types.", OasInvalidEncodingForMPMTRule.class));
        this.rules.add(md("OP-009", "Unexpected Request Body", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Request Body is not supported for ${'method'} operations.", OasUnexpectedRequestBodyRule.class));
        this.rules.add(md("OP-011", "Missing Path Parameter Definition", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "No definition found for path variable \"${'param'}\" for path '${'path'}' and method '${'method'}'.", OasMissingPathParamDefinitionRule.class));
        this.rules.add(md("OP-013", "Missing Response for Operation", "Invalid Property Value", "Operation", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Operation must have at least one Response.", OasMissingResponseForOperationRule.class));
        this.rules.add(md("PAR-022", "Unknown Parameter Style", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter Style must be one of: [\"matrix\", \"label\", \"form\", \"simple\", \"spaceDelimited\", \"pipeDelimited\", \"deepObject\"] (Found \"${'style'}\").", OasUnknownParamStyleRule.class));
        this.rules.add(md("PAR-023", "Unknown Query Parameter Style", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Query Parameter Style must be one of: [\"form\", \"spaceDelimited\", \"pipeDelimited\", \"deepObject\"] (Found \"${'style'}\").", OasUnknownQueryParamStyleRule.class));
        this.rules.add(md("PAR-024", "Unknown Cookie Parameter Style", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Cookie Parameter style must be \"form\". (Found \"${'style'}\")", OasUnknownCookieParamStyleRule.class));
        this.rules.add(md("PAR-025", "Unknown Header Parameter Style", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header Parameter Style must be \"simple\". (Found \"${'style'}\").", OasUnknownHeaderParamStyleRule.class));
        this.rules.add(md("PAR-027", "Unknown Path Parameter Style", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path Parameter Style must be one of: [\"matrix\", \"label\", \"simple\"]  (Found \"${'style'}\").", OasUnknownPathParamStyleRule.class));
        this.rules.add(md("PAR-028", "'Allow Reserved' Not Allowed for Param", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Allow Reserved is only allowed for Query Parameters.", OasAllowReservedNotAllowedForParamRule.class));
        this.rules.add(md("PAR-029", "Unexpected Number of Parameter Media Types", "Invalid Property Value", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter content cannot have multiple media types.", OasUnexpectedNumOfParamMTsRule.class));
        this.rules.add(md("SCH-002", "Unexpected Usage of Discriminator", "Invalid Property Value", "Schema", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema Discriminator is only allowed when using one of: [\"oneOf\", \"anyOf\", \"allOf\"]", OasUnexpectedUsageOfDiscriminatorRule.class));
        this.rules.add(md("SS-016", "Invalid HTTP Security Scheme Type", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "HTTP Security Scheme must be one of: [\"basic\", \"bearer\", \"digest\", \"hoba\", \"mutual\", \"negotiate\", \"oauth\", \"vapid\", \"scram-sha-1\", \"scram-sha-256\"] (Found: '${'scheme'}')", OasInvalidHttpSecuritySchemeTypeRule.class));
        this.rules.add(md("SS-017", "Unexpected Usage of 'bearerFormat'", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Scheme \"Bearer Format\" only allowed for HTTP Bearer auth scheme.", OasUnexpectedUsageOfBearerTokenRule.class));
        this.rules.add(md("SREQ-004", "Invalid Security Requirement Scopes", "Invalid Property Value", "Security Requirement", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Value (scopes) for Security Requirement \"${'name'}\" must be an array.", OasInvalidSecurityReqScopesRule.class));
        this.rules.add(md("SVAR-003", "Server Variable Not Found in Template", "Invalid Property Value", "XXX", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Server Variable \"${'name'}\" is not found in the server url template.", OasServerVarNotFoundInTemplateRule.class));
        /** Invalid Reference **/
        this.rules.add(md("CHAN-003", "Invalid Channel Reference", "Invalid Reference", "Channel", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Channel Reference must refer to a valid Channel definition.", AaInvalidChannelReferenceRule.class));
        this.rules.add(md("CHAN-004", "Invalid Server Reference in Channel", "Invalid Reference", "Channel", new ModelType[]{ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Server reference '${'server'}' must point to a valid Server.", AaInvalidServerReferenceInChannelRule.class));
        this.rules.add(md("SRV-008", "Invalid Server Reference", "Invalid Reference", "Server", new ModelType[]{ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Server Reference must refer to a valid Server definition.", AaInvalidServerReferenceRule.class));
        this.rules.add(md("SVAR-005", "Invalid Server Variable Reference", "Invalid Reference", "Server Variable", new ModelType[]{ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Server variable $ref must point to valid server variable in components.", AaInvalidServerVariableReferenceRule.class));
        this.rules.add(md("AAO-006", "Invalid Operation Trait Reference", "Invalid Reference", "Operation Trait", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Operation trait reference must point to a valid operation trait definition.", AaInvalidOperationTraitReferenceRule.class));
        this.rules.add(md("AAO-007", "Invalid Operation Channel Reference", "Invalid Reference", "Operation", new ModelType[]{ModelType.ASYNCAPI30}, true, "Operation channel reference must point to valid channel.", AaInvalidOperationChannelReferenceRule.class));
        this.rules.add(md("AAO-008", "Invalid Message Reference", "Invalid Reference", "Operation", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Message references must point to valid messages.", AaInvalidMessageReferenceRule.class));
        this.rules.add(md("AAM-006", "Invalid Message Trait Reference", "Invalid Reference", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Message trait references must point to valid message traits.", AaInvalidMessageTraitReferenceRule.class));
        this.rules.add(md("AAM-007", "Invalid Correlation ID Reference", "Invalid Reference", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Correlation ID reference must point to valid correlation ID.", AaInvalidCorrelationIdReferenceRule.class));
        this.rules.add(md("AAREPLY-001", "Invalid Reply Channel Reference", "Invalid Reference", "Operation Reply", new ModelType[]{ModelType.ASYNCAPI30}, true, "Reply channel reference must point to valid channel.", AaInvalidReplyChannelReferenceRule.class));
        this.rules.add(md("AAREPLY-002", "Invalid Reply Message References", "Invalid Reference", "Operation Reply", new ModelType[]{ModelType.ASYNCAPI30}, true, "Reply message references must point to valid messages.", AaInvalidReplyMessageReferencesRule.class));
        this.rules.add(md("AAREPLY-003", "Invalid Reply Reference", "Invalid Reference", "Operation Reply", new ModelType[]{ModelType.ASYNCAPI30}, true, "Reply $ref must point to valid reply in components.", AaInvalidReplyReferenceRule.class));
        this.rules.add(md("AARADDR-001", "Invalid Reply Address Reference", "Invalid Reference", "Operation Reply Address", new ModelType[]{ModelType.ASYNCAPI30}, true, "Reply address $ref must point to valid reply address in components.", AaInvalidReplyAddressReferenceRule.class));
        this.rules.add(md("AATAG-001", "Invalid Tag Reference", "Invalid Reference", "Tag", new ModelType[]{ModelType.ASYNCAPI30}, true, "Tag reference must point to a valid tag in components.", AaInvalidTagReferenceRule.class));
        this.rules.add(md("AAED-002", "Invalid External Documentation Reference", "Invalid Reference", "External Documentation", new ModelType[]{ModelType.ASYNCAPI30}, true, "External docs $ref must point to valid external docs in components.", AaInvalidExternalDocsReferenceRule.class));
        this.rules.add(md("AASS-002", "Security Scheme Scopes Only in 3.0", "Invalid Property Value", "Security Scheme", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "The 'scopes' property is only allowed in security schemes in AsyncAPI 3.0, not in version '${'version'}'.", AaSecuritySchemeScopesOnlyIn3xRule.class));
        this.rules.add(md("PAR-018", "Invalid Parameter Reference", "Invalid Reference", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Parameter Reference must refer to a valid Parameter Definition.", InvalidParameterReferenceRule.class));
        this.rules.add(md("PATH-001", "Invalid Path Item Reference", "Invalid Reference", "Path Item", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path Item Reference must refer to a valid Path Item Definition.", OasInvalidPathItemReferenceRule.class));
        this.rules.add(md("RES-002", "Invalid Response Reference", "Invalid Reference", "Response", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Response Reference must refer to a valid Response Definition.", OasInvalidResponseReferenceRule.class));
        this.rules.add(md("SCH-001", "Invalid Schema Reference", "Invalid Reference", "Schema", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema Reference must refer to a valid Schema Definition.", OasInvalidSchemaReferenceRule.class));
        this.rules.add(md("SREQ-001", "Invalid Security Requirement Name", "Invalid Reference", "Security Requirement", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Security Requirement '${'name'}' must refer to a valid Security Scheme.", OasInvalidSecurityRequirementNameRule.class));
        this.rules.add(md("SS-018", "Invalid Security Scheme Reference", "Invalid Reference", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Security Scheme Reference must refer to a valid Security Scheme Definition.", InvalidSecuritySchemeReferenceRule.class));
        this.rules.add(md("CALL-001", "Invalid Callback Reference", "Invalid Reference", "Callback", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Callback Reference must refer to a valid Callback Definition.", OasInvalidCallbackReferenceRule.class));
        this.rules.add(md("EX-003", "Invalid Example Reference", "Invalid Reference", "Example", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Example Reference must refer to a valid Example Definition.", OasInvalidExampleReferenceRule.class));
        this.rules.add(md("HEAD-012", "Invalid Head Reference", "Invalid Reference", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header Reference must refer to a valid Header Definition.", OasInvalidHeaderReferenceRule.class));
        this.rules.add(md("LINK-005", "Invalid Link Reference", "Invalid Reference", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Link Reference must refer to a valid Link Definition.", OasInvalidLinkReferenceRule.class));
        this.rules.add(md("LINK-003", "Invalid Link Operation Reference", "Invalid Reference", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Link \"Operation Reference\" must refer to a valid Operation Definition.", OasInvalidLinkOperationReferenceRule.class));
        this.rules.add(md("RB-003", "Invalid Request Body Reference", "Invalid Reference", "Request Body", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Request Body Reference must refer to a valid Request Body Definition.", OasInvalidRequestBodyReferenceRule.class));
        /** Mutual Exclusivity **/
        this.rules.add(md("AAM-009", "Message OneOf and Properties are Mutually Exclusive", "Mutual Exclusivity", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Message \"oneOf\" and \"${'property'}\" are mutually exclusive.", AaMessageOneOfMutualExclusivityRule.class));
        this.rules.add(md("AAMTRT-002", "Message Trait MessageId in 2.4-2.6", "Invalid Property Value", "Message Trait", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23}, true, "The 'messageId' property is only allowed for message traits in AsyncAPI 2.4-2.6.", AaMessageTraitMessageIdIn24To26Rule.class));
        this.rules.add(md("AAMTRT-004", "Message Trait No MessageId in 3.0", "Invalid Property Value", "Message Trait", new ModelType[]{ModelType.ASYNCAPI30}, true, "The 'messageId' property is not allowed for message traits in AsyncAPI 3.0.", AaMessageTraitNoMessageIdIn3xRule.class));
        this.rules.add(md("AAM-014", "Message MessageId Only in 2.4-2.6", "Invalid Property Value", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23}, true, "The 'messageId' property is only allowed for messages in AsyncAPI 2.4-2.6.", AaMessageMessageIdIn24To26Rule.class));
        this.rules.add(md("AAM-013", "Message No MessageId in 3.0", "Invalid Property Value", "Message", new ModelType[]{ModelType.ASYNCAPI30}, true, "The 'messageId' property is not allowed for messages in AsyncAPI 3.0.", AaMessageNoMessageIdIn3xRule.class));
        this.rules.add(md("PATH-002", "Body and Form Data Params are Mutually Exclusive", "Mutual Exclusivity", "Operation", new ModelType[]{ModelType.OPENAPI20}, true, "Operation may not have both Body and Form Data parameters.", OasBodyAndFormDataMutualExclusivityRule.class));
        this.rules.add(md("EX-004", "Example Value and External Value are Mutually Exclusive", "Mutual Exclusivity", "Example", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Example \"Value\" and \"External Value\" are mutually exclusive.", OasExampleValueMutualExclusivityRule.class));
        this.rules.add(md("HEAD-013", "Header Example and Examples are Mutually Exclusive", "Mutual Exclusivity", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header \"Example\" and \"Examples\" are mutually exclusive.", OasHeaderExamplesMutualExclusivityRule.class));
        this.rules.add(md("HEAD-014", "Header Schema and Content are Mutually Exclusive", "Mutual Exclusivity", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Header cannot have both a Schema and Content.", OasHeaderSchemaContentMutualExclusivityRule.class));
        this.rules.add(md("LINK-001", "Link Operation Ref and Operation ID are Mutually Exclusive", "Mutual Exclusivity", "Link", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Link Operation Reference and Operation ID cannot both be used.", OasLinkOperationRefMutualExclusivityRule.class));
        this.rules.add(md("MT-001", "Media Type Example and Examples are Mutually Exclusive", "Mutual Exclusivity", "Media Type", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Media Type \"Example\" and \"Examples\" are mutually exclusive.", OasMediaTypeExamplesMutualExclusivityRule.class));
        this.rules.add(md("PAR-030", "Parameter Schema and Content are Mutually Exclusive", "Mutual Exclusivity", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter cannot have both a Schema and Content.", OasParameterSchemaContentMutualExclusivityRule.class));
        this.rules.add(md("PAR-031", "Parameter Example and Examples are Mutually Exclusive", "Mutual Exclusivity", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter \"Example\" and \"Examples\" are mutually exclusive.", OasParameterExamplesMutualExclusivityRule.class));
        this.rules.add(md("EX-005", "Example DataValue and Value are Mutually Exclusive", "Mutual Exclusivity", "Example", new ModelType[]{ModelType.OPENAPI32}, true, "Example \"dataValue\" and \"value\" are mutually exclusive.", OasExampleDataValueMutualExclusivityRule.class));
        this.rules.add(md("EX-006", "Example SerializedValue and Value/ExternalValue are Mutually Exclusive", "Mutual Exclusivity", "Example", new ModelType[]{ModelType.OPENAPI32}, true, "Example \"serializedValue\" and \"${'otherProperty'}\" are mutually exclusive.", OasExampleSerializedValueMutualExclusivityRule.class));
        this.rules.add(md("XML-003", "XML nodeType and attribute/wrapped are Mutually Exclusive", "Mutual Exclusivity", "XML", new ModelType[]{ModelType.OPENAPI32}, true, "XML \"${'otherProperty'}\" must not be used when \"nodeType\" is present.", OasXmlNodeTypeMutualExclusivityRule.class));
        /** Invalid Property Value (OpenAPI 3.2) **/
        this.rules.add(md("PATH-003", "Invalid Additional Operations Key", "Invalid Property Value", "Path Item", new ModelType[]{ModelType.OPENAPI32}, true, "Additional operations key \"${'method'}\" conflicts with a fixed HTTP method field.", OasAdditionalOperationsInvalidKeyRule.class));
        this.rules.add(md("TAG-004", "Invalid Tag Parent Reference", "Invalid Property Value", "Tag", new ModelType[]{ModelType.OPENAPI32}, true, "Tag parent \"${'parent'}\" does not reference an existing tag name.", OasInvalidTagParentRule.class));
        this.rules.add(md("TAG-005", "Tag Parent Circular Reference", "Invalid Property Value", "Tag", new ModelType[]{ModelType.OPENAPI32}, true, "Tag \"${'tagName'}\" has a circular parent reference.", OasTagParentCircularRefRule.class));
        this.rules.add(md("XML-004", "Invalid XML nodeType Value", "Invalid Property Value", "XML", new ModelType[]{ModelType.OPENAPI32}, true, "XML nodeType \"${'nodeType'}\" is not valid (must be one of: element, attribute, text, cdata, none).", OasInvalidXmlNodeTypeRule.class));
        /** Required Property **/
        this.rules.add(md("R-001", "Missing OpenAPI Property", "Required Property", "API", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "API is missing the 'openapi' property.", OasMissingOpenApiPropertyRule.class));
        this.rules.add(md("R-002", "Missing API Information", "Required Property", "API", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "API is missing the 'info' property.", MissingApiInformationRule.class));
        this.rules.add(md("R-003", "Missing API Paths", "Required Property", "API", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "API is missing the 'paths' property.", OasMissingApiPathsRule.class));
        this.rules.add(md("AAD-001", "Missing AsyncAPI Property", "Required Property", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "API is missing the 'asyncapi' property.", AaMissingAsyncApiPropertyRule.class));
        this.rules.add(md("AAD-002", "Missing Info Property", "Required Property", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "API is missing the 'info' property.", MissingApiInformationRule.class));
        this.rules.add(md("AAD-003", "Missing Channels Property", "Required Property", "API", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "API is missing the 'channels' property.", AaMissingChannelsPropertyRule.class));
        this.rules.add(md("CHAN-001", "Missing Channel Address", "Required Property", "Channel", new ModelType[]{ModelType.ASYNCAPI30}, true, "Channel is missing the 'address' property.", AaMissingChannelAddressRule.class));
        this.rules.add(md("INF-001", "Missing API Title", "Required Property", "Info", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API is missing a title.", MissingApiTitleRule.class));
        this.rules.add(md("INF-002", "Missing API Version", "Required Property", "Info", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API is missing a version.", MissingApiVersionRule.class));
        this.rules.add(md("LIC-001", "Missing License Name", "Required Property", "License", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "License is missing a name.", MissingLicenseNameRule.class));
        this.rules.add(md("OP-007", "Missing Operation Responses", "Required Property", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Operation must have at least one response.", OasMissingOperationResponsesRule.class));
        this.rules.add(md("OP-008", "Missing Operation ID", "Required Property", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, false, "Operation is missing a operation id.", MissingOperationIdRule.class));
        this.rules.add(md("OP-014", "Missing Operation Summary", "Required Property", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, false, "Operation is missing a summary.", MissingOperationSummaryRule.class));
        this.rules.add(md("OP-015", "Missing Operation Description", "Required Property", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, false, "Operation is missing a description.", MissingOperationDescriptionRule.class));
        this.rules.add(md("OP-016", "Missing Operation Tags", "Required Property", "Operation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, false, "Operation must have at least one tag.", OasMissingOperationTagsRule.class));
        this.rules.add(md("AAO-001", "Missing Operation Action", "Required Property", "Operation", new ModelType[]{ModelType.ASYNCAPI30}, true, "Operation is missing a required 'action' property.", AaMissingOperationActionRule.class));
        this.rules.add(md("AAO-002", "Missing Operation Channel Reference", "Required Property", "Operation", new ModelType[]{ModelType.ASYNCAPI30}, true, "Operation is missing a required 'channel' reference.", AaMissingOperationChannelReferenceRule.class));
        this.rules.add(md("AAPARAM-001", "Missing Parameter Location (AsyncAPI 2.x)", "Required Property", "Parameter", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Parameter is missing the 'location' property.", AaMissingParameterLocationRule.class));
        this.rules.add(md("AAPARAM-002", "Missing Parameter Schema (AsyncAPI 2.x)", "Required Property", "Parameter", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Parameter is missing the 'schema' property.", AaMissingParameterSchemaRule.class));
        this.rules.add(md("ED-001", "Missing External Documentation URL", "Required Property", "External Documentation", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "External Documentation is missing a URL.", OasMissingExternalDocumentationUrlRule.class));
        this.rules.add(md("PAR-001", "Missing Parameter Name", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter is missing a name.", OasMissingParameterNameRule.class));
        this.rules.add(md("PAR-002", "Missing Parameter Location", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Parameter is missing a location (Query, Header, etc).", OasMissingParameterLocationRule.class));
        this.rules.add(md("PAR-003", "Path Parameters Must Be Required", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Path Parameter \"${'name'}\" must be marked as required.", OasPathParamsMustBeRequiredRule.class));
        this.rules.add(md("PAR-004", "Missing Body Parameter Schema", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Body Parameters must have a schema defined.", OasMissingBodyParameterSchemaRule.class));
        this.rules.add(md("PAR-005", "Missing Parameter Type", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter '${'name'}' is missing a type.", OasMissingParameterTypeRule.class));
        this.rules.add(md("PAR-006", "Missing Parameter Array Type", "Required Property", "Parameter", new ModelType[]{ModelType.OPENAPI20}, true, "Parameter '${'name'}' marked as array but no array type provided.", OasMissingParameterArrayTypeRule.class));
        this.rules.add(md("IT-001", "Missing Items Type", "Required Property", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Type information is missing for array items.", OasMissingItemsTypeRule.class));
        this.rules.add(md("IT-002", "Missing Items Array Information", "Required Property", "Items", new ModelType[]{ModelType.OPENAPI20}, true, "Type information missing for array items.", OasMissingItemsArrayInformationRule.class));
        this.rules.add(md("RES-001", "Missing Response Description", "Required Property", "Response", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Response (code ${'statusCode'}) is missing a description.", OasMissingResponseDescriptionRule.class));
        this.rules.add(md("RDEF-002", "Missing Response Definition Description", "Required Property", "Response Definition", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Response Definition '${'name'}' is missing a description.", OasMissingResponseDefinitionDescriptionRule.class));
        this.rules.add(md("HEAD-001", "Missing Header Type", "Required Property", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header is missing type information.", OasMissingHeaderTypeRule.class));
        this.rules.add(md("HEAD-002", "Missing Header Array Information", "Required Property", "Header", new ModelType[]{ModelType.OPENAPI20}, true, "Header is missing array type information.", OasMissingHeaderArrayInformationRule.class));
        this.rules.add(md("SCH-005", "Missing Schema Array Information", "Required Property", "Schema", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema is missing array type information.", OasMissingSchemaArrayInformationRule.class));
        this.rules.add(md("TAG-001", "Missing Tag Name", "Required Property", "Tag", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Tag is missing a name.", MissingTagNameRule.class));
        this.rules.add(md("SS-001", "Missing Security Scheme Type", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Security Scheme is missing a type.", MissingSecuritySchemeTypeRule.class));
        this.rules.add(md("SS-002", "Missing API-Key Scheme Parameter Name", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "API Key Security Scheme is missing a parameter name (e.g. name of a header or query param).", MissingApiKeySchemeParamNameRule.class));
        this.rules.add(md("SS-003", "Missing API-Key Scheme Parameter Location", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20, ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "API Key Security Scheme must describe where the Key can be found (e.g. header, query param, etc).", MissingApiKeySchemeParamLocationRule.class));
        this.rules.add(md("SS-004", "Missing OAuth Scheme Flow Type", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "OAuth Security Scheme is missing a flow type.", OasMissingOAuthSchemeFlowTypeRule.class));
        this.rules.add(md("SS-005", "Missing OAuth Scheme Auth URL", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "OAuth Security Scheme is missing an Authorization URL.", OasMissingOAuthSchemeAuthUrlRule.class));
        this.rules.add(md("SS-006", "Missing OAuth Scheme Token URL", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "OAuth Security Scheme is missing a Token URL.", OasMissingOAuthSchemeTokenUrlRule.class));
        this.rules.add(md("SS-007", "Missing OAuth Scheme Scopes", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI20}, true, "OAuth Security Scheme is missing defined scopes.", OasMissingOAuthSchemeScopesRule.class));
        this.rules.add(md("DISC-001", "Missing a Discriminator Property Name", "Required Property", "Discriminator", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Discriminator must indicate a property (by name).", OasMissingDiscriminatorPropertyNameRule.class));
        this.rules.add(md("FLOW-006", "Missing OAuth Flow Scopes", "Required Property", "OAuth Flow", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "OAuth Flow is missing defined scopes.", MissingOAuthFlowScopesRule.class));
        this.rules.add(md("FLOW-001", "Missing OAuth Flow Authorization URL", "Required Property", "OAuth Flow", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "${'flowType'} OAuth Flow is missing an Authorization URL.", MissingOAuthFlowAuthUrlRule.class));
        this.rules.add(md("FLOW-002", "Missing OAuth Flow Token URL", "Required Property", "OAuth Flow", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "${'flowType'} OAuth Flow is missing a Token URL.", MissingOAuthFlowRokenUrlRule.class));
        this.rules.add(md("FLOW-007", "Missing OAuth Flow Device Authorization URL", "Required Property", "OAuth Flow", new ModelType[]{ModelType.OPENAPI32}, true, "Device Authorization OAuth Flow is missing a Device Authorization URL.", OasMissingOAuthFlowDeviceAuthUrlRule.class));
        this.rules.add(md("RB-002", "Missing Request Body Content", "Required Property", "Request Body", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Request Body content is missing.", OasMissingRequestBodyContentRule.class));
        this.rules.add(md("SRV-001", "Missing Server Template URL", "Required Property", "Server", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Server is missing a template URL.", MissingServerTemplateUrlRule.class));
        this.rules.add(md("SRV-004", "Missing Server Protocol", "Required Property", "Server", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Server is missing a protocol.", AasMissingServerProtocolRule.class));
        this.rules.add(md("SRV-005", "Missing Server Host", "Required Property", "Server", new ModelType[]{ModelType.ASYNCAPI30}, true, "Server is missing a host.", AaMissingServerHostRule.class));
        this.rules.add(md("SS-019", "Missing HTTP Security Scheme Style", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "HTTP Security Scheme is missing a scheme (Basic, Digest, etc).", MissingHttpSecuritySchemeTypeRule.class));
        this.rules.add(md("SS-020", "Missing OAuth Security Scheme Flows", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "OAuth Security Scheme does not define any OAuth flows.", MissingOAuthSecuritySchemeFlowsRule.class));
        this.rules.add(md("SS-021", "Missing OID Connect Security Scheme Connect URL", "Required Property", "Security Scheme", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32, ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "OpenID Connect Security Scheme is missing a Connect URL.", MissingOpenIdConnectSecuritySchemeConnectUrlRule.class));
        this.rules.add(md("SVAR-001", "Missing Server Variable Default Value", "Required Property", "Server Variable", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Server Variable \"${'name'}\" is missing a default value.", OasMissingServerVarDefaultValueRule.class));
        this.rules.add(md("CID-001", "Missing Correlation ID Location", "Required Property", "Correlation ID", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26}, true, "Correlation ID is missing a Location", AaMissingCorrelationIdLocationRule.class));
        this.rules.add(md("AAM-001", "Missing Message Payload or Headers", "Required Property", "Message", new ModelType[]{ModelType.ASYNCAPI20, ModelType.ASYNCAPI21, ModelType.ASYNCAPI22, ModelType.ASYNCAPI23, ModelType.ASYNCAPI24, ModelType.ASYNCAPI25, ModelType.ASYNCAPI26, ModelType.ASYNCAPI30}, true, "Message should have either payload or headers defined.", AaMissingMessagePayloadOrHeadersRule.class));
        this.rules.add(md("AAMFS-002", "Missing Schema Definition", "Required Property", "Multi-Format Schema", new ModelType[]{ModelType.ASYNCAPI30}, true, "Multi-format schema must have a schema property defined.", AaMissingSchemaDefinitionRule.class));
        this.rules.add(md("AAMFS-003", "Invalid Multi-Format Schema Reference", "Invalid Reference", "Multi-Format Schema", new ModelType[]{ModelType.ASYNCAPI30}, true, "Multi-format schema $ref must point to valid schema.", AaInvalidMultiFormatSchemaReferenceRule.class));

        /** Ignored Property **/
        this.rules.add(md("HEAD-008", "Ignored Content-Type Header", "Ignored Property", "Header", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "The \"Content-Type\" header will be ignored.", OasIgnoredContentTypeHeaderRule.class));
        this.rules.add(md("PAR-021", "Ignored Header Parameter", "Ignored Property", "Parameter", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "The \"${'name'}\" header parameter will be ignored.", OasIgnoredHeaderParameterRule.class));
        /** Invalid Property Type **/
        this.rules.add(md("SCH-003", "Invalid Schema Type Value", "Invalid Property Type", "Schema", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema type value of \"${'type'}\" is not allowed.  Must be one of: [${'allowedTypes'}]", OasInvalidSchemaTypeValueRule.class));
        this.rules.add(md("SCH-004", "Invalid Schema Array Items", "Invalid Property Type", "Schema", new ModelType[]{ModelType.OPENAPI30, ModelType.OPENAPI31, ModelType.OPENAPI32}, true, "Schema items must be present only for schemas of type 'array'.", OasInvalidSchemaArrayItemsRule.class));

        /** OpenRPC Rules **/
        this.rules.add(md("ORPC-001", "Missing OpenRPC Property", "Required Property", "API", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API is missing the 'openrpc' property.", OrpcMissingOpenRpcPropertyRule.class));
        this.rules.add(md("ORPC-002", "Missing OpenRPC Info Property", "Required Property", "API", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API is missing the 'info' property.", MissingApiInformationRule.class));
        this.rules.add(md("ORPC-003", "Missing OpenRPC Methods Property", "Required Property", "API", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "API is missing the 'methods' property.", OrpcMissingDocumentMethodsRule.class));
        this.rules.add(md("ORPC-004", "Invalid OpenRPC Version Format", "Invalid Property Format", "API", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "OpenRPC version '${'version'}' is an incorrect format (must be major.minor.patch).", OrpcInvalidOpenRpcVersionFormatRule.class));
        this.rules.add(md("ORPC-005", "Missing OpenRPC Server Variable Default Value", "Required Property", "Server Variable", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Server Variable is missing a default value.", OrpcMissingServerVariableDefaultRule.class));
        this.rules.add(md("ORPC-006", "Missing Method Name", "Required Property", "Method", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Method is missing a name.", OrpcMissingMethodNameRule.class));
        this.rules.add(md("ORPC-007", "Missing Method Params", "Required Property", "Method", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Method is missing the 'params' property.", OrpcMissingMethodParamsRule.class));
        this.rules.add(md("ORPC-008", "Missing ContentDescriptor Name", "Required Property", "Content Descriptor", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Content Descriptor is missing a name.", OrpcMissingContentDescriptorNameRule.class));
        this.rules.add(md("ORPC-009", "Missing ContentDescriptor Schema", "Required Property", "Content Descriptor", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Content Descriptor is missing a schema.", OrpcMissingContentDescriptorSchemaRule.class));
        this.rules.add(md("ORPC-010", "Missing Error Code", "Required Property", "Error", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Error is missing a code.", OrpcMissingErrorCodeRule.class));
        this.rules.add(md("ORPC-011", "Missing Error Message", "Required Property", "Error", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Error is missing a message.", OrpcMissingErrorMessageRule.class));
        this.rules.add(md("ORPC-012", "Missing ExamplePairing Name", "Required Property", "Example Pairing", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Example Pairing is missing a name.", OrpcMissingExamplePairingNameRule.class));
        this.rules.add(md("ORPC-013", "Missing ExamplePairing Params", "Required Property", "Example Pairing", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Example Pairing is missing the 'params' property.", OrpcMissingExamplePairingParamsRule.class));
        this.rules.add(md("ORPC-014", "Missing Link Name", "Required Property", "Link", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Link is missing a name.", OrpcMissingLinkNameRule.class));
        this.rules.add(md("ORPC-015", "Invalid Method Param Structure", "Invalid Property Value", "Method", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Method paramStructure '${'paramStructure'}' is invalid. Must be one of: by-name, by-position, either.", OrpcInvalidMethodParamStructureRule.class));
        this.rules.add(md("ORPC-016", "Duplicate Method Name", "Uniqueness", "Method", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Duplicate method name '${'methodName'}' found (method names must be unique).", OrpcDuplicateMethodNameRule.class));
        this.rules.add(md("ORPC-017", "Invalid OpenRPC Schema Reference", "Invalid Reference", "Schema", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Schema Reference must refer to a valid Schema Definition.", OrpcInvalidSchemaReferenceRule.class));
        this.rules.add(md("ORPC-018", "Invalid ContentDescriptor Reference", "Invalid Reference", "Content Descriptor", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Content Descriptor Reference must refer to a valid Content Descriptor Definition.", OrpcInvalidContentDescriptorReferenceRule.class));
        this.rules.add(md("ORPC-019", "Invalid OpenRPC Example Reference", "Invalid Reference", "Example", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Example Reference must refer to a valid Example Definition.", OrpcInvalidExampleReferenceRule.class));
        this.rules.add(md("ORPC-020", "Invalid OpenRPC Link Reference", "Invalid Reference", "Link", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Link Reference must refer to a valid Link Definition.", OrpcInvalidLinkReferenceRule.class));
        this.rules.add(md("ORPC-021", "Invalid Error Reference", "Invalid Reference", "Error", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Error Reference must refer to a valid Error Definition.", OrpcInvalidErrorReferenceRule.class));
        this.rules.add(md("ORPC-022", "Invalid ExamplePairing Reference", "Invalid Reference", "Example Pairing", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Example Pairing Reference must refer to a valid Example Pairing Definition.", OrpcInvalidExamplePairingReferenceRule.class));
        this.rules.add(md("ORPC-023", "Invalid OpenRPC Tag Reference", "Invalid Reference", "Tag", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Tag Reference must refer to a valid Tag Definition.", OrpcInvalidTagReferenceRule.class));
        this.rules.add(md("ORPC-024", "Invalid Method Reference", "Invalid Reference", "Method", new ModelType[]{ModelType.OPENRPC13, ModelType.OPENRPC14}, true, "Method Reference must refer to a valid Method Definition.", OrpcInvalidMethodReferenceRule.class));

        this.validateRuleData();
    }

    /**
     * Verify that there are no duplicate codes in the set of rules.
     */
    private void validateRuleData() {
        Set<String> codes = new HashSet<>();
        Set<String> names = new HashSet<>();

        for (ValidationRuleMetaData rule : this.rules) {
            if (codes.contains(rule.code)) {
                throw new RuntimeException("Duplicate rule code found: " + rule.code);
            } else {
                codes.add(rule.code);
            }
            if (names.contains(rule.name)) {
                throw new RuntimeException("Duplicate rule name found: " + rule.name);
            } else {
                names.add(rule.name);
            }
        }
    }

    /**
     * Gets all of the registered rules.
     */
    public List<ValidationRuleMetaData> getAllRules() {
        return this.rules;
    }

    /**
     * Gets the actual rule instances (visitors) that should be applied to the given document.
     */
    public List<ValidationRule> getRulesFor(ModelType type) {
        List<ValidationRule> rval = new ArrayList<>();
        for (ValidationRuleMetaData rule : this.rules) {
            if (rule.appliesTo(type)) {
                ValidationRule rc = ValidationUtil.instantiate(rule);
                rval.add(rc);
            }
        }
        return rval;
    }


}
