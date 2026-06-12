package io.test.synthetic.v1;

import io.test.synthetic.Node;
import io.test.synthetic.NodeImpl;
import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.visitors.Visitor;

public class Syn1ContactImpl extends NodeImpl implements Syn1Contact {

	private String name;
	private String email;
	private String url;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String value) {
		this.name = value;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String value) {
		this.email = value;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String value) {
		this.url = value;
	}

	@Override
	public void accept(Visitor visitor) {
		Syn1Visitor viz = (Syn1Visitor) visitor;
		viz.visitContact(this);
	}

	@Override
	public Node emptyClone() {
		return new Syn1ContactImpl();
	}
}