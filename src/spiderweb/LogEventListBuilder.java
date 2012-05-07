package spiderweb;

//[start] Imports
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//[end] Imports

public class LogEventListBuilder  {
	
	//[start] private variables
	private P2PNetworkGraph hiddenGraph;
	private List<LoadingListener> loadingListeners;
	private List<LogEvent> logEvents;
	//[end] private variables
	
	//[start] Constructor and listener initializer
	public LogEventListBuilder(P2PNetworkGraph hiddenGraph) {
		loadingListeners = new ArrayList<LoadingListener>();
		this.hiddenGraph = hiddenGraph;
	}
	
	public void addLoadingListener(LoadingListener loadingListener) {
		loadingListeners.add(loadingListener);
	}
	//[end] Constructor and listener initializer

	//[start] List initializers
	/**
	 * 
	 * @param logFile The file for which to read from 
	 * @return The created linked list of log events
	 */
	public LinkedList<LogEvent> createLinkedList(BufferedReader logFile) {
		logEvents = new LinkedList<LogEvent>();
		createList(logFile);
		
		return (LinkedList<LogEvent>) logEvents;
	}
	
	/**
	 * 
	 * @param logFile The file for which to read from 
	 * @return The created array list of log events
	 */
	public ArrayList<LogEvent> createArrayList(BufferedReader logFile) {
		logEvents = new ArrayList<LogEvent>();
		createList(logFile);
		
		return (ArrayList<LogEvent>) logEvents;
	}
	//[end] List initializers
	
	//[start] List Creator
	private void createList(BufferedReader logFile) {
		try {
			//[start] Create local variables for the creation of the list
			P2PNetworkGraph tempGraph = new P2PNetworkGraph();
			String str; //will contain each log event as it is read.
			List<LogEvent> colouringEvents = new LinkedList<LogEvent>();//list of events that represent the colouring and decolouring of graph elements
			List<P2PVertex> queryPeers = new LinkedList<P2PVertex>();//
			HashMap<String, List<String>> peerMap = new HashMap<String, List<String>>();
			
			final int totalLines = Integer.parseInt(logFile.readLine()); //the total number of lines so the loading bar can size itself properly
			int lineCount = 0;
			//[end] Create local variables for the creation of the list
			//[start] Notify listeners that the log events have begun loading
			for(LoadingListener l : loadingListeners) { //notify the listeners that the log events have begun loading
				l.loadingChanged(totalLines, "LogEvents");
			}
			//[end] Notify listeners that the log events have begun loading
			//[start] Loop reading the file and creating the list
			logEvents.add(new LogEvent("0:start:0:0")); //a start event to know when to stop playback of a reversing graph
			while ((str = logFile.readLine()) != null) //reading lines log file
			{
				//[start] Increment the line number and notify the loading listeners that we are lineCount number of lines through
				lineCount++;
				for(LoadingListener l : loadingListeners) { 
					l.loadingProgress(lineCount);
				}
				//[end] Increment the line number and notify the loading listeners that we are lineCount number of lines through
				
				LogEvent gev = new LogEvent(str);//create the log event
				
				if (gev.isConstructing()){ //construct the hiddenGraph as we go
					P2PNetworkGraph.graphConstructionEvent(gev,hiddenGraph);
				}
				
				createColouringEvents(gev,colouringEvents,queryPeers); //add in colouring events if needed
				createOfflineEvents(gev, peerMap); //create events for when a peer goes offline
				logEvents.add(gev); //add this read log event to the list
				createBackOnlineEvents(gev, peerMap, tempGraph);

				// Update the temporary graph afterwards so it can be used as a reference
				if (gev.isStructural()) { //construct the tempGraph which has the current connections including disconnects as a reference.
					P2PNetworkGraph.graphEvent(gev, true, tempGraph, hiddenGraph);
				}
				
			}//end while
			for(LogEvent leftOver : colouringEvents) {
				logEvents.add(leftOver); //if any colouring events are left over add them into the list
			}
			logEvents.add(new LogEvent((logEvents.get(logEvents.size()-1).getTime()+100)+":end:0:0")); //add an end log to know to stop the playback of the graph 100 ms after 
			//[end] Loop reading the file and creating the list
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	//[end] List Creator
	
	//[start] Create re-online events
	/**
	 * peer was previously online, went offline, and now came back online, re-add their documents and re-connect to their peers
	 */
	private void createBackOnlineEvents(LogEvent gev, HashMap<String, List<String>> peerMap, P2PNetworkGraph tempGraph) {
		//if the event is a peer coming online and the map contains a key for this peer (created when a peer goes offline)
			//re-connect all documents and peers
		if(gev.getType().equals("online") && peerMap.containsKey(Integer.toString(gev.getParam(1)))) {
			for(String evt : peerMap.get(Integer.toString(gev.getParam(1)))) { //iterate over the list of events created when this peer went offline.
				LogEvent event = new LogEvent(gev.getTime()+evt); //create the event to add
				if(event.getType().equals("publish")) {
					logEvents.add(event); //re-publish documents
				} else if(event.getType().equals("connect")) { 
					//if the peer we are trying to connect to has gone offline(known through the reference graph) don't re-connect
					if(tempGraph.containsVertex(new PeerVertex(event.getParam(2)))){ 
						logEvents.add(event); //but if they are online, connect!
					}
				}
			}
			peerMap.remove(Integer.toString(gev.getParam(1))); //once re-connected, remove the peer from the map until they go back offline
		}
	}
	//[end] Create re-online events
	
	//[start] Create events for when a peer goes offline
	private void createOfflineEvents(LogEvent gev, HashMap<String, List<String>> peerMap) {
		if(gev.getType().equals("offline")) { // if a peer goes offline
			P2PVertex peerGoingOffline = new PeerVertex(gev.getParam(1));
			
			if(!peerMap.containsKey(Integer.toString(gev.getParam(1)))) {
				peerMap.put(Integer.toString(gev.getParam(1)), new LinkedList<String>());
			}
			for(P2PConnection edge : hiddenGraph.getIncidentEdges(peerGoingOffline)) { //check through the nodes they are attached to
				
				P2PVertex opposite = hiddenGraph.getOpposite(peerGoingOffline, edge);
				if(opposite.getClass().equals(PeerDocumentVertex.class))//store their documents to re-publish if they decide to come back online 
				{
					peerMap.get(Integer.toString(gev.getParam(1))).add(":publish:"+gev.getParam(1)+":"+((PeerDocumentVertex) opposite).getDocumentNumber());
					logEvents.add(new LogEvent(gev.getTime(),"depublish",gev.getParam(1),((PeerDocumentVertex) opposite).getDocumentNumber()));
				}
				else if(opposite.getClass().equals(PeerVertex.class)) {//store their connected peers for when they come back online
					peerMap.get(Integer.toString(gev.getParam(1))).add(":connect:"+gev.getParam(1)+":"+((PeerVertex) opposite).getKey());
				}
			}
			
		}
	}
	//[end] Create events for when a peer goes offline

	//[start] Create and add colouring Events
	/**
	 * 
	 * @param gev	The LogEvent which is being handled.
	 * @param colouringEvents	The list of Colouring Events for which to add to.
	 * @param queryPeers	List of peers who are querying(for highlighting edges as the query propagates.
	 */
	private void createColouringEvents(LogEvent gev, List<LogEvent> colouringEvents, List<P2PVertex> queryPeers) {
		String eventType = gev.getType();
		if(eventType.equals("query") || eventType.equals("queryhit"))
		{
			colouringEvents.add(LogEvent.createOpposingLogEvent(gev,2000)); // add an opposing event to decolour/debold
			if(eventType.equals("query")) { //if it was a query then add the peer to the list of querying peers
				queryPeers.add(new PeerVertex(gev.getParam(1))); 
			}
		} 
		else if(eventType.equals("queryreachespeer")) {
			colouringEvents.add(LogEvent.createOpposingLogEvent(gev,750)); // add
			P2PVertex queriedPeer = new PeerVertex(gev.getParam(1));
			//[start] bold the edge between the peer which the query reached and the sender
			for(P2PVertex querySender : queryPeers) {
				if(hiddenGraph.findEdge(querySender, queriedPeer) != null) {
					LogEvent ev = new LogEvent(gev.getTime()+1,"queryedge",querySender.getKey(),queriedPeer.getKey());
					
					colouringEvents.add(ev);
					colouringEvents.add(LogEvent.createOpposingLogEvent(ev,750));
					break;
				}
			}
			//[end] bold the edge between the peer which the query reached and the sender
			queryPeers.add(queriedPeer);
		}
		Collections.sort(colouringEvents); // sort the events prior to adding them to the list of log events
		
		//List is sorted in order of ascending time so iterate from the start.
		for(Iterator<LogEvent> iter = colouringEvents.iterator(); iter.hasNext(); ) {
			
			LogEvent event = (LogEvent)iter.next();
			if(event.getTime() < gev.getTime() ) { //add only if the event takes place before the LogEvent that was read this iteration
				logEvents.add(event); //add the colouring event
				iter.remove(); //remove it from the list now that it is added
			} else {
				break; //if this Event's time is greater than gev, the rest should be too, then there is no point continuing
			}
		}
	}
	//[end] Create and add colouring Events
}
