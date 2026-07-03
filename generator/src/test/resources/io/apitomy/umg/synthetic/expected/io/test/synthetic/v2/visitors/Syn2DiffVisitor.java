package io.test.synthetic.v2.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.SchemaOrBoolean;
import io.test.synthetic.SynContact;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.SynOperation;
import io.test.synthetic.SynSchema;
import io.test.synthetic.v2.Syn2Contact;
import io.test.synthetic.v2.Syn2Document;
import io.test.synthetic.v2.Syn2Info;
import io.test.synthetic.v2.Syn2Item;
import io.test.synthetic.v2.Syn2Operation;
import io.test.synthetic.v2.Syn2PathItem;
import io.test.synthetic.v2.Syn2Paths;
import io.test.synthetic.v2.Syn2Schema;
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.PairingKey;
import java.util.List;
import java.util.Map;

public abstract class Syn2DiffVisitor<P extends PairingKey> {

	public boolean visitDocument(Syn2Document original, Syn2Document updated) {
		return true;
	}

	public void afterVisitDocument(Syn2Document original, Syn2Document updated) {
	}

	public void diffDocumentVersion(String original, String updated) {
	}

	public boolean diffDocumentInfo(SynInfo original, SynInfo updated) {
		return true;
	}

	public void afterDiffDocumentInfo(SynInfo original, SynInfo updated) {
	}

	public void diffDocumentItems(List<SynItem> original, List<SynItem> updated, CollectionDiff<P, SynItem> diff) {
	}

	public boolean visitDocumentItemsItem(SynItem original, SynItem updated) {
		return true;
	}

	public void afterVisitDocumentItemsItem(SynItem original, SynItem updated) {
	}

	public void diffDocumentTags(List<String> original, List<String> updated) {
	}

	public void diffDocumentMetadata(Map<String, String> original, Map<String, String> updated) {
	}

	public void diffDocumentWebhooks(Map<String, Syn2PathItem> original, Map<String, Syn2PathItem> updated,
			CollectionDiff<P, Syn2PathItem> diff) {
	}

	public boolean visitDocumentWebhook(Syn2PathItem original, Syn2PathItem updated) {
		return true;
	}

	public void afterVisitDocumentWebhook(Syn2PathItem original, Syn2PathItem updated) {
	}

	public boolean diffDocumentAdditionalSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
		return true;
	}

	public void afterDiffDocumentAdditionalSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public boolean visitInfo(Syn2Info original, Syn2Info updated) {
		return true;
	}

	public void afterVisitInfo(Syn2Info original, Syn2Info updated) {
	}

	public void diffInfoName(String original, String updated) {
	}

	public boolean diffInfoContact(SynContact original, SynContact updated) {
		return true;
	}

	public void afterDiffInfoContact(SynContact original, SynContact updated) {
	}

	public void diffInfoVersion(String original, String updated) {
	}

	public void diffInfoLicense(String original, String updated) {
	}

	public boolean visitContact(Syn2Contact original, Syn2Contact updated) {
		return true;
	}

	public void afterVisitContact(Syn2Contact original, Syn2Contact updated) {
	}

	public void diffContactName(String original, String updated) {
	}

	public void diffContactEmail(String original, String updated) {
	}

	public void diffContactUrl(String original, String updated) {
	}

	public boolean visitItem(Syn2Item original, Syn2Item updated) {
		return true;
	}

	public void afterVisitItem(Syn2Item original, Syn2Item updated) {
	}

	public void diffItem$ref(String original, String updated) {
	}

	public void diffItemDescription(String original, String updated) {
	}

	public void diffItemRequired(Boolean original, Boolean updated) {
	}

	public void diffItemOrder(Integer original, Integer updated) {
	}

	public void diffItemWeight(Number original, Number updated) {
	}

	public void diffItemExtra(JsonNode original, JsonNode updated) {
	}

	public void diffItemRaw(ObjectNode original, ObjectNode updated) {
	}

	public boolean diffItemSchema(SynSchema original, SynSchema updated) {
		return true;
	}

	public void afterDiffItemSchema(SynSchema original, SynSchema updated) {
	}

	public void diffItemExamples(List<JsonNode> original, List<JsonNode> updated) {
	}

	public boolean diffItemDefaultValue(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		return true;
	}

	public void afterDiffItemDefaultValue(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffItemTitle(String original, String updated) {
	}

	public void diffItemDeprecated(Boolean original, Boolean updated) {
	}

	public boolean visitSchema(Syn2Schema original, Syn2Schema updated) {
		return true;
	}

	public void afterVisitSchema(Syn2Schema original, Syn2Schema updated) {
	}

	public void diffSchema$ref(String original, String updated) {
	}

	public void diffSchemaType(String original, String updated) {
	}

	public boolean diffSchemaItems(BooleanSchemaSchemaListUnion original, BooleanSchemaSchemaListUnion updated) {
		return true;
	}

	public void afterDiffSchemaItems(BooleanSchemaSchemaListUnion original, BooleanSchemaSchemaListUnion updated) {
	}

	public void diffSchemaProperties(Map<String, BooleanSchemaUnion> original, Map<String, BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public boolean visitSchemaProperty(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		return true;
	}

	public void afterVisitSchemaProperty(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaAllOf(List<BooleanSchemaUnion> original, List<BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public boolean visitSchemaAllOfItem(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		return true;
	}

	public void afterVisitSchemaAllOfItem(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaDefinitions(Map<String, BooleanSchemaUnion> original, Map<String, BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public boolean visitSchemaDefinition(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		return true;
	}

	public void afterVisitSchemaDefinition(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaNestedSchemas(Map<String, SchemaOrBoolean> original, Map<String, SchemaOrBoolean> updated,
			CollectionDiff<P, SchemaOrBoolean> diff) {
	}

	public boolean visitSchemaNestedSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
		return true;
	}

	public void afterVisitSchemaNestedSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaComposedSchemas(List<SchemaOrBoolean> original, List<SchemaOrBoolean> updated,
			CollectionDiff<P, SchemaOrBoolean> diff) {
	}

	public boolean visitSchemaComposedSchemasItem(SchemaOrBoolean original, SchemaOrBoolean updated) {
		return true;
	}

	public void afterVisitSchemaComposedSchemasItem(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaMinLength(Integer original, Integer updated) {
	}

	public void diffSchemaMaxLength(Integer original, Integer updated) {
	}

	public void diffSchemaEnum(List<JsonNode> original, List<JsonNode> updated) {
	}

	public boolean visitPaths(Syn2Paths original, Syn2Paths updated) {
		return true;
	}

	public void afterVisitPaths(Syn2Paths original, Syn2Paths updated) {
	}

	public boolean visitPathItem(Syn2PathItem original, Syn2PathItem updated) {
		return true;
	}

	public void afterVisitPathItem(Syn2PathItem original, Syn2PathItem updated) {
	}

	public void diffPathItem$ref(String original, String updated) {
	}

	public void diffPathItemSummary(String original, String updated) {
	}

	public boolean diffPathItemGet(SynOperation original, SynOperation updated) {
		return true;
	}

	public void afterDiffPathItemGet(SynOperation original, SynOperation updated) {
	}

	public boolean diffPathItemPut(SynOperation original, SynOperation updated) {
		return true;
	}

	public void afterDiffPathItemPut(SynOperation original, SynOperation updated) {
	}

	public boolean diffPathItemPost(SynOperation original, SynOperation updated) {
		return true;
	}

	public void afterDiffPathItemPost(SynOperation original, SynOperation updated) {
	}

	public boolean diffPathItemDelete(Syn2Operation original, Syn2Operation updated) {
		return true;
	}

	public void afterDiffPathItemDelete(Syn2Operation original, Syn2Operation updated) {
	}

	public boolean visitOperation(Syn2Operation original, Syn2Operation updated) {
		return true;
	}

	public void afterVisitOperation(Syn2Operation original, Syn2Operation updated) {
	}

	public void diffOperationOperationId(String original, String updated) {
	}

	public void diffOperationSummary(String original, String updated) {
	}

	public void diffOperationTags(List<String> original, List<String> updated) {
	}

	public void diffOperationParameters(List<SynItem> original, List<SynItem> updated,
			CollectionDiff<P, SynItem> diff) {
	}

	public boolean visitOperationParametersItem(SynItem original, SynItem updated) {
		return true;
	}

	public void afterVisitOperationParametersItem(SynItem original, SynItem updated) {
	}

	public void diffSchemaOrBoolean(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffBooleanSchemaSchemaListUnion(BooleanSchemaSchemaListUnion original,
			BooleanSchemaSchemaListUnion updated) {
	}

	public void diffBooleanSchemaUnion(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}
}