package script.ase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cn.zju.edu.blf.dao.GroupedInteraction;

public class CorrelatedStat {
	String title;
	String app;
	int total = 0;
	int internal = 0;
	
	HashMap<String, Integer> cmap = new HashMap<String, Integer>();
	
	List<GroupedInteraction> details = new ArrayList<GroupedInteraction>();
	
	public CorrelatedStat(String title, String app)
	{
		this.title = title;
		this.app = app;
	}
	
	public void addDetail(GroupedInteraction g)
	{
		details.add(g);
	}
	
	public boolean contain(GroupedInteraction g)
	{
		return details.indexOf(g) >= 0;
	}
	
	public void add(String capp)
	{
		if(cmap.containsKey(capp))
		{
			cmap.put(capp, cmap.get(capp) + 1);
		}
		else
		{
			cmap.put(capp, 1);
		}
		
		if(capp.equals(app))
		{
			internal += 1;
		}
		
		total += 1;
	}
	
	public List<GroupedInteraction> getDetails() {
		return details;
	}

	public void setDetails(List<GroupedInteraction> details) {
		this.details = details;
	}

	public int getTotalOfApp(String appin)
	{
		for(Entry<String, Integer> e : cmap.entrySet())
		{
			if(e.getKey().equals(appin))
			{
				return e.getValue();
			}
		}
		
		return 0;
	}
	
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getInternal() {
		return internal;
	}

	public void setInternal(int internal) {
		this.internal = internal;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public HashMap<String, Integer> getCmap() {
		return cmap;
	}

	public void setCmap(HashMap<String, Integer> cmap) {
		this.cmap = cmap;
	}
	
	
}


