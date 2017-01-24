package me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ChangeLogCommand implements CommandExecutor {
    @Command(aliases = {"k!changelog"}, usage = "k!changelog [full]", description = "Displays the latest changes made to me or sends the full changelog.")
    public void onCommand(String command, Message message, String[] args) {
        if (KyoukoBot.ChangeLog.isEmpty())
        	message.reply("`Failed to load the changelog.`");
        else
        	if ((args.length > 0) && (args[0].equalsIgnoreCase("full")))
        		//message.replyFile(new File("changelog.txt"));
        		message.getReceiver().sendFile(new File("changelog.txt"));
        	else
        		message.reply("```xml\n" + KyoukoBot.ChangeLog + "```");
    }
}
