package script.ase;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.util.MyImageUtil;

public class MainEntry {

	public static void main(String[] args) throws Exception
	{
		
//		writeImage("2015-03-20 17:24:02.739", "2015-03-20 17:24:02.739", "D:/temp/1.png");
//		writeImage("2015-03-20 17:25:21.139", "2015-03-20 17:25:21.139", "D:/temp/2.png");
//		writeImage("2015-03-20 17:25:30.331", "2015-03-20 17:25:30.331", "D:/temp/3.png");
//		writeImage("2015-03-20 15:44:34.056", "2015-03-20 15:44:34.056","D:/temp/4.png");
//		writeImage("2015-03-20 15:44:43.528", "2015-03-20 15:44:43.528", "D:/temp/5.png");
		
		writeImage("2015-04-26 13:48:52.687", "2015-04-26 13:48:52.687", "D:/temp/6.png");
		
		
		//writeImage("2015-03-20 15:36:44.493", "2015-03-20 15:36:44.493", "D:/temp/6.png");
	}
	
	public static void writeImage(String time, String time2, String out) throws Exception
	{
		System.out.println(time);
		DBImpl db = new DBImpl();
		
		LowLevelInteraction i = db.getAnInteractionsWithScreen(time, "baolingfeng");
		BufferedImage img = i.getScreen();
		if(img == null)
		{
			LowLevelInteraction i2 = db.getAnInteractionsWithScreen(time2, "baolingfeng");
			img = i2.getScreen();
		}
		
		BufferedImage img2 = MyImageUtil.drawCircleOnImage(img, i.getPx(), i.getPy(), 6);
		BufferedImage img3 = MyImageUtil.drawRectOnImage(img2, i.getUiBoundLeft(), i.getUiBoundTop(),
				i.getUiBoundRight()-i.getUiBoundLeft(), i.getUiBoundBottom()-i.getUiBoundTop());
		
		ImageIO.write(img3, "png", new FileOutputStream(out));
	}
	
}
