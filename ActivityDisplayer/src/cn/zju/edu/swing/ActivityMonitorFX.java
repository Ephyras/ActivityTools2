package cn.zju.edu.swing;

import java.awt.EventQueue;

import javax.swing.UIManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class ActivityMonitorFX extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActivityMonitor window = new ActivityMonitor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}
