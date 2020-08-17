package main.java.me.kagerou.kyoukobot;

import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//posts a random cat from random.cat, API for that site is very simple
public class CatCommand implements CommandExecutor {
    @Command(aliases = {"k!cat", "k!neko", "k!meow", "k!nya"}, description = "Posts a random cat.")
    public void onCommand(Message message) {
        String CatURL;
        try {
            message.getReceiver().type();
            CatURL = new JSONObject(IOUtils.toString(new URL("https://aws.random.cat/meow"), Charset.forName("UTF-8"))).getString("file");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            CatURL = KyoukoBot.OneCat;
        }
        // KyoukoBot.postFile(message, CatURL, "cat", "image");
        message.reply(CatURL);
    }
}
