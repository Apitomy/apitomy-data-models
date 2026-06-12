package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.ModelType;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.RootNodeImpl;
import io.test.synthetic.SynInfo;
import io.test.synthetic.SynItem;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn1DocumentImpl extends RootNodeImpl implements Syn1Document {

	private String version;
	private SynInfo info;
	private List<SynItem> items;
	private List<String> tags;
	private Map<String, String> metadata;
	private Map<String, JsonNode> extensions;

	public Syn1DocumentImpl() {
		super(ModelType.SYN1);
	}

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
	public Syn1Info createInfo() {
		Syn1InfoImpl node = new Syn1InfoImpl();
		node.setParent(this);
		return node;
	}

	@Override
	public Syn1Item createItem() {
		Syn1ItemImpl node = new Syn1ItemImpl();
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
		Syn1Visitor viz = (Syn1Visitor) visitor;
		viz.visitDocument(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn1DocumentImpl();
	}
}