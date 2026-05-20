package io.apitomy.umg.base;

import io.apitomy.umg.base.visitors.Visitor;

public interface Visitable {

    public void accept(Visitor visitor);

}
