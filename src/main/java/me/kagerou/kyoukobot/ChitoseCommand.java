package me.kagerou.kyoukobot;

import java.util.Random;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//posts a random picture from the imgur album with Chitose, maybe there should be an ImgurCommand super class for this type of commands
public class ChitoseCommand implements CommandExecutor {
	@Command(aliases = {"k!Chitose"}, description = "Posts a picture of Chitose.")
    public void onCommand(Message message) {
		message.getReceiver().type();
        String Chitose = KyoukoBot.AllChitoses.get(new Random().nextInt(KyoukoBot.AllChitoses.size()));
        KyoukoBot.postFile(message, Chitose, "Chitose", "image");
    }
}