package spiderweb;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class LoadingBar extends JDialog implements LoadingListener{
	private static final long serialVersionUID = 1L;
	JProgressBar progressBar;
	
	public LoadingBar(Component parent) {
		setTitle("Loading...");
		
		setBounds(parent.getWidth()/2-125, parent.getHeight()/2-50, 250, 100);
		setResizable(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBackground(Color.BLACK);
	}
	
	public LoadingBar() {
		setTitle("Loading...");
		
		setBounds(100, 100, 250, 100);
		setResizable(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBackground(Color.BLACK);
		setLayout(new GridLayout(1,1));
		
		progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
		
		
		//this.pack();
		//this.validate();
	}
	
	@Override
	public void loadingStarted(int numberLines, String whatIsLoading) {
		progressBar.setMinimum(0);
		progressBar.setMaximum(numberLines);
		progressBar.setName(whatIsLoading);
		progressBar.setValue(0);
		progressBar.setString(whatIsLoading+": 0%");
		progressBar.setStringPainted(true);
		
		add(progressBar);
		setVisible(true);
		
	}

	@Override
	public void loadingProgress(int lineNumber) {
		progressBar.setValue(lineNumber);
		progressBar.setString((String.format(progressBar.getName()+": %.3g%n", progressBar.getPercentComplete()*100))+"%");
		//repaint();
	}

	@Override
	public void loadingComplete() {
		setVisible(false);
		
	}

	@Override
	public void loadingChanged(int numberLines, String whatIsLoading) {
		setTitle("Loading "+whatIsLoading+"...");
		progressBar.setMaximum(numberLines);
		progressBar.setValue(0);
		progressBar.setName(whatIsLoading);
		progressBar.setString(whatIsLoading+": 0%");
		//repaint();
	}
	
	/*
	public static void main(String[] args) {
		LoadingBar test = new LoadingBar();
		int size = 100000;
		
		test.loadingStarted(size, "Test 1");
		for(int i=0;i<size;i++) {
			test.loadingProgress(i);
		}
		test.loadingChanged(size*5, "Test 2");
		for(int i=0;i<size*5;i++) {
			test.loadingProgress(i);
		}
		test.loadingComplete();
		System.exit(0);
	}
	*/
}
