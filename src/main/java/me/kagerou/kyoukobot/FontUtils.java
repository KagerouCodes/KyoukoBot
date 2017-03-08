package me.kagerou.kyoukobot;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Arrays;

import com.google.common.collect.Iterables;

//static class for everything font-related
public class FontUtils
{
	static void printAllFonts()
	{
		for (Font f: GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
			System.out.println(f);
	}
	static boolean installFont(String name, String FileName)
	{ //installs the font from file if it's not registered already
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (Iterables.any(Arrays.asList(ge.getAllFonts()), x -> (x.getName().equalsIgnoreCase(name))))
			return false;
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/fonts/" + FileName)));
		}
		catch (Exception e)
		{
			System.out.println("Failed to register the font " + FileName);
			e.printStackTrace();
			return false;
		}
		System.out.println("Registered the font " + FileName + " successfully.");
		return true;
	}
	//returns the name of a font which can display the text
	static String appropriateFontName(String text, String... fontNames)
	{
		for (String fontName: fontNames)
			if (new Font(fontName, Font.BOLD, 72).canDisplayUpTo(text) == -1)
				return fontName;
		return fontNames[fontNames.length - 1];
	}
}
