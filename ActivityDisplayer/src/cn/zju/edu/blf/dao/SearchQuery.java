package cn.zju.edu.blf.dao;

import java.util.HashMap;

public class SearchQuery {
	private String query;
	private String engine;
	private String application;
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public boolean equals(Object o)
	{
		if(o == null) return false;
		
		if(o instanceof SearchQuery)
		{
			return query.equals(((SearchQuery) o).getQuery()) && engine.equals(((SearchQuery) o).getEngine());
		}
		return false;
	}
	
	public int hashCode()
	{
		return (query+engine).hashCode();
	}
	
	public static void main(String[] args)
	{
		HashMap<SearchQuery, String> map = new HashMap<SearchQuery, String>();
		SearchQuery s1 = new SearchQuery();
		s1.setQuery("test");
		s1.setEngine("Google");
		
		SearchQuery s2 = new SearchQuery();
		s2.setQuery("test");
		s2.setEngine("Google");
		
		map.put(s1, "1111");
		if(map.containsKey(s2))
		{
			map.put(s2, "2222");
		}
		map.put(s2, "2222");
		
	}
}
