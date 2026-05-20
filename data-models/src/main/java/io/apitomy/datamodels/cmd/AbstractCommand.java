package io.apitomy.datamodels.cmd;

import io.apitomy.datamodels.util.NodeUtil;

import java.util.Set;

/**
 * A base class for all command implementations.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractCommand implements ICommand {
    
    /**
     * Constructor.
     */
    public AbstractCommand() {
    }

    /**
     * Returns true if the argument is either null or undefined.
     * @param object
     */
    protected boolean isNullOrUndefined(Object object) {
        return NodeUtil.isNullOrUndefined(object);
    }

    /**
     * @see ICommand#type()
     */
    @Override
    public final String type() {
        return this.getClass().getSimpleName();
    }

    protected static int indexOf(Set<String> names, String name) {
        int idx = 0;
        for (String n : names) {
            if (n.equals(name)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

}
