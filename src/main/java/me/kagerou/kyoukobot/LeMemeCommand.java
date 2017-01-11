package me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class LeMemeCommand implements CommandExecutor {
	static String dirName = "memes";
	@Command(aliases = {"k!meme"}, description = "Posts a \"meme\" picture of questionable hilarity.")
    public void onCommand(Message message) {
		File result = KyoukoBot.memeBase.GetMeme();
		if (result == null)
			message.reply("`I-I'm out of memes >_< But you can always k!donate them to me!`");
		else
			message.replyFile(result);
    }
}
