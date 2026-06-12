package io.test.synthetic.v1;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.RootNode;
import io.test.synthetic.SynDocument;
import java.util.Map;

public interface Syn1Document extends RootNode, SynDocument, Syn1Extensible {

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	public void insertExtension(String name, JsonNode value, int atIndex);
}