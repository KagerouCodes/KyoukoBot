package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class IntroCommand implements CommandExecutor {
	final int MaxIntroLength = 200;
	@Command(aliases = {"k!intro", "k!introduce"}, description = "Lets you introduce yourself.", usage = "k!intro[duce] text")
    public String onCommand(DiscordAPI api, Message message, String args[])
	{
		if (args.length == 0)
		{
			KyoukoBot.Database.set(message.getAuthor().getName(), "");
			return "Forgot about you >_<";
		}
		String intro = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().replace('\n', ' ');
		if (intro.length() > MaxIntroLength)
			intro = intro.substring(0, MaxIntroLength);
		StringBuilder no_preview_intro = new StringBuilder();
		int index = 0;
		while (index < intro.length())
		{
			int next_http = intro.indexOf("http://", index);
			int next_https = intro.indexOf("https://", index);
			int next_url_start;
			String protocol;
			if ((next_https == -1) || (next_http < next_https) && (next_http != -1))
			{
				next_url_start = next_http;
				protocol = "http://";
			}
			else
			{
				next_url_start = next_https;
				protocol = "https://";
			}	
			if (next_url_start == -1)
			{
				no_preview_intro.append(intro.substring(index));
				index = intro.length(); 
			}
			else
			{
				no_preview_intro.append(intro.substring(index, next_url_start));
				int next_space = intro.indexOf(' ', next_url_start);
				if (next_space == -1)
					next_space = intro.length();
				while ("!:,.;".indexOf(intro.charAt(next_space - 1)) != -1)
					next_space--;
				//int dot = intro.indexOf('.', next_url_start);
				if (next_url_start + protocol.length() + 2 <= next_space) //at least two valid symbols after the protocol //((dot != -1) && (dot < next_space))
				{
					no_preview_intro.append('<').append(intro.substring(next_url_start, next_space)).append('>');
					index = next_space;
				}
				else
				{
					no_preview_intro.append(intro.substring(next_url_start, next_space));
					index = next_space;
				}
			}
		}
		//KyoukoBot.Database.set(message.getAuthor().getName(), intro);
		KyoukoBot.Database.set(message.getAuthor().getName(), no_preview_intro.toString());
		return "Nice to meet you~";
	}
}
