package spiderweb;

//[start] Imports
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.uci.ics.jung.graph.util.Pair;
//[end] Imports

/**
 * @author Matthew Smith
 *
 */
public class GraphSaverAndLoader {
	
	//[start] File Choosers
	/**
	 * 
	 * @return
	 */
	private static String chooseSaveFile() {
		JFileChooser fileNamer = new JFileChooser();
		fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
		int returnVal = fileNamer.showSaveDialog(null);
		

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(".xml")) {

				return fileNamer.getSelectedFile().getAbsolutePath();
			}
			return new File(fileNamer.getSelectedFile().getAbsolutePath()+".xml").getAbsolutePath();
		}
		else if(returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	/**
	 * 
	 * @return
	 */
	private static String chooseLoadFile() {
		JFileChooser fileNamer = new JFileChooser();
		fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
		int returnVal = fileNamer.showOpenDialog(null);
		

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(".xml")) {

				return fileNamer.getSelectedFile().getAbsolutePath();
			}
			return new File(fileNamer.getSelectedFile().getAbsolutePath()+".xml").getAbsolutePath();
		}
		else if(returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	//[end] File Choosers
	
	//[start] XML outputters
	/**
     * This method shows how to use XMLOutputter to output a JDOM document to
     * the stdout.
     * This method corresponds to Listing 5.
     * @param myDocument the JDOM document built from Listing 2.
     */
    protected static boolean outputDocument(Document myDocument) {
    	try {
    		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(myDocument, System.out);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * This method shows how to use XMLOutputter to output a JDOM document to
     * a file located at xml/myFile.xml.
     * @param myDocument the JDOM document built from Listing 2.
     */
    protected static boolean outputDocumentToFile(Document myDocument, String path) {
        //setup this like outputDocument
        try {
        	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            //output to a file
            FileWriter writer = new FileWriter(path);
            outputter.output(myDocument, writer);
            writer.close();

        } catch(java.io.IOException e) {
        	JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
            //e.printStackTrace();
            return false;
        }
        return true;
    }
  //[end] XML outputters
	
    //[start] Saving Methods
    /**
	 * @param args
	 */
	public static void save(P2PNetworkGraph graph) {		
		String path = chooseSaveFile();
		if(path != null) {
			if(fileWriter(path, graph, null, 0)) {
				JOptionPane.showMessageDialog(null, "Success: File Saved", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void save(P2PNetworkGraph graph, List<LogEvent> events, long currentTime) {		
		String path = chooseSaveFile();
		if(path != null) {
			if(fileWriter(path, graph, events, currentTime)) {
				JOptionPane.showMessageDialog(null, "Success: File Saved", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
    
	private static boolean fileWriter(String path, P2PNetworkGraph graph, List<LogEvent> events, long currentTime) {
		//[start] compile separate lists for peers, documents and peerdocuments
		LinkedList<PeerVertex> peers = new LinkedList<PeerVertex>();
		LinkedList<DocumentVertex> documents = new LinkedList<DocumentVertex>();
		LinkedList<PeerDocumentVertex> peerDocuments = new LinkedList<PeerDocumentVertex>();
		LinkedList<P2PConnection> edges = new LinkedList<P2PConnection>(graph.getEdges());
		
		for(P2PVertex vertex : graph.getVertices()) {
			if(vertex.getClass().equals(PeerVertex.class)) {
				peers.addLast((PeerVertex) vertex);
			}
			else if(vertex.getClass().equals(DocumentVertex.class)) {
				documents.addLast((DocumentVertex) vertex);
			}
			else if(vertex.getClass().equals(PeerDocumentVertex.class)) {
				peerDocuments.addLast((PeerDocumentVertex) vertex);
			}
		}
		//[end] compile separate lists for peers, documents and peerdocuments
		
		// Create the root element
		Element networkElement = new Element("network");
		
		
		//[start] Creating Graph Elements
        Element graphElement = new Element("graph");
        //add a comment
        graphElement.addContent(new Comment("Description of a P2PNetworkGraph"));
        
        Element nodemap = new Element("nodemap");
		for(PeerVertex peer : peers) { //write out all the peer information
			Element node = new Element("node");
	        node.setAttribute("type", "PeerVertex");
	       
	        Element key = new Element("key");
	        key.addContent(Integer.toString(peer.getKey())); 
	        node.addContent(key);
	        
	        nodemap.addContent(node);
		}
		for(DocumentVertex doc : documents) {//write out all the document information
			Element node = new Element("node");
	        node.setAttribute("type", "DocumentVertex");
	        
	        Element key = new Element("key");
	        key.addContent(Integer.toString(doc.getDocumentNumber())); 
	        node.addContent(key);
	        
	        nodemap.addContent(node);
		}
		for(PeerDocumentVertex peerDoc : peerDocuments) {//write out all the peerdocument information
			Element node = new Element("node");
	        node.setAttribute("type", "PeerDocumentVertex");
	        
	        Element key = new Element("document");
	        key.addContent(Integer.toString(peerDoc.getDocumentNumber())); 
	        node.addContent(key);
	        
	        Element publisher = new Element("peer");
	        publisher.addContent(Integer.toString(peerDoc.getPeerNumber()));
	        node.addContent(publisher);
	        
	        nodemap.addContent(node);
		}
		graphElement.addContent(nodemap);
		
		Element edgemap = new Element("edgemap");
		for(P2PConnection e : edges) {//write out all the edge information
			Element edge = new Element("edge");
			
			switch(e.getType()) {
			case P2PConnection.P2P:
				edge.setAttribute("type", "PeerToPeer");
				break;
			case P2PConnection.P2DOC:
				edge.setAttribute("type", "PeerToDocument");
				break;
			case P2PConnection.P2PDOC:
				edge.setAttribute("type", "PeerToPeerDocument");
				break;
			case P2PConnection.DOC2PDOC:
				edge.setAttribute("type", "DocumentToPeerDocument");
				break;
			}
			Element key = new Element("key");
			key.addContent(Integer.toString(e.getKey()));
	        edge.addContent(key);
				        
	        Pair<P2PVertex> ends = graph.getEndpoints(e);
	        
	        Element v1 = new Element("v1");
	        v1.addContent(Integer.toString(ends.getFirst().getKey()));
	        edge.addContent(v1);
	        Element v2 = new Element("v2");
	        v2.addContent(Integer.toString(ends.getSecond().getKey()));
	        edge.addContent(v2);
	        
	        edgemap.addContent(edge);
		}
		graphElement.addContent(edgemap);
		networkElement.addContent(graphElement);
		//[end] Creating Graph Elements
		
		//[start] Creating LogEvent Elements
		if(events != null) {
			Element logEventsElement = new Element("logevents");
			for(LogEvent ev : events) {
				Element event = new Element("event");
				event.setAttribute("type", ev.getType());
		        
		        Element timeDifference = new Element("timedifference");
		        timeDifference.addContent(Integer.toString((int)(ev.getTime()-currentTime))); 
		        event.addContent(timeDifference);
		        
		        Element paramOne = new Element("param1");
		        paramOne.addContent(Integer.toString(ev.getParam(1)));
		        event.addContent(paramOne);
		        
		        if(ev.hasParamTwo()) {
		        	Element paramTwo = new Element("param2");
		        	paramTwo.addContent(Integer.toString(ev.getParam(2)));
			        event.addContent(paramTwo);
		        }
		        
		        logEventsElement.addContent(event);
			}
			
			networkElement.addContent(logEventsElement);
		}
		//[end] Creating LogEvent Elements
				
		
		//[start] Output Document to the file
		//create the document
        Document networkDoc = new Document(networkElement);
        
	    return outputDocumentToFile(networkDoc,path);
	    			
		//[end] Output Document to the file
	}
	//[end] Saving Methods
	
	//[start] Loading Methods
	
	/**
	 * @param args
	 */
	public static P2PNetworkGraph load() {
		String path = chooseLoadFile();
		if(path != null) {
			P2PNetworkGraph graph = fileLoader(path);
			if(graph != null) {
				JOptionPane.showMessageDialog(null, "Success: File Loaded", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
			return graph;
		}
		return null;
	}
	
	private static P2PNetworkGraph fileLoader(String path) {
		Document graphDoc;
		try {
			SAXBuilder builder = new SAXBuilder();
			graphDoc = builder.build(new File(path));
			//return anotherDocument;
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return graphBuilder(graphDoc);
	}
	
	private static P2PNetworkGraph graphBuilder(Document graphDoc) {
		if(graphDoc.getRootElement().getName().equals("network")) {
			P2PNetworkGraph graph = new P2PNetworkGraph();
			Element graphElem = graphDoc.getRootElement();
			
			//[start] Add Vertices to graph
			Element nodeMap = graphElem.getChild("nodemap");
			for(Object o : nodeMap.getChildren()) {
				Element elem = (Element)o;
				String type = elem.getAttribute("type").getName();
				
				if(type.equals("PeerVertex")) {
					int key = Integer.parseInt(elem.getChild("key").getText());
					graph.addVertex(new PeerVertex(key));
				}
				else if(type.equals("DocumentVertex")) {
					int key = Integer.parseInt(elem.getChild("key").getText());
					graph.addVertex(new DocumentVertex(key));
				}
				else if(type.equals("PeerDocumentVertex")) {
					int key = Integer.parseInt(elem.getChild("key").getText());
					int publisher = Integer.parseInt(elem.getChild("publisher").getText());
					graph.addVertex(new PeerDocumentVertex(publisher, key));
				}
			}
			//[end] Add Vertices to graph
			
			//[start] Add Edges to graph
			Element edgeMap = graphElem.getChild("edgemap");
			for(Object o : edgeMap.getChildren()) {
				Element elem = (Element)o;
				String type = elem.getAttribute("type").getName();
				
				if(type.equals("PeerToPeer")) { //Peer to Peer
					int key = Integer.parseInt(elem.getChild("key").getText());
					int v1Key = Integer.parseInt(elem.getChild("v1").getText());
					int v2Key = Integer.parseInt(elem.getChild("v2").getText());
					Pair<P2PVertex> pair = new Pair<P2PVertex>(new PeerVertex(v2Key), new PeerVertex(v1Key));
					graph.addEdge(new P2PConnection(P2PConnection.P2P,key),pair);
				}
				else if(type.equals("PeerToDocument")) { //Peer to Document
					int key = Integer.parseInt(elem.getChild("key").getText());
					int v1Key = Integer.parseInt(elem.getChild("v1").getText());
					int v2Key = Integer.parseInt(elem.getChild("v2").getText());
					Pair<P2PVertex> pair = new Pair<P2PVertex>(new DocumentVertex(v2Key-1000), new PeerVertex(v1Key));
					graph.addEdge(new P2PConnection(P2PConnection.P2DOC,key),pair);
				}
				else if(type.equals("PeerToPeerDocument")) { //Peer to PeerDocument
					int key = Integer.parseInt(elem.getChild("key").getText());
					int v1Key = Integer.parseInt(elem.getChild("v1").getText());
					int v2Key = Integer.parseInt(elem.getChild("v2").getText());
					Pair<P2PVertex> pair = new Pair<P2PVertex>(new PeerDocumentVertex(v1Key,v2Key-2000), new PeerVertex(v1Key));
					graph.addEdge(new P2PConnection(P2PConnection.P2PDOC,key),pair);
				}
				else if(type.equals("DocumentToPeerDocument")) { //Document to PeerDocument
					int key = Integer.parseInt(elem.getChild("key").getText());
					int v1Key = Integer.parseInt(elem.getChild("v1").getText());
					int v2Key = Integer.parseInt(elem.getChild("v2").getText());
					Pair<P2PVertex> pair = new Pair<P2PVertex>(new PeerDocumentVertex(v1Key,v2Key-2000), new DocumentVertex(v1Key-1000));
					graph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,key),pair);
				}
			}
			//[end] Add Edges to graph
			
			return graph;
		}
		return null;
	}
	
	//[end] Loading Methods
}
