package cn.zju.edu.blf.dao;

import java.io.Serializable;

public class CodeChangeDetail implements Serializable{
	private String type;
	private String content;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
