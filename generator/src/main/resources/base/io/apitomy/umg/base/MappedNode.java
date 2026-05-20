package io.apitomy.umg.base;

import java.util.List;

public interface MappedNode<T> {

    /**
     * Gets a single item (indexed child) by name. Returns undefined if not found.
     * 
     * @param name
     */
    public T getItem(String name);

    /**
     * Returns an array of all the child items.
     */
    public List<T> getItems();

    /**
     * Gets a list of the names of all indexed children.
     */
    public List<String> getItemNames();

    /**
     * Adds a child item.
     * 
     * @param name
     * @param item
     */
    public void addItem(String name, T item);

    /**
     * Inserts a child item.
     *
     * @param name
     * @param item
     * @param atIndex
     */
    public void insertItem(String name, T item, int atIndex);

    /**
     * Removes a child item by name and returns the deleted child or undefined if there wasn't one.
     * 
     * @param name
     */
    public T removeItem(String name);

    /**
     * Removes all children.
     */
    public void clearItems();

}
