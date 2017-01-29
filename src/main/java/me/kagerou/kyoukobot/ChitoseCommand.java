package me.kagerou.kyoukobot;

import java.util.Random;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ChitoseCommand implements CommandExecutor {
	@Command(aliases = {"k!Chitose"}, description = "Posts a picture of Chitose.")
    public void onCommand(Message message) {
        	String Chitose = KyoukoBot.AllChitoses.get(new Random().nextInt(KyoukoBot.AllChitoses.size()));
        	//message.reply("", new EmbedBuilder().setImage(Chitose));
        	KyoukoBot.postFile(message, Chitose, "Chitose", "image");
    }
}