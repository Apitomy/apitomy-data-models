package io.test.synthetic.v1.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.Any;
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
import io.test.synthetic.visitors.TraversalAction;
import io.test.synthetic.visitors.diff.AbstractDiffTraverser;
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.PairingKey;
import io.test.synthetic.visitors.diff.PairingStrategyProvider;
import java.util.List;
import java.util.Map;

public class Syn1DiffTraverser<P extends PairingKey> extends AbstractDiffTraverser<P, Syn1DiffVisitor<P>> {

	public Syn1DiffTraverser(Syn1DiffVisitor<P> visitor) {
		super(visitor);
		visitor.setTraversalContext(this.originalContext);
	}

	public Syn1DiffTraverser(Syn1DiffVisitor visitor, PairingStrategyProvider<P> pairingProvider) {
		super(visitor, pairingProvider);
		visitor.setTraversalContext(this.originalContext);
	}

	@Override
	public void traverse(Any original, Any updated) {
		Any target = original != null ? original : updated;
		if (target == null)
			return;
		if (target instanceof SchemaOrBoolean) {
			this.traverseSchemaOrBoolean((SchemaOrBoolean) original, (SchemaOrBoolean) updated);
		} else if (target instanceof BooleanSchemaSchemaListUnion) {
			this.traverseBooleanSchemaSchemaListUnion((BooleanSchemaSchemaListUnion) original,
					(BooleanSchemaSchemaListUnion) updated);
		} else if (target instanceof BooleanSchemaUnion) {
			this.traverseBooleanSchemaUnion((BooleanSchemaUnion) original, (BooleanSchemaUnion) updated);
		} else if (target instanceof Syn1Document) {
			this.traverseDocument((Syn1Document) original, (Syn1Document) updated);
		} else if (target instanceof Syn1Info) {
			this.traverseInfo((Syn1Info) original, (Syn1Info) updated);
		} else if (target instanceof Syn1Contact) {
			this.traverseContact((Syn1Contact) original, (Syn1Contact) updated);
		} else if (target instanceof Syn1Item) {
			this.traverseItem((Syn1Item) original, (Syn1Item) updated);
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
		this.originalContext.resetAction();
		visitor.visitDocument(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitDocument(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("version");
			visitor.diffDocumentVersion(original.getVersion(), updated.getVersion());
			pop();
		}
		{
			pushProperty("info");
			this.originalContext.resetAction();
			visitor.diffDocumentInfo(original.getInfo(), updated.getInfo());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getInfo() != null && updated.getInfo() != null) {
					traverse(original.getInfo(), updated.getInfo());
				}
			}
			visitor.afterDiffDocumentInfo(original.getInfo(), updated.getInfo());
			pop();
		}
		{
			pushProperty("items");
			CollectionDiff<P, SynItem> diff = this.pairList("items", original.getItems(), updated.getItems());
			visitor.diffDocumentItems(original.getItems(), updated.getItems(), diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				pushListIndex(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitDocumentItemsItem(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					if (pair.getOriginal() != null && pair.getUpdated() != null) {
						traverse(pair.getOriginal(), pair.getUpdated());
					}
				}
				visitor.afterVisitDocumentItemsItem(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("tags");
			visitor.diffDocumentTags(original.getTags(), updated.getTags());
			pop();
		}
		{
			pushProperty("metadata");
			visitor.diffDocumentMetadata(original.getMetadata(), updated.getMetadata());
			pop();
		}
		{
			pushProperty("additionalSchema");
			this.originalContext.resetAction();
			visitor.diffDocumentAdditionalSchema(original.getAdditionalSchema(), updated.getAdditionalSchema());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				this.traverseSchemaOrBoolean(original.getAdditionalSchema(), updated.getAdditionalSchema());
			}
			visitor.afterDiffDocumentAdditionalSchema(original.getAdditionalSchema(), updated.getAdditionalSchema());
			pop();
		}
		visitor.afterVisitDocument(original, updated);
	}

	public void traverseInfo(Syn1Info original, Syn1Info updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitInfo(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitInfo(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("name");
			visitor.diffInfoName(original.getName(), updated.getName());
			pop();
		}
		{
			pushProperty("contact");
			this.originalContext.resetAction();
			visitor.diffInfoContact(original.getContact(), updated.getContact());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getContact() != null && updated.getContact() != null) {
					traverse(original.getContact(), updated.getContact());
				}
			}
			visitor.afterDiffInfoContact(original.getContact(), updated.getContact());
			pop();
		}
		{
			pushProperty("version");
			visitor.diffInfoVersion(original.getVersion(), updated.getVersion());
			pop();
		}
		visitor.afterVisitInfo(original, updated);
	}

	public void traverseContact(Syn1Contact original, Syn1Contact updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitContact(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitContact(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("name");
			visitor.diffContactName(original.getName(), updated.getName());
			pop();
		}
		{
			pushProperty("email");
			visitor.diffContactEmail(original.getEmail(), updated.getEmail());
			pop();
		}
		{
			pushProperty("url");
			visitor.diffContactUrl(original.getUrl(), updated.getUrl());
			pop();
		}
		visitor.afterVisitContact(original, updated);
	}

	public void traverseItem(Syn1Item original, Syn1Item updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitItem(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitItem(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("$ref");
			visitor.diffItem$ref(original.get$ref(), updated.get$ref());
			pop();
		}
		{
			pushProperty("description");
			visitor.diffItemDescription(original.getDescription(), updated.getDescription());
			pop();
		}
		{
			pushProperty("required");
			visitor.diffItemRequired(original.isRequired(), updated.isRequired());
			pop();
		}
		{
			pushProperty("order");
			visitor.diffItemOrder(original.getOrder(), updated.getOrder());
			pop();
		}
		{
			pushProperty("weight");
			visitor.diffItemWeight(original.getWeight(), updated.getWeight());
			pop();
		}
		{
			pushProperty("extra");
			visitor.diffItemExtra(original.getExtra(), updated.getExtra());
			pop();
		}
		{
			pushProperty("raw");
			visitor.diffItemRaw(original.getRaw(), updated.getRaw());
			pop();
		}
		{
			pushProperty("schema");
			this.originalContext.resetAction();
			visitor.diffItemSchema(original.getSchema(), updated.getSchema());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getSchema() != null && updated.getSchema() != null) {
					traverse(original.getSchema(), updated.getSchema());
				}
			}
			visitor.afterDiffItemSchema(original.getSchema(), updated.getSchema());
			pop();
		}
		{
			pushProperty("examples");
			visitor.diffItemExamples(original.getExamples(), updated.getExamples());
			pop();
		}
		{
			pushProperty("defaultValue");
			this.originalContext.resetAction();
			visitor.diffItemDefaultValue(original.getDefaultValue(), updated.getDefaultValue());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				this.traverseBooleanSchemaUnion(original.getDefaultValue(), updated.getDefaultValue());
			}
			visitor.afterDiffItemDefaultValue(original.getDefaultValue(), updated.getDefaultValue());
			pop();
		}
		{
			pushProperty("title");
			visitor.diffItemTitle(original.getTitle(), updated.getTitle());
			pop();
		}
		visitor.afterVisitItem(original, updated);
	}

	public void traverseSchema(Syn1Schema original, Syn1Schema updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitSchema(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitSchema(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("$ref");
			visitor.diffSchema$ref(original.get$ref(), updated.get$ref());
			pop();
		}
		{
			pushProperty("type");
			visitor.diffSchemaType(original.getType(), updated.getType());
			pop();
		}
		{
			pushProperty("items");
			this.originalContext.resetAction();
			visitor.diffSchemaItems(original.getItems(), updated.getItems());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				this.traverseBooleanSchemaSchemaListUnion(original.getItems(), updated.getItems());
			}
			visitor.afterDiffSchemaItems(original.getItems(), updated.getItems());
			pop();
		}
		{
			pushProperty("properties");
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("properties", original.getProperties(),
					updated.getProperties());
			visitor.diffSchemaProperties(original.getProperties(), updated.getProperties(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				pushMapKey(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitSchemaProperty(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
				}
				visitor.afterVisitSchemaProperty(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("allOf");
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairList("allOf", original.getAllOf(),
					updated.getAllOf());
			visitor.diffSchemaAllOf(original.getAllOf(), updated.getAllOf(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				pushListIndex(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitSchemaAllOfItem(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
				}
				visitor.afterVisitSchemaAllOfItem(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("definitions");
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("definitions", original.getDefinitions(),
					updated.getDefinitions());
			visitor.diffSchemaDefinitions(original.getDefinitions(), updated.getDefinitions(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				pushMapKey(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitSchemaDefinition(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
				}
				visitor.afterVisitSchemaDefinition(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("nestedSchemas");
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairMap("nestedSchemas", original.getNestedSchemas(),
					updated.getNestedSchemas());
			visitor.diffSchemaNestedSchemas(original.getNestedSchemas(), updated.getNestedSchemas(), diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				pushMapKey(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitSchemaNestedSchema(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					this.traverseSchemaOrBoolean(pair.getOriginal(), pair.getUpdated());
				}
				visitor.afterVisitSchemaNestedSchema(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("composedSchemas");
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairList("composedSchemas", original.getComposedSchemas(),
					updated.getComposedSchemas());
			visitor.diffSchemaComposedSchemas(original.getComposedSchemas(), updated.getComposedSchemas(), diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				pushListIndex(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitSchemaComposedSchemasItem(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					this.traverseSchemaOrBoolean(pair.getOriginal(), pair.getUpdated());
				}
				visitor.afterVisitSchemaComposedSchemasItem(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		{
			pushProperty("minLength");
			visitor.diffSchemaMinLength(original.getMinLength(), updated.getMinLength());
			pop();
		}
		{
			pushProperty("maxLength");
			visitor.diffSchemaMaxLength(original.getMaxLength(), updated.getMaxLength());
			pop();
		}
		{
			pushProperty("enum");
			visitor.diffSchemaEnum(original.getEnum(), updated.getEnum());
			pop();
		}
		visitor.afterVisitSchema(original, updated);
	}

	public void traversePaths(Syn1Paths original, Syn1Paths updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitPaths(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitPaths(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		visitor.afterVisitPaths(original, updated);
	}

	public void traversePathItem(Syn1PathItem original, Syn1PathItem updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitPathItem(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitPathItem(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("$ref");
			visitor.diffPathItem$ref(original.get$ref(), updated.get$ref());
			pop();
		}
		{
			pushProperty("summary");
			visitor.diffPathItemSummary(original.getSummary(), updated.getSummary());
			pop();
		}
		{
			pushProperty("get");
			this.originalContext.resetAction();
			visitor.diffPathItemGet(original.getGet(), updated.getGet());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getGet() != null && updated.getGet() != null) {
					traverse(original.getGet(), updated.getGet());
				}
			}
			visitor.afterDiffPathItemGet(original.getGet(), updated.getGet());
			pop();
		}
		{
			pushProperty("put");
			this.originalContext.resetAction();
			visitor.diffPathItemPut(original.getPut(), updated.getPut());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getPut() != null && updated.getPut() != null) {
					traverse(original.getPut(), updated.getPut());
				}
			}
			visitor.afterDiffPathItemPut(original.getPut(), updated.getPut());
			pop();
		}
		{
			pushProperty("post");
			this.originalContext.resetAction();
			visitor.diffPathItemPost(original.getPost(), updated.getPost());
			if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
				if (original.getPost() != null && updated.getPost() != null) {
					traverse(original.getPost(), updated.getPost());
				}
			}
			visitor.afterDiffPathItemPost(original.getPost(), updated.getPost());
			pop();
		}
		visitor.afterVisitPathItem(original, updated);
	}

	public void traverseOperation(Syn1Operation original, Syn1Operation updated) {
		if (original == null && updated == null)
			return;
		this.originalContext.resetAction();
		visitor.visitOperation(original, updated);
		if (this.originalContext.consumeAction() == TraversalAction.SKIP) {
			visitor.afterVisitOperation(original, updated);
			return;
		}
		if (original == null || updated == null)
			return;
		{
			pushProperty("operationId");
			visitor.diffOperationOperationId(original.getOperationId(), updated.getOperationId());
			pop();
		}
		{
			pushProperty("summary");
			visitor.diffOperationSummary(original.getSummary(), updated.getSummary());
			pop();
		}
		{
			pushProperty("tags");
			visitor.diffOperationTags(original.getTags(), updated.getTags());
			pop();
		}
		{
			pushProperty("parameters");
			CollectionDiff<P, SynItem> diff = this.pairList("parameters", original.getParameters(),
					updated.getParameters());
			visitor.diffOperationParameters(original.getParameters(), updated.getParameters(), diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				pushListIndex(pair.getKey());
				this.originalContext.resetAction();
				visitor.visitOperationParametersItem(pair.getOriginal(), pair.getUpdated());
				if (this.originalContext.consumeAction() != TraversalAction.SKIP) {
					if (pair.getOriginal() != null && pair.getUpdated() != null) {
						traverse(pair.getOriginal(), pair.getUpdated());
					}
				}
				visitor.afterVisitOperationParametersItem(pair.getOriginal(), pair.getUpdated());
				pop();
			}
			pop();
		}
		visitor.afterVisitOperation(original, updated);
	}

	public void traverseSchemaOrBoolean(SchemaOrBoolean original, SchemaOrBoolean updated) {
		if (original == null && updated == null)
			return;
		visitor.diffSchemaOrBoolean(original, updated);
		if (original instanceof Syn1Schema && updated instanceof Syn1Schema) {
			this.traverseSchema((Syn1Schema) original, (Syn1Schema) updated);
		}
	}

	public void traverseBooleanSchemaSchemaListUnion(BooleanSchemaSchemaListUnion original,
			BooleanSchemaSchemaListUnion updated) {
		if (original == null && updated == null)
			return;
		visitor.diffBooleanSchemaSchemaListUnion(original, updated);
		if (original instanceof Syn1Schema && updated instanceof Syn1Schema) {
			this.traverseSchema((Syn1Schema) original, (Syn1Schema) updated);
		}
	}

	public void traverseBooleanSchemaUnion(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		if (original == null && updated == null)
			return;
		visitor.diffBooleanSchemaUnion(original, updated);
		if (original instanceof Syn1Schema && updated instanceof Syn1Schema) {
			this.traverseSchema((Syn1Schema) original, (Syn1Schema) updated);
		}
	}
}