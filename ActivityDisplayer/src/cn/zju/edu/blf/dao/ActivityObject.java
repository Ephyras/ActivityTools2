package cn.zju.edu.blf.dao;

import cn.zju.edu.util.*;

public class ActivityObject implements Comparable<ActivityObject> {
	private String title;
	private String application;
	private String lastTime;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		
		if(o == null) return false;
		
		if(o instanceof ActivityObject)
		{
			ActivityObject a = (ActivityObject)o;
			return title != null && application != null && this.title.equals(a.getTitle()) && this.application.equals(a.getApplication());
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return (title + "#" + application).hashCode();
	}
	
	@Override
	public int compareTo(ActivityObject a) 
	{
		try 
		{
			return -(int)(DateUtil.formatTime(lastTime).getTime() - DateUtil.formatTime(a.lastTime).getTime());
		}catch(Exception e)
		{
			return 0;
		}
 
	}	
}
