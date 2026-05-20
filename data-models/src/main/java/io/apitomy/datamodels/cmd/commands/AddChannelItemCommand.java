package io.apitomy.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apitomy.datamodels.cmd.AbstractCommand;
import io.apitomy.datamodels.models.Document;
import io.apitomy.datamodels.models.MappedNode;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apitomy.datamodels.models.asyncapi.AsyncApiChannels;
import io.apitomy.datamodels.models.asyncapi.AsyncApiDocument;
import io.apitomy.datamodels.util.LoggerUtil;
import io.apitomy.datamodels.util.NodeUtil;

/**
 * A command used to add a new channelItem in a document. Source for the new
 * channelItem must be provided.  This source will be converted to an AAI
 * channelItem object and then added to the data model.
 * @author laurent.broudoux@gmail.com
 */
public class AddChannelItemCommand extends AbstractCommand {

   public boolean _channelItemExists;
   public boolean _emptyChannelItems;

   public String _newChannelItemName;
   public ObjectNode _newChannelItemObj;


   public AddChannelItemCommand() {
   }

   public AddChannelItemCommand(String channelItemName, ObjectNode from) {
      this._newChannelItemName = channelItemName;
      this._newChannelItemObj = from;
   }

   private boolean isEmptyChannelItems(AsyncApiChannels channels) {
      if (NodeUtil.isNullOrUndefined(channels)) {
         return false;
      }
      return ((MappedNode<?>) channels).getItemNames().isEmpty();
   }

   private AsyncApiChannelItem getChannelItem(AsyncApiChannels channels, String name) {
      if (NodeUtil.isNullOrUndefined(channels)) {
         return null;
      }
      return (AsyncApiChannelItem) ((MappedNode<?>) channels).getItem(name);
   }

   @Override
   public void execute(Document document) {
      LoggerUtil.info("[AddChannelItemCommand] Executing.");
      AsyncApiDocument doc = (AsyncApiDocument) document;
      if (isEmptyChannelItems(doc.getChannels())) {
         this._emptyChannelItems = true;
      }

      AsyncApiChannelItem channelItem = getChannelItem(doc.getChannels(), this._newChannelItemName);
      if (!NodeUtil.isNullOrUndefined(channelItem)) {
         LoggerUtil.info("[AddChannelItemCommand] AddChannelItemCommand with name %s already exists.", this._newChannelItemName);
         this._channelItemExists = true;
      } else {
         // TODO the model is broken - createChannelItem() needs to exist on AsyncApiChannels
//         channelItem = doc.getChannels().createChannelItem(this._newChannelItemName);
//         doc.addChannelItem(channelItem);
//         Library.readNode(this._newChannelItemObj, channelItem);
//         this._channelItemExists = false;
      }
   }

   @Override
   public void undo(Document document) {
      LoggerUtil.info("[AddChannelItemCommand] Reverting.");
      if (this._channelItemExists) {
         return;
      }
      AsyncApiDocument doc = (AsyncApiDocument) document;
      if (this._emptyChannelItems) {
         doc.setChannels(doc.createChannels());
      } else {
         ((MappedNode<?>) doc.getChannels()).removeItem(this._newChannelItemName);
      }
   }
}
