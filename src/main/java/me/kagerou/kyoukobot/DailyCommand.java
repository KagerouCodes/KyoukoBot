package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//subscribes the user to reminders about Tatsumaki's t!daily and t!rep commands or shows the time left before those alarms go off
//k!daily off unsubscribes the user
public class DailyCommand implements CommandExecutor
{
	@Command(aliases = {"k!daily", "k!rep", "k!tatsumaki"}, description = "Subscribes you to reminders about Tatsumaki's stuff or shows the status of your reminders. k!daily off unsubscribes you from those.", usage = "k!daily [off]", showInHelpPage = true)
    public void onCommand(DiscordAPI api, Message message, String args[])
    {
		if ((args.length > 0) && args[0].equalsIgnoreCase("off")) //k!daily off
			if (KyoukoBot.Database.isSubscribed(message.getAuthor()))
			{ //unsub if subbed
				KyoukoBot.Database.unsubscribe(message.getAuthor());
				message.reply("`You have unsubscribed from daily reminders.`");
			}
			else //user wasn't even subbed to begin with
				message.reply("`You are not subscribed to daily reminders.`");
		else
			if (KyoukoBot.Database.isSubscribed(message.getAuthor())) 
			{ //if subscribed, show the status
				long dailyDelay = KyoukoBot.Database.getDailyDelay(message.getAuthor());
				long repDelay = KyoukoBot.Database.getRepDelay(message.getAuthor());
				String msg = "`Time left until t!daily: ";
				if (dailyDelay == -1)
					msg += "unknown (type t!daily to set my timer)`";
				else
					msg += KyoukoBot.msToTimeString(dailyDelay) + "`";
				msg += '\n';
				msg += "`Time left until t!rep: ";
				if (repDelay == -1)
					msg += "unknown (type t!rep to set my timer)`";
				else
					msg += KyoukoBot.msToTimeString(repDelay) + "`";
				msg += "\n\n`Type k!daily off to unsubscribe from daily reminders.`";
				message.reply(msg);
			}
			else
			{ //if not subbed, subscribe them
				KyoukoBot.Database.subscribe(message.getAuthor());
				message.reply("`You have subscribed to daily reminders!`\n`Check your t!daily and t!rep status now!`\n\n`Type k!daily off to unsubscribe from daily reminders.`");
			}
    }
}
