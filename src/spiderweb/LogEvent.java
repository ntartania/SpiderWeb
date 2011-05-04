package spiderweb;

/**
 * a simple class to encapsulate events to the graph as read from the
 * processed log file. 
 * Testing
 * @author adavoust
 *
 */
public class LogEvent {

	private long time=0;
	private String type="default"; //publish, join, connect...
	private int param1=0;
	private int param2=0;
	
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
			param2= Integer.parseInt(words[3]);

	}
	/** indicates whether this event is a "construction" event in the graph (adds an edge or a vertex)*/
	public boolean isConstructing(){
		return (type.equals("connect")||type.equals("publish")||type.equals("online"));
		
	}
	public long getTime(){
		return time;
	}
	public String getType(){
		return type;
	}
	
	/**events that modify the graph*/ 
	public boolean isStructural(){
		return (isConstructing()||type.equals("offline"));
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
	
	
}
