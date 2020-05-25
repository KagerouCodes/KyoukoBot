package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
//a template command, puts a user-submitted image (or a mentioned user's avatar) at the back side of this letter: https://i.imgur.com/X1rO0A7.png
public class MeguminCommand extends ImageOnTemplateCommand {
	private final static String template_link = KyoukoBot.MeguminLink;
	MeguminCommand()
	{
		super(template_link, StretchOption.SCALE_MIN, new Triangle(102, 114, 665, -2, 201, 491));
	}
	
	@Command(aliases = {"k!fetish", "k!megumin"}, description = "Arouses Megumin. Accepts attachments.", usage = "k!fetish username|image")
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args)
    {
		super.onCommand(api, message, server, args);
    }
}
