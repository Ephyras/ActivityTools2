package cn.zju.edu.blf.dao;

import java.io.Serializable;
import java.util.List;

public class CodeChange implements Serializable{
	private String time;
	private String change;
	private String source;
	private String timespan;
	
	List<CodeChangeDetail> detail;
	
	public void addDetail(String type, String content)
	{
		CodeChangeDetail d = new CodeChangeDetail();
		d.setType(type);
		d.setContent(content);
		
		detail.add(d);
	}
	
	public List<CodeChangeDetail> getDetail() {
		return detail;
	}
	public void setDetail(List<CodeChangeDetail> detail) {
		this.detail = detail;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getChange() {
		return change;
	}
	public void setChange(String change) {
		this.change = change;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getTimespan() {
		return timespan;
	}

	public void setTimespan(String timespan) {
		this.timespan = timespan;
	}
	
	
}
