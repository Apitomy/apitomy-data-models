package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.RootCapable;
import io.test.synthetic.SynSchema;
import java.util.Map;

public interface Syn2Schema extends RootCapable, SynSchema, Syn2Extensible, Syn2Referenceable {

	public String get$ref();

	public void set$ref(String value);

	public Map<String, JsonNode> getExtensions();

	public void addExtension(String name, JsonNode value);

	public void clearExtensions();

	public void removeExtension(String name);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertExtension(String name, JsonNode value, int atIndex);
}