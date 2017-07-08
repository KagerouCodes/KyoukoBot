package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.kagerou.kyoukobot.MemeBase.MemeResult;
//lets users upload image(s) to the "meme" folder (up to 8 MB), accepts attachments/links
public class UploadCommand implements CommandExecutor {	
	@Command(aliases = {"k!upload", "k!donate"}, description = "Uploads image(s) to the \"meme\" collection, accepts attachments too.\nYou don't need to use the command in #animemes. Images over 8 MB are not accepted.", usage = "k!upload image(s)")
    public void onCommand(Message message, String args[]) {
		if ((args.length == 0) && (message.getAttachments().isEmpty()))
		{
			message.reply("`Link or attach an image.`");
			return;
		}
		int uploaded = 0, dupes = 0;
		for (String arg: args) //check link(s) in the message first
		{
			MemeResult result = KyoukoBot.memeBase.DownloadImage(arg);
			if (result == MemeResult.DR_OK)
				uploaded++;
			if (result == MemeResult.DR_DUPE)
				dupes++;
		}
		for (MessageAttachment attachment: message.getAttachments()) //then check the attachment(s)
		{
			MemeResult result = KyoukoBot.memeBase.DownloadImage(attachment.getUrl());
			if (result == MemeResult.DR_OK)
				uploaded++;
			if (result == MemeResult.DR_DUPE)
				dupes++;
		}
		if (uploaded == 0)
			if (dupes == 0)
					message.reply("`No images provided or failed to download any >_<`");
				else
					if (dupes == 1)
						message.reply("`Thank you, i already have this image.`");
					else
						message.reply("`Thanks, i already have these images.`");
		else
			if (dupes == 0)
				message.reply("`Downloaded " + uploaded + " image(s) successfully!`");
			else
				message.reply("`Downloaded " + uploaded + " image(s) successfully! Found " + dupes + " duplicate(s).`");
    }
}