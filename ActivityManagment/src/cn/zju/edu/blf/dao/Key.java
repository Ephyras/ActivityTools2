package cn.zju.edu.blf.dao;

public class Key {
	private String key;
	private int number;
	
	public Key()
	{
		key = "";
		number = 1;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	public boolean equals(Object o)
	{
		if(o == this) return true;
		
		if(o == null) return false;
		
		if(o instanceof Key)
		{
			return key.equals(((Key)o).getKey());
		}
		
		return false;
	}
	
	public int hashCode()
	{
		return key.hashCode();
	}
}
