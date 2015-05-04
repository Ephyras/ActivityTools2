package script.ase;

public class GroupStat
{
	int numDay;
	
	public int level1 = 0; //1 day
	public int level2 = 0; //2-4 day
	public int level3 = 0; //5-7 day
	public int level4 = 0; //8-10 day
	public int level5 = 0; //>10 day
	
	public GroupStat(int numDay)
	{
		this.numDay = numDay;
	}
	
	public int total()
	{
		return level1 + level2 + level3 + level4 + level5;
	}
	
	
}