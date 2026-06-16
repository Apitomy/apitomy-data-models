package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.ParentPropertyType;
import io.test.synthetic.SynContact;
import io.test.synthetic.util.DataModelUtil;
import io.test.synthetic.v2.visitors.Syn2Visitor;
import io.test.synthetic.visitors.Visitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Syn2InfoImpl extends NodeImpl implements Syn2Info {

	private String name;
	private SynContact contact;
	private String version;
	private String license;
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
		if (value != null) {
			((NodeImpl) value)._setParent(this);
			((NodeImpl) value)._setParentPropertyName("contact");
			((NodeImpl) value)._setParentPropertyType(ParentPropertyType.standard);
		}
	}

	@Override
	public Syn2Contact createContact() {
		Syn2ContactImpl node = new Syn2ContactImpl();
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
	public String getLicense() {
		return license;
	}

	@Override
	public void setLicense(String value) {
		this.license = value;
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
		viz.visitInfo(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn2InfoImpl();
	}
}