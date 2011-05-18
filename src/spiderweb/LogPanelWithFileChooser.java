package spiderweb;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/////////additional swing components for the file chooser, etc./////////////////////////
	// only used in non-web mode
public class LogPanelWithFileChooser extends JFrame implements ActionListener, LoadingListener{
	
	private static final long serialVersionUID = 1L;
	
	JTextArea log;
	
	JScrollPane logScrollPane;
	JButton openButton;
	JFileChooser fc;
	P2PApplet myapp;
	JDialog loadingDialog;
	JProgressBar loadingProgress;
	
	//constructor
	public LogPanelWithFileChooser(P2PApplet app) {
		// Create the log first, because the action listeners
		// need to refer to it.
		log = new JTextArea(8,60);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		logScrollPane = new JScrollPane(log);
		
		log.setLineWrap(true);
		
		
		myapp = app;
		//running through this main method means we're not running the applet on the web
		myapp.setOnWeb(false);

		//Create a file chooser
		fc = new JFileChooser();

		
		//Create the open button.  We use the image from the JLF
		//Graphics Repository (but we extracted it from the jar).
		openButton = new JButton("Open a File...");
		openButton.addActionListener(this);

		//For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); //use FlowLayout
		buttonPanel.add(openButton);
		add(new JLabel("P2P Network Simulation Viewer"), BorderLayout.PAGE_START);
		//Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_END);
		log.append("Open a processed log file to get started...");
		add(logScrollPane, BorderLayout.CENTER);
		
		
	}

	/**
	 * handles the button to open a file 
	 */
	public void actionPerformed(ActionEvent e) {


		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			//use this log file
			myapp.setLogFile(file);
			log.append("\nOpening: " + file.getAbsolutePath() + ".");

			log.append("\nPlease be patient while the processed log file is read.");
			log.append("\nThis may take around 30 seconds for a 200kB file, as the graph layout is also processed at the same time.");
			log.append("\nOnce the data is loaded, a new screen will pop up. The layout of the P2P " +
					"graph will be visible, you may modify it by zooming, panning, and moving nodes around.\n" +
					"once you are happy with the layout and are ready to start the simulation, click the button at the bottom.");
			log.setCaretPosition(log.getDocument().getLength());

			/*if (args.length>0) // log file name can be input on running the applet as well
	myapp.setLogFileName(args[0]);*/
			
			//start the applet using a new thread. 
			Thread starter = new Thread(new Runnable() {
				public void run() {
					//Turn off metal's use of bold fonts
					JFrame frame = new JFrame();
					frame.setBounds(0, 0, P2PApplet.DEFWIDTH, P2PApplet.DEFHEIGHT);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.getContentPane().add(myapp);

					myapp.init();
					myapp.start();
					frame.pack();
					//this.setVisible(false);
					frame.setVisible(true);

				}
			});
			starter.start();

		} else {
			log.append("Open command cancelled by user.");
		}
		
	}

	@Override
	public void loadingStarted(int numberLines, String whatIsLoading) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		loadingDialog = new JDialog(this,"Loading "+whatIsLoading+"...");
		loadingDialog.setBounds(getWidth()/2, getHeight()/2, 250, 100);
		loadingDialog.setResizable(false);
		loadingDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		loadingDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		loadingProgress = new JProgressBar(JProgressBar.HORIZONTAL,0,numberLines);
		loadingProgress.setValue(0);
		loadingProgress.setName(whatIsLoading);
		loadingProgress.setString(whatIsLoading+": 0%");
		loadingProgress.setStringPainted(true);
		
		
		loadingDialog.add(loadingProgress);
		loadingDialog.setVisible(true);
	}

	@Override
	public void loadingProgress(int lineNumber) {
		loadingProgress.setValue(lineNumber);
		loadingProgress.setString((String.format(loadingProgress.getName()+": %.3g%n", loadingProgress.getPercentComplete()*100))+"%");
	}

	@Override
	public void loadingComplete() {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		loadingDialog.setVisible(false);
	}

	@Override
	public void loadingChanged(int numberLines, String whatIsLoading) {
		loadingDialog.setTitle("Loading "+whatIsLoading+"...");
		loadingProgress.setMaximum(numberLines);
		loadingProgress.setValue(0);
		loadingProgress.setName(whatIsLoading);
		loadingProgress.setString(whatIsLoading+": 0%");
	}
}