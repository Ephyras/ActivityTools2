package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;

public class GroupDetail {
	private String time;
	private int screenStatus;
	private BufferedImage screen;
	
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
