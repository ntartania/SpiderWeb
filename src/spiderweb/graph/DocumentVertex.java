/**
 * Contains the class definition for a DocumentVertex in the P2P Network Graph project.
 * @author Matthew Smith
 * @version May 12, 2011
 */
package spiderweb.graph;

/**
 * <p>A class which represents a Document as a vertex in the P2P network visualization.</p>
 * <ul>
 * <li>A document is drawn differently than a peer, as well, it has fewer states.</li>
 * <li>Has a state which describes if it was hit by a query or not.</li>
 * <li>Stores the key of it's publisher so it knows where it came from.</li>
 * </ul>
 * @author Matthew Smith
 * @version May 12, 2011
 */
public class DocumentVertex extends P2PVertex {
	
	//[start] Constructors
	
	/**
	 * Creates a vertex which represents a Document in the P2P network visualization.
	 * @param publisher	The key of the Peer which published this document.
	 * @param key	The key value of this document.
	 */
	public DocumentVertex(Integer key) {
		super(1000+key.intValue());
		//this.publisher = null; //remove this line
		this.label = new Integer(key.intValue() % 1000);
		//this.queryHit = false;
	}
	
	/**
	 * Copy Constructor
	 * @param vertex The DocumentVertex to copy
	 */
	public DocumentVertex(P2PVertex vertex) {
		super((vertex.getClass().equals(DocumentVertex.class) ? vertex.getKey().intValue():1000+vertex.getKey().intValue()));
		this.label = new Integer(vertex.getKey()%1000);
	}
	//[end] Constructors
	
	//[start] Getters and Setters
	public Integer getDocumentNumber() {
		return key % 1000;
	}
	//[end] Getters and Setters
	
	//[start] Overridden Methods
	@Override
	public boolean equals(Object other){
		if(other instanceof DocumentVertex) {
			return ( super.equals(other) );
		}
		return false; 
	}
	
	@Override
	public int compareTo(P2PVertex other) {
		if(other instanceof DocumentVertex)
			return ( super.compareTo(other) );
		else 
		return 0;
	}
	
	@Override
	public String toString() {
		return "D"+super.toString();
	}
	//[end] Overridden Methods
}