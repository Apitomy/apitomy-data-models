package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.cmd.ICommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.util.LoggerUtil;

import java.util.List;

/**
 * A command used to aggregate an array of other commands into a single logical command.  This is used
 * for example to make multiple changes as a single "undoable" change.
 * @author eric.wittmann@gmail.com
 */
public class AggregateCommand extends AbstractCommand {

    public String name;
    public ObjectNode info;
    public List<ICommand> _commands;

    public AggregateCommand() {
    }

    public AggregateCommand(String name, ObjectNode info, List<ICommand> commands) {
        this.name = name;
        this.info = info;
        this._commands = commands;
    }
    
    /**
     * @see ICommand#execute(Document)
     */
    @Override
    public void execute(Document document) {
        LoggerUtil.info("[AggregateCommand] Executing %d child commands.", this._commands.size());
        this._commands.forEach( command -> {
            command.execute(document);
        });
    }
    
    /**
     * @see ICommand#undo(Document)
     */
    @Override
    public void undo(Document document) {
        LoggerUtil.info("[AggregateCommand] Reverting %d child commands.", this._commands.size());

        for (int idx = this._commands.size() - 1; idx >= 0; idx--) {
            ICommand command = this._commands.get(idx);
            command.undo(document);
        }
    }
    
}
