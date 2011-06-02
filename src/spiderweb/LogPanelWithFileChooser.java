package spiderweb;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class LogPanelWithFileChooser extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
		
	JButton openButton;
	JFileChooser fc;
	P2PApplet myapp;
	
	//constructor
	public LogPanelWithFileChooser(P2PApplet app) {
		myapp = app;

		//Create a file chooser
		fc = new JFileChooser();

		openButton = new JButton("Open a File...");
		openButton.addActionListener(this);

		//Add the buttons and the log to this panel.
		add(openButton);
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
					frame.setVisible(true);

				}
			});
			starter.start();

		} else {
		}
		
	}

	
}