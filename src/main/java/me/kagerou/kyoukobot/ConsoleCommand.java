package me.kagerou.kyoukobot;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ConsoleCommand implements CommandExecutor {
	@Command(aliases = {"k!console"}, description = "Cheesy admin-only command.", usage = "k!console", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String args[])
	{
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
		{
			message.reply("Y-you're touching me inappropriately!");
			return;
		}
		if (!KyoukoBot.manual_reconnecting)
		{
			message.reply("`Not tracking console output currently.`");
			return;
		}
		try {
			File leFile = new File("console.txt");
			FileUtils.writeStringToFile(leFile, KyoukoBot.coc.getLastOutput(), Charset.forName("UTF-8"));
			message.replyFile(leFile).get();
			leFile.delete();
		}
		catch (Exception e)
		{
			System.out.println("Failed to send last console output.");
			e.printStackTrace();
			message.reply("`Failed to send last console output >_<`");
		}
	}
}
