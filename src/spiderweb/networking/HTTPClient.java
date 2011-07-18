package spiderweb.networking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * @author  Matty
 */
public class HTTPClient implements ActionListener{
	//[start] Attributes
	private static int readTimeOut = 120000;
	private static int connectTimeOut = 10000;
	private static int sleepTime = 1000;

	private LinkedList<NetworkListener> networkListeners;

	private String serverURL;
	private long latestTime;
	private boolean connected;
	private Timer networkScheduler;
	//[end] Attributes	

	//[start] Constructors
	public HTTPClient() {
		networkListeners = new LinkedList<NetworkListener>();
		latestTime=0;
		connected = false;
		
		networkScheduler = new Timer(sleepTime,this);
	}

	public HTTPClient(NetworkListener listener) {
		this();
		addNetworkListener(listener);
	}
	//[end] Constructors
	
	public void startNetwork(String serverURL) {
		this.serverURL = serverURL;
		try {
			getGraph();
			connected = true;
		} catch (Exception e) {
			connected = false;
			JOptionPane.showMessageDialog(null, "Could not open connection.", "Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
		if(connected) {
			networkScheduler.start();
		}
	}
	
	public void closeNetwork() {
		connected = false;
		networkScheduler.stop();
	}
	
	/**
	 * @param latestTime
	 */
	public void setLatestTime(long latestTime) {
		this.latestTime = latestTime;
	}

	//[start] Listener Methods	
	public void addNetworkListener(NetworkListener listener) {
		networkListeners.add(listener);
	}

	protected void notifyIncomingGraph(InputStream inStream) {
		for(NetworkListener l : networkListeners) {
			l.incomingGraph(inStream);
		}
	}

	protected void notifyIncomingLogEvents(InputStream inStream) {
		for(NetworkListener l : networkListeners) {
			l.incomingLogEvents(inStream);
		}
	}

	//[end] Listener Methods

	//[start] HTTP Methods
	private InputStream connect(String url) throws IOException {
		
		URLConnection conn = new URL(serverURL + url).openConnection();
		conn.setConnectTimeout(connectTimeOut);
		conn.setReadTimeout(readTimeOut);
		//conn.setRequestProperty("User-Agent", USER_AGENT);
		InputStream in = conn.getInputStream();
		return in;
	}

	//[start] HTTP Getters
	private void getGraph() throws IOException, Exception {
		String url = "/getGraph";

		notifyIncomingGraph(connect(url));
	}

	private void getLogEvents() throws IOException, Exception {
		String url = "/getLogEvents?";
		url = addTimeOfLastResponse(url);		
		
		InputStream inStream = connect(url);
		
		//LinkedList<LogEvent> events = P2PNetworkGraphLoader.buildLogs(in);
		notifyIncomingLogEvents(inStream);

	}
	//[end] HTTP Getters

	//[start] URL Helpers
	private String addTimeOfLastResponse(String url) {
		url = url + "&time=" + latestTime;
		return url;
	}
	//[end] URL Helpers

	//[end] HTTP Methods
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(connected) {
			try {
				getLogEvents();
				connected = false;
			} catch (IOException e) {
				connected = false;
				e.printStackTrace();
			} catch (Exception e) {
				connected = false;
				e.printStackTrace();
			}
		}
	}
}
