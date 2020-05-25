package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//posts le funny Shiyu face
public class ShiyuCommand implements CommandExecutor {
	final static String ShiyuFace = "https://i.imgur.com/c6etpGJ.png";
	final static String FakeShiyuFace = "http://www.asianloveslave.com/wp-content/uploads/2012/08/shiyu-china-shenzhen.jpg";
    @Command(aliases = {"k!shiyu"}, description = "Posts a picture of baby Shiyu's face.")
    public static void onCommand(Message message) {
    	message.getReceiver().type();
    	if (KyoukoBot.isAprilFools())
    		KyoukoBot.postFile(message, FakeShiyuFace, "shiyu");
    	else
    		KyoukoBot.postFile(message, ShiyuFace, "shiyu"); //TODO add a Shiyu emote to it 
    }
}