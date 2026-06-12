package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynPaths;
import java.util.Map;

public interface Syn2Paths extends SynPaths, Syn2Extensible {

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	public void insertExtension(String name, JsonNode value, int atIndex);
}