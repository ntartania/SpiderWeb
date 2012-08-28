/*
 * File:         HTTPClient.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.networking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * HTTPClient handles incoming and outgoing HTTP Messages for the 
 * Spiderweb Network Graph Visualizer. It allows for live graphs to be
 * displayed.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 20/07/2011 
 */
public class HTTPClient implements ActionListener{
	//[start] Attributes
	private static int readTimeOut = 120000;
	private static int connectTimeOut = 10000;
	private static int sleepTime = 10000;

	private List<NetworkGraphListener> networkListeners;

	private String serverURL;
	private long latestTime;
	private long startingTime;
	private boolean connected;
	private Timer networkScheduler;
	//[end] Attributes	

	//[start] Constructors
	public HTTPClient() {
		networkListeners = new LinkedList<NetworkGraphListener>();
		latestTime=0;
		startingTime=0;
		connected = false;
		
		networkScheduler = new Timer(sleepTime,this);
	}

	public HTTPClient(NetworkGraphListener listener) {
		this();
		addNetworkListener(listener);
	}
	//[end] Constructors
	
	public void startNetwork(String serverURL) {
		this.serverURL = serverURL;
		Thread starterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				
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
			
		});
		starterThread.start();
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
	
	/**
	 * sets the zero(epoch) time as well as sets that as the latest time to be received.
	 * @param firstReceivedTime The time received when the graph came in.(to be used as the epoch)
	 */
	public void setZeroTime(long firstReceivedTime) {
		latestTime = firstReceivedTime;
		startingTime = firstReceivedTime;
	}
	
	public long getZeroTime() {
		return startingTime;
	}

	//[start] Listener Methods	
	/**
	 * Adds a network listener that is interested in handling any incoming graphs or log events.
	 * @param listener The NetworkListener which will handle incoming streams.
	 */
	public void addNetworkListener(NetworkGraphListener listener) {
		networkListeners.add(listener);
	}

	/**
	 * Notify all Listeners that a message has been receieved which contains a Graph.
	 * @param inStream InputStream containing the Network Graph XML file as it's payload.
	 */ 
	protected void notifyIncomingGraph(InputStream inStream) {
		for(NetworkGraphListener l : networkListeners) {
			l.incomingGraph(inStream);
		}
	}

	/**
	 * Notify all Listeners that a message has been receieved which contains Log Events.
	 * @param inStream InputStream containing the Log Event XML file as it's payload.
	 */
	protected void notifyIncomingLogEvents(InputStream inStream) {
		for(NetworkGraphListener l : networkListeners) {
			l.incomingLogEvents(inStream);
		}
	}

	//[end] Listener Methods

	//[start] HTTP Methods
	/**
	 * Connects through HTTP to the passed URL and returns an input stream with the incoming data.
	 * 
	 * Uses serverURL as the base and the passed url as an extension with possible attributes.
	 * @param url The URL to connect to possibly with attributes (/getLogEvents?&time=0)
	 * @return The incoming data stream.
	 */
	private InputStream connect(String url) throws IOException {
		
		URLConnection conn = new URL(serverURL + url).openConnection();
		conn.setConnectTimeout(connectTimeOut);
		conn.setReadTimeout(readTimeOut);

		InputStream in = conn.getInputStream();
		return in;
	}

	//[start] HTTP Getters
	/**
	 * 
	 * @throws IOException
	 */
	private void getGraph() throws IOException {
		String url = "/getGraph";

		notifyIncomingGraph(connect(url));
	}

	/**
	 * 
	 * @throws IOException
	 */
	private void getLogEvents() throws IOException {
		String url = "/getLogEvents?";
		url = addTimeOfLastResponse(url);//add a time attribute to the URL
		//Connect to the HTTP Server and receive an input stream.
		InputStream inStream = connect(url); 
		
		notifyIncomingLogEvents(inStream); //notify listeners of the incoming stream.

	}
	//[end] HTTP Getters

	//[start] URL Helpers
	/**
	 * Adds the latest time of a received event to the url for
	 * informing the server what events have already been received.
	 * 
	 * @param url The URL to add time as an attribute to.
	 * @return The full URL with a time attribute added on.
	 */
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