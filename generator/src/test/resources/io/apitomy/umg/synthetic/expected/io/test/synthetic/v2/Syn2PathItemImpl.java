package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynOperation;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Syn2PathItemImpl extends NodeImpl implements Syn2PathItem {

	private String $ref;
	private String summary;
	private SynOperation get;
	private SynOperation put;
	private SynOperation post;
	private Syn2Operation delete;
	private Map<String, JsonNode> extensions;

	@Override
	public String get$ref() {
		return $ref;
	}

	@Override
	public void set$ref(String value) {
		this.$ref = value;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	@Override
	public void setSummary(String value) {
		this.summary = value;
	}

	@Override
	public SynOperation getGet() {
		return get;
	}

	@Override
	public void setGet(SynOperation value) {
		this.get = value;
		if (value != null) {
			((NodeImpl) value)._setParent(this);
			((NodeImpl) value)._setParentPropertyName("get");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Syn2Operation createOperation() {
		Syn2OperationImpl node = new Syn2OperationImpl();
		node._setParent(this);
		return node;
	}

	@Override
	public SynOperation getPut() {
		return put;
	}

	@Override
	public void setPut(SynOperation value) {
		this.put = value;
		if (value != null) {
			((NodeImpl) value)._setParent(this);
			((NodeImpl) value)._setParentPropertyName("put");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public SynOperation getPost() {
		return post;
	}

	@Override
	public void setPost(SynOperation value) {
		this.post = value;
		if (value != null) {
			((NodeImpl) value)._setParent(this);
			((NodeImpl) value)._setParentPropertyName("post");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Syn2Operation getDelete() {
		return delete;
	}

	@Override
	public void setDelete(Syn2Operation value) {
		this.delete = value;
		if (value != null) {
			((NodeImpl) value)._setParent(this);
			((NodeImpl) value)._setParentPropertyName("delete");
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
		viz.visitPathItem(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn2PathItemImpl();
	}
}