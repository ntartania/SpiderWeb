/*
 * File:         NetworkGraphListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.networking;

import java.io.InputStream;

/**
 * The NetworkGraphListener is an interface for classes which are 
 * interested in getting log events and graphs from a HTTP server.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public interface NetworkGraphListener {
	
	/**
	 * The input stream coming in cointains information of Log Events after a given time.
	 * @param inStream InputStream containing the Log Event XML file as it's payload.
	 */
	public void incomingLogEvents(InputStream inStream);
	
	/**
	 * The input stream coming in cointains information of the Network Graph.
	 * @param inStream InputStream containing the Network Graph XML file as it's payload.
	 */
	public void incomingGraph(InputStream inStream);
}
