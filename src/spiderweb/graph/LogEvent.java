/*
 * File:         LogEvent.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

/**
 * The LogEvent class encapsulate information that modifies the network graph.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @author Alan Davoust
 * @version Date: 21/07/2011 
 */
public class LogEvent implements Comparable<LogEvent>{
	
	/* (non-javaDoc) Types of Events
	 * start
	 * end
	 * 
	 * online
	 * offline
	 * publish
	 * remove
	 * connect
	 * disconnect
	 * linkdocument
	 * delinkdocument
	 * 
	 * query
	 * unquery
	 * queryhit
	 * unqueryhit
	 * queryedge
	 * unqueryedge
	 * queryreachespeer
	 * unqueryreachespeer
	 */

	private long time=0;
	private String type="default"; //publish, join, connect...
	private int param1=0;
	private int param2=0;
	private int param3=0;

	/**constructor for an event as represented by a line in a (processed) log file*/
	public LogEvent(String str){
		// possible lines :
		//timemillisec [online |offline] peernumber
		//timemillisec [publish | remove] peernumber documentnumber
		//timemillisec query peernumber querynumber
		//timemillisec queryhit peernumber docnumber
		//timemillisec [connect | disconnect] peer1 peer2
		//timemillisec linkdocument doc1 doc2

		str.trim();
		//tokenize line.
		String [] words = str.split("[:]+");
	
		time = Long.parseLong(words[0]);
		type = words[1];
		param1 = Integer.parseInt(words[2]);
		param2 = 0;
		param3 = 0;
		if(words.length==4) {
			param2 = Integer.parseInt(words[3]);
		}
		if(words.length==5) {
			param3 = Integer.parseInt(words[4]);
		}

	}
	
	/**
	 * Constructor for the static colouredLogEvent which creates an event from another LogEvent which created it.
	 * @param time		the time in milliseconds the event happened
	 * @param type		the type of event occurring
	 * @param param1 	the first parameter (peer number)
	 * @param param2	the second parameter (peer2/doc/query number)
	 * @param param3	the third parameter(queryID)
	 */
	public LogEvent(long time, String type, int param1, int param2, int param3)
	{
		this.time = time;
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	/**
	 * Create a new LogEvent that depends on a LogEvent which created it.
	 * @param creator	The LogEvent which triggered the colouringEvent
	 * @param colouring	True if the event is to colour, false if it is a decolouring event
	 * @return a new LogEvent with parameters to colour 
	 */
	public static LogEvent createOpposingLogEvent(LogEvent creator,int delay)
	{
		return new LogEvent((creator.getTime()+delay), "un"+creator.getType(), creator.getParam(1), creator.getParam(2), creator.getParam(3));
	}
	
	/** indicates whether this event is a "construction" event in the graph (adds an edge or a vertex)*/
	public boolean isConstructing(){
		return (type.equals("connect")||type.equals("publish")||type.equals("online")||type.equals("linkdocument"));
	}
	/**events that modify the graph*/ 
	public boolean isStructural(){
		return (isConstructing()||type.equals("offline")||type.equals("disconnect")||type.equals("remove")||type.equals("delinkdocument"));
	}
	public boolean isImportantToPeer() {
		return (type.equals("connect")||type.equals("publish")||type.equals("disconnect")||type.equals("remove"));
	}
	
	public boolean isColouringEvent() {
		return !isStructural();
	}
	
	public long getTime(){
		return time;
	}

	public String getType(){
		return type;
	}
	
	/**
	 * Returns <code>true</code> if the event type has a second parameter.
	 * @return<code>true</code> if the event type has a second parameter.
	 */
	public boolean hasParamTwo() {
		return typeHasParamTwo(this.type);
	}
	
	/**
	 * Returns <code>true</code> if the event type has a second parameter.
	 * @return<code>true</code> if the event type has a second parameter.
	 */
	public static boolean typeHasParamTwo(String eventType) {
		return !(eventType.equals("online")||eventType.equals("offline"));
	}
	
	/**
	 * Returns <code>true</code> if the event type has a third parameter.
	 * @return<code>true</code> if the event type has a third parameter.
	 */
	public boolean hasParamThree() {
		return typeHasParamThree(this.type);
	}
	
	/**
	 * Returns <code>true</code> if the event type has a third parameter.
	 * @return<code>true</code> if the event type has a third parameter.
	 */
	public static boolean typeHasParamThree(String eventType) {
		return (eventType.equals("query") || eventType.equals("queryhit"));
	}
	
	
	/**
	 * Get the one of the three parameters from the Log Event
	 * corresponding to the passed value (1, 2 or 3)
	 * 
	 * -1 if not a valid parameter.
	 * @param which The parameter to get (1, 2 or 3)
	 * @return the value of the parameter (-1 if not a valid parameter)
	 */
	public int getParam(int which){
		if (which==1) {
			return param1;
		}
		else if(which==2) {
			return param2;
		}
		else if(which==3) {
			return param3;
		}
		return -1;
	}
	
	public Object[] toArray() {
		Object[] array = { (new Long(time)), type, (new Integer(param1)), (new Integer(param2)) };
		return array;
	}
	
	@Override
	public String toString() {
		return(time+":"+type+":"+param1+":"+param2);
	}
	
	@Override
	public int compareTo(LogEvent other) {
		if(this.time < other.time) { //sort first by time
			return -1;
		}
		else if(this.time > other.time) {
			return 1;
		}
		if(this.type.charAt(0) < other.type.charAt(0)) { //if the time is the same check the first letter of each
			return -1;
		}
		else if(this.type.charAt(0) < other.type.charAt(0)) {
			return 1;
		}
		return 0; //Otherwise they may as well be equal
	}
	
	
	public static LogEvent getStartEvent() {
		return new LogEvent("0:start:0:0");
	}
	public static LogEvent getEndEvent(LogEvent lastEventInList) {
		return new LogEvent((lastEventInList.getTime()+100)+":end:0:0");
	}
}
