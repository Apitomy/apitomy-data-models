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
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.DiffVisitor;
import java.util.List;
import java.util.Map;

public abstract class Syn1DiffVisitor extends DiffVisitor {

	public boolean visitDocument(Syn1Document original, Syn1Document updated) {
		return true;
	}

	public void diffDocumentVersion(String original, String updated) {
	}

	public void diffDocumentInfo(SynInfo original, SynInfo updated) {
	}

	public void diffDocumentItems(CollectionDiff<Integer, SynItem> diff) {
	}

	public void visitDocumentItemsItem(Integer index, SynItem original, SynItem updated) {
	}

	public void diffDocumentTags(List<String> original, List<String> updated) {
	}

	public void diffDocumentMetadata(Map<String, String> original, Map<String, String> updated) {
	}

	public void diffDocumentAdditionalSchema(SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public boolean visitInfo(Syn1Info original, Syn1Info updated) {
		return true;
	}

	public void diffInfoName(String original, String updated) {
	}

	public void diffInfoContact(SynContact original, SynContact updated) {
	}

	public void diffInfoVersion(String original, String updated) {
	}

	public boolean visitContact(Syn1Contact original, Syn1Contact updated) {
		return true;
	}

	public void diffContactName(String original, String updated) {
	}

	public void diffContactEmail(String original, String updated) {
	}

	public void diffContactUrl(String original, String updated) {
	}

	public boolean visitItem(Syn1Item original, Syn1Item updated) {
		return true;
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

	public void diffItemExamples(List<JsonNode> original, List<JsonNode> updated) {
	}

	public void diffItemDefaultValue(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffItemTitle(String original, String updated) {
	}

	public boolean visitSchema(Syn1Schema original, Syn1Schema updated) {
		return true;
	}

	public void diffSchema$ref(String original, String updated) {
	}

	public void diffSchemaType(String original, String updated) {
	}

	public void diffSchemaItems(BooleanSchemaSchemaListUnion original, BooleanSchemaSchemaListUnion updated) {
	}

	public void diffSchemaProperties(CollectionDiff<String, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaProperties(String key, BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaAllOf(CollectionDiff<Integer, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaAllOfItem(Integer index, BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaDefinitions(CollectionDiff<String, BooleanSchemaUnion> diff) {
	}

	public void visitSchemaDefinitions(String key, BooleanSchemaUnion original, BooleanSchemaUnion updated) {
	}

	public void diffSchemaNestedSchemas(CollectionDiff<String, SchemaOrBoolean> diff) {
	}

	public void visitSchemaNestedSchemas(String key, SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaComposedSchemas(CollectionDiff<Integer, SchemaOrBoolean> diff) {
	}

	public void visitSchemaComposedSchemasItem(Integer index, SchemaOrBoolean original, SchemaOrBoolean updated) {
	}

	public void diffSchemaMinLength(Integer original, Integer updated) {
	}

	public void diffSchemaMaxLength(Integer original, Integer updated) {
	}

	public void diffSchemaEnum(List<JsonNode> original, List<JsonNode> updated) {
	}

	public boolean visitPaths(Syn1Paths original, Syn1Paths updated) {
		return true;
	}

	public boolean visitPathItem(Syn1PathItem original, Syn1PathItem updated) {
		return true;
	}

	public void diffPathItem$ref(String original, String updated) {
	}

	public void diffPathItemSummary(String original, String updated) {
	}

	public void diffPathItemGet(SynOperation original, SynOperation updated) {
	}

	public void diffPathItemPut(SynOperation original, SynOperation updated) {
	}

	public void diffPathItemPost(SynOperation original, SynOperation updated) {
	}

	public boolean visitOperation(Syn1Operation original, Syn1Operation updated) {
		return true;
	}

	public void diffOperationOperationId(String original, String updated) {
	}

	public void diffOperationSummary(String original, String updated) {
	}

	public void diffOperationTags(List<String> original, List<String> updated) {
	}

	public void diffOperationParameters(CollectionDiff<Integer, SynItem> diff) {
	}

	public void visitOperationParametersItem(Integer index, SynItem original, SynItem updated) {
	}
}