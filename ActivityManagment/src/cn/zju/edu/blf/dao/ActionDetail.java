package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;
import java.util.List;

import cn.zju.edu.util.InteractionUtil;

public class ActionDetail implements Comparable<ActionDetail>{
	private String time = "";
	private String action = "";
	private String controlType = "";
	private String parent = "";
	private String imgUrl = "";
	private BufferedImage img = null;
	
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getAction() {
		String res = action;
		res = res.replaceAll("\\\\n", "\n");
		res = res.replaceAll("\\\\t", "\t");
		
		String[] lines = res.split("\n");
		
		res = "";
		for(int i=lines.length-1; i>=0; i--)
		{
			res += lines[i] + "\n";
			if(lines.length - i > 50) break;
		}
		
		return res;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getControlType() {
		return controlType;
	}
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public BufferedImage getImg() {
		return img;
	}
	public void setImg(BufferedImage img) {
		this.img = img;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public boolean equals(Object o)
	{
		if(o == this) return true;
		
		if(o == null) return false;
		
		if(o instanceof ActionDetail)
		{
			ActionDetail a = (ActionDetail)o;
			
			return time.equals(a.getTime());
			
			/*
			if("No Accessibility Information".equals(action)) return false;
			
			if(!time.equals(a.getTime())) return false;
			
			return action.equals(a.getAction()) && this.controlType.equals(a.getControlType()) && parent.equals(a.getParent());
			*/
		}
		
		return false;
	}
	
	public int hashCode()
	{
		/*
		if("No Accessibility Information".equals(action) && img != null)
		{
			return img.hashCode();
		}
		else
		{
			return (time + action + controlType + parent).hashCode();
		}*/
		return time.hashCode();
	}
	
	 @Override
    public int compareTo(ActionDetail a){
		 return time.compareTo(a.getTime());
		 
		 /*
		 if("No Accessibility Information".equals(action) && !"No Accessibility Information".equals(a.getAction()))
		 {
			 return -1;
		 }
		 else if("No Accessibility Information".equals(a.getAction()) && !"No Accessibility Information".equals(action))
		 {
			 return 1;
		 }
		 else 
		 {
			 int res = parent.compareTo(a.getParent());
			 
			 if(res == 0)
			 {
				 res = controlType.compareTo(a.getControlType());
				 if(res == 0)
				 {
					 res = action.compareTo(a.getAction());
				 }
			 }
			 
			 return res;
		 }
		 */
    }
}
