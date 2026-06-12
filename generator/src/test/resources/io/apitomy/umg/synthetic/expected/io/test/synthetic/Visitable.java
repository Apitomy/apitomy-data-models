package io.test.synthetic;

import io.test.synthetic.visitors.Visitor;

public interface Visitable {

	public void accept(Visitor visitor);

}
