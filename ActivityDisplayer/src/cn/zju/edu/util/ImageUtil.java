package cn.zju.edu.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageUtil {
	public static Image getScaledImage(Image srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();
	    
	    return resizedImg;
	}
	
	public static BufferedImage drawCircleOnImage(BufferedImage img, int x, int y, int r)
	{
		BufferedImage copyImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = copyImg.createGraphics();
		g2.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		
		g2.setColor(Color.red);
		
		g2.drawOval(x, y, r, r);
		g2.dispose();
		
		return copyImg;
	}
	
	public static BufferedImage drawRectOnImage(BufferedImage img, int x, int y, int w, int h)
	{
		BufferedImage copyImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = copyImg.createGraphics();
		g2.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		
		g2.setColor(Color.BLUE);
		
		g2.drawRect(x, y, w, h);
		g2.dispose();
		
		return copyImg;
	}
}
