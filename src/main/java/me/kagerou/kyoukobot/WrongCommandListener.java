package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;
//listens to wrong commands, as in: message starting with k! which are not commands, private messages which aren't commands or messages starting with mentioning the bot
class WrongCommandListener implements MessageCreateListener
{
	private final CommandHandler handler;
	
	WrongCommandListener(CommandHandler handler)
	{
		this.handler = handler;
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		String msg = message.getContent().toLowerCase();
        if (!message.getAuthor().isBot() && (msg.startsWith("k!") || message.isPrivateMessage()))
        {
        	for (SimpleCommand command: handler.getCommands())
        		for (String alias: command.getCommandAnnotation().aliases())
        			if (msg.startsWith(alias.toLowerCase() + ' ') || msg.equals(alias.toLowerCase())) //return if the message contains a command
        				return;
        	message.reply("`Incorrect command. Type k!help to see the list of supported commands!`");
        	return;
        }
        if (msg.startsWith(api.getYourself().getMentionTag()))
        	message.reply("`Type k!help to see the list of supported commands! Y-You don't need to mention me.`");
    }
}