package me.kagerou.kyoukobot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class BreakingNewsCommand implements CommandExecutor {
	BufferedImage template;
	int width, height;
	
	BreakingNewsCommand (String template_link)
	{
		try {
			template = ImageIO.read(new URL(template_link));
			width = template.getWidth();
			height = template.getHeight();
		}
		catch (Exception e)
		{
			template = null;
			System.out.println("Failed to load the pretty template >_<");
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//for (Font f: ge.getAllFonts())
			//System.out.println(f);
	}
	
	String appropriateFontName(String text)
	{
		if (new Font("Signika", Font.PLAIN, 72).canDisplayUpTo(text) == -1)
			return "Signika";
		if (new Font("Calibri", Font.PLAIN, 72).canDisplayUpTo(text) == -1)
			return "Calibri";
		return "Arial Unicode MS";
	}
	
	@Command(aliases = {"k!news", "k!break", "k!breaking", "k!breakingnews"}, description = "Breaks your own news.", usage = "k!news image \u21B5 headline \u21B5 ticker")
    public void onCommand(DiscordAPI api, Message message, String[] args) {
		if (template == null)
		{
			message.reply("`Failed to load the template >_<`");
			return;
		}
		
		String headline = "", ticker = "";
		URL imgURL = null;
		int offset;
		String[] params = message.getContent().split("\\n");
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
		if (params.length > offset)
			headline = params[offset].toUpperCase();
		if (params.length > offset + 1)
			ticker = params[offset + 1].toUpperCase();
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
		
		BufferedImage result = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		double img_width = image.getWidth();
		double img_height = image.getHeight();
		AffineTransform at = new AffineTransform();
		at.translate(width / 2.0, height / 2.0);
		at.scale(Math.max(width / img_width, height / img_height), Math.max(width / img_width, height / img_height));
		at.translate(-img_width / 2, -img_height / 2);
		
		graphics.drawImage(image, at, null);
		graphics.drawImage(template, 0, 0, null);
		
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		/*String fontName = appropriateFontName(headline + ticker);
		Font smallFont = new Font(fontName, Font.BOLD, 28);
		Font bigFont = new Font(fontName, Font.BOLD, 72);*/	
		Font smallFont = new Font(appropriateFontName(ticker), Font.BOLD, 28);
		Font bigFont = new Font(appropriateFontName(headline), Font.BOLD, 72);
		
		graphics.setFont(new Font("Signika", Font.BOLD, 28));
		graphics.setColor(Color.decode("#FFFFFF"));
		graphics.drawString(new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()), 96, 660);
		
		graphics.setFont(bigFont);
		graphics.setColor(Color.decode("#000000"));
		graphics.drawString(headline, 100, 590);
		
		graphics.setFont(smallFont);
		graphics.setColor(Color.decode("#000000"));
		graphics.drawString(ticker, 200, 660);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		 try {
			 ImageIO.write(result, "PNG", os);
			 message.replyFile(new ByteArrayInputStream(os.toByteArray()), "news.png");
		 }
		 catch (Exception e)
		 {
			 message.reply("`An error occured >_<`");
			 return;
		 }
	}
}
