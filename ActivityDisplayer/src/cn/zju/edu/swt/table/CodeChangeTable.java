package cn.zju.edu.swt.table;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.DefaultJaretTableModel;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.PropCol;
import de.jaret.util.ui.table.model.PropListeningTableModel;
import cn.zju.edu.manager.*;
import cn.zju.edu.blf.dao.*;

public class CodeChangeTable {
	Shell _shell;
	 IJaretTableModel _tableModel;
	 JaretTable _jt;
	 Thread thread;
	 
	 private String title;
	 private String app;
	 
	 public CodeChangeTable(IJaretTableModel tableModel, String title, String app) {
	        _tableModel = tableModel;
	        _shell = new Shell(Display.getCurrent());
	        _shell.setText("jaret table example");
	        createControls();
	        _shell.open();
	        Display display;
	        display = _shell.getDisplay();
	        _shell.pack();
	        _shell.setSize(1000, 700);
	        
	        this.title = title;
	        this.app = app;
	        
	        Runnable r = new Runnable() {
			      public void run() {
			        try {
			        	loadModel();
			        } catch (Exception x) {
			          x.printStackTrace();
			        }
			      }
			 };
			 
			 display.asyncExec(r);
	        
	        /*
	         * do the event loop until the shell is closed to block the call
	         */
	        while (_shell != null && !_shell.isDisposed()) {
	            try {
	                if (!display.readAndDispatch())
	                    display.sleep();
	            } catch (Throwable e) {
	                e.printStackTrace();
	            }
	        }
	        display.update();
	 }
	 
	 public void loadModel()
	 {
		 try
		 {
			 List<CodeChange> changes = HistoryActivityManager.getInstance().getJavaCodeChange(title, app);
			 
			 DefaultJaretTableModel model = new PropListeningTableModel();
			 IColumn ct1 = new PropCol("t1", "Timestamp", "T1");
	         model.addColumn(ct1);
	         IColumn ct2 = new PropCol("t2", "Change", "T2");
	         model.addColumn(ct2);
	         IColumn ct3 = new PropCol("t3", "Source", "T3");
	         model.addColumn(ct3);
			 
	         for(int i=1; i<changes.size(); i++)
			 {
	        	 CodeChange c = changes.get(i);
	        	 model.addRow(new DummyRow(c.getTime(), c.getChange(), "View Source"));
			 }
			 
			 _jt.setTableModel(model);
			 
			 _jt.getTableViewState().setColumnWidth(model.getColumn(0), 100);
	         _jt.getTableViewState().setColumnWidth(model.getColumn(1), 600);
	         _jt.getTableViewState().setColumnWidth(model.getColumn(2), 50);
	         
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	 }
	 
	 protected void createControls() 
	 {
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		_shell.setLayout(gl);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		
		_jt = new JaretTable(_shell, SWT.V_SCROLL | SWT.H_SCROLL);
		_jt.setLayoutData(gd);
		
		if (_tableModel != null) 
		{
			 _jt.setTableModel(_tableModel);
		}
		
	 }
	 
	 public static DefaultJaretTableModel createModel()
	 {
		 DefaultJaretTableModel model = new PropListeningTableModel();

        model.addRow(new DummyRow("r1", "The quick brown fox jumps over the crazy dog.", "Mars"));
        model.addRow(new DummyRow("r2", "The quick brown fox jumps over the crazy dog.", "Mars"));
        model.addRow(new DummyRow("r3", "The quick brown fox jumps over the crazy dog.", "Mars"));
        model.addRow(new DummyRow("r4", "The quick brown fox jumps over the crazy dog.", "Mars"));
        model.addRow(new DummyRow("r5", "The quick brown fox jumps over the crazy dog.", "Mars"));
        
        IColumn ct1 = new PropCol("t1", "column 1", "T1");
        model.addColumn(ct1);
        model.addColumn(new PropCol("t2", "column 2", "T2"));
        model.addColumn(new PropCol("t3", "column 3", "T3"));
        
        return model;
	 }
	 
	 public static void main(String[] args)
	 {
		 CodeChangeTable tb = new CodeChangeTable(null, "", "");
	 }
}
