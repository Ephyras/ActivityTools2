package activity.web.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.mysql.jdbc.Blob;

public class DBRecord {
	private HashMap<String, Object> r = new HashMap<String, Object>();
	
	public void add(String column, Object value)
	{
		r.put(column, value);
	}
	
	public String getString(String column)
	{
		return (String)r.get(column);
	}
	
	public int getInt(String column)
	{
		return (Integer)r.get(column);
	}
	
	public double getDouble(String column)
	{
		return (Double)r.get(column);
	}
	
	public boolean getBoolean(String column)
	{
		return getInt(column) != 0 ? true : false;
	}
	
	public long getLong(String column)
	{
		return (Long)r.get(column);
	}
	
	public BufferedImage getBufferedImage(String column)
	{
		try
		{
			return (BufferedImage)r.get(column);
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
