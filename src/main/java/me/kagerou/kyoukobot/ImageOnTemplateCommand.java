package me.kagerou.kyoukobot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplUser;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.CommandExecutor;
//base class for commands which draw images on top of templates
public class ImageOnTemplateCommand extends TemplateLoader implements CommandExecutor {
	static class Triangle //what, there isn't one in awt??
	{
		Point2D.Double A, B, C;
		Triangle(Point2D.Double A, Point2D.Double B, Point2D.Double C)
		{
			this.A = A;
			this.B = B;
			this.C = C;
		}
		Triangle(double Ax, double Ay, double Bx, double By, double Cx, double Cy)
		{
			this(new Point2D.Double(Ax, Ay), new Point2D.Double(Bx, By), new Point2D.Double(Cx, Cy));
		}
		Triangle()
		{
			this(0, 0, 0, 0, 0, 0);
		}
	}
	
	enum StretchOption
	{
		STRETCH, //stretch images to fit areas perfectly
		SCALE_MIN, //stretch images in a way which would make 16:9 ones fir the areas perfectly, then scale to fit them entirely, leave white stripes on the sides
		SCALE_MAX; //scale images so that they would contain areas
	}
	
	private Triangle[] triangles; //areas to draw custom images in, point A of the triangle is the top-left corner of a resulting parallelogram, B is top-right corner, C is bottom-left
	private StretchOption stretch; //how to stretch custom images
	
	ImageOnTemplateCommand (String template_link, StretchOption stretch, Triangle... triangles)
	{
		super(template_link);
		this.triangles = triangles;
		this.stretch = stretch;
	}
	//returns an affine transform which translates triangle ABC to A1B1C1, thanks stackoverflow
	//don't ask me why parameters are not of my own Triangle type
	public static AffineTransform TriangleToTriangle (Point2D.Double A, Point2D.Double B, Point2D.Double C,
			Point2D.Double A1, Point2D.Double B1, Point2D.Double C1)
	{
		double x11 = A.getX();
		double x12 = A.getY();
		double x21 = B.getX();
		double x22 = B.getY();
		double x31 = C.getX();
		double x32 = C.getY();
		double y11 = A1.getX();
		double y12 = A1.getY();
		double y21 = B1.getX();
		double y22 = B1.getY();
		double y31 = C1.getX();
		double y32 = C1.getY();

		double a1 = ((y11-y21)*(x12-x32)-(y11-y31)*(x12-x22))/
                ((x11-x21)*(x12-x32)-(x11-x31)*(x12-x22));
		double a2 = ((y11-y21)*(x11-x31)-(y11-y31)*(x11-x21))/
                ((x12-x22)*(x11-x31)-(x12-x32)*(x11-x21));
		double a3 = y11-a1*x11-a2*x12;
		double a4 = ((y12-y22)*(x12-x32)-(y12-y32)*(x12-x22))/
                ((x11-x21)*(x12-x32)-(x11-x31)*(x12-x22));
		double a5 = ((y12-y22)*(x11-x31)-(y12-y32)*(x11-x21))/
                ((x12-x22)*(x11-x31)-(x12-x32)*(x11-x21));
		double a6 = y12-a4*x11-a5*x12;
		return new AffineTransform(a1, a4, a2, a5, a3, a6);
	}
	//draws an image on top of graphics in an parallelogramm area corresponding to ABC using the specified stretch option
	void DrawOnTop(Graphics2D graphics, BufferedImage image, Point2D.Double A, Point2D.Double B, Point2D.Double C, StretchOption stretch)
	{
		AffineTransform transform;
		BufferedImage img = image;
		if (stretch == StretchOption.STRETCH)
			transform = TriangleToTriangle(new Point2D.Double(), new Point2D.Double(image.getWidth(), 0.0), new Point2D.Double(0.0, image.getHeight()), A, B, C);
		else
		{
			double ratio = A.distance(B) / A.distance(C); //aspect ratio of the area
			System.out.println("Ratio = " + ratio);
			//calculate the needed transform by translating the center of the image to (0, 0) then determining coordinates of points to be translated into corners of the area
			double x, y;
			if (stretch == StretchOption.SCALE_MIN)
			{
				ratio = 16.0 / 9.0; //Potato's request, bad code
				x = Math.max(image.getHeight() * ratio, image.getWidth()) / 2.0;
				y = Math.max(image.getWidth() / ratio, image.getHeight()) / 2.0;
			}
			else
			{
				x = Math.min(image.getHeight() * ratio, image.getWidth()) / 2.0;
				y = Math.min(image.getWidth() / ratio, image.getHeight()) / 2.0;
				//cut the image to avoid the overflowing part of it to be drawn in the adjacent areas
				img = image.getSubimage(Math.max((int) (image.getWidth() / 2.0 - x), 0), Math.max((int) (image.getHeight() / 2.0 - y), 0), Math.min((int) (2 * x), image.getWidth()), Math.min((int) (2 * y), image.getHeight()));
			}
			transform = TriangleToTriangle(new Point2D.Double(-x, -y), new Point2D.Double(x, -y), new Point2D.Double(-x, y), A, B, C);
			transform.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
		}
		graphics.drawImage(img, transform, null);
	}
	
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args) { //TODO do something with https://2static2.fjcdn.com/thumbnails/comments/Yes+_12bb1d42b9794b90b53cae6196c9baed.png ??
		if (template == null)
		{
			message.reply("`Failed to load the template >_<`");
			return;
		}
		message.getReceiver().type();
		//determine the image first: it could be either an avatar of a specified user/author of the message or a linked/atttached image
		URL imgURL = null;
		String original_arg = KyoukoBot.getArgument(message, false);
		String arg = original_arg.toLowerCase();
		if (!message.getAttachments().isEmpty())
			imgURL = message.getAttachments().iterator().next().getUrl(); //use the attached image if there is one
		if (imgURL == null)
		{ //detecting usernames
			User target = null;
			if (arg.isEmpty())
				target = message.getAuthor(); //just use the author himself if there's no argument
			if (target == null)
				if (!message.getMentions().isEmpty())
					target = message.getMentions().get(0); //use the first mention if there is one
			if (target == null)
				target = KyoukoBot.findUserOnServer(arg, server, message.getAuthor()); //try to find a user otherwise
			if (target != null)
				imgURL = getAvatarUrl(target); //User#getAvatarUrl() doesn't always return up to date avatar, gotta implement it myself
				//imgURL = target.getAvatarUrl();
		}
		if (imgURL == null) //if everything else, use argument as URL
			try {
				imgURL = new URL(original_arg); //using the original one in case of a case-sensitive URL
			}
			catch (MalformedURLException e)
			{
				imgURL = null;
			}
		//loading the image
		boolean foundImage;
		String format = "";
		try {
			format = KyoukoBot.leTika.detect(imgURL);
			foundImage = (imgURL != null) && format.startsWith("image");
		}
		catch (Exception e)
		{
			foundImage = false;
		}
		if (!foundImage)
		{
			message.reply("`Couldn't find the user/image >_<`");
			return;
		}
		BufferedImage image;
		try {
			image = ImageIO.read(imgURL);
		}
		catch (IOException e)
		{
			message.reply("`Couldn't read the image >_<`");
			return;
		}
		//create the graphics and fill it with white colour first
		BufferedImage result = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(0, 0, width, height);
		//then draw the custom image and the template on top of it
		for (Triangle tri: triangles)
			DrawOnTop(graphics, image, tri.A, tri.B, tri.C, stretch);
		graphics.drawImage(template, 0, 0, null); //TODO maybe integration with Google Image Search??
		//sending the result
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(result, "PNG", os);
		 	message.getReceiver().sendFile(new ByteArrayInputStream(os.toByteArray()), "image" + KyoukoBot.DefaultMimeTypes.forName(format).getExtension());
		}
		catch (Exception e)
		{
			message.reply("`An error occured >_<`");
		 	return;
		}
	}
    //fetch the URL manually
	private URL getAvatarUrl(User user) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("https://discordapp.com/api/users/" + user.getId()).openConnection();
			conn.setRequestProperty("Authorization", "Bot " + KyoukoBot.token);
			conn.setRequestProperty("Content-Type", "application/json");
			JSONObject userJSON = new JSONObject(IOUtils.toString(conn.getInputStream(), "UTF-8"));
			ImplUser real_user = (ImplUser) user;
			real_user.setAvatarId(userJSON.getString("avatar"));
		}
		catch (Exception e)
		{e.printStackTrace();}
		return user.getAvatarUrl();
	}
}
