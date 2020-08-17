package main.java.me.kagerou.kyoukobot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

//a command which writes text on the blackboard: https://i.imgur.com/mHZxFlV.png
//TODO lift the text in k!correct up a bit?? 
//TODO DK Crayon Crumble font for it??
//TODO find a font with :weary: :ok_hand: and other emojis
public class CorrectCommand extends TemplateLoader implements CommandExecutor
{
	Rectangle2D.Float bounds = new Rectangle2D.Float(72, 48, 718, 321); //the rectangle to fit the text in
	//final int MAX_LINES = 10; //max amount of lines, should be redundant by now but i'm too lazy
	CorrectCommand(String template_link)
	{
		super(template_link);
		FontUtils.installFont("AR CENA", "ARCENA.ttf");
		FontUtils.installFont("Comic Sans MS", "comici.ttf");
		FontUtils.installFont("Arial Unicode MS", "Arial_Unicode_MS.ttf");
	}
	
	@Command(aliases = {"k!correct"}, description = "Turns your words into undeniable truth.", usage = "k!correct text")
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args)
    {
		message.getReceiver().type();
		String arg = KyoukoBot.getArgument(message, false);
		if (arg.isEmpty())
			arg = "Anime was a mistake.";
		//construct the list of lines since it's resizeable
		//can't use Arrays.asList() because that's just a wrapper and won't support resizing 
		String[] lines_array = arg.split("\\n");
		//int number_of_lines = Math.min(lines_array.length, MAX_LINES);
		ArrayList<String> lines = new ArrayList<String>();
		for (String str: lines_array)
			lines.add(str.trim());
		//for (int index = 0; index < number_of_lines; index++)
//			lines.add(lines_array[index].trim());
		
		//determine the font which can display the text
		String FontName = FontUtils.appropriateFontName(arg, "AR CENA", "Comic Sans MS", "Arial Unicode MS");
		//draw the template first
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		graphics.drawImage(template, 0, 0, null);
		graphics.setColor(Color.WHITE);
		//graphics.drawLine((int) bounds.getX(), (int) bounds.getMaxY(), (int) bounds.getMaxX(), (int) bounds.getMaxY());
		FontRenderContext frc = graphics.getFontRenderContext();
		ArrayList<Point2D.Float> TextLayout = new ArrayList<Point2D.Float>();
		for (int i = 0; i < lines.size(); i++)
			TextLayout.add(null); //i wish i was coding in C++ right now
		//determine the font size and cut the lines if needed
		int FontSize = appropriateFontSize(lines, bounds, FontName, frc, TextLayout, 84, 72, 48, 36);
		//the actual writing
		Font font = new Font(FontName, Font.ITALIC, FontSize);
		graphics.setFont(font);
		for (int line = 0; line < lines.size(); line++)
		{
			String cur_line = lines.get(line);
			graphics.drawString(cur_line, TextLayout.get(line).x, TextLayout.get(line).y);
		}
		//send the image
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(result, "PNG", os);
			message.getReceiver().sendFile(new ByteArrayInputStream(os.toByteArray()), "correct.png");
		}
		catch (Exception e)
		{
			message.reply("`An error occured >_<`");
			return;
		}
    }
	//determines the font size which would let writing the text in the given area
	int appropriateFontSize(ArrayList<String> lines, Rectangle2D.Float bounds, String FontName, FontRenderContext frc, ArrayList<Point2D.Float> TextLayout, int... sizes)
	{
		for (int FontSize: sizes)
		{ //going through all the given sizes in order
			ArrayList<String> lines_copy = new ArrayList<String>(lines);
			float ascent_line = bounds.y; //ascent line of the current line
			Font font = new Font(FontName, Font.ITALIC, FontSize);
			boolean fits = true;
			TextLayout.clear();
			int line;
			for (line = 0; line < lines_copy.size(); line++)
			{
				String cur_line = lines_copy.get(line);
				LineMetrics metrics = font.getLineMetrics(cur_line, frc);
				float line_width = (float) font.getStringBounds(cur_line, frc).getWidth();
				if (line_width > bounds.width)
				{ //the line's too wide, try to cut it
					//try to fit as many words into width as possible
					int fits_index = 0, next_index = 0;
					boolean substr_fits = true;
					while ((next_index < cur_line.length()) && substr_fits)
					{
						next_index = cur_line.indexOf(' ', fits_index + 1);
						if (next_index == -1)
							next_index = cur_line.length();
						if (font.getStringBounds(cur_line.substring(0, next_index), frc).getWidth() <= bounds.width)
						{
							fits_index = next_index;
							line_width = (float) font.getStringBounds(cur_line.substring(0, next_index), frc).getWidth();
						}
						else
							substr_fits = false;
					}
					//if not even a single word fits, just try to fit as many symbols
					substr_fits = true;
					if (fits_index == 0)
						for (next_index = 1; (next_index < cur_line.length()) && substr_fits; next_index++)
							if ((float) font.getStringBounds(cur_line.substring(0, next_index), frc).getWidth() <= bounds.width)
							{
								fits_index = next_index;
								line_width = (float) font.getStringBounds(cur_line.substring(0, next_index), frc).getWidth();
							}
							else
								substr_fits = false;
					//cut the current line, add the rest to the next one with a spacebar (unless we're at MAX_LINES already)
					//if (line < MAX_LINES - 1)
					//{
					if (line != lines_copy.size() - 1)
						lines_copy.add(line + 1, cur_line.substring(fits_index).trim());
						//lines_copy.set(line + 1, (cur_line.substring(fits_index) + ' ' + lines_copy.get(line + 1)).trim());
					else
					{
						String new_line = cur_line.substring(fits_index).trim();
						if (!new_line.isEmpty())
							lines_copy.add(new_line);
					}
					lines_copy.set(line, cur_line.substring(0, fits_index));
					//}
					//else
						//lines_copy.set(line, cur_line.substring(0, fits_index).trim());
				}
				if (ascent_line + metrics.getAscent() > bounds.getMaxY())
				{
					fits = false;
					break;
				}
				//memorise current coordinates in TextLayout
				if (TextLayout.size() == line)
					TextLayout.add(null);
				System.out.println("Font size: " + FontSize + ", line number " + line + ".");
				TextLayout.set(line, new Point2D.Float(bounds.x + (bounds.width - line_width) / 2, ascent_line + metrics.getAscent()));
				ascent_line += metrics.getAscent() + metrics.getLeading();
			}
			if (fits || FontSize == sizes[sizes.length - 1]) //success! or we're down to the last option
			{
				lines.clear();
				//for (String str: lines_copy)
					//lines.add(str);
				for (int index = 0; index < line; index++)
					lines.add(lines_copy.get(index));
				float offset = (float) bounds.getMaxY() - TextLayout.get(TextLayout.size() - 1).y;
				for (int index = 0; index < TextLayout.size(); index++)
					TextLayout.get(index).setLocation(TextLayout.get(index).getX(), TextLayout.get(index).getY() + offset);
				return FontSize;
			}
		}
		return 0; //should never trigger
	}

}
