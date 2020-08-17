package main.java.me.kagerou.kyoukobot;

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
import org.apache.commons.io.FilenameUtils;
//handles uploading and downloading pictures from/to the dirName subdirectory
//the filename format is <md5>.<extension for the content type>
public class MemeBase
{
	static long FileSizeLimit = 8000000; //it's 8 MB, not 8 MiB, right?
	private String dirName; //"memes"
	MemeBase (String memeDir)
	{
		dirName = memeDir;
	}
	
	enum MemeResult
	{ //results for downloading/uploading a single picture
		DR_OK, //success
		DR_FAIL, //picture not found or is too big
		DR_DUPE, //the image to add is already there or the one to be deleted doesn't exist in the directory
		DR_LIMIT; //currently unused
	}
	
	private String FullDirName() {
		return System.getProperty("user.dir") + "/" + dirName + "/";
	}
	//tries to download an image at imgURL
	synchronized MemeResult DownloadImage(URL imgURL)
	{
		URLConnection leConnection;
		String type;
		try {
			leConnection = imgURL.openConnection();
			/*type = leConnection.getContentType();
			if (type == null) //getContentType() fails sometimes*/
				type = KyoukoBot.leTika.detect(imgURL);
			if (!type.startsWith("image"))
			{ //not an image
				System.out.println(imgURL + " does not link to an image!");
				return MemeResult.DR_FAIL;
			}
			if (type.equals("image/svg+xml"))
			{
				System.out.println("SVG format is not supported by Discord.");
				return MemeResult.DR_FAIL;
			}
			if (leConnection.getContentLength() > FileSizeLimit)
			{ //file's too big
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
		//determine the filename to save the picture as, if the file already exists, the dupe's detected
		try {
			String hash = DigestUtils.md5Hex(leConnection.getInputStream());
			String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
			File imgFile = new File(fullDirName + hash + ext);
			if (imgFile.exists())
				return MemeResult.DR_DUPE;
			FileUtils.copyURLToFile(imgURL, imgFile);
			if (imgFile.length() > FileSizeLimit) //i heard getContentLength() fails at times too
			{
				imgFile.delete();
				System.out.println("File " + imgURL + " is too big!");
				return MemeResult.DR_FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MemeResult.DR_FAIL;
		}
		return MemeResult.DR_OK;
	}
	//the same as previous function but URL is passed as a String
	synchronized MemeResult DownloadImage(String imgLink)
	{
		try {
			return DownloadImage(new URL(imgLink));
		}
		catch (MalformedURLException e)
		{
		}
		if (imgLink.contains(".")) //just in case the link's passed without the http(s)://
			try {
				return DownloadImage(new URL("https://" + imgLink));
			}
			catch (MalformedURLException e)
			{
			}
		return MemeResult.DR_FAIL;
	}
	//tries to delete an image at imgURL from the "meme" directory
	//the function's way too similar to DownloadImage...
	synchronized MemeResult DeleteImage(URL imgURL) 
	{
		URLConnection leConnection;
		String type;
		try {
			leConnection = imgURL.openConnection();
			/*type = leConnection.getContentType();
			if (type == null)
			{
				System.out.println("Needed to use Tika to identify the image type.");*/
				type = KyoukoBot.leTika.detect(imgURL);
			//}
			System.out.println("Image type: " + type);
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
		try {
			String hash = DigestUtils.md5Hex(leConnection.getInputStream());
			String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
			System.out.println("Expected file name: " + hash + ext);
			File imgFile = new File(fullDirName + hash + ext);
			// if the file doesn't exist, search for the file with the same name
			// this works around some weird issue of Discord changing images and nobody's going to fake md5 hashes
			if (!imgFile.exists())
			{
				String FileName = FilenameUtils.getName(imgURL.toString());
				if (FilenameUtils.getExtension(FileName).isEmpty())
					FileName += ext;
				imgFile = new File(fullDirName + FileName);
			}
			if (!imgFile.exists())
				return MemeResult.DR_DUPE;
			imgFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return MemeResult.DR_FAIL;
		}
		return MemeResult.DR_OK;
	}
	//the same as previous function but URL is passed as a String
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
	//returns a random file from the directory as a File
	synchronized File getMeme()
	{
		String fullDirName = FullDirName();
		File MemesDir = new File(fullDirName);
		ArrayList<String> filenames = new ArrayList<String>(); //File#listFiles() exist but i'd rather not create a whole bunch of File objects for no reason
		if (MemesDir.isDirectory())
			filenames = new ArrayList<String> (Arrays.asList(MemesDir.list()));
		if (filenames.isEmpty())
			return null;
		String LeMeme = filenames.get(new Random().nextInt(filenames.size()));
		return new File(fullDirName + LeMeme);
	}
	//renames all the files in the directory according to the format, returns a String with the result
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
			{ //can't rename files instantly in case other files with the needed names exist, so renaming to temp files instead
				File tmp = Files.createTempFile(Paths.get(fullDirName), null, null).toFile();
				tmp.delete();
				if (files.get(index).renameTo(tmp))
					files.set(index, tmp); //replace the File in the ArrayList with a temporary one because rename() doesn't actually change the filename associated with the File 
			}
		}
		catch (IOException e)
		{
			return "`Failed to create temp files somehow >_<`";
		}
		int dupes = 0, non_images = 0;
		try {
			for (int index = 0; index < files.size(); index++)
			{ //determine the correct name for the file
				FileInputStream fis = new FileInputStream(files.get(index));
				String hash = DigestUtils.md5Hex(fis);
				String type = KyoukoBot.leTika.detect(files.get(index));
				if (!type.startsWith("image"))
				{ //not an image
					files.get(index).delete();
					non_images++;
					continue;
				}
				String ext = KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
				fis.close();
				File dest = new File(fullDirName + hash + ext);
				if (dest.exists())
				{ //a files with the same md5 and content type already exists
					files.get(index).delete();
					dupes++;
				}
				else
					files.get(index).renameTo(dest); //renaming the temp file
			}
		}
		catch (Exception e)
		{
			return "`Failed to rehash somehow >_<`";
		}
		String deleted = "";
		if (dupes > 0)
		{
			deleted = " Deleted " + dupes + " duplicates";
			if (non_images > 0)
				deleted += " and " + non_images + " non-images"; 
			deleted += ".";
		}
		else
			if (non_images > 0)
				deleted = " Deleted " + non_images + " non-images.";
		return "`Rehashed " + (files.size() - dupes) + " files successfully!" + deleted + "`"; 
		//((dupes > 0) ? (" Deleted " + dupes + " duplicates.") : "") + "`";
	}
}
