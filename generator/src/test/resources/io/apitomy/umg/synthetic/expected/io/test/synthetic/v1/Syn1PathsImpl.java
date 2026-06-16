package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynPathItem;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Syn1PathsImpl extends NodeImpl implements Syn1Paths {

	private Map<String, SynPathItem> _items = new LinkedHashMap<>();
	private Map<String, JsonNode> extensions;

	@Override
	public SynPathItem getItem(String name) {
		return this._items.get(name);
	}

	@Override
	public List<SynPathItem> getItems() {
		List<SynPathItem> rval = new ArrayList<>();
		rval.addAll(this._items.values());
		return rval;
	}

	@Override
	public List<String> getItemNames() {
		List<String> rval = new ArrayList<>();
		rval.addAll(this._items.keySet());
		return rval;
	}

	@Override
	public void addItem(String name, SynPathItem item) {
		this._items.put(name, item);
		if (item != null) {
			((NodeImpl) item)._setParent(this);
			((NodeImpl) item)._setParentPropertyName(null);
			((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);
			((NodeImpl) item)._setMapPropertyName(name);
		}
	}

	@Override
	public void insertItem(String name, SynPathItem item, int atIndex) {
		this._items = DataModelUtil.insertMapEntry(this._items, name, item, atIndex);
		if (item != null) {
			((NodeImpl) item)._setParent(this);
			((NodeImpl) item)._setParentPropertyName(null);
			((NodeImpl) item)._setParentPropertyType(ParentPropertyType.map);
			((NodeImpl) item)._setMapPropertyName(name);
		}
	}

	@Override
	public SynPathItem removeItem(String name) {
		SynPathItem removed = this._items.remove(name);
		if (removed != null)
			removed.detach();
		return removed;
	}

	@Override
	public void clearItems() {
		this._items.values().forEach(item -> {
			if (item != null)
				item.detach();
		});
		this._items.clear();
	}

	@Override
	public Syn1PathItem createPathItem() {
		Syn1PathItemImpl node = new Syn1PathItemImpl();
		node._setParent(this);
		return node;
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
		viz.visitPaths(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn1PathsImpl();
	}
}