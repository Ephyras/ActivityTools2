package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;

public class GroupDetail {
	private int groupId;
	private String time;
	private int screenStatus;
	private BufferedImage screen;
	
	
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getScreenStatus() {
		return screenStatus;
	}
	public void setScreenStatus(int screenStatus) {
		this.screenStatus = screenStatus;
	}
	public BufferedImage getScreen() {
		return screen;
	}
	public void setScreen(BufferedImage screen) {
		this.screen = screen;
	}
	
	
}
