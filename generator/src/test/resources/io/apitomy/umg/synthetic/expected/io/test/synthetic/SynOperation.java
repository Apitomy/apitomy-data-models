package io.test.synthetic;

import java.util.List;

public interface SynOperation extends Node {

	public String getOperationId();

	public void setOperationId(String value);

	public String getSummary();

	public void setSummary(String value);

	public List<String> getTags();

	public void setTags(List<String> value);

	public SynItem createItem();

	public List<SynItem> getParameters();

	public void addParameter(SynItem value);

	public void clearParameters();

	public void removeParameter(SynItem value);

	public void insertParameter(SynItem value, int atIndex);
}