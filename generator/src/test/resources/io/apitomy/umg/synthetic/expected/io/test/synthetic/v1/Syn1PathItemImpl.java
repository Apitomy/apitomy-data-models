package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynOperation;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Syn1PathItemImpl extends NodeImpl implements Syn1PathItem {

	private String $ref;
	private String summary;
	private SynOperation get;
	private SynOperation put;
	private SynOperation post;
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
		DataModelUtil.setParent(value, this, "get", ParentPropertyType.standard);
	}

	@Override
	public Syn1Operation createOperation() {
		Syn1OperationImpl node = new Syn1OperationImpl();
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
		DataModelUtil.setParent(value, this, "put", ParentPropertyType.standard);
	}

	@Override
	public SynOperation getPost() {
		return post;
	}

	@Override
	public void setPost(SynOperation value) {
		this.post = value;
		DataModelUtil.setParent(value, this, "post", ParentPropertyType.standard);
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

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	@Override
	public void insertExtension(String name, JsonNode value, int atIndex) {
		if (this.extensions == null) {
			this.extensions = new LinkedHashMap<>();
		}
		this.extensions = DataModelUtil.insertMapEntry(this.extensions, name, value, atIndex);
	}

	@Override
	public void accept(Visitor visitor) {
		Syn1Visitor viz = (Syn1Visitor) visitor;
		viz.visitPathItem(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn1PathItemImpl();
	}
}