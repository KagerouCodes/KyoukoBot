package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class MeguminCommand extends TemplateCommand implements CommandExecutor {
	private final static String template_link = KyoukoBot.MeguminLink;
	MeguminCommand()
	{
		super(template_link, StretchOption.SCALE_MIN, new Triangle(102, 114, 665, -2, 201, 491)); //211, 477, 765, 371not sure if STRETCH is the way to go -> 754, 381
	}
	
	@Command(aliases = {"k!fetish", "k!megumin"}, description = "Arouses Megumin. Accepts attachments.", usage = "k!fetish username|image")
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args)
    {
		super.onCommand(api, message, server, args);
    }
}
