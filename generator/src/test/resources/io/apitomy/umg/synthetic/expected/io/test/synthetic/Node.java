package io.test.synthetic;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;

/**
 * A tree node representing an entity in the data model. Extends {@link Any}
 * (which provides parent/metadata/root access) and {@link Visitable} (for the
 * visitor pattern).
 * <p>
 * Nodes form a tree structure where each node has at most one parent. Parent
 * relationships are managed automatically by generated setters and collection
 * methods — see {@link Any} for details.
 */
public interface Node extends Any, Visitable {

	int modelId();
	Object getNodeAttribute(String attributeName);
	void setNodeAttribute(String attributeName, Object attributeValue);
	Collection<String> getNodeAttributeNames();
	void clearNodeAttributes();
	void addExtraProperty(String key, JsonNode value);
	JsonNode removeExtraProperty(String name);
	boolean hasExtraProperties();
	List<String> getExtraPropertyNames();
	JsonNode getExtraProperty(String name);
	Node emptyClone();

}
