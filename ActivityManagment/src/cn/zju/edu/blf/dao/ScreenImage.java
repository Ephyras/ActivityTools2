package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ScreenImage {
	private BufferedImage image;
	private String time;
	private List<LowLevelInteraction> interactions = new ArrayList<LowLevelInteraction>();
	
	public BufferedImage getImage() {
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<LowLevelInteraction> getInteractions() {
		return interactions;
	}
	public void setInteractions(List<LowLevelInteraction> interactions) {
		this.interactions = interactions;
	}
	
	public void addInteraction(LowLevelInteraction i)
	{
		interactions.add(i);
	}
}
