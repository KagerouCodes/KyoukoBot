package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.CommandExecutor;
//base class for commands that just post a random link from an ImageCollection (imgur album)
public class ImgurPoster implements CommandExecutor
{
	private ImageCollection Collection;
	private String FileName;
	
	ImgurPoster(ImageCollection Collection, String FileName)
	{
		this.Collection = Collection;
		this.FileName = FileName;
	}
	
	public void onCommand(Message message)
	{
		message.getReceiver().type();
        KyoukoBot.postFile(message, Collection.getImage(true), FileName, "image");
	}
}
