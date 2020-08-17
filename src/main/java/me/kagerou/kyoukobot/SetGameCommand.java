package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//sets the game currently played by the bot and memorises it in the DB
public class SetGameCommand implements CommandExecutor {
    @Command(aliases = {"k!setgame"}, description = "Cheesy admin-only command.", usage = "k!setgame game", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String[] args)
    {
        String game = KyoukoBot.getArgument(message, false);
        KyoukoBot.Database.setGame(game);
        api.setGame(game);
    }
}