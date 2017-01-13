package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.kagerou.kyoukobot.MemeBase.MemeResult;

public class DeleteCommand implements CommandExecutor {
	@Command(aliases = {"k!delete", "k!delmeme"}, description = "Deletes image(s) from the \"meme\" collection (moderator/owner only).", usage = "k!delete image(s)")
    public void onCommand(Message message, String args[]) {
		boolean allowed = false;
		if (message.getAuthor().getId().equals(KyoukoBot.adminID))
			allowed = true;
		if (!message.isPrivateMessage())
			for (Role role: message.getAuthor().getRoles(message.getChannelReceiver().getServer()))
				allowed = allowed || (role.getPermissions().getState(PermissionType.MANAGE_MESSAGES) == PermissionState.ALLOWED) ||
						(role.getPermissions().getState(PermissionType.ADMINISTATOR) == PermissionState.ALLOWED);
		if (!allowed)
		{
			message.reply("Y-you're touching me inappropriately!");
			return;
		}
		
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
		{
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
