package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//lets a user set up personal alarms (owner only by now because the whole interface isn't finished)
//TODO canceling personal alarms??
//TODO let the user set time in minutes/hours/days
public class RemindMeCommand implements CommandExecutor { 
    @Command(aliases = {"k!remindme", "k!remind", "k!alarm"}, description = "Reminds you about things.", usage = "k!remindme delay [message]\nDelay is set in seconds.", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
        long seconds = -1;
        String[] splitContent = message.getContent().split("\\s+", 3);
        if (splitContent.length == 1)
        {
            message.reply("`Enter the delay (in seconds).`");
            return;
        }
        try {
            seconds = Long.parseLong(splitContent[1]);
        }
        catch (NumberFormatException e)
        {
            message.reply("`Enter the delay (in seconds).`");
            return;
        }
        if (seconds < 0)
        {
            message.reply("`Delay should be a positive number.`");
            return;
        }
        long alarmTime = System.currentTimeMillis() + seconds * 1000;
        String msg;
        if (splitContent.length == 3)
            msg = splitContent[2];
        else
            msg = "Alarm!"; //default alarm message
        KyoukoBot.Database.registerReminder(message.getAuthor(), msg, alarmTime, KyoukoBot.timer, true);
        message.reply("Scheduled an alarm in " + seconds + " seconds!");
    }
}
