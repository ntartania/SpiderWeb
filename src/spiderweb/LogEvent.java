package spiderweb;

/**
 * a simple class to encapsulate events to the graph as read from the
 * processed log file. 
 * @author adavoust
 *
 */
public class LogEvent implements Comparable<LogEvent>{
	
	/* (non-javaDoc) Types of Events
	 * start
	 * end
	 * 
	 * online
	 * offline
	 * publish
	 * depublish
	 * connect
	 * disconnect
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
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	
	/**constructor for an event as represented by a line in a (processed) log file*/
	public LogEvent(String str){
		// possible lines :
		//timemillisec [online |offline] peernumber
		//timemillisec publish peernumber documentnumber
		//timemillisec query peernumber querynumber
		//timemillisec queryhit peernumber docnumber
		//timemillisec connect peer1 peer2

		str.trim();
		//tokenize line.
		String [] words = str.split(":");
	
		time = Long.parseLong(words[0]);
		type = words[1];
		param1 = Integer.parseInt(words[2]);
		param2 = 0;
		if(words.length>3)
			param2 = Integer.parseInt(words[3]);

	}
	
	/**
	 * Constructor for the static colouredLogEvent which creates an event from another LogEvent which created it.
	 * @param time		the time in milliseconds the event happened
	 * @param type		the type of event occurring
	 * @param param1 	the first parameter (peer number)
	 * @param param2	the second parameter (peer2/doc/query number)
	 */
	public LogEvent(long time, String type, int param1, int param2)
	{
		this.time = time;
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
	}
	
	/**
	 * Create a new LogEvent that depends on a LogEvent which created it.
	 * @param creator	The LogEvent which triggered the colouringEvent
	 * @param colouring	True if the event is to colour, false if it is a decolouring event
	 * @return a new LogEvent with parameters to colour 
	 */
	public static LogEvent createOpposingLogEvent(LogEvent creator,int delay)
	{
		return new LogEvent((creator.getTime()+delay), "un"+creator.getType(), creator.getParam(1), creator.getParam(2));
	}
	
	/** indicates whether this event is a "construction" event in the graph (adds an edge or a vertex)*/
	public boolean isConstructing(){
		return (type.equals("connect")||type.equals("publish")||type.equals("online"));
	}
	/**events that modify the graph*/ 
	public boolean isStructural(){
		return (isConstructing()||type.equals("offline")||type.equals("disconnect")||type.equals("depublish")||type.equals("remove"));
	}
	public boolean isImportantToPeer() {
		return (type.equals("connect")||type.equals("publish")||type.equals("disconnect")||type.equals("remove"));
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
		return !(type.equals("online")||type.equals("offline"));
	}	
	
	/**
	 * get one of the parameters of the event
	 * @param which 1 or 2
	 * @return the value of the parameter
	 */
	public int getParam(int which){
		if (which==1)
			return param1;
		else
			return param2;
	}
	
	public Object[] toArray() {
		Object[] array = { (new Long(time)), type, (new Integer(param1)), (new Integer(param2)) };
		return array;
	}
	
	@Override
	public String toString() {
		return(time+":"+type+":"+param1+":"+param2+"\n");
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
}
