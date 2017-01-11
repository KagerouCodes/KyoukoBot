package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class TwitchCommand implements CommandExecutor {
	@Command(aliases = {"k!twitch", "k!emote"}, description = "Posts a Twitch emote in chat.", usage = "k!twitch emote", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
	{
		if (args.length == 0)
		{
			message.reply("Enter an emote.");
			return;
		}
		String name = args[0].toLowerCase();
		for (Emote emo: KyoukoBot.Emotes)
			if (emo.name.equals(name))
			{
				KyoukoBot.postFile(message, emo.url, name);
				return;
			}
		/*if (KyoukoBot.Emotes.containsKey(name))
		{
			KyoukoBot.postFile(message, KyoukoBot.Emotes.get(name), name);
			return;
		}*/
		message.reply("Emote not found >_<");
	}
}
