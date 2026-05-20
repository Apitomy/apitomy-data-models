package io.apitomy.umg.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class NodeImpl implements Node {

    private static int __modelIdCounter = 0;

    protected int _modelId = __modelIdCounter++;
    private String _parentPropertyName;
    private String _mapPropertyName;
    private ParentPropertyType _parentPropertyType = ParentPropertyType.standard;
    private Node _parent;
    private Map<String, JsonNode> _extraProperties;
    private Map<String, Object> _attributes;

    @Override
    public RootNode root() {
        return this._parent.root();
    }

    @Override
    public Node parent() {
        return this._parent;
    }

    @Override
    public String parentPropertyName() {
        return this._parentPropertyName;
    }

    @Override
    public ParentPropertyType parentPropertyType() {
        return this._parentPropertyType;
    }

    @Override
    public String mapPropertyName() {
        return this._mapPropertyName;
    }

    public void setParent(Node parent) {
        this._parent = parent;
    }

    public void _setParentPropertyName(String name) {
        this._parentPropertyName = name;
    }

    public void _setParentPropertyType(ParentPropertyType type) {
        this._parentPropertyType = type;
    }

    public void _setMapPropertyName(String name) {
        this._mapPropertyName = name;
    }

    @Override
    public int modelId() {
        return this._modelId;
    }

    @Override
    public Object getNodeAttribute(String attributeName) {
        if (this._attributes != null) {
            return this._attributes.get(attributeName);
        } else {
            return null;
        }
    }

    @Override
    public void setNodeAttribute(String attributeName, Object attributeValue) {
        if (this._attributes == null) {
            this._attributes = new HashMap<>();
        }
        this._attributes.put(attributeName, attributeValue);
    }

    @Override
    public Collection<String> getNodeAttributeNames() {
        if (this._attributes != null) {
            return this._attributes.keySet();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void clearNodeAttributes() {
        if (this._attributes != null) {
            this._attributes.clear();
        }
    }

    @Override
    public void addExtraProperty(String key, JsonNode value) {
        if (this._extraProperties == null) {
            this._extraProperties = new LinkedHashMap<>();
        }
        this._extraProperties.put(key, value);
    }

    @Override
    public JsonNode removeExtraProperty(String name) {
        if (this._extraProperties != null && this._extraProperties.containsKey(name)) {
            return this._extraProperties.remove(name);
        }
        return null;
    }

    @Override
    public boolean hasExtraProperties() {
        return this._extraProperties != null && this._extraProperties.size() > 0;
    }

    @Override
    public List<String> getExtraPropertyNames() {
        if (this.hasExtraProperties()) {
            return new ArrayList<String>(this._extraProperties.keySet());
        }
        return Collections.emptyList();
    }

    @Override
    public JsonNode getExtraProperty(String name) {
        if (this.hasExtraProperties()) {
            return this._extraProperties.get(name);
        }
        return null;
    }

    @Override
    public boolean isAttached() {
        if (_parent == null) {
            return false;
        }
        return true;
    }

    @Override
    public void attach(Node parent) {
        if (!parent.isAttached())
            throw new IllegalArgumentException("Target parent node (method argument) is not itself attached.");
        this.setParent(parent);
    }

    /*
     * Some methods that must be implemented on an entity only when the entity is
     * part of a union type. We put them here so we don't have to generate them. But
     * perhaps we *should* generate them only on the appropriate entity
     * implementation classes.
     */

    public boolean isEntity() {
        return true;
    }

    public boolean isEntityList() {
        return false;
    }

    public boolean isEntityMap() {
        return false;
    }

}
