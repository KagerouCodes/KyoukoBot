package me.kagerou.kyoukobot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
// puts your text on a breaking news template like breakyourownnews.com (but with no watermark)
public class BreakingNewsCommand extends TemplateLoader implements CommandExecutor
{
	BreakingNewsCommand (String template_link)
	{ //read the template and install three fonts
		super(template_link);
		FontUtils.installFont("Signika-Bold", "Signika-Bold.ttf");
		FontUtils.installFont("Calibri Bold", "CalibriB.ttf");
		FontUtils.installFont("Arial Unicode MS", "Arial_Unicode_MS.ttf");
	}	
	// Usage (awkward, i know):
	// k!news imglink        k!news headline
	// headline         OR   ticker
	// ticker                (w/ an image attachment)
	@Command(aliases = {"k!news", "k!break", "k!breaking", "k!breakingnews"}, description = "Breaks your own news.", usage = "k!news image \u21B5 headline \u21B5 ticker\nOR k!news headline \u21B5 ticker (with an image attachment)")
    public void onCommand(DiscordAPI api, Message message, String[] args) {
		if (template == null)
		{
			message.reply("`Failed to load the template >_<`");
			return;
		}
		message.getReceiver().type();
		String headline = "", ticker = "";
		URL imgURL = null;
		int offset; //the position in params where the headline is stored
		String[] params = message.getContent().split("\\n"); //can't really split random lines of text with anything else 
		if (params.length > 0)
			if (params[0].indexOf(' ') == -1)
				params[0] = "";
			else
				params[0] = params[0].substring(params[0].indexOf(' ')).trim();
		if (!message.getAttachments().isEmpty())
		{
			imgURL = message.getAttachments().iterator().next().getUrl();
			offset = 0;
		}
		else
		{
			try {
				imgURL = new URL(params[0]);
			}
			catch (MalformedURLException e)
			{
				imgURL = null;
			}
			offset = 1;
		}
		//if there are not enough parameters, leave the lines empty
		if (params.length > offset)
			headline = params[offset].toUpperCase();
		if (params.length > offset + 1)
			ticker = params[offset + 1].toUpperCase();
		//loading the image
		BufferedImage image = null;
		boolean foundImage;
		try {
			String format = KyoukoBot.leTika.detect(imgURL);
			foundImage = (imgURL != null) && format.startsWith("image");
		}
		catch (Exception e)
		{
			foundImage = false;
		}
		try {
			image = ImageIO.read(imgURL);
		}
		catch (Exception e)
		{
			foundImage = false;
		}
		if (!foundImage)
		{
			message.reply("`Couldn't read the image >_<`");
			return;
		}
		//the drawing code itself is pretty much copied from breakyourownnews.com
		//scale the picture to get inside contain the template while not changing the proportions
		BufferedImage result = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		double img_width = image.getWidth();
		double img_height = image.getHeight();
		AffineTransform at = new AffineTransform();
		at.translate(width / 2.0, height / 2.0);
		at.scale(Math.max(width / img_width, height / img_height), Math.max(width / img_width, height / img_height));
		at.translate(-img_width / 2, -img_height / 2);
		//draw the template on top of the picture
		graphics.drawImage(image, at, null);
		graphics.drawImage(template, 0, 0, null);
		//turn on the anti-aliasing
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		//figure out the appropriate fonts
		Font smallFont = new Font(FontUtils.appropriateFontName(ticker, "Signika-Bold", "Calibri Bold", "Arial Unicode MS"), Font.BOLD, 28);
		System.out.println(smallFont);
		Font bigFont = new Font(FontUtils.appropriateFontName(headline, "Signika-Bold", "Calibri Bold", "Arial Unicode MS"), Font.BOLD, 72);
		System.out.println(bigFont);
		//current time
		graphics.setFont(new Font("Signika-Bold", Font.BOLD, 28));
		graphics.setColor(Color.decode("#FFFFFF"));
		graphics.drawString(new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()), 96, 660);
		//write the headline
		graphics.setFont(bigFont);
		graphics.setColor(Color.decode("#000000"));
		graphics.drawString(headline, 100, 590);
		//write the ticker		
		graphics.setFont(smallFont);
		graphics.setColor(Color.decode("#000000"));
		graphics.drawString(ticker, 200, 660);
		//post the picture and delete the post with the command if possible
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		 try {
			 ImageIO.write(result, "PNG", os);
			 boolean can_manage = false;
			 if (!message.isPrivateMessage())
				 can_manage = Iterables.any(api.getYourself().getRoles(message.getChannelReceiver().getServer()), (role) -> role.getPermissions().getState(PermissionType.MANAGE_MESSAGES) == PermissionState.ALLOWED);
			 if (can_manage)
				 System.out.println("I am allowed to delete messages here.");
			 else
				 System.out.println("I'm NOT allowed to delete messages here!");
			 final boolean final_can_manage = can_manage;
			 message.getReceiver().sendFile(new ByteArrayInputStream(os.toByteArray()), "news.png", "News from " + message.getAuthor().getMentionTag() + ":", 
					new FutureCallback<Message>() {
	        			@Override
	        			public void onSuccess(Message msg) {
	        				try {
	        					message.delete().get();
	        				}
	        				catch (ExecutionException | InterruptedException e)
	        				{	
	        					if (final_can_manage)
	        					{
	        						System.out.println("Failed to delete the message " + message.getId() + ".");
	        						e.printStackTrace();
	        					}
	        				}
	        			}
	            		@Override
	            		public void onFailure(Throwable t) {
	            			System.out.println("Failed to break the news >_<");
	            			t.printStackTrace();
	            			
	            		}
	        		});
		 }
		 catch (Exception e)
		 {
			 message.reply("`An error occured >_<`");
			 return;
		 }
	}
}
