package io.test.synthetic;

import java.util.List;
import java.util.Map;

public interface SynDocument extends Node {

	public SchemaOrBoolean getAdditionalSchema();

	public void setAdditionalSchema(SchemaOrBoolean value);

	public SynInfo getInfo();

	public void setInfo(SynInfo value);

	public SynInfo createInfo();

	public SynItem createItem();

	public List<SynItem> getItems();

	public void addItem(SynItem value);

	public void clearItems();

	public void removeItem(SynItem value);

	public void insertItem(SynItem value, int atIndex);

	public Map<String, String> getMetadata();

	public void setMetadata(Map<String, String> value);

	public List<String> getTags();

	public void setTags(List<String> value);

	public String getVersion();

	public void setVersion(String value);
}