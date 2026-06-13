package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.union.SchemaOrBoolean;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn2DocumentImpl extends NodeImpl implements Syn2Document {

	private String version;
	private SynInfo info;
	private List<SynItem> items;
	private List<String> tags;
	private Map<String, String> metadata;
	private Map<String, Syn2PathItem> webhooks;
	private SchemaOrBoolean additionalSchema;
	private Map<String, JsonNode> extensions;

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String value) {
		this.version = value;
	}

	@Override
	public SynInfo getInfo() {
		return info;
	}

	@Override
	public void setInfo(SynInfo value) {
		this.info = value;
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("info");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Syn2Info createInfo() {
		Syn2InfoImpl node = new Syn2InfoImpl();
		node.setParent(this);
		return node;
	}

	@Override
	public Syn2Item createItem() {
		Syn2ItemImpl node = new Syn2ItemImpl();
		node.setParent(this);
		return node;
	}

	@Override
	public List<SynItem> getItems() {
		return items;
	}

	@Override
	public void addItem(SynItem value) {
		if (this.items == null) {
			this.items = new ArrayList<>();
		}
		this.items.add(value);
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("items");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
		}
	}

	@Override
	public void clearItems() {
		if (this.items != null) {
			this.items.clear();
		}
	}

	@Override
	public void removeItem(SynItem value) {
		if (this.items != null) {
			this.items.remove(value);
		}
	}

	@Override
	public void insertItem(SynItem value, int atIndex) {
		if (this.items == null) {
			this.items = new ArrayList<>();
			this.items.add(value);
		} else {
			this.items = DataModelUtil.insertListEntry(this.items, value, atIndex);
		}
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("items");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.array);
		}
	}

	@Override
	public List<String> getTags() {
		return tags;
	}

	@Override
	public void setTags(List<String> value) {
		this.tags = value;
	}

	@Override
	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(Map<String, String> value) {
		this.metadata = value;
	}

	@Override
	public Syn2PathItem createPathItem() {
		Syn2PathItemImpl node = new Syn2PathItemImpl();
		node.setParent(this);
		return node;
	}

	@Override
	public Map<String, Syn2PathItem> getWebhooks() {
		return webhooks;
	}

	@Override
	public void addWebhook(String name, Syn2PathItem value) {
		if (this.webhooks == null) {
			this.webhooks = new LinkedHashMap<>();
		}
		this.webhooks.put(name, value);
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("webhooks");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
			((NodeImpl) value)._setMapPropertyName(name);
		}
	}

	@Override
	public void clearWebhooks() {
		if (this.webhooks != null) {
			this.webhooks.clear();
		}
	}

	@Override
	public void removeWebhook(String name) {
		if (this.webhooks != null) {
			this.webhooks.remove(name);
		}
	}

	@Override
	public void insertWebhook(String name, Syn2PathItem value, int atIndex) {
		if (this.webhooks == null) {
			this.webhooks = new LinkedHashMap<>();
			this.webhooks.put(name, value);
		} else {
			this.webhooks = DataModelUtil.insertMapEntry(this.webhooks, name, value, atIndex);
		}
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("webhooks");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.map);
			((NodeImpl) value)._setMapPropertyName(name);
		}
	}

	@Override
	public SchemaOrBoolean getAdditionalSchema() {
		return additionalSchema;
	}

	@Override
	public void setAdditionalSchema(SchemaOrBoolean value) {
		this.additionalSchema = value;
		if (value != null) {
			((NodeImpl) value)._setParentPropertyName("additionalSchema");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Map<String, JsonNode> getExtensions() {
		return extensions;
	}

	@Override
	public void addExtension(String name, JsonNode value) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
		}
		this.extensions.put(name, value);
	}

	@Override
	public void clearExtensions() {
		if (this.extensions != null) {
			this.extensions.clear();
		}
	}

	@Override
	public void removeExtension(String name) {
		if (this.extensions != null) {
			this.extensions.remove(name);
		}
	}

	@Override
	public void insertExtension(String name, JsonNode value, int atIndex) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
			this.extensions.put(name, value);
		} else {
			this.extensions = DataModelUtil.insertMapEntry(this.extensions, name, value, atIndex);
		}
	}

	@Override
	public void accept(Visitor visitor) {
		Syn2Visitor viz = (Syn2Visitor) visitor;
		viz.visitDocument(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn2DocumentImpl();
	}
}