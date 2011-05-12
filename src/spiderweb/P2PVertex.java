package spiderweb;


/**
 * a class for vertices of the P2P network visualization:
 * 
 * @author Alan
 *
 */
public abstract class P2PVertex implements Comparable<P2PVertex> {
	
	protected Integer label;
	protected Integer key;
		
	/**
	 * Creates a vertex in the network Graph
	 * @param key The identifier which defines this vertex.
	 */
	public P2PVertex(Integer key){
		this.key = key;
	}
	
	/**
	 * 
	 * @return Returns the label of this vertex.
	 */
	public Integer getLabel(){
		return label;
	}
	
	@Override
	public String toString() {
		return label.toString();
	}
	
	public Integer getKey(){
		return key;
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
	public int compareTo(P2PVertex other) {
		if(other instanceof P2PVertex)
			return (key.compareTo(((P2PVertex)other).getKey()));
		else 
		return 0; // there's a problem anyway : can only compare two P2PVertices
	}
}
