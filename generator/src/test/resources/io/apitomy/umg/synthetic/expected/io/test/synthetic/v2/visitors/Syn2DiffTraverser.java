package io.test.synthetic.v2.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.test.synthetic.Any;
import io.test.synthetic.BooleanSchemaSchemaListUnion;
import io.test.synthetic.BooleanSchemaUnion;
import io.test.synthetic.Node;
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
import io.test.synthetic.visitors.diff.AbstractDiffTraverser;
import io.test.synthetic.visitors.diff.CollectionDiff;
import io.test.synthetic.visitors.diff.PairingStrategyProvider;
import java.util.List;
import java.util.Map;

public class Syn2DiffTraverser<P> extends AbstractDiffTraverser<P, Syn2DiffVisitor<P>> {

	public Syn2DiffTraverser(Syn2DiffVisitor<P> visitor) {
		super(visitor);
	}

	public Syn2DiffTraverser(Syn2DiffVisitor visitor, PairingStrategyProvider<P> pairingProvider) {
		super(visitor, pairingProvider);
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
		} else if (target instanceof Syn2Document) {
			this.traverseDocument((Syn2Document) original, (Syn2Document) updated);
		} else if (target instanceof Syn2Info) {
			this.traverseInfo((Syn2Info) original, (Syn2Info) updated);
		} else if (target instanceof Syn2Contact) {
			this.traverseContact((Syn2Contact) original, (Syn2Contact) updated);
		} else if (target instanceof Syn2Item) {
			this.traverseItem((Syn2Item) original, (Syn2Item) updated);
		} else if (target instanceof Syn2Schema) {
			this.traverseSchema((Syn2Schema) original, (Syn2Schema) updated);
		} else if (target instanceof Syn2Paths) {
			this.traversePaths((Syn2Paths) original, (Syn2Paths) updated);
		} else if (target instanceof Syn2PathItem) {
			this.traversePathItem((Syn2PathItem) original, (Syn2PathItem) updated);
		} else if (target instanceof Syn2Operation) {
			this.traverseOperation((Syn2Operation) original, (Syn2Operation) updated);
		}
	}

	public void traverseDocument(Syn2Document original, Syn2Document updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitDocument(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffDocumentVersion(original.getVersion(), updated.getVersion());
		visitor.diffDocumentInfo(original.getInfo(), updated.getInfo());
		if (original.getInfo() != null && updated.getInfo() != null) {
			traverse(original.getInfo(), updated.getInfo());
		}
		{
			CollectionDiff<P, SynItem> diff = this.pairList("items", original.getItems(), updated.getItems());
			visitor.diffDocumentItems(original.getItems(), updated.getItems(), diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				visitor.visitDocumentItemsItem(pair.getOriginal(), pair.getUpdated());
				if (pair.getOriginal() != null && pair.getUpdated() != null) {
					traverse(pair.getOriginal(), pair.getUpdated());
				}
			}
		}
		visitor.diffDocumentTags(original.getTags(), updated.getTags());
		visitor.diffDocumentMetadata(original.getMetadata(), updated.getMetadata());
		{
			CollectionDiff<P, Syn2PathItem> diff = this.pairMap("webhooks", original.getWebhooks(),
					updated.getWebhooks());
			visitor.diffDocumentWebhooks(original.getWebhooks(), updated.getWebhooks(), diff);
			for (CollectionDiff.MatchedPair<P, Syn2PathItem> pair : diff.getMatched()) {
				visitor.visitDocumentWebhook(pair.getOriginal(), pair.getUpdated());
				if (pair.getOriginal() != null && pair.getUpdated() != null) {
					traverse(pair.getOriginal(), pair.getUpdated());
				}
			}
		}
		visitor.diffDocumentAdditionalSchema(original.getAdditionalSchema(), updated.getAdditionalSchema());
		this.traverseSchemaOrBoolean(original.getAdditionalSchema(), updated.getAdditionalSchema());
	}

	public void traverseInfo(Syn2Info original, Syn2Info updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitInfo(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffInfoName(original.getName(), updated.getName());
		visitor.diffInfoContact(original.getContact(), updated.getContact());
		if (original.getContact() != null && updated.getContact() != null) {
			traverse(original.getContact(), updated.getContact());
		}
		visitor.diffInfoVersion(original.getVersion(), updated.getVersion());
		visitor.diffInfoLicense(original.getLicense(), updated.getLicense());
	}

	public void traverseContact(Syn2Contact original, Syn2Contact updated) {
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

	public void traverseItem(Syn2Item original, Syn2Item updated) {
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
			traverse(original.getSchema(), updated.getSchema());
		}
		visitor.diffItemExamples(original.getExamples(), updated.getExamples());
		visitor.diffItemDefaultValue(original.getDefaultValue(), updated.getDefaultValue());
		this.traverseBooleanSchemaUnion(original.getDefaultValue(), updated.getDefaultValue());
		visitor.diffItemTitle(original.getTitle(), updated.getTitle());
		visitor.diffItemDeprecated(original.isDeprecated(), updated.isDeprecated());
	}

	public void traverseSchema(Syn2Schema original, Syn2Schema updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitSchema(original, updated))
			return;
		if (original == null || updated == null)
			return;
		visitor.diffSchema$ref(original.get$ref(), updated.get$ref());
		visitor.diffSchemaType(original.getType(), updated.getType());
		visitor.diffSchemaItems(original.getItems(), updated.getItems());
		this.traverseBooleanSchemaSchemaListUnion(original.getItems(), updated.getItems());
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("properties", original.getProperties(),
					updated.getProperties());
			visitor.diffSchemaProperties(original.getProperties(), updated.getProperties(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaProperty(pair.getOriginal(), pair.getUpdated());
				this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairList("allOf", original.getAllOf(),
					updated.getAllOf());
			visitor.diffSchemaAllOf(original.getAllOf(), updated.getAllOf(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaAllOfItem(pair.getOriginal(), pair.getUpdated());
				this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, BooleanSchemaUnion> diff = this.pairMap("definitions", original.getDefinitions(),
					updated.getDefinitions());
			visitor.diffSchemaDefinitions(original.getDefinitions(), updated.getDefinitions(), diff);
			for (CollectionDiff.MatchedPair<P, BooleanSchemaUnion> pair : diff.getMatched()) {
				visitor.visitSchemaDefinition(pair.getOriginal(), pair.getUpdated());
				this.traverseBooleanSchemaUnion(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairMap("nestedSchemas", original.getNestedSchemas(),
					updated.getNestedSchemas());
			visitor.diffSchemaNestedSchemas(original.getNestedSchemas(), updated.getNestedSchemas(), diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				visitor.visitSchemaNestedSchema(pair.getOriginal(), pair.getUpdated());
				this.traverseSchemaOrBoolean(pair.getOriginal(), pair.getUpdated());
			}
		}
		{
			CollectionDiff<P, SchemaOrBoolean> diff = this.pairList("composedSchemas", original.getComposedSchemas(),
					updated.getComposedSchemas());
			visitor.diffSchemaComposedSchemas(original.getComposedSchemas(), updated.getComposedSchemas(), diff);
			for (CollectionDiff.MatchedPair<P, SchemaOrBoolean> pair : diff.getMatched()) {
				visitor.visitSchemaComposedSchemasItem(pair.getOriginal(), pair.getUpdated());
				this.traverseSchemaOrBoolean(pair.getOriginal(), pair.getUpdated());
			}
		}
		visitor.diffSchemaMinLength(original.getMinLength(), updated.getMinLength());
		visitor.diffSchemaMaxLength(original.getMaxLength(), updated.getMaxLength());
		visitor.diffSchemaEnum(original.getEnum(), updated.getEnum());
	}

	public void traversePaths(Syn2Paths original, Syn2Paths updated) {
		if (original == null && updated == null)
			return;
		if (!visitor.visitPaths(original, updated))
			return;
		if (original == null || updated == null)
			return;
	}

	public void traversePathItem(Syn2PathItem original, Syn2PathItem updated) {
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
			traverse(original.getGet(), updated.getGet());
		}
		visitor.diffPathItemPut(original.getPut(), updated.getPut());
		if (original.getPut() != null && updated.getPut() != null) {
			traverse(original.getPut(), updated.getPut());
		}
		visitor.diffPathItemPost(original.getPost(), updated.getPost());
		if (original.getPost() != null && updated.getPost() != null) {
			traverse(original.getPost(), updated.getPost());
		}
		visitor.diffPathItemDelete(original.getDelete(), updated.getDelete());
		if (original.getDelete() != null && updated.getDelete() != null) {
			traverse(original.getDelete(), updated.getDelete());
		}
	}

	public void traverseOperation(Syn2Operation original, Syn2Operation updated) {
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
			visitor.diffOperationParameters(original.getParameters(), updated.getParameters(), diff);
			for (CollectionDiff.MatchedPair<P, SynItem> pair : diff.getMatched()) {
				visitor.visitOperationParametersItem(pair.getOriginal(), pair.getUpdated());
				if (pair.getOriginal() != null && pair.getUpdated() != null) {
					traverse(pair.getOriginal(), pair.getUpdated());
				}
			}
		}
	}

	public void traverseSchemaOrBoolean(SchemaOrBoolean original, SchemaOrBoolean updated) {
		if (original == null && updated == null)
			return;
		visitor.diffSchemaOrBoolean(original, updated);
		if (original instanceof Syn2Schema && updated instanceof Syn2Schema) {
			this.traverseSchema((Syn2Schema) original, (Syn2Schema) updated);
		}
	}

	public void traverseBooleanSchemaSchemaListUnion(BooleanSchemaSchemaListUnion original,
			BooleanSchemaSchemaListUnion updated) {
		if (original == null && updated == null)
			return;
		visitor.diffBooleanSchemaSchemaListUnion(original, updated);
		if (original instanceof Syn2Schema && updated instanceof Syn2Schema) {
			this.traverseSchema((Syn2Schema) original, (Syn2Schema) updated);
		}
	}

	public void traverseBooleanSchemaUnion(BooleanSchemaUnion original, BooleanSchemaUnion updated) {
		if (original == null && updated == null)
			return;
		visitor.diffBooleanSchemaUnion(original, updated);
		if (original instanceof Syn2Schema && updated instanceof Syn2Schema) {
			this.traverseSchema((Syn2Schema) original, (Syn2Schema) updated);
		}
	}
}