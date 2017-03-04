package me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//just a ping-pong command, the hello world of bots
public class PingCommand implements CommandExecutor {
    @Command(aliases = {"k!ping"}, description = "Pong!")
    public String onCommand(String command, String[] args) {
        return "Pong!";
    }
}