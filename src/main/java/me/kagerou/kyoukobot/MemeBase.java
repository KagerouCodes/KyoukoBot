package me.kagerou.kyoukobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class MemeBase {
	static long FileSizeLimit = 8000000; //not sure if it's 
	private String dirName;
	MemeBase (String memeDir)
	{
		dirName = memeDir;
	}
	
	enum MemeResult
	{
		DR_OK, DR_FAIL, DR_DUPE, DR_LIMIT; 
	}
	
	private String FullDirName() {
		return System.getProperty("user.dir") + "/" + dirName + "/";
	}
	
	synchronized MemeResult DownloadImage(URL imgURL) //TODO check for size!
	{
		URLConnection leConnection;
		String type;
		try {
			leConnection = imgURL.openConnection();
			type = leConnection.getContentType();
			if (type == null)
				type = KyoukoBot.leTika.detect(imgURL);
			if (!type.startsWith("image"))
			{
				System.out.println(imgURL + " does not link to an image!");
				return MemeResult.DR_FAIL;
			}
			if (leConnection.getContentLength() > FileSizeLimit)
			{
				System.out.println("File " + imgURL + " is too big!");
				return MemeResult.DR_FAIL;
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to " + imgURL);
			return MemeResult.DR_FAIL;
		}
		String fullDirName = FullDirName();
		File MemesDir = new File(fullDirName);
		if (!MemesDir.isDirectory())
			MemesDir.mkdirs();
		/*ArrayList<String> filenames = new ArrayList<String> (Arrays.asList(MemesDir.list()));
		Collections.sort(filenames);*/
		try {
			String hash = DigestUtils.md5Hex(leConnection.getInputStream());
			String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
			File imgFile = new File(fullDirName + hash + ext);
			if (imgFile.exists())
				return MemeResult.DR_DUPE;
			FileUtils.copyURLToFile(imgURL, imgFile);
		} catch (Exception e) {
			e.printStackTrace();
			return MemeResult.DR_FAIL;
		}
		return MemeResult.DR_OK;
	}

	synchronized MemeResult DownloadImage(String imgLink)
	{
		try {
			return DownloadImage(new URL(imgLink));
		}
		catch (MalformedURLException e)
		{
		}
		if (imgLink.contains("."))
			try {
				return DownloadImage(new URL("https://" + imgLink));
			}
			catch (MalformedURLException e)
			{
			}
		return MemeResult.DR_FAIL;
	}
	
	synchronized MemeResult DeleteImage(URL imgURL) //the function's way too similar to DownloadImage...
	{
		URLConnection leConnection;
		String type;
		try {
			leConnection = imgURL.openConnection();
			type = leConnection.getContentType();
			if (type == null)
				type = KyoukoBot.leTika.detect(imgURL);
			if (!type.startsWith("image"))
			{
				System.out.println(imgURL + " does not link to an image!");
				return MemeResult.DR_FAIL;
			}
			if (leConnection.getContentLength() > FileSizeLimit)
			{
				System.out.println("File " + imgURL + " is too big!");
				return MemeResult.DR_FAIL;
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to " + imgURL);
			return MemeResult.DR_FAIL;
		}
		String fullDirName = FullDirName();
		File MemesDir = new File(fullDirName);
		if (!MemesDir.isDirectory())
		{
			MemesDir.mkdirs();
			return MemeResult.DR_DUPE;
		}
		/*ArrayList<String> filenames = new ArrayList<String> (Arrays.asList(MemesDir.list()));
		Collections.sort(filenames);*/
		try {
			String hash = DigestUtils.md5Hex(leConnection.getInputStream());
			String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
			File imgFile = new File(fullDirName + hash + ext);
			if (!imgFile.exists())
				return MemeResult.DR_DUPE;
			imgFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return MemeResult.DR_FAIL;
		}
		return MemeResult.DR_OK;
	}
	
	synchronized MemeResult DeleteImage(String imgLink)
	{
		try {
			return DeleteImage(new URL(imgLink));
		}
		catch (MalformedURLException e)
		{
		}
		if (imgLink.contains("."))
			try {
				return DeleteImage(new URL("https://" + imgLink));
			}
			catch (MalformedURLException e)
			{
			}
		return MemeResult.DR_FAIL;
	}
	
	synchronized File GetMeme()
	{
		String fullDirName = FullDirName();
		File MemesDir = new File(fullDirName);
		ArrayList<String> filenames = new ArrayList<String>();
		if (MemesDir.isDirectory())
			filenames = new ArrayList<String> (Arrays.asList(MemesDir.list()));
		if (filenames.isEmpty())
			return null;
		String LeMeme = filenames.get(new Random().nextInt(filenames.size()));
		return new File(fullDirName + LeMeme);
	}
	
	synchronized String reHash()
	{
		String fullDirName = FullDirName();
		File MemesDir = new File(fullDirName);
		ArrayList<File> files = new ArrayList<File>();
		if (MemesDir.isDirectory())
			files = new ArrayList<File> (Arrays.asList(MemesDir.listFiles()));
		if (files.isEmpty())
			return "`I-I'm out of memes >_< But you can always k!donate them to me!`";
		try {
			for (int index = 0; index < files.size(); index++)
			{
				File tmp = Files.createTempFile(Paths.get(fullDirName), null, null).toFile();
				tmp.delete();
				if (files.get(index).renameTo(tmp))
					files.set(index, tmp);
			}
		}
		catch (IOException e)
		{
			return "`Failed to create temp files somehow >_<`";
		}
		int dupes = 0;
		try {
			for (int index = 0; index < files.size(); index++)
			{
				FileInputStream fis = new FileInputStream(files.get(index));
				String hash = DigestUtils.md5Hex(fis);
				String type = KyoukoBot.leTika.detect(files.get(index));
				String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
				fis.close();
				File dest = new File(fullDirName + hash + ext);
				if (dest.exists())
				{
					files.get(index).delete();
					dupes++;
				}
				else
					files.get(index).renameTo(dest);
			}
		}
		catch (Exception e)
		{
			return "`Failed to rehash somehow >_<`";
		}
		return "`Rehashed " + (files.size() - dupes) + " files successfully!" + ((dupes > 0) ? (" Deleted " + dupes + " duplicates.") : "") + "`";
	}
}
