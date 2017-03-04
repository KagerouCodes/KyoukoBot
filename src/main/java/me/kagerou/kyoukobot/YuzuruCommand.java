package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//a template command, puts a user-submitted image (or a mentioned user's avatar) on the piece of paper there: http://i.imgur.com/9FulDOt.png
public class YuzuruCommand extends TemplateCommand implements CommandExecutor {
	private final static String template_link = KyoukoBot.YuzuruLink;
	YuzuruCommand()
	{
		super(template_link, StretchOption.STRETCH, new Triangle(270, 356, 518, 292, 310, 528) ); //not sure if STRETCH is the way to go
	}
	
	@Command(aliases = {"k!yuzuru"}, description = "Draws your image on Yuzuru-chan's piece of paper. Accepts attachments.", usage = "k!yuzuru username|image")
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args)
    {
		super.onCommand(api, message, server, args);
    }
}
