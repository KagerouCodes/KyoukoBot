package main.java.me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class DelFileCommand implements CommandExecutor {
    @Command(aliases = {"k!delfile"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
        String msg = KyoukoBot.getArgument(message, false);
        File file = new File(System.getProperty("user.dir") + "/" + msg);
        if (!file.exists())
        {
            message.reply("`File not found.`");
            return;
        }
        if (file.delete())
            message.reply("`Deleted the file " + msg + " successfully!`");
        else
            message.reply("`Failed to delete the file ` + msg + `.`");
    }
}
