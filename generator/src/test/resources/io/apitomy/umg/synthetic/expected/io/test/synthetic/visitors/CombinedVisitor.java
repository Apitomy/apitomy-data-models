package io.test.synthetic.visitors;

import io.test.synthetic.v1.visitors.Syn1Visitor;
import io.test.synthetic.v2.visitors.Syn2Visitor;

public interface CombinedVisitor extends Syn2Visitor, Syn1Visitor {
}