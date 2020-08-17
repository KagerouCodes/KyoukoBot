package main.java.me.kagerou.kyoukobot;

import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//posts a random cat from random.cat, API for that site is very simple
public class DogCommand implements CommandExecutor {
    @Command(aliases = {"k!dog", "k!doggo", "k!woof", "k!wan"}, description = "Posts a random dog.")
    public void onCommand(Message message) {
        String DogURL;
        try {
            message.getReceiver().type();
            DogURL = new JSONObject(IOUtils.toString(new URL("https://random.dog/woof.json"),
                     Charset.forName("UTF-8"))).getString("url");
        }
        catch (Exception e) {
            e.printStackTrace();
            DogURL = KyoukoBot.OneDog;
        }
        KyoukoBot.postFile(message, DogURL, "dog", "image");
    }
}
