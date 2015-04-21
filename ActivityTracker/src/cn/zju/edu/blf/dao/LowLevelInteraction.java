package cn.zju.edu.blf.dao;

import java.awt.image.BufferedImage;

public class LowLevelInteraction {
	private String timestamp;
	private String window;
	private String parentWindow;
	private String application;
	private int px;
	private int py;
	private int wRectLeft;
	private int wRectTop;
	private int wRectRight;
	private int wRectBottom;
	private String uiName;
	private String uiType;
	private String uiValue;
	private String parentUiName;
	private String parentUiType;
	private int uiBoundLeft;
	private int uiBoundTop;
	private int uiBoundRight;
	private int uiBoundBottom;
	private boolean hasScreen;
	private BufferedImage screen;
	
	public BufferedImage getScreen() {
		return screen;
	}
	public void setScreen(BufferedImage screen) {
		this.screen = screen;
	}
	public boolean isHasScreen() {
		return hasScreen;
	}
	public void setHasScreen(boolean hasScreen) {
		this.hasScreen = hasScreen;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getWindow() {
		return window;
	}
	public void setWindow(String window) {
		this.window = window;
	}
	public String getParentWindow() {
		return parentWindow;
	}
	public void setParentWindow(String parentWindow) {
		this.parentWindow = parentWindow;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public int getPx() {
		return px;
	}
	public void setPx(int px) {
		this.px = px;
	}
	public int getPy() {
		return py;
	}
	public void setPy(int py) {
		this.py = py;
	}
	public int getwRectLeft() {
		return wRectLeft;
	}
	public void setwRectLeft(int wRectLeft) {
		this.wRectLeft = wRectLeft;
	}
	public int getwRectTop() {
		return wRectTop;
	}
	public void setwRectTop(int wRectTop) {
		this.wRectTop = wRectTop;
	}
	public int getwRectRight() {
		return wRectRight;
	}
	public void setwRectRight(int wRectRight) {
		this.wRectRight = wRectRight;
	}
	public int getwRectBottom() {
		return wRectBottom;
	}
	public void setwRectBottom(int wRectBottom) {
		this.wRectBottom = wRectBottom;
	}
	public String getUiName() {
		return uiName;
	}
	public void setUiName(String uiName) {
		this.uiName = uiName;
	}
	public String getUiType() {
		return uiType;
	}
	public void setUiType(String uiType) {
		this.uiType = uiType;
	}
	public String getUiValue() {
		return uiValue;
	}
	public void setUiValue(String uiValue) {
		this.uiValue = uiValue;
	}
	public String getParentUiName() {
		return parentUiName;
	}
	public void setParentUiName(String parentUiName) {
		this.parentUiName = parentUiName;
	}
	public String getParentUiType() {
		return parentUiType;
	}
	public void setParentUiType(String parentUiType) {
		this.parentUiType = parentUiType;
	}
	public int getUiBoundLeft() {
		return uiBoundLeft;
	}
	public void setUiBoundLeft(int uiBoundLeft) {
		this.uiBoundLeft = uiBoundLeft;
	}
	public int getUiBoundTop() {
		return uiBoundTop;
	}
	public void setUiBoundTop(int uiBoundTop) {
		this.uiBoundTop = uiBoundTop;
	}
	public int getUiBoundRight() {
		return uiBoundRight;
	}
	public void setUiBoundRight(int uiBoundRight) {
		this.uiBoundRight = uiBoundRight;
	}
	public int getUiBoundBottom() {
		return uiBoundBottom;
	}
	public void setUiBoundBottom(int uiBoundBottom) {
		this.uiBoundBottom = uiBoundBottom;
	}
	
	
}
