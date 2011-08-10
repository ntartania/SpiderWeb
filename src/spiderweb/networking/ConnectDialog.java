package spiderweb.networking;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.border.SoftBevelBorder;

public class ConnectDialog extends JDialog implements ActionListener{

	/**eclipse generated serial UID*/
	private static final long serialVersionUID = 4050586871897666736L;
	public static final String CANCELED = "CANCELED";

	private JTextField serverName;
	private JTextField serverPort;
	private JLabel serverURL;
	private String returnText = CANCELED;

	/**
	 * Create the dialog.
	 */
	private ConnectDialog() {
		super();
		init();
	}
	private ConnectDialog(Dialog owner) {
		super(owner);
		init();
	}
	private ConnectDialog(Frame owner) {
		super(owner);
		init();
	}
	private ConnectDialog(Window owner) {
		super(owner);
		init();
	}
	
	private void init() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Input Server Address");
		setBounds(100, 100, 550, 300);
		getContentPane().setLayout(new BorderLayout());
		setResizable(false);

		getContentPane().add(getAddressPane(), BorderLayout.CENTER);
		getContentPane().add(getButtonPane(), BorderLayout.SOUTH);
		setURLLabel();
	}
	
	public static String getConnectURL() {
		ConnectDialog cd = new ConnectDialog();
		cd.setVisible(true);
		return cd.returnText;
	}

	private JPanel getAddressPane() {
		JPanel addressPane = new JPanel();
		addressPane.setBorder(BorderFactory.createTitledBorder("Connect to:"));
		addressPane.setLayout(new GridLayout(3,1));

		URLListener urlListener = new URLListener();
		{
			serverName = new JTextField();
			serverName.setColumns(25);
			serverName.setText("134.117.60.66");
			serverName.setToolTipText("Server name to connect to");
			serverName.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
			serverName.addKeyListener(urlListener);

			JPanel namePanel = new JPanel();
			namePanel.setBorder(BorderFactory.createTitledBorder("Server Name"));
			namePanel.setLayout(new BorderLayout());
			namePanel.add(serverName, BorderLayout.CENTER);

			addressPane.add(namePanel, BorderLayout.CENTER);
		}
		{
			serverPort = new JTextField();
			serverPort.setText("8080");
			serverPort.setToolTipText("Server port to connect to");
			serverPort.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
			serverPort.addKeyListener(urlListener);

			JPanel portPanel = new JPanel();
			portPanel.setBorder(BorderFactory.createTitledBorder("Server Port"));
			portPanel.setLayout(new BorderLayout());
			portPanel.add(serverPort, BorderLayout.CENTER);

			addressPane.add(portPanel, BorderLayout.CENTER);
		}

		serverURL = new JLabel("http://");
		serverURL.setToolTipText("The URL to connect to");
		serverURL.setBorder(BorderFactory.createTitledBorder("Connect URL"));
		addressPane.add(serverURL, BorderLayout.SOUTH);

		return addressPane;
	}

	private JPanel getButtonPane() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		buttonPane.add(cancelButton);

		return buttonPane;
	}

	private void setURLLabel() {
		StringBuffer url = new StringBuffer("http://");
		url.append(serverName.getText());
		url.append(":");
		url.append(serverPort.getText());
		url.append("/graphServer");

		serverURL.setText(url.toString());
	}

	private class URLListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent ke) {
			setURLLabel();
		}

		@Override
		public void keyReleased(KeyEvent ke) {
			setURLLabel();
		}

	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if ("OK".equals(ae.getActionCommand())) {
			returnText = serverURL.getText();
		}
		this.dispose();
	}

}
