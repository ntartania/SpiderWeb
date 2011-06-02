package spiderweb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JSlider;
import javax.swing.Timer;

/**
 * an internal class extending thread, that can play the sequence of events from the log file in real time
 * or fast forward.
 * @author alan
 *
 */
public class EventPlayer implements ActionListener{
	
	private Timer schedule;
	private TimeCounter timeCounter;
	
	private static final int speed = 33; // 33 millisec between events while playing regularly
	private int fastMultiplier = 10;
	
	private PlayState state;

	private List<LogEvent> myEventList;
	
	private List<EventPlayerListener> my_listeners;
	
	private int current_index;
	
	private P2PNetworkGraph hiddenGraph;
	private P2PNetworkGraph visibleGraph;
	
	private long myTimeNow = 0L;
	
	JSlider playbackSlider;
	
	public EventPlayer(LinkedList<LogEvent> eventlist, P2PNetworkGraph hiddenGraph, P2PNetworkGraph visibleGraph, JSlider playbackSlider){
		this.hiddenGraph = hiddenGraph;
		this.visibleGraph = visibleGraph;
		this.playbackSlider = playbackSlider;
		myEventList = eventlist;
		current_index = 0; 
		state = PlayState.FORWARD;
		timeCounter = new TimeCounter(speed,0,eventlist.getFirst().getTime(),eventlist.getLast().getTime());
		my_listeners = new LinkedList<EventPlayerListener>();
	}
	
	public void addEventPlayerListener(EventPlayerListener epl) {
		my_listeners.add(epl);
	}
	
	public void setFastSpeed(int value) {
		if(value!=fastMultiplier) {
			fastMultiplier = value;
			if(state == PlayState.FASTFORWARD) {
				timeCounter.setIncrement(speed*fastMultiplier);
			} else if(state == PlayState.FASTREVERSE) {
				timeCounter.setIncrement(-speed*fastMultiplier);
			}
		}
	}
	
	public void save(File file) {
		
	}
	
	public PlayState getPlayState() {
		return state;
	}
	
	public int getCurrentIndex() {
		return current_index;
	}
	
	//[start] Playback Properties
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
	 * 
	 * @return
	 */
	public boolean atFront() {
		if(timeCounter.getTime() == timeCounter.getLowerBound()) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public boolean atBack() {
		if (timeCounter.getTime() == timeCounter.getUpperBound()) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public boolean atAnEnd() {
		if(atFront() || atBack()) {
			return true;
		}
		return false;
	}
	//[end]
	
	//[start] State Change handlers for button clicks.
	/**
	 * 
	 */
	public void fastReverse() {
		for(EventPlayerListener epl : my_listeners) {
			epl.playbackFastReverse();
		}
		if(state != PlayState.FASTREVERSE) {
			PlayState prevState = state;
			state = PlayState.FASTREVERSE;
			timeCounter.setIncrement(-speed*fastMultiplier);
			wakeup(prevState);
		}
	}
	
	public void reverse() {
		for(EventPlayerListener epl : my_listeners) {
			epl.playbackReverse();
		}
		if(state != PlayState.REVERSE) {
			PlayState prevState = state;
			state = PlayState.REVERSE;
			timeCounter.setIncrement(-speed);
			wakeup(prevState);
		}
	}

	public void fastForward(){
		for(EventPlayerListener epl : my_listeners) {
			epl.playbackFastForward();
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
			epl.playbackForward();
		}
		if(state != PlayState.FORWARD) {
			PlayState prevState = state;
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
			epl.playbackPause();
		}
		if(state != PlayState.PAUSE) {
			state = PlayState.PAUSE;
			notify();
			//schedule.stop();
			/*for(EventPlayerListener epl : my_listeners) {
				epl.doRepaint();
			}*/
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
		
		/*for( LogEvent evt : getLogEventsUntil(value) ) {
			handleLogEvent(evt);
		}*/
		/*for(EventPlayerListener epl : my_listeners) {
			epl.doRepaint();
		}*/
		
		timeCounter.setTime(value);
		state = prevState;
	}
	
	public void stopPlayback() {
		wakeup(state);
		schedule.stop();
	}
	//[end]
			 
	//[start] Graph Editors for highlighting and changing colours
	/**
	 * Visualize a query
	 * @param peer
	 * @param q
	 */
	public void doQuery(int peer, int queryMessageID){
		hiddenGraph.getPeer(peer).query(queryMessageID);
	}
	
	public void undoQuery(int peer, int queryMessageID){
		hiddenGraph.getPeer(peer).endQuery(queryMessageID);
	}
	
	
	public void doQueryEdge(int peerFrom, int peerTo) {
		hiddenGraph.findPeerConnection(peerFrom, peerTo).query();
	}
	
	public void undoQueryEdge(int peerFrom, int peerTo) {
		hiddenGraph.findPeerConnection(peerFrom, peerTo).backToNormal();
	}
	
	/**
	 * Visualize a query reaches peer event (bold edges)
	 * @param peer
	 * @param q
	 */
	public void doQueryReachesPeer(int peer, int queryMessageID){
		hiddenGraph.getPeer(peer).receiveQuery(queryMessageID);
	}
	/**
	 * Visualize a query reaches peer event (bold edges)
	 * @param peer
	 */
	public void undoQueryReachesPeer(int peer, int queryMessageID){
		hiddenGraph.getPeer(peer).endReceivedQuery(queryMessageID);
	}

	/**
	 * Visualize a queryHit
	 * @param peer
	 * @param q
	 */
	public void doQueryHit(int peerNumber, int documentNumber) {
		hiddenGraph.getPeerDocument(peerNumber, documentNumber).setQueryHit(true);
	}
	
	/**
	 * Visualize a queryHit
	 * @param peer
	 * @param q
	 */
	public void undoQueryHit(int peerNumber, int documentNumber) {
		hiddenGraph.getPeerDocument(peerNumber, documentNumber).setQueryHit(false);
	}
	//[end]


	public void run() {
		//System.out.println("Starting log event sequence.");

		//READING FROM CD++ LOG FILE/////////////
		
		schedule = new Timer(speed,this);
		schedule.start();

	}
	
	//[start] Graph Event Getting & Handling
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
				//if the difference in time is more than 5 seconds ignore query events as they will not have any 
				//importance since they all happen at once(both the query and unquery)
				/*if((timeGoingTo-evt.getTime()) > 5000) { 
					if(evt.isStructural()) {
						events.add(evt);
					} //else don't add it
				}
				else {
					events.add(evt);
				}*/
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
				//if the difference in time is more than 5 seconds ignore query events as they will not have any 
				//importance since they all happen at once(both the query and unquery)
				/*if((evt.getTime()-timeGoingTo) > 5000) { 
					if(evt.isStructural()) {
						events.add(evt);
					} //else don't add it
				}
				else {
					events.add(evt);
				}*/
				events.add(evt);
				evt = myEventList.get(current_index-1);
			}
		}
		return events;
	}
	
	/**
	 * Handles the passed LogEvent be it structural or visual.
	 * @param evt The Log event to handle.
	 */
	private void handleLogEvent(LogEvent evt, boolean forward) {
		
		if (evt.isStructural()){ //if the event is to modify the structure of the graph
			P2PNetworkGraph.graphEvent(evt,forward,visibleGraph,hiddenGraph);
		} else { //other events: queries
			String what = evt.getType();
			int val1 = evt.getParam(1);
			int val2 = evt.getParam(2);
			if(what.equals("query")) {
				if(forward) {
					doQuery(val1, val2);
				} else {
					undoQuery(val1,val2);
				}
			}
			else if (what.equals("unquery")) {
				if(forward) {
					undoQuery(val1,val2);
				} else {
					doQuery(val1, val2);
				}
			}
			else if (what.equals("queryhit")) {
				if(forward) {
					doQueryHit(val1, val2);
				} else {
					undoQueryHit(val1, val2);
				}
			}
			else if (what.equals("unqueryhit")) {
				if(forward) {
					undoQueryHit(val1, val2);
				} else {
					doQueryHit(val1, val2);
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
		}
	}
	//[end] Graph Event Handling

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(state != PlayState.PAUSE) {
			timeCounter.doIncrement();
		}
		long nextTime = timeCounter.getTime();

		boolean isforward = nextTime>myTimeNow;
		
		
		if(atAnEnd()) {
			pause();
		}
		
		List<LogEvent> events = getLogEventsUntil(nextTime);
		
		for( LogEvent evt :  events) {
			handleLogEvent(evt,isforward);
		}
		myTimeNow = nextTime; //advance time
		playbackSlider.setValue((int)myTimeNow);
		if(!events.isEmpty()) {
			for(EventPlayerListener epl : my_listeners) {
				epl.doRepaint();
			}// if anything happened, update visual
		}
		
	}

	public List<LogEvent> getSaveEvents() {
		ListIterator<LogEvent> i = myEventList.listIterator(current_index);
		List<LogEvent> events = new LinkedList<LogEvent>();
		while(i.hasNext()) {
			events.add(i.next());
		}
		return events;
	}
	
	public long getCurrentTime() {
		return myTimeNow;
	}
}