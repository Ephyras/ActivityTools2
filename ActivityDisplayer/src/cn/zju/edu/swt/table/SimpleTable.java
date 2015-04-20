package cn.zju.edu.swt.table;

import java.util.Date;

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

public class SimpleTable {
	 Shell _shell;
	 IJaretTableModel _tableModel;
	 JaretTable _jt;
	 Thread thread;
	 
	 public SimpleTable(IJaretTableModel tableModel) {
	        _tableModel = tableModel;
	        _shell = new Shell(Display.getCurrent());
	        _shell.setText("jaret table example");
	        createControls();
	        _shell.open();
	        Display display;
	        display = _shell.getDisplay();
	        _shell.pack();
	        _shell.setSize(1000, 700);
	        
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
			 Thread.sleep(1000);
			 _jt.setTableModel(createModel());
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
		 SimpleTable tb = new SimpleTable(null);
	 }
}
