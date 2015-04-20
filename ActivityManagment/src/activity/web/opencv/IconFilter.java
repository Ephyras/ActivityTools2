package activity.web.opencv;

public class IconFilter
{
	private String application;
	private String uiType;
	
	public IconFilter(String app, String uiType)
	{
		this.application = app;
		this.uiType = uiType;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getUiType() {
		return uiType;
	}

	public void setUiType(String uiType) {
		this.uiType = uiType;
	}
	
	
};
