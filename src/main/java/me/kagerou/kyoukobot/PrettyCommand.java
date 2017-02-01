package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class PrettyCommand extends TemplateCommand implements CommandExecutor {
	private final static String template_link = KyoukoBot.PrettyLink;
	PrettyCommand()
	{
		super(template_link, StretchOption.STRETCH, new Triangle(121, 622, 278, 605, 169, 842), new Triangle(284, 557, 444, 567, 331, 769),
				new Triangle(224, 858, 494, 858, 253, 1011), new Triangle(498, 561, 714, 513, 539, 731), new Triangle(730, 517, 843, 508, 772, 675),
				new Triangle(693, 707, 878, 681, 725, 859), new Triangle(643, 897, 815, 864, 667, 1012));
	}
	
	@Command(aliases = {"k!nice", "k!pretty"}, description = "Fills Mirai's collection of nice and pretty things. Accepts attachments.", usage = "k!nice username|image")
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args)
    {
		super.onCommand(api, message, server, args);
    }
}