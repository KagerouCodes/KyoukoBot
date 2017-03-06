package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
//posts a random picture from the imgur album with emilia, maybe there should be an ImgurCommand super class for this type of commands
public class EMTCommand extends ImgurPoster {
    EMTCommand(ImageCollection Collection, String FileName) {
		super(Collection, FileName);
	}

	@Command(aliases = {"k!EMT", "k!Emilia", "k!Emilia-tan"}, description = "Posts a picture of Emilia-tan.")
    public void onCommand(Message message) {
		super.onCommand(message);
    }
}
