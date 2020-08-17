package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Random;

import com.sangupta.imgur.api.ImgurClient;
import com.sangupta.imgur.api.model.Image;
//stores links to pictures from an imgur album for commands like k!EMT and k!Chitose
public class ImageCollection
{
    ArrayList<String> links = new ArrayList<String>(); // links themselves
    String single_pic, album; // a single fallback picture and the album name
    ImgurClient client;
    
    ImageCollection(ImgurClient client, String album, String single_pic) 
    {
        this.client = client;
        this.album = album;
        this.single_pic = single_pic;
        load();
    }
    // loads the links
    private void load()
    {
        links.clear();
        System.out.println("Loading imgur album " + album + "...");
        if (client == null) {
            System.out.println("Imgur API secrets are missing.");
            return;
        }

        try {
            for (Image img: client.getAlbumDetails(album).data.images) {
                links.add(img.link);
            }
            if (links.isEmpty()) {
                throw new Exception();
            }
            System.out.println("Imgur album " + album + " loaded successfully!");
        }
        catch (Exception e)
        {
            System.out.println("Failed to load imgur album " + album + "!");
        }
    }
    //returns a random link from the collection
    //if it's not loaded and load_if_empty == true, attempts to load again
    //if it's still empty, returns the fallback image
    String getImage(boolean load_if_empty)
    {
        if (links.isEmpty() && load_if_empty)
            load();
        if (links.isEmpty())
            return single_pic;
        return links.get(new Random().nextInt(links.size()));
    }
}
