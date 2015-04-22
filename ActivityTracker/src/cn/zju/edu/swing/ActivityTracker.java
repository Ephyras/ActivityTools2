package cn.zju.edu.swing; 

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.application.Platform;

import javax.swing.*;

import cn.zju.edu.ActivityConfiguration;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class ActivityTracker {
  public static void main(String [] args){

    SwingUtilities.invokeLater(new Runnable() {
    @Override
      public void run() {
        ApplicationFrame mainFrame = new ApplicationFrame(null, null);
        mainFrame.setTitle("Activity Tracker");
        URL url = ActivityTracker.class.getResource("/icons/appicon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
        
		mainFrame.setIconImage(img);
        mainFrame.setVisible(true);
      }
    });

  }
  
  public static void run(final String title, final String app)
  {
	  SwingUtilities.invokeLater(new Runnable() {
		    @Override
		      public void run() {
		        ApplicationFrame mainFrame = new ApplicationFrame(title, app);
		        mainFrame.setTitle("Activity Tracker");
		        URL url = ActivityTracker.class.getResource("/icons/appicon.png");
				Toolkit kit = Toolkit.getDefaultToolkit();
				Image img = kit.createImage(url);
		        
				mainFrame.setIconImage(img);
		        mainFrame.setVisible(true);
		      }
	 }); 
  }
  
}


class ApplicationFrame extends JFrame{
	
  String DEMO_URL;
	
  JFXPanel javafxPanel;
  WebView webComponent;
  JPanel mainPanel;

  JTextField urlField;
  JButton refreshBtn;
  JButton setBtn;
  String user;
  
  
  public ApplicationFrame(String filter, String app){
	user = System.getProperty("user.name");
	
	DEMO_URL = ActivityConfiguration.getInstance().getWEB_APPLICATION() + "/jsp/index.jsp";
	
	DEMO_URL += "?user="+user;
	
	if(filter != null && !"".equals(filter.trim()))
	{
		DEMO_URL += "&filter=" + filter + "&app="+app;
	}
	
    javafxPanel = new JFXPanel();

    initSwingComponents();

    loadJavaFXScene();
  }

  /**
  * Instantiate the Swing compoents to be used
  */
  private void initSwingComponents(){
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(javafxPanel, BorderLayout.CENTER);

    JPanel urlPanel = new JPanel(new FlowLayout());
   
    refreshBtn = new JButton("Refresh");
    setBtn = new JButton("Setting");
    
    /**
     * Handling the loading of new URL, when the user
     * enters the URL and clicks on Go button.
     */
    refreshBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            if ( DEMO_URL != null && DEMO_URL.length() > 0){
                webComponent.getEngine().load(DEMO_URL);
            }
          }
        });

      }
    });

    urlPanel.add(refreshBtn);
    urlPanel.add(setBtn);
    
    mainPanel.add(urlPanel, BorderLayout.SOUTH);

    this.add(mainPanel);
    //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setSize(1400,1000);
  }
  
  /**
  * Instantiate the JavaFX Components in
  * the JavaFX Application Thread.
  */
  private void loadJavaFXScene(){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {

        BorderPane borderPane = new BorderPane();
        webComponent = new WebView();
        
        webComponent.getEngine().load(DEMO_URL);
        borderPane.setCenter(webComponent);
        Scene scene = new Scene(borderPane,1000,800);
        javafxPanel.setScene(scene);

      }
    });
  }
}
