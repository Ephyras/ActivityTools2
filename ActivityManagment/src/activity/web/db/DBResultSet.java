package activity.web.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBResultSet {
	List<DBRecord> records = new ArrayList<DBRecord>();
	HashMap<String, String> cMeta = new HashMap<String, String>();
	
	public void addRecord(DBRecord r)
	{
		records.add(r);
	}
	
	public void addColumn(String column, String type)
	{
		cMeta.put(column, type);
	}

	public List<DBRecord> getRecords() {
		return records;
	}

	public HashMap<String, String> getcMeta() {
		return cMeta;
	}
}
