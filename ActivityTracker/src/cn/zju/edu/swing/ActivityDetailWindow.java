package cn.zju.edu.swing;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.JPanel;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.dao.ActionDetail;
import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.util.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;

public class ActivityDetailWindow {

	private JFrame frmWhatDidYou;
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel loadingLabel;
	JScrollPane scrollPane;
	
	private String title;
	private String app;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActivityDetailWindow window = new ActivityDetailWindow("What did you do", null);
					window.frmWhatDidYou.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ActivityDetailWindow(String title, String app) {
		this.title = title;
		this.app = app;
		initialize();
		
		Runnable r = new Runnable() {
		      public void run() {
		        try {
		        	JDialog dialog = new JDialog();
		        	JLabel label = new JLabel("Please wait...");
		        	dialog.setLocationRelativeTo(null);
		        	dialog.setTitle("Please Wait...");
		        	dialog.add(label);
		        	dialog.pack();
		        	dialog.setVisible(true);
		        	
		        	loadActions();
		        	dialog.setVisible(false);
		        } catch (Exception x) {
		          x.printStackTrace();
		        }
		      }
		 };
		 
		 Thread t = new Thread(r, "loadaction");
		 t.start();
	}

	public void show()
	{
		frmWhatDidYou.setVisible(true);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frmWhatDidYou = new JFrame();
		frmWhatDidYou.setTitle("What did you do - " + title);
		frmWhatDidYou.setBounds(100, 100, 458, 382);
		//frmWhatDidYou.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWhatDidYou.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		table = new JTable();
		
		tableModel = new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"Action Name", "Control Type", "Parent Control", "Screen-Capture"
				}
				
			){
			@Override
		    public boolean isCellEditable(int row, int column) {
		        if(column == 3)
		        	return false;
		        return true;
		    }
		};
		
		table.setModel(tableModel);
		table.getColumnModel().getColumn(3).setCellRenderer(new ImageRenderer());
		//DefaultTableModel model = (DefaultTableModel)table.getModel();
		
		table.addMouseListener(new MouseAdapter() {
			  public void mouseClicked(MouseEvent e) {
			    if (e.getClickCount() == 2) {
			      JTable target = (JTable)e.getSource();
			      int row = target.getSelectedRow();
			      int column = target.getSelectedColumn();
			      //System.out.println(target.getModel().getValueAt(row, column));
			      if(column != 3) return;
			      
			      if(target.getModel().getValueAt(row, column) instanceof BufferedImage)
			      {
			    	  BufferedImage img = (BufferedImage)target.getModel().getValueAt(row, column);
			    	  ScabledLabelPanel.show("Show Screen-Captured Image", img);
			    	  //System.out.println("clicked image: " + img.getWidth() + "/" + img.getHeight());
			      }
			      
			      // do some action if appropriate column
			    }
			  }
			});
		
		//loadingLabel = new JLabel("loading...");
		scrollPane = new JScrollPane(table);
		frmWhatDidYou.getContentPane().add(scrollPane);
		//frmWhatDidYou.getContentPane().add(table);
	}
	
	public void loadActions()
	{
		try
		{
			//JOptionPane.showMessageDialog(null, "loading now, Please wait....");
			
			List<ActionDetail> details = HistoryActivityManager.getInstance().getLLInteractionsForDetail(title, app);
			for(ActionDetail ad : details)
			{
				List<Object> row = new ArrayList<Object>();
				row.add(ad.getAction());
				row.add(ad.getControlType());
				row.add(ad.getParent());
				row.add(ad.getImg());
				tableModel.addRow(row.toArray());
			}
			System.out.println("load completed");
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	class ImageRenderer  implements TableCellRenderer{
		JLabel lbl;
		  
		  //ImageIcon icon = new ImageIcon(getClass().getResource("sample.png"));

		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		      boolean hasFocus, int row, int column) {
			  
			lbl = new JLabel("");
			
			if(value instanceof BufferedImage && value != null)
			{
				BufferedImage img = (BufferedImage)value;
				int w = img.getWidth();
				int h = img.getHeight();
				
				w = w>50 ? 50 : w;
				h = h>50 ? 50 : h;
				
				lbl.setIcon(new ImageIcon(ImageUtil.getScaledImage(img, w, h)));
				table.setRowHeight(row, h);
				
				//table.setEnabled(false);
			}
			
		    return lbl;
		  }
		}
	
}
