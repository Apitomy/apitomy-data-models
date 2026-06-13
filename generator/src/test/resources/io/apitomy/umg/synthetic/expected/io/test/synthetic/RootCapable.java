package io.test.synthetic;

public interface RootCapable extends Any {

	default boolean isRoot() {
		return false;
	}

	default ModelType modelType() {
		return null;
	}

}
