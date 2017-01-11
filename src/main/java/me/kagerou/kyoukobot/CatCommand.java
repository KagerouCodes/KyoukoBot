package me.kagerou.kyoukobot;

import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class CatCommand implements CommandExecutor {
	@Command(aliases = {"k!cat", "k!neko", "k!meow", "k!nya"}, description = "Posts a random cat.")
    public void onCommand(Message message) {
		String CatURL;
		try {
			CatURL = new JSONObject(IOUtils.toString(new URL("http://random.cat/meow"), Charset.forName("UTF-8"))).getString("file");
		}
		catch (Exception e)
		{
			CatURL = KyoukoBot.OneCat;
		}
		//message.reply("", new EmbedBuilder().setImage(CatURL));
		KyoukoBot.postFile(message, CatURL, "cat", "image");
    }
}
