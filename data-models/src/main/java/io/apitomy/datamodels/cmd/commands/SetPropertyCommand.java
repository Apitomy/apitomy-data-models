package io.apitomy.datamodels.cmd.commands;

import io.apitomy.datamodels.paths.NodePath;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to modify the simple property of a node.  Should not be used
 * to modify complex (object) properties, only simple property types like
 * string, boolean, number, etc.  This differs from {@link ChangePropertyCommand}
 * in that it can be constructed from a {@link NodePath} rather than a {@link io.apitomy.datamodels.models.Node}
 * and therefore can be used in {@link AggregateCommand} use-cases where there is
 * a Create followed by a SetProperty.
 * 
 * @author eric.wittmann@gmail.com
 */
public class SetPropertyCommand<T> extends ChangePropertyCommand<T> {

    /**
     * Constructor.
     */
    public SetPropertyCommand() {
    }

    /**
     * Constructor.
     */
    public SetPropertyCommand(NodePath nodePath, String property, T newValue) {
        super();
        if (NodeUtil.isDefined(nodePath)) {
            this._nodePath = nodePath;
        }
        this._property = property;
        this._newValue = newValue;
    }

}
