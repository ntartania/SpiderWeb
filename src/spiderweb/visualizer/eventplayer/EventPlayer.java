/*
 * File:         EventPlayer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.eventplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JSlider;
import javax.swing.Timer;

import spiderweb.graph.LogEvent;
import spiderweb.graph.ReferencedNetworkGraph;

/**
 * The Event Player is what plays the graph backwards and forward, 
 * it maintains a time and the state of the playback.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @author Alan Davoust
 * @version Date: 21/07/2011 
 */
public class EventPlayer implements ActionListener{

	private Timer schedule;
	private TimeCounter timeCounter;

	private static final int speed = 33; // 33 millisec between events while playing regularly
	private int fastMultiplier = 10;

	private PlayState state;
	private PlayState prevState;
	
	private List<LogEvent> myEventList;

	private List<EventPlayerListener> my_listeners;

	private int current_index;


	private ReferencedNetworkGraph graph;

	private long myTimeNow;

	JSlider playbackSlider;

	private boolean playable; //for when a graph is loaded without any events
	private boolean robustMode=false;

	/**
	 * TODO:
	 * @param graph
	 * @param eventlist
	 * @param playbackSlider
	 */
	public EventPlayer(ReferencedNetworkGraph graph, List<LogEvent> eventlist, JSlider playbackSlider){
		this.graph = graph;
		this.playbackSlider = playbackSlider;
		myEventList = eventlist;
		current_index = 0; 
		state = PlayState.FORWARD;
		prevState = PlayState.FORWARD;
		//timeCounter = new TimeCounter(speed,eventlist.getFirst().getTime(),eventlist.getFirst().getTime(),eventlist.getLast().getTime());
		timeCounter = new TimeCounter(speed,0,0,eventlist.get(eventlist.size()-1).getTime());
		my_listeners = new LinkedList<EventPlayerListener>();
		myTimeNow = timeCounter.getLowerBound();
		playable=true;
		
		
	}

	/**
	 * TODO:
	 * @param graph
	 */
	public EventPlayer(ReferencedNetworkGraph graph){
		this.graph = graph;
		this.playbackSlider = null;
		myEventList = new ArrayList<LogEvent>();
		current_index = 0; 
		state = PlayState.PAUSE;
		prevState = PlayState.PAUSE;
		timeCounter = new TimeCounter(0,0,0,0);
		my_listeners = new LinkedList<EventPlayerListener>();
		playable = false;
		
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.PAUSE);
		}
	}

	/**
	 * Sets the event player to be in robust mode
	 * 
	 * (robust mode mean that when an event is received that does not fully match
	 * with the current graph, an additional event will be placed in the list which 
	 * will satisfy the original event)
	 * @param robustMode <code>true</code> if the graph will be set to robust mode 
	 */
	public void setRobustMode(boolean robustMode) {
		this.robustMode = robustMode;
	}

	/**
	 * Returns whether or not the event player is in robust mode
	 * 
	 * (robust mode mean that when an event is received that does not fully match
	 * with the current graph, an additional event will be placed in the list which 
	 * will satisfy the original event)
	 * @return <code>true</code> if the graph is presently set to robust mode.
	 */
	public boolean isRobustMode() {
		return robustMode;
	}

	/**
	 * adds a <code>EventPlayerListener</code> to the player.
	 * @param listener the <code>EventPlayerListener</code> to be removed
	 */
	public void addEventPlayerListener(EventPlayerListener listener) {
		my_listeners.add(listener);
	}

	/**
	 * Removes a <code>EventPlayerListener</code> from the player.
	 * @param listener the <code>EventPlayerListener</code> to be removed
	 */
	public void removeEventPlayerListener(EventPlayerListener listener) {
		my_listeners.remove(listener);
	}
	
	/**
	 * Sets the speed multiplier of the fast playback
	 * @param multiplier the new speed multiplier.
	 */
	public void setSpeedMultiplier(int multiplier) {
		if(multiplier!=fastMultiplier) {
			fastMultiplier = multiplier;
			if(state == PlayState.FASTFORWARD) {
				timeCounter.setIncrement(speed*fastMultiplier);
			} else if(state == PlayState.FASTREVERSE) {
				timeCounter.setIncrement(-speed*fastMultiplier);
			}
		}
	}

	/**
	 * Returns the current <code>PlayState</code> of the player
	 * @return the player's current <code>PlayState</code>
	 */
	public PlayState getPlayState() {
		return state;
	}

	/**
	 * Returns the current index through the list of events the player is presently at.
	 * @return
	 */
	public int getCurrentIndex() {
		return current_index;
	}

	/**
	 * Returns whether or not the graph is playing forward or backwards.
	 * @return <code>true</code> if the Play State is forward or fast forward.
	 */
	public boolean isForward() {
		return ((state == PlayState.FASTFORWARD) || (state == PlayState.FORWARD));
	}
	/**
	 * Returns whether or not the graph is playing fast
	 * @return <code>true</code> if the Play State is fast in forward or reverse.
	 */
	public boolean isFast() {
		return ((state == PlayState.FASTFORWARD) || (state == PlayState.FASTREVERSE));
	}
	/**
	 * Returns whether or not the playback is at the start of the list of events.
	 * @return <code>true</code> if the playback is on the first event.
	 */
	public boolean atFront() {
		if(timeCounter.getTime() == timeCounter.getLowerBound()) {
			return true;
		}
		return false;
	}
	/**
	 * Returns whether or not the playback is at the end of the list of events.
	 * @return <code>true</code> if the playback is on the last event.
	 */
	public boolean atBack() {
		if (timeCounter.getTime() == timeCounter.getUpperBound()) {
			return true;
		}
		return false;
	}
	/**
	 * Returns whether or not the playback is at the start or at the end of the list of events.
	 * @return <code>true</code> if the playback is on the first or last event.
	 */
	public boolean atAnEnd() {
		if(atFront() || atBack()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public void fastReverse() {
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.FASTREVERSE);
		}
		if(state != PlayState.FASTREVERSE) {
			prevState = state;
			state = PlayState.FASTREVERSE;
			timeCounter.setIncrement(-speed*fastMultiplier);
			wakeup(prevState);
		}
	}

	public void reverse() {
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.REVERSE);
		}
		if(state != PlayState.REVERSE) {
			prevState = state;
			state = PlayState.REVERSE;
			timeCounter.setIncrement(-speed);
			wakeup(prevState);
		}
	}

	public void fastForward(){
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.FASTFORWARD);
		}
		if(state != PlayState.FASTFORWARD) {
			PlayState prevState = state;
			state = PlayState.FASTFORWARD;
			timeCounter.setIncrement(speed*fastMultiplier);
			wakeup(prevState);
		}
	}

	public void forward(){
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.FORWARD);
		}
		if(state != PlayState.FORWARD) {
			prevState = state;
			state = PlayState.FORWARD;
			timeCounter.setIncrement(speed);
			wakeup(prevState);
		}
	}

	private synchronized void wakeup(PlayState previousState) {
		if(previousState == PlayState.PAUSE) {
			if(atAnEnd()) {
				timeCounter.doIncrement();
			}
			schedule.start();
			notify();
		}
	}

	public synchronized void pause(){
		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(PlayState.PAUSE);
		}
		if(state != PlayState.PAUSE) {
			prevState = state;
			state = PlayState.PAUSE;
			notify();
		}
	}

	public void goToTime(int value) {
		PlayState prevState = state;

		if(value < timeCounter.getTime()) {
			state = PlayState.REVERSE;
		}
		else {
			state = PlayState.FORWARD;
		}

		timeCounter.setTime(value);
		state = prevState;
	}

	public void stopPlayback() {
		wakeup(state);
		schedule.stop();
	}

	/**
	 * Visualize a query
	 * @param peer
	 */
	public void doQuery(int peer, int queryString, int queryMessageID){
		graph.getFullGraph().getPeer(peer).query(queryMessageID);
	}

	public void undoQuery(int peer, int queryString, int queryMessageID){
		graph.getFullGraph().getPeer(peer).endQuery(queryMessageID);
	}


	public void doQueryEdge(int peerFrom, int peerTo) {
		graph.getFullGraph().findPeerConnection(peerFrom, peerTo).query();
	}

	public void undoQueryEdge(int peerFrom, int peerTo) {
		graph.getFullGraph().findPeerConnection(peerFrom, peerTo).backToNormal();
	}

	/**
	 * Visualize a query reaches peer event (bold edges)
	 * @param peer
	 * @param queryMessageID
	 */
	public void doQueryReachesPeer(int peer, int queryMessageID){
		graph.getFullGraph().getPeer(peer).receiveQuery(queryMessageID);
	}
	/**
	 * Visualize a query reaches peer event (bold edges)
	 * @param peer
	 */
	public void undoQueryReachesPeer(int peer, int queryMessageID){
		graph.getFullGraph().getPeer(peer).endReceivedQuery(queryMessageID);
	}


	public void doQueryHit(int peerNumber, int documentNumber, int queryMessageID) {
		graph.getFullGraph().getPeerDocument(peerNumber, documentNumber).setQueryHit(true);
	}

	public void undoQueryHit(int peerNumber, int documentNumber, int queryMessageID) {
		graph.getFullGraph().getPeerDocument(peerNumber, documentNumber).setQueryHit(false);
	}


	public void beginPlayback() {

		for(EventPlayerListener epl : my_listeners) {
			epl.stateChanged(state);
		}
		
		schedule = new Timer(speed,this);
		schedule.start();

	}

	/**
	 * current_index is always the next event with time greater than the simulation time.
	 * 
	 * if current index is 3, simulation time (represented by '|') will be less than the index.
	 * [0]-[1]-[2]-[3]-[4]-[5]-[6]
	 *            |
	 * 
	 * @param timeGoingTo The simulation time (in milliseconds) to play events up to.
	 * @return	The list of log events which need to be taken care of for this time span.
	 */
	private List<LogEvent> getLogEventsUntil(long timeGoingTo) {
		List<LogEvent> events = new LinkedList<LogEvent>();
		LogEvent evt;
		if(myTimeNow<timeGoingTo) {
			evt = myEventList.get(current_index);
			while(evt.getTime() < timeGoingTo) {
				current_index++;
				if(current_index >= myEventList.size()) {
					current_index = myEventList.size()-1;
					break;
				}
				events.add(evt);
				evt = myEventList.get(current_index);

			}
		}
		else {
			evt = myEventList.get(current_index-1);
			while(evt.getTime() > timeGoingTo) {

				current_index--;
				if(current_index < 1) {
					break;
				}
				events.add(evt);
				evt = myEventList.get(current_index-1);
			}
		}
		return events;
	}

	private void robustHandleLogEvent(LogEvent evt, boolean forward) {

		try {
			graph.graphEvent(evt,forward);
			String what = evt.getType();
			int val1 = evt.getParam(1);
			int val2 = evt.getParam(2);
			int val3 = evt.getParam(3);
			if(what.equals("query")) {
				if(forward) {
					doQuery(val1, val2, val3);
				} else {
					undoQuery(val1,val2, val3);
				}
			}
			else if (what.equals("unquery")) {
				if(forward) {
					undoQuery(val1,val2, val3);
				} else {
					doQuery(val1, val2, val3);
				}
			}
			else if (what.equals("queryhit")) {
				if(forward) {
					doQueryHit(val1, val2, val3);
				} else {
					undoQueryHit(val1, val2, val3);
				}
			}
			else if (what.equals("unqueryhit")) {
				if(forward) {
					undoQueryHit(val1, val2, val3);
				} else {
					doQueryHit(val1, val2, val3);
				}
			}
			else if (what.equals("queryreachespeer")) {
				if(forward) {
					doQueryReachesPeer(val1,val2);
				} else {
					undoQueryReachesPeer(val1,val2);
				}
			}
			else if (what.equals("unqueryreachespeer")) {
				if(forward) {
					undoQueryReachesPeer(val1,val2);
				} else {
					doQueryReachesPeer(val1,val2);
				}
			}
			else if (what.equals("queryedge")) {
				if(forward) {
					doQueryEdge(val1,val2);
				} else {
					undoQueryEdge(val1,val2);
				}
			}
			else if (what.equals("unqueryedge")) {
				if(forward) {
					undoQueryEdge(val1,val2);
				} else {
					doQueryEdge(val1,val2);
				}
			}

		}catch(Exception e) {
			e.printStackTrace();
			System.err.println(evt);
		}
	}

	/**
	 * Handles the passed LogEvent be it structural or visual.
	 * @param evt The Log event to handle.
	 */
	private void handleLogEvent(LogEvent evt, boolean forward) {
		try {
			if (evt.isStructural()){ //if the event is to modify the structure of the graph
				graph.graphEvent(evt,forward);
			} else { //other events: queries
				String evtType = evt.getType();
				int val1 = evt.getParam(1);
				int val2 = evt.getParam(2);
				int val3 = evt.getParam(3);
				if(evtType.equals("query")) {
					if(forward) {
						doQuery(val1, val2, val3);
					} else {
						undoQuery(val1,val2, val3);
					}
				}
				else if (evtType.equals("unquery")) {
					if(forward) {
						undoQuery(val1,val2, val3);
					} else {
						doQuery(val1, val2, val3);
					}
				}
				else if (evtType.equals("queryhit")) {
					if(forward) {
						doQueryHit(val1, val2, val3);
					} else {
						undoQueryHit(val1, val2, val3);
					}
				}
				else if (evtType.equals("unqueryhit")) {
					if(forward) {
						undoQueryHit(val1, val2, val3);
					} else {
						doQueryHit(val1, val2, val3);
					}
				}
				else if (evtType.equals("queryreachespeer")) {
					if(forward) {
						doQueryReachesPeer(val1,val2);
					} else {
						undoQueryReachesPeer(val1,val2);
					}
				}
				else if (evtType.equals("unqueryreachespeer")) {
					if(forward) {
						undoQueryReachesPeer(val1,val2);
					} else {
						doQueryReachesPeer(val1,val2);
					}
				}
				else if (evtType.equals("queryedge")) {
					if(forward) {
						doQueryEdge(val1,val2);
					} else {
						undoQueryEdge(val1,val2);
					}
				}
				else if (evtType.equals("unqueryedge")) {
					if(forward) {
						undoQueryEdge(val1,val2);
					} else {
						doQueryEdge(val1,val2);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println(evt);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(playable) {
			if(state != PlayState.PAUSE) {
				timeCounter.doIncrement();
				if(atAnEnd()) {
					pause();
				}
			}
			long nextTime = timeCounter.getTime();

			boolean isforward = nextTime>myTimeNow;

			List<LogEvent> events = getLogEventsUntil(nextTime);

			for( LogEvent evt :  events) {
				if(robustMode) {
					robustHandleLogEvent(evt,isforward);
				}
				else {
					handleLogEvent(evt,isforward);
				}
			}
			myTimeNow = nextTime; //advance time
			playbackSlider.setValue((int)myTimeNow);
			if(!events.isEmpty()) {
				for(EventPlayerListener epl : my_listeners) {
					epl.doRepaint();
				}// if anything happened, update visual
			}
		}
		else { 
			for(EventPlayerListener epl : my_listeners) {
				epl.doRepaint();
			}// since it isn't playable, re-draw the graph every scheduled time.
		}
	}

	/**
	 * returns all the events after the current index for saving
	 * @return <code>List</code> of LogEvents for saving to an xml file.
	 */
	public List<LogEvent> getSaveEvents() {
		ListIterator<LogEvent> i = myEventList.listIterator(current_index);
		List<LogEvent> events = new LinkedList<LogEvent>();
		while(i.hasNext()) {
			events.add(i.next());
		}
		return events;
	}

	/**
	 * Adds the passed list of <code>LogEvent</code>s to the event player
	 * @param events List of <code>LogEvent</code>s to add.
	 */
	public synchronized void addEvents(List<LogEvent> events) {
		//current_index--;
		myEventList.remove(myEventList.size()-1);
		myEventList.addAll(events);
		playbackSlider.setMaximum((int) myEventList.get(myEventList.size()-1).getTime());
		timeCounter.setUpperBound(myEventList.get(myEventList.size()-1).getTime());
		if(state.equals(PlayState.PAUSE)) {
			for(EventPlayerListener epl : my_listeners) {
				epl.stateChanged(PlayState.PAUSE);
			}
		}
	}

	/**
	 * Returns the current time of the player through the playback.
	 * @return the player's time.
	 */
	public long getCurrentTime() {
		return myTimeNow;
	}
}