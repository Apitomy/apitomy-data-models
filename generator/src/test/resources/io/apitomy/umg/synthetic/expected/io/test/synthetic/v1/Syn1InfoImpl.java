package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynContact;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Syn1InfoImpl extends NodeImpl implements Syn1Info {

	private String name;
	private SynContact contact;
	private String version;
	private Map<String, JsonNode> extensions;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String value) {
		this.name = value;
	}

	@Override
	public SynContact getContact() {
		return contact;
	}

	@Override
	public void setContact(SynContact value) {
		this.contact = value;
		DataModelUtil.setParent(value, this, "contact", ParentPropertyType.standard);
	}

	@Override
	public Syn1Contact createContact() {
		Syn1ContactImpl node = new Syn1ContactImpl();
		node._setParent(this);
		return node;
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
		viz.visitInfo(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn1InfoImpl();
	}
}