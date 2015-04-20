package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class GroupDetail implements Serializable{
	private String time;
	private int screenStatus;
	private BufferedImage screen;
	private double duration;
	
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
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	
}
