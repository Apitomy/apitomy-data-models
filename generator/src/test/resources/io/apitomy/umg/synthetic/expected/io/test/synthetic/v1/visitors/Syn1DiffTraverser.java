package io.test.synthetic.v1.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.Node;
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
import io.test.synthetic.visitors.diff.AbstractDiffTraverser;
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.PairingStrategyProvider;
import java.util.List;
import java.util.Map;

public class Syn1DiffTraverser<P> extends AbstractDiffTraverser<P, Syn1DiffVisitor<P>> {

	public Syn1DiffTraverser(Syn1DiffVisitor<P> visitor) {
		super(visitor);
	}

	public Syn1DiffTraverser(Syn1DiffVisitor visitor, PairingStrategyProvider<P> pairingProvider) {
		super(visitor, pairingProvider);
	}

	@Override
	protected void traverseNode(Node original, Node updated) {
		Node target = original != null ? original : updated;
		if (target instanceof Syn1Document) {
			this.traverseDocument((Syn1Document) original, (Syn1Document) updated);
		} else if (target instanceof Syn1Info) {
			this.traverseInfo((Syn1Info) original, (Syn1Info) updated);
		} else if (target instanceof Syn1Contact) {
			this.traverseContact((Syn1Contact) original, (Syn1Contact) updated);
		} else if (target instanceof Syn1Item) {
			this.traverseItem((Syn1Item) original, (Syn1Item) updated);
		} else if (target instanceof Syn1Schema) {
			this.traverseSchema((Syn1Schema) original, (Syn1Schema) updated);
		} else if (target instanceof Syn1Paths) {
			this.traversePaths((Syn1Paths) original, (Syn1Paths) updated);
		} else if (target instanceof Syn1PathItem) {
			this.traversePathItem((Syn1PathItem) original, (Syn1PathItem) updated);
		} else if (target instanceof Syn1Operation) {
			this.traverseOperation((Syn1Operation) original, (Syn1Operation) updated);
		}
	}

	public void traverseDocument(Syn1Document original, Syn1Document updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitDocument(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffDocumentVersion(original.getVersion(), updated.getVersion());
		visitor.diffDocumentInfo(original.getInfo(), updated.getInfo());
		if (original.getInfo() != null && updated.getInfo() != null) {
			traverseNode(original.getInfo(), updated.getInfo());
		}
		{
			CollectionDiff<P, SynItem> diff = this.pairList("items", original.getItems(), updated.getItems());
			visitor.diffDocumentItems(diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				visitor.visitDocumentItemsItem(pair.getOriginal(), pair.getUpdated());
				if (pair.getOriginal() != null && pair.getUpdated() != null) {
					traverseNode(pair.getOriginal(), pair.getUpdated());
				}
			}
		}
		visitor.diffDocumentTags(original.getTags(), updated.getTags());
		visitor.diffDocumentMetadata(original.getMetadata(), updated.getMetadata());
		visitor.diffDocumentAdditionalSchema(original.getAdditionalSchema(), updated.getAdditionalSchema());
	}

	public void traverseInfo(Syn1Info original, Syn1Info updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitInfo(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffInfoName(original.getName(), updated.getName());
		visitor.diffInfoContact(original.getContact(), updated.getContact());
		if (original.getContact() != null && updated.getContact() != null) {
			traverseNode(original.getContact(), updated.getContact());
		}
		visitor.diffInfoVersion(original.getVersion(), updated.getVersion());
	}

	public void traverseContact(Syn1Contact original, Syn1Contact updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitContact(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffContactName(original.getName(), updated.getName());
		visitor.diffContactEmail(original.getEmail(), updated.getEmail());
		visitor.diffContactUrl(original.getUrl(), updated.getUrl());
	}

	public void traverseItem(Syn1Item original, Syn1Item updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitItem(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffItem$ref(original.get$ref(), updated.get$ref());
		visitor.diffItemDescription(original.getDescription(), updated.getDescription());
		visitor.diffItemRequired(original.isRequired(), updated.isRequired());
		visitor.diffItemOrder(original.getOrder(), updated.getOrder());
		visitor.diffItemWeight(original.getWeight(), updated.getWeight());
		visitor.diffItemExtra(original.getExtra(), updated.getExtra());
		visitor.diffItemRaw(original.getRaw(), updated.getRaw());
		visitor.diffItemSchema(original.getSchema(), updated.getSchema());
		if (original.getSchema() != null && updated.getSchema() != null) {
			traverseNode(original.getSchema(), updated.getSchema());
		}
		visitor.diffItemExamples(original.getExamples(), updated.getExamples());
		visitor.diffItemDefaultValue(original.getDefaultValue(), updated.getDefaultValue());
		visitor.diffItemTitle(original.getTitle(), updated.getTitle());
	}

	public void traverseSchema(Syn1Schema original, Syn1Schema updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitSchema(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffSchema$ref(original.get$ref(), updated.get$ref());
		visitor.diffSchemaType(original.getType(), updated.getType());
		visitor.diffSchemaItems(original.getItems(), updated.getItems());
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("properties", original.getProperties(),
					updated.getProperties());
			visitor.diffSchemaProperties(diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaProperties(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairList("allOf", original.getAllOf(),
					updated.getAllOf());
			visitor.diffSchemaAllOf(diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaAllOfItem(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("definitions", original.getDefinitions(),
					updated.getDefinitions());
			visitor.diffSchemaDefinitions(diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaDefinitions(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairMap("nestedSchemas", original.getNestedSchemas(),
					updated.getNestedSchemas());
			visitor.diffSchemaNestedSchemas(diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				visitor.visitSchemaNestedSchemas(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairList("composedSchemas", original.getComposedSchemas(),
					updated.getComposedSchemas());
			visitor.diffSchemaComposedSchemas(diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				visitor.visitSchemaComposedSchemasItem(pair.getOriginal(), pair.getUpdated());
			}
		}
		visitor.diffSchemaMinLength(original.getMinLength(), updated.getMinLength());
		visitor.diffSchemaMaxLength(original.getMaxLength(), updated.getMaxLength());
		visitor.diffSchemaEnum(original.getEnum(), updated.getEnum());
	}

	public void traversePaths(Syn1Paths original, Syn1Paths updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitPaths(original, updated))
			return;
		if (original == null || updated == null)
			return;
	}

	public void traversePathItem(Syn1PathItem original, Syn1PathItem updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitPathItem(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffPathItem$ref(original.get$ref(), updated.get$ref());
		visitor.diffPathItemSummary(original.getSummary(), updated.getSummary());
		visitor.diffPathItemGet(original.getGet(), updated.getGet());
		if (original.getGet() != null && updated.getGet() != null) {
			traverseNode(original.getGet(), updated.getGet());
		}
		visitor.diffPathItemPut(original.getPut(), updated.getPut());
		if (original.getPut() != null && updated.getPut() != null) {
			traverseNode(original.getPut(), updated.getPut());
		}
		visitor.diffPathItemPost(original.getPost(), updated.getPost());
		if (original.getPost() != null && updated.getPost() != null) {
			traverseNode(original.getPost(), updated.getPost());
		}
	}

	public void traverseOperation(Syn1Operation original, Syn1Operation updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitOperation(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffOperationOperationId(original.getOperationId(), updated.getOperationId());
		visitor.diffOperationSummary(original.getSummary(), updated.getSummary());
		visitor.diffOperationTags(original.getTags(), updated.getTags());
		{
			CollectionDiff<P, SynItem> diff = this.pairList("parameters", original.getParameters(),
					updated.getParameters());
			visitor.diffOperationParameters(diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				visitor.visitOperationParametersItem(pair.getOriginal(), pair.getUpdated());
				if (pair.getOriginal() != null && pair.getUpdated() != null) {
					traverseNode(pair.getOriginal(), pair.getUpdated());
				}
			}
		}
	}
}