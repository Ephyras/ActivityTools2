package cn.zju.edu.blf.dao;

public class KeyPair
{
	private String k1;
	private String k2;
	private int number;
	
	public KeyPair()
	{
		number = 1;
	}
	
	public KeyPair(String k1, String k2)
	{
		this.k1 = k1;
		this.k2 = k2;
	}
	
	public String getK1() {
		return k1;
	}

	public void setK1(String k1) {
		this.k1 = k1;
	}

	public String getK2() {
		return k2;
	}

	public void setK2(String k2) {
		this.k2 = k2;
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String toString()
	{
		return k1 + " + " + k2;
	}
	
	public boolean equals(Object o)
	{
		if(this == o) return true;
		
		if(o == null) return false;
		
		if(o instanceof KeyPair)
		{
			return k1.equals(((KeyPair) o).getK1()) && k2.equals(((KeyPair) o).getK2()) || 
					(k1.equals(((KeyPair) o).getK2()) && k2.equals(((KeyPair) o).getK1()));
		}
		
		return false;
	}
	
	public int hashCode()
	{
		if(k1.compareTo(k2) > 0)
		{
			String temp = k1;
			k1 = k2;
			k2 = temp;
		}
		
		return (k1 + "/" + k2).hashCode();
	}
}
