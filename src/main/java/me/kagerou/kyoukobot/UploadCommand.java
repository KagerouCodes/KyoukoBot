package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.kagerou.kyoukobot.MemeBase.DownloadResult;

public class UploadCommand implements CommandExecutor {	
	@Command(aliases = {"k!upload", "k!donate"}, description = "Uploads image(s) to the \"meme\" collection, accepts attachments too. You don't need to use the command in #animemes.", usage = "k!upload image(s)")
    public void onCommand(Message message, String args[]) {
		if ((args.length == 0) && (message.getAttachments().isEmpty()))
		{
			message.reply("`Link or attach an image.`");
			return;
		}
		int uploaded = 0, dupes = 0;
		for (String arg: args)
		{
			DownloadResult result = KyoukoBot.memeBase.DownloadImage(arg);
			if (result == DownloadResult.DR_OK)
				uploaded++;
			if (result == DownloadResult.DR_DUPE)
				dupes++;
		}
		for (MessageAttachment attachment: message.getAttachments())
		{
			DownloadResult result = KyoukoBot.memeBase.DownloadImage(attachment.getUrl());
			if (result == DownloadResult.DR_OK)
				uploaded++;
			if (result == DownloadResult.DR_DUPE)
				dupes++;
		}
		if (uploaded == 0)
			if (dupes == 0)
				message.reply("`No images provided or failed to uploade any >_<`");
			else
				if (dupes == 1)
					message.reply("`Thank you, i already have this image.`");
				else
					message.reply("`Thanks, i already have these images.`");
		else
			if (dupes == 0)
				message.reply("`Uploaded " + uploaded + " image(s) successfully!`");
			else
				message.reply("`Uploaded " + uploaded + " image(s) successfully! Found " + dupes + " duplicate(s).`");
    }
}