package me.kagerou.kyoukobot;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

//base class for all the commands which draw/write something on top of a template image
public class TemplateLoader {
	BufferedImage template;
	int width, height;
	
	TemplateLoader(String template_link)
	{ //load the template, lol
		try {
			template = ImageIO.read(new URL(template_link));
			width = template.getWidth();
			height = template.getHeight();
		}
		catch (Exception e)
		{
			template = null;
			width = height = 0;
			System.out.println("Failed to load the template " + template_link + " >_<");
			e.printStackTrace();
		}
	}
}
