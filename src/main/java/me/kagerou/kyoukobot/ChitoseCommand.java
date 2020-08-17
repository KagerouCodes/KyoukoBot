package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
//posts a random picture from the imgur album with Chitose, maybe there should be an ImgurCommand super class for this type of commands
public class ChitoseCommand extends ImgurPoster {
    ChitoseCommand(ImageCollection Collection, String FileName) {
        super(Collection, FileName);
    }

    @Command(aliases = {"k!Chitose"}, description = "Posts a picture of Chitose.")
    public void onCommand(Message message) {
        super.onCommand(message);
    }
}