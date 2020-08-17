package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.Sdcf4jMessage;
import main.java.me.kagerou.kyoukobot.MemeBase.MemeResult;
// deletes an attached/linked image from a "meme" collection (moderator/owner only)
// the bot's designed to be run on just one server, one could clear the collection by inviting the bot to their own server
// so the safe way of doing things would be a DB of images' availability on servers
public class DeleteCommand implements CommandExecutor {
    static final String ShiyuID = "133599547850096640"; //she'd be given the permission to delete "memes"
    @Command(aliases = {"k!delete", "k!delmeme"}, description = "Deletes image(s) from the \"meme\" collection (moderator/owner only), accepts both links and attachments.", usage = "k!delete image(s)")
    public void onCommand(Message message, String args[])
    {
        boolean allowed = false; //check if the user is myself or has the MANAGE_MESSAGES or ADMINISTRATOR permissions
        if (KyoukoBot.adminIDs.contains(message.getAuthor().getId()) || message.getAuthor().getId().equals(ShiyuID))
            allowed = true;
        if (!message.isPrivateMessage()) //waiting for User.getPermissions(Server) or something like that to be implemented
            for (Role role: message.getAuthor().getRoles(message.getChannelReceiver().getServer()))
                allowed = allowed || (role.getPermissions().getState(PermissionType.MANAGE_MESSAGES) == PermissionState.ALLOWED) ||
                        (role.getPermissions().getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED);
        if (!allowed)
        {
            message.reply(Sdcf4jMessage.MISSING_PERMISSIONS.getMessage());
            return;
        }
        //check all the links and attachments
        int deleted = 0, not_found = 0;
        for (String arg: args)
        {
            MemeResult result = KyoukoBot.memeBase.DeleteImage(arg);
            if (result == MemeResult.DR_OK)
                deleted++;
            if (result == MemeResult.DR_DUPE)
                not_found++;
        }
        for (MessageAttachment attachment: message.getAttachments())
        { //to be fair, i've never seen a message have 2 attachments
            MemeResult result = KyoukoBot.memeBase.DeleteImage(attachment.getUrl());
            if (result == MemeResult.DR_OK)
                deleted++;
            if (result == MemeResult.DR_DUPE)
                not_found++;
        }
        if (deleted == 0)
            if (not_found == 0)
                message.reply("`No images provided or failed to download any >_<`");
            else
                if (not_found == 1)
                    message.reply("`This image was not a part of the collection.`");
                else
                    message.reply("`These images were not a part of the collection.`");
        else
            if (not_found == 0)
                message.reply("`Deleted " + deleted + " image(s) successfully!`");
            else
                message.reply("`Deleted " + deleted + " image(s) successfully! " + not_found  + " image(s) were not in the collection.`");
    }
}
