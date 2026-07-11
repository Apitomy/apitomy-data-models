package io.apitomy.umg.base;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface Node extends Visitable {

    public int modelId();
    public RootNode root();
    public Node parent();
    public String parentPropertyName();
    public ParentPropertyType parentPropertyType();
    public String mapPropertyName();
    public Object getNodeAttribute(String attributeName);
    public void setNodeAttribute(String attributeName, Object attributeValue);
    public Collection<String> getNodeAttributeNames();
    public void clearNodeAttributes();
    public void addExtraProperty(String key, JsonNode value);
    public JsonNode removeExtraProperty(String name);
    public boolean hasExtraProperties();
    public List<String> getExtraPropertyNames();
    public JsonNode getExtraProperty(String name);
    public boolean isAttached();
    public void attach(Node parent);
    public Node emptyClone();

}
