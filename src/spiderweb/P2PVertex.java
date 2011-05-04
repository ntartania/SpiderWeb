package spiderweb;

/**
 * a class for vertices of my P2P network visualization:
 * this vertex can be a peer or a document, which makes a difference in its rendering.
 * The node may also have different states, it can be querying, answering a query...
 * 
 * @author Alan
 *
 */
public class P2PVertex implements Comparable {

	public static final int PEER = 0;
	public static final int DOC = 10;
	public static final int QUERYING = 1;
	public static final int ANSWERING = 2;
	public static final int GETQUERY = 3;
	public static final int MATCHING_DOC = 11;
	private int mytypeandstate;
	private int publisher;
	private int querymessageId; // this indicates which query has been received when in the state "GETQUERY"
	private Integer mylabel;
	private Integer key;
	
	/** 
	 * a static number to create a document vertex where the document is published by a particular peer (sets the vertex key correctly)
	 * @param peernumber peer publishing the doc
	 * @param docnumber doc being published
	 * @return
	 */
	public static P2PVertex PeerPublishesDoc(int peernumber, int docnumber){
		P2PVertex v = new P2PVertex(DOC, new Integer((peernumber+1)*1000+docnumber)); 
		v.setPublisher(peernumber);
		return v;
	}
	
	public static P2PVertex makePeerVertex(int number){
		return  new P2PVertex(PEER, new Integer(number));
	}
	
	public void setPublisher(int i) {
		publisher = i;
	}
	
	public int getmessageid(){
		return querymessageId;
	}
	
	public P2PVertex(int PeerOrDoc, Integer kkey){
		mytypeandstate = PeerOrDoc;
		key = kkey;
		
		if (isPeer())
			mylabel = key;
		else{
			// the label for documents is just the document number, but their key is the publishing peer *1000 (+1 to deal with peer #0) + the document number
			int lblvalue = key.intValue()% 1000;
			mylabel = new Integer(lblvalue); 
		}
	}
	
	/** informs the caller whether this vertex is a */
	public boolean isPeer(){
		return (mytypeandstate <10);
	}
	
	public Integer getLabel(){
		return mylabel;
	}
	
	public String toString(){
		return (isPeer()? "P":"") + mylabel.toString();
	}
	
	public Integer getKey(){
		return key;
	}
	
	public int getQueryState(){
		return mytypeandstate;
	}
	
	public void query(int q) {
		if (isPeer()){
			mytypeandstate = QUERYING;
			querymessageId = q;
		}
		
	}
	/**
	 * highlights a peer as answering a query OR a doc as being a queryhit
	 */
	public void answering(){
		if (isPeer()){
			mytypeandstate = ANSWERING;
		} else
			mytypeandstate = MATCHING_DOC;
	}
	
	//Important : most Graph classes seem to rely on equals() to find vertices in their collection.
	@Override
	public boolean equals(Object other){
		if(other instanceof P2PVertex)
			return (key.equals(((P2PVertex)other).getKey()));
			else 
		return false; 
	}

	@Override
	public int hashCode(){
	return key.hashCode();	
	}
	
	@Override
	public int compareTo(Object other) {
		if(other instanceof P2PVertex)
			return (key.compareTo(((P2PVertex)other).getKey()));
		else 
		return 0; // there's a problem anyway : can only compare two P2PVertices
	}

	/** change state back to normal if the node was in one of the states "query", etc.*/
	public void backToNormal() {
		if (isPeer()){
			mytypeandstate = PEER;
		} else 
			mytypeandstate = DOC;
	}

	public int getPublishingPeer() {
		if (isPeer())
			return mylabel.intValue();
		else
			return publisher;
		
	}

	/** state when the peer receives a query*/
	public void receivingQuery(int qid) {
		if (isPeer()){
			mytypeandstate = GETQUERY;
			querymessageId= qid; // this can be helpful to track a query
		}
	}

	
	
}
