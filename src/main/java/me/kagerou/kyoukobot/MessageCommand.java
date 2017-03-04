package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//returns the contents of a message by its number, owner-only, was needed for testing purposes
public class MessageCommand implements CommandExecutor
{
	@Command(aliases = {"k!message", "k!msg"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		try
		{
			String id = message.getContent().split("\\s+", 2)[1];
			message.reply(api.getMessageById(id).getContent());
		}
		catch (Exception e)
		{
			message.reply("`Failed to find the message >_<`");
		}
    }
}
