package io.test.synthetic.io;

import io.test.synthetic.ModelType;
import io.test.synthetic.v1.io.Syn1ModelCloner;
import io.test.synthetic.v1.io.Syn1ModelClonerDispatcher;
import io.test.synthetic.v2.io.Syn2ModelCloner;
import io.test.synthetic.v2.io.Syn2ModelClonerDispatcher;

public class ModelClonerFactory {

	public static ModelCloner createModelCloner(ModelType modelType) {
		ModelCloner cloner = null;
		switch (modelType) {
			case SYN1 :
				cloner = new Syn1ModelClonerDispatcher(new Syn1ModelCloner());
				break;
			case SYN2 :
				cloner = new Syn2ModelClonerDispatcher(new Syn2ModelCloner());
				break;
		}
		return cloner;
	}
}