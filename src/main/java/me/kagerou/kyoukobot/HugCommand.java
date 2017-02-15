package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//hugs a user over TCP/IP (or the author if the message if there's no argument/user not found)
public class HugCommand implements CommandExecutor {
	@Command(aliases = {"k!hug"}, description = "Hugs a user over TCP/IP! You can get a hug too~", usage = "k!hug [name]"/*"k!hug [username|random]"*/)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
	{
        String who = "*hugs ";
        User target = null;
        if (args.length == 0) //hugs the author if there are no arguments
        	target = message.getAuthor();
        else
        	if (!message.getMentions().isEmpty()) //if there's a mention in the message, just grab it
        		target = message.getMentions().get(0);
        	else
        	{ //search for the user otherwise
        		//String arg = message.getContent().split(" ", 2)[1].toLowerCase().trim();
        		String arg = KyoukoBot.getArgument(message);
        		target = KyoukoBot.findUserOnServer(arg, server, message.getAuthor());
        	}
        if (target == null)
    		who = "Couldn't find them, have your hug back >_<\n" + who + message.getAuthor().getMentionTag() + "*";
        else
        	if (target.isYourself())
        		who += "herself >_<*"; //change this to something else??
        	else
        		who += target.getMentionTag() + "*";
        message.reply(who);
	}
}
