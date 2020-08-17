package main.java.me.kagerou.kyoukobot;

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//this one used to convert the database from old to new format (admin-only)
public class ConvertCommand implements CommandExecutor {
    @Command(aliases = {"k!convert"}, description = "Cheesy admin-only command.", usage = "k!convert", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String args[])
    {
        message.getReceiver().sendFile(IOUtils.toInputStream(KyoukoBot.Database.convert(api), Charset.forName("UTF-8")), "people.txt", "");
    }
}
