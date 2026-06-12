package io.test.synthetic;
public interface SynInfo extends Node {

	public SynContact getContact();

	public void setContact(SynContact value);

	public SynContact createContact();

	public String getName();

	public void setName(String value);

	public String getVersion();

	public void setVersion(String value);
}