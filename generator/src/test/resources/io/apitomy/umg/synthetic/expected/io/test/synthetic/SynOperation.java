package io.test.synthetic;

import java.util.List;

public interface SynOperation extends Node {

	public String getOperationId();

	public void setOperationId(String value);

	public SynItem createItem();

	public List<SynItem> getParameters();

	public void addParameter(SynItem value);

	public void clearParameters();

	public void removeParameter(SynItem value);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertParameter(SynItem value, int atIndex);

	public String getSummary();

	public void setSummary(String value);

	public List<String> getTags();

	public void setTags(List<String> value);
}