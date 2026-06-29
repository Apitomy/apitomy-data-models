package io.test.synthetic.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.test.synthetic.SynDocument;
import java.util.Map;

public interface Syn2Document extends SynDocument, Syn2Extensible {

	public Syn2PathItem createPathItem();

	public Map<String, Syn2PathItem> getWebhooks();

	public void addWebhook(String name, Syn2PathItem value);

	public void clearWebhooks();

	public void removeWebhook(String name);

	/**
	 * Inserts an item at the given index.
	 * 
	 * @param atIndex
	 *            insertion position: &lt;= 0 inserts at the beginning, &gt;= size
	 *            inserts at the end, otherwise inserts at the given position
	 *            shifting existing items to the right
	 */
	public void insertWebhook(String name, Syn2PathItem value, int atIndex);

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