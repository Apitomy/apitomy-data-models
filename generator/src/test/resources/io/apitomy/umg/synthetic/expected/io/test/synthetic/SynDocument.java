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

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertItem(SynItem value, int atIndex);

	public Map<String, String> getMetadata();

	public void setMetadata(Map<String, String> value);

	public List<String> getTags();

	public void setTags(List<String> value);

	public String getVersion();

	public void setVersion(String value);
}