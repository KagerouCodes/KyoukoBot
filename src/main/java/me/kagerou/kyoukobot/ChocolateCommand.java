package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//a slight modification of the hug command for the Valentine's Day
public class ChocolateCommand implements CommandExecutor
{
	@Command(aliases = {"k!choco", "k!chocolate", "k!valentine", "k!rabu"}, description = "Gives some valentine chocolate to a user over TCP/IP~", usage = "k!choco [name]")
	public String onCommand(DiscordAPI api, Message message, Server server, String args[])
	{
		User target = null;
		String arg = KyoukoBot.getArgument(message);
		if (arg.isEmpty()) //assume a gift to Kyouko if there's no argument
			target = api.getYourself();
		else
			if (!message.getMentions().isEmpty()) //if there's a mention in the message, just grab it
        		target = message.getMentions().get(0);
			else
				target = KyoukoBot.findUserOnServer(arg, server, message.getAuthor());
		if (target == null) //no user found
			return "Couldn't find them, sorry >_<";
		if (target.isYourself()) //be thankful
			return "Chocolate? For me? Thank you :heart:";
		if (target.getId().equals(message.getAuthor().getId())) //gifting to themselves, forever alone
			return "Chocolate is too precious to give away, that's right!";
		return target.getMentionTag() + " *" + KyoukoBot.getNickname(message.getAuthor(), message.getReceiver()) + " has gifted you a :heart:-shaped :chocolate_bar:! Happy Valentine's Day!*";
	}
}
