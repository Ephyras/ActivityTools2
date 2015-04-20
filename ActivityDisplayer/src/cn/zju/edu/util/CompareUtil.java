package cn.zju.edu.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;
import cn.zju.edu.blf.db.*;
import cn.zju.edu.blf.dao.*;

public class CompareUtil {
	private static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        try {
                BufferedReader in = new BufferedReader(new FileReader(filename));
                while ((line = in.readLine()) != null) {
                        lines.add(line);
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return lines;
	}
	
	private static List<String> textToLines(String text)
	{
		String temp = text.replaceAll("\\\\n", "\n");
		temp = temp.replaceAll("\\\\t", "\t");
		
		return Arrays.asList(temp.split("\n"));
	}
	
	public static String compareText(String text1, String text2) 
	{
		 List<String> original = textToLines(text1);
         List<String> revised  = textToLines(text2);
         
         Patch patch = DiffUtils.diff(original, revised);
         
         String change = "";
         for (Delta delta: patch.getDeltas()) 
         {
        	 change += delta.getType().toString() + "(Position: " + delta.getOriginal().getPosition() + ")\n";
        	 change += "Lines: \n";
        	 
        	 if(delta.getType() == TYPE.INSERT)
        	 {
	        	 for(int i=0; i<delta.getRevised().getLines().size(); i++)
	        	 {
	        		 change += delta.getRevised().getLines().get(i) + "\n";
	        	 }
        	 }
        	 else if(delta.getType() == TYPE.DELETE)
        	 {
        		 for(int i=0; i<delta.getOriginal().getLines().size(); i++)
	        	 {
	        		 change += delta.getOriginal().getLines().get(i) + "\n";
	        	 }
        	 }else if(delta.getType() == TYPE.CHANGE)
        	 {
        		 change += "Original: \n";
        		 for(int i=0; i<delta.getOriginal().getLines().size(); i++)
	        	 {
	        		 change += delta.getOriginal().getLines().get(i) + "\n";
	        	 }
        		 
        		 change += "Revised: \n";
        		 for(int i=0; i<delta.getRevised().getLines().size(); i++)
	        	 {
	        		 change += delta.getRevised().getLines().get(i) + "\n";
	        	 } 
        	 }
        	 
        	 change +=  "\n\n";
         }
         
         return change;
	}
	
	private static int getColorRed(int rgb)
	{
		return (rgb >> 16) & 0xff;
	}
	
	private static int getColorGreen(int rgb)
	{
		return (rgb >> 8) & 0xff;
	}
	
	private static int getColorBlue(int rgb)
	{
		return rgb & 0xff;
	}
	
	public static double meanOfImage(BufferedImage img)
	{
		double sum = 0;
		for (int x = 0; x < img.getWidth(); x++) 
		{
            for (int y = 0; y < img.getHeight(); y++) 
            {
            	sum += img.getRGB(x, y);
            }
        }
		
		return sum * 1.0 / (img.getWidth() * img.getHeight());
	}
	
	public static double compareImage(BufferedImage img1, BufferedImage img2)
	{
		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight())
		{
			double m1 = meanOfImage(img1);
			double m2 = meanOfImage(img2);
			double sum = 0;
			double sum1 = 0;
			double sum2 = 0;
			for (int x = 0; x < img1.getWidth(); x++) 
			{
	            for (int y = 0; y < img1.getHeight(); y++) 
	            {
	            	sum += (img1.getRGB(x, y) - m1) * (img2.getRGB(x, y) - m2);
	            	sum1 += (img1.getRGB(x, y) - m1) * (img1.getRGB(x, y) - m1);
	            	sum2 += (img2.getRGB(x, y) - m2) * (img2.getRGB(x, y) - m2);
	            }
	        }
			
			return sum / (Math.sqrt(sum1) * Math.sqrt(sum2));
		}
		return 0;
	}
	
	public static void main(String[] args) throws Exception
	{
		DBImpl db = new DBImpl();
		
		//LowLevelInteraction i1 = db.getAnInteractions("2015-02-06 09:36:05.333", "baolingfeng");
		//LowLevelInteraction i2 = db.getAnInteractions("2015-02-06 09:17:22.408", "baolingfeng");
		//CompareUtil.compareText(i2.getUiValue(), i1.getUiValue());
		BufferedImage img1 = ImageIO.read(new File("C:\\Users\\baolingfeng\\Desktop\\Collecter\\log\\screen\\2015-02-08-20-06-49-624.png"));
		BufferedImage img2 = ImageIO.read(new File("C:\\Users\\baolingfeng\\Desktop\\Collecter\\log\\screen\\2015-02-08-20-08-12-449.png"));
		
		System.out.println(CompareUtil.compareImage(img1, img2));
	}
}
