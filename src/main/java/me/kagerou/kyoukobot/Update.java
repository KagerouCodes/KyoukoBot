package me.kagerou.kyoukobot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Update {

	public static void main(String[] args) {
		if (args.length != 2)
		{
			System.out.println("Usage: java -jar update.jar tmpfile jarfile");
			return;
		}
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			System.out.println("Interrupted!");
			return;
		}
		File tmpFile = new File(args[0]);
		File jarFile = new File(args[1]);
		if (jarFile.delete())
			System.out.println("Deleted " + args[1] + " successfully!");
		else
			System.out.println("File " + args[1] + " not found.");
		if (tmpFile.renameTo(jarFile))
			System.out.println("Renamed " + args[0] + " to " + args[1] + ".");
		else
			System.out.println("Failed to rename " + args[0] + " to " + args[1] + ".");
		try {
			Runtime.getRuntime().exec("java -jar " + args[1]);
			//Runtime.getRuntime().exec("xterm -e java -jar " + args[1]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try {
			FileUtils.writeStringToFile(new File("kek.txt"), "top kek", "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
