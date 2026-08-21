package io.test.synthetic.v1.visitors;

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
import io.test.synthetic.v1.Syn1Contact;
import io.test.synthetic.v1.Syn1Document;
import io.test.synthetic.v1.Syn1Info;
import io.test.synthetic.v1.Syn1Item;
import io.test.synthetic.v1.Syn1Operation;
import io.test.synthetic.v1.Syn1PathItem;
import io.test.synthetic.v1.Syn1Paths;
import io.test.synthetic.v1.Syn1Schema;
import io.test.synthetic.visitors.TraversalContext;
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.PairingKey;
import java.util.List;
import java.util.Map;

public abstract class Syn1DiffVisitor<P extends PairingKey> {

	protected TraversalContext traversalContext;

	public void setTraversalContext(TraversalContext context) {
		this.traversalContext = context;
	}

	public void visitDocument(Syn1Document original, Syn1Document updated) {
	}

	public void afterVisitDocument(Syn1Document original, Syn1Document updated) {
	}

	public void diffDocumentVersion(String original, String updated) {
	}

	public void diffDocumentInfo(SynInfo original, SynInfo updated) {
	}

	public void afterDiffDocumentInfo(SynInfo original, SynInfo updated) {
	}

	public void diffDocumentItems(List<SynItem> original, List<SynItem> updated, CollectionDiff<P, SynItem> diff) {
	}

	public void visitDocumentItemsItem(SynItem original, SynItem updated) {
	}

	public void afterVisitDocumentItemsItem(SynItem original, SynItem updated) {
	}

	public void diffDocumentTags(List<String> original, List<String> updated) {
	}

	public void diffDocumentMetadata(Map<String, String> original, Map<String, String> updated) {
	}

	public void diffDocumentAdditionalSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void afterDiffDocumentAdditionalSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void visitInfo(Syn1Info original, Syn1Info updated) {
	}

	public void afterVisitInfo(Syn1Info original, Syn1Info updated) {
	}

	public void diffInfoName(String original, String updated) {
	}

	public void diffInfoContact(SynContact original, SynContact updated) {
	}

	public void afterDiffInfoContact(SynContact original, SynContact updated) {
	}

	public void diffInfoVersion(String original, String updated) {
	}

	public void visitContact(Syn1Contact original, Syn1Contact updated) {
	}

	public void afterVisitContact(Syn1Contact original, Syn1Contact updated) {
	}

	public void diffContactName(String original, String updated) {
	}

	public void diffContactEmail(String original, String updated) {
	}

	public void diffContactUrl(String original, String updated) {
	}

	public void visitItem(Syn1Item original, Syn1Item updated) {
	}

	public void afterVisitItem(Syn1Item original, Syn1Item updated) {
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

	public void diffItemSchema(SynSchema original, SynSchema updated) {
	}

	public void afterDiffItemSchema(SynSchema original, SynSchema updated) {
	}

	public void diffItemExamples(List<JsonNode> original, List<JsonNode> updated) {
	}

	public void diffItemDefaultValue(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void afterDiffItemDefaultValue(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffItemTitle(String original, String updated) {
	}

	public void visitSchema(Syn1Schema original, Syn1Schema updated) {
	}

	public void afterVisitSchema(Syn1Schema original, Syn1Schema updated) {
	}

	public void diffSchema$ref(String original, String updated) {
	}

	public void diffSchemaType(String original, String updated) {
	}

	public void diffSchemaItems(BooleanSchemaSchemaListUnion original, BooleanSchemaSchemaListUnion updated) {
	}

	public void afterDiffSchemaItems(BooleanSchemaSchemaListUnion original, BooleanSchemaSchemaListUnion updated) {
	}

	public void diffSchemaProperties(Map<String, BooleanSchemaUnion> original, Map<String, BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaProperty(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void afterVisitSchemaProperty(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaAllOf(List<BooleanSchemaUnion> original, List<BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaAllOfItem(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void afterVisitSchemaAllOfItem(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaDefinitions(Map<String, BooleanSchemaUnion> original, Map<String, BooleanSchemaUnion> updated,
			CollectionDiff<P, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaDefinition(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void afterVisitSchemaDefinition(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaNestedSchemas(Map<String, SchemaOrBoolean> original, Map<String, SchemaOrBoolean> updated,
			CollectionDiff<P, SchemaOrBoolean> diff) {
	}

	public void visitSchemaNestedSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void afterVisitSchemaNestedSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaComposedSchemas(List<SchemaOrBoolean> original, List<SchemaOrBoolean> updated,
			CollectionDiff<P, SchemaOrBoolean> diff) {
	}

	public void visitSchemaComposedSchemasItem(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void afterVisitSchemaComposedSchemasItem(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaMinLength(Integer original, Integer updated) {
	}

	public void diffSchemaMaxLength(Integer original, Integer updated) {
	}

	public void diffSchemaEnum(List<JsonNode> original, List<JsonNode> updated) {
	}

	public void visitPaths(Syn1Paths original, Syn1Paths updated) {
	}

	public void afterVisitPaths(Syn1Paths original, Syn1Paths updated) {
	}

	public void visitPathItem(Syn1PathItem original, Syn1PathItem updated) {
	}

	public void afterVisitPathItem(Syn1PathItem original, Syn1PathItem updated) {
	}

	public void diffPathItem$ref(String original, String updated) {
	}

	public void diffPathItemSummary(String original, String updated) {
	}

	public void diffPathItemGet(SynOperation original, SynOperation updated) {
	}

	public void afterDiffPathItemGet(SynOperation original, SynOperation updated) {
	}

	public void diffPathItemPut(SynOperation original, SynOperation updated) {
	}

	public void afterDiffPathItemPut(SynOperation original, SynOperation updated) {
	}

	public void diffPathItemPost(SynOperation original, SynOperation updated) {
	}

	public void afterDiffPathItemPost(SynOperation original, SynOperation updated) {
	}

	public void visitOperation(Syn1Operation original, Syn1Operation updated) {
	}

	public void afterVisitOperation(Syn1Operation original, Syn1Operation updated) {
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

	public void visitOperationParametersItem(SynItem original, SynItem updated) {
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