/*
 * File:         LogEvent.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       Matthew Smith
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
	
	/* (non-javaDoc) 
	 * Types of Events:
	 * 
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

	/**
	 * constructor for an event as represented by a line in a (processed) log file
	 * <pre>
	 * possible lines :
	 * 
	 * timemillisec:[online |offline]:peernumber
	 * timemillisec:[publish | remove]:peernumber documentnumber
	 * timemillisec:query:peernumber:querynumber:queryid
	 * timemillisec:queryreachespeer:peernumber:queryid
	 * timemillisec:queryhit:peernumber:docnumber:queryid
	 * timemillisec:[connect | disconnect]:peer1:peer2
	 * timemillisec:[linkdocument | delinkdocument]:doc1:doc2
	 * </pre>
	 * @param rawEvent The string to parse into a Log Event
	 */
	public LogEvent(String rawEvent){

		rawEvent.trim();
		//tokenize line.
		String [] words = rawEvent.split("[:]+");
	
		time = Long.parseLong(words[0]);
		type = words[1];
		param1 = Integer.parseInt(words[2]);
		param2 = 0;
		param3 = 0;
		if(words.length==4) {
			param2 = Integer.parseInt(words[3]);
		}
		if(words.length==5) {
			param2 = Integer.parseInt(words[3]);
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
	 * Create a new opposing LogEvent that depends on a LogEvent which created it.
	 * @param creator	The LogEvent which triggered the colouringEvent
	 * @param delay the amount of time after the creator for this event to occur
	 * @return a new LogEvent with parameters to undo what it's creator did 
	 */
	public static LogEvent createOpposingLogEvent(LogEvent creator,int delay)
	{
		return new LogEvent((creator.getTime()+delay), "un"+creator.getType(), creator.getParam(1), creator.getParam(2), creator.getParam(3));
	}
	
	/** 
	 * Indicates whether this event is a "construction" event in the graph (adds an edge or a vertex)
	 * @return <code>true</code>
	 */
	public boolean isConstructing(){
		return (type.equals("connect")||type.equals("publish")||type.equals("online")||type.equals("linkdocument"));
	}
	
	/**
	 * Indicates whether this event modifies the structure of the graph.
	 * @return <code>true</code> if the event is a type which modifies the structure of the graph.
	 */
	public boolean isStructural(){
		return (isConstructing()||type.equals("offline")||type.equals("disconnect")||type.equals("remove")||type.equals("delinkdocument"));
	}
	
	/**
	 * Indicates whether this event is important to an individual peer.
	 * @return <code>true</code> if this event matters to a peer.
	 */
	public boolean isImportantToPeer() {
		return (type.equals("connect")||type.equals("publish")||type.equals("disconnect")||type.equals("remove"));
	}
	
	/**
	 * Indicates whether or not this event type is a valid, known, event type.
	 * @param type	The type of event to check the validity of.
	 * @return <code>true</code> if the event is a valid type.
	 */
	public static boolean isValidEventType(String type) {
		return type.equals("start") || type.equals("end") || type.equals("online") || type.equals("offline") ||
			   type.equals("publish") || type.equals("remove") || type.equals("connect") || type.equals("disconnect") ||
			   type.equals("linkdocument") || type.equals("delinkdocument") || type.equals("query") || 
			   type.equals("unquery") || type.equals("queryhit") || type.equals("unqueryhit") || type.equals("queryedge") ||
			   type.equals("unqueryedge") || type.equals("queryreachespeer") || type.equals("unqueryreachespeer");
	}
	
	/**
	 * Gets whether this event is a "colouring" event in the graph
	 * @return <code>true</code> if the event has a type that requires colouring.
	 */
	public boolean isColouringEvent() {
		return (!isStructural() && isValidEventType(type));
	}
	
	/**
	 * Gets the time this event takes place.
	 * @return <code>long</code> time of this event.
	 */
	public long getTime(){
		return time;
	}

	/**
	 * Gets the type of log event this is.
	 * @return The type of this log event as a String.
	 */
	public String getType(){
		return type;
	}
	
	/**
	 * Returns <code>true</code> if the event type has a second parameter.
	 * @return <code>true</code> if the event type has a second parameter.
	 */
	public boolean hasParamTwo() {
		return typeHasParamTwo(this.type);
	}
	
	/**
	 * Returns <code>true</code> if the event type has a second parameter.
	 * @return <code>true</code> if the event type has a second parameter.
	 */
	public static boolean typeHasParamTwo(String eventType) {
		return !(eventType.equals("online")||eventType.equals("offline"));
	}
	
	/**
	 * Returns <code>true</code> if the event type has a third parameter.
	 * @return <code>true</code> if the event type has a third parameter.
	 */
	public boolean hasParamThree() {
		return typeHasParamThree(this.type);
	}
	
	/**
	 * Returns <code>true</code> if the event type has a third parameter.
	 * @return <code>true</code> if the event type has a third parameter.
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
	
	/**
	 * Converts this LogEvent to an Object array where:
	 * [ time, type, param1, param2, param3 ]
	 * @return The LogEvent array.
	 */
	public Object[] toArray() {
		Object[] array = { new Long(time), type, new Integer(param1), new Integer(param2), new Integer(param3) };
		return array;
	}
	
	@Override
	public String toString() {
		return(time+":"+type+":"+param1+":"+param2+":"+param3);
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
	
	/**
	 * Returns a LogEvent with type 'start' to signify it is the first event in the list.
	 * 
	 * @return LogEvent signifying the start of the list of events.
	 */
	public static LogEvent getStartEvent() {
		return new LogEvent("0:start:0:0");
	}
	
	/**
	 * Returns a LogEvent with type 'end' to signify it is the last event in the list.
	 * 
	 * Event is 100 ms after the passed 'last event'
	 * @param lastEventInList The last event in the current list as a way of adjusting time properly.
	 * @return LogEvent signifying the end of the list of events.
	 */
	public static LogEvent getEndEvent(LogEvent lastEventInList) {
		return new LogEvent((lastEventInList.getTime()+100)+":end:0:0");
	}
}
