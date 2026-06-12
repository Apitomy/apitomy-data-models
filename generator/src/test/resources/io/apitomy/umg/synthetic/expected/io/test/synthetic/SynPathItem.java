package io.test.synthetic;
public interface SynPathItem extends Node {

	public SynOperation getGet();

	public void setGet(SynOperation value);

	public SynOperation createOperation();

	public SynOperation getPost();

	public void setPost(SynOperation value);

	public SynOperation getPut();

	public void setPut(SynOperation value);

	public String getSummary();

	public void setSummary(String value);
}