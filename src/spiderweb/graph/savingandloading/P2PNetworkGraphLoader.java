/*
 * File:         P2PNetworkGraphLoader.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 26/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import spiderweb.graph.*;
import spiderweb.networking.HTTPClient;
import spiderweb.graph.LogEvent;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * The P2PNetworkGraphLoader is a class that will load multiple types 
 * of files and create graphs and log events from them.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public class P2PNetworkGraphLoader extends ProgressAdapter{
	
	private List<LogEvent> logList;
	private ReferencedNetworkGraph graph;
	

	public P2PNetworkGraphLoader() {
		super();
		logList = new ArrayList<LogEvent>();
		graph = new ReferencedNetworkGraph();
		
	}

	/**
	 * @return <code>true</code> if file loaded successfully
	 */
	public boolean doLoad() {
		String[] acceptedExtensions = { "xml","txt" };
		File file = chooseLoadFile(".xml and .txt Files", acceptedExtensions);
		if(file != null) {
			if(file.getAbsolutePath().endsWith(".txt")) {
				try {
					taskStarted(1,"Log Files");
					LogEventListBuilder logBuilder = new LogEventListBuilder();
					for(LoadingListener l : progressListeners) {
						logBuilder.addProgressListener(l);
					}
					logList = logBuilder.createList(new BufferedReader(new FileReader(file)));

					graph = logBuilder.getGraph(); //load hidden graph but keep visible graph empty
					taskComplete();
					return true;
				} catch(Exception e) {
					taskComplete();
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(file.getAbsolutePath().endsWith(".xml")) {
				try {
					SAXBuilder builder = new SAXBuilder();
					final Document networkDoc = builder.build(file);
					graphBuilder(networkDoc);
					logList.add(0,LogEvent.getStartEvent());
					logList.add(LogEvent.getEndEvent(logList.get(logList.size()-1)));
					return true;
				} catch(Exception e) {
					taskComplete();
					JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return false;
	}

	
	private void buildGraphVertices(Element nodeMap, P2PNetworkGraph startGraph) {
		int counter = 0;
		//Tell the listeners what is going to be loaded and how much
		taskStarted(nodeMap.getChildren().size(), "Vertices"); 

		for(Object o : nodeMap.getChildren()) {
			Element elem = (Element)o;
			String type = elem.getAttribute("type").getValue();

			if(type.equals("PeerVertex")) {
				int key = Integer.parseInt(elem.getChild("key").getText());
				graph.getReferenceGraph().addVertex(new PeerVertex(key));
				startGraph.addVertex(new PeerVertex(key));
			}
			else if(type.equals("DocumentVertex")) {
				int key = Integer.parseInt(elem.getChild("key").getText());
				graph.getReferenceGraph().addVertex(new DocumentVertex(key));
				startGraph.addVertex(new DocumentVertex(key));
			}
			progress(++counter);
		}
	}
	
	private int buildGraphEdges(Element edgeMap, P2PNetworkGraph startGraph) {
		int counter = 0;
		int edgeCounter=0;
		//Tell the listeners what is going to be loaded and how much
		taskStarted(edgeMap.getChildren().size(), "Edges");
		
		for(Object o : edgeMap.getChildren()) {
			Element elem = (Element)o;
			String type = elem.getAttribute("type").getValue();

			if(type.equals("PeerToPeer")) { //Peer to Peer
				int v1Key = Integer.parseInt(elem.getChild("v1").getText());
				int v2Key = Integer.parseInt(elem.getChild("v2").getText());
				P2PVertex peer1 = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(v1Key));
				P2PVertex peer2 = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(v2Key));
				startGraph.addEdge(new P2PConnection(P2PConnection.P2P,edgeCounter), peer1, peer2);
				graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2P,edgeCounter), peer1, peer2);
				edgeCounter++;
			}
			else if(type.equals("PeerToDocument")) { //Peer to Document
				int v1Key = Integer.parseInt(elem.getChild("v1").getText());
				int v2Key = Integer.parseInt(elem.getChild("v2").getText())%1000;
				P2PVertex document = graph.getReferenceGraph().getVertexInGraph(new DocumentVertex(v2Key));
				P2PVertex peer = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(v1Key));
				Pair<P2PVertex> pair = new Pair<P2PVertex>(peer, document);
				startGraph.addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter),pair);
				graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter),pair);
				edgeCounter++;

				PeerDocumentVertex pdv = new PeerDocumentVertex(v1Key, v2Key);
				graph.getReferenceGraph().addVertex(pdv);
				startGraph.addVertex(pdv);
				startGraph.addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter),peer,pdv);
				graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter),peer,pdv);
				edgeCounter++;

				startGraph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter),pdv,document);
				graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter),pdv,document);
				edgeCounter++;
			}
			progress(++counter);
		}
		return edgeCounter;
	}
	
	private void buildLogEvents(Element logElem, P2PNetworkGraph startGraph, int edgeCounter) {
		if(logElem != null) {
			taskStarted(logElem.getChildren().size(), "Events");
			int counter=0;
			for(Object o : logElem.getChildren()) {

				Element event = (Element)o;
				String type = event.getAttribute("type").getValue();
				if(type.equals("start") || type.equals("end")) {
					continue;
				}
				long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
				int paramOne = Integer.parseInt(event.getChildText("param1"));
				int paramTwo = 0;
				int paramThree = 0;
				if(LogEvent.typeHasParamTwo(type)) {
					paramTwo = Integer.parseInt(event.getChildText("param2"));
				}
				if(LogEvent.typeHasParamThree(type)) {
					paramThree = Integer.parseInt(event.getChildText("param3"));
				}
				LogEvent evt = new LogEvent(timeDifference, type, paramOne, paramTwo, paramThree);
				if(evt.isConstructing()) {
					if (evt.getType().equals("online")){
						graph.getReferenceGraph().addVertex(new PeerVertex(evt.getParam(1)));
					} else if(evt.getType().equals("connect")){
						P2PVertex from = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(1)));
						P2PVertex to = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(2)));
						if(graph.getReferenceGraph().findEdge(to, from) == null) {
							P2PConnection edgeOne = new P2PConnection(P2PConnection.P2P,edgeCounter);
							edgeCounter++;
							P2PConnection edgeTwo = new P2PConnection(P2PConnection.P2P,edgeCounter);
							edgeCounter++;
							graph.getReferenceGraph().addEdge(edgeOne, from, to);
							graph.getReferenceGraph().addEdge(edgeTwo, to, from);
						}
						// else the edge already exists
					} else if(evt.getType().equals("publish")){
						P2PVertex document = new DocumentVertex(evt.getParam(2));
						P2PVertex peerDocument = new PeerDocumentVertex(evt.getParam(1), evt.getParam(2));
						P2PVertex peer = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(1)));

						if(!graph.getReferenceGraph().containsVertex(document)) {
							graph.getReferenceGraph().addVertex(document);
						}
						else {
							document = graph.getReferenceGraph().getVertexInGraph(document);
						}
						graph.getReferenceGraph().addVertex(peerDocument);

						if(graph.getReferenceGraph().findEdge(peer, document) == null) {
							graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter), peer, document);
							edgeCounter++;
						}
						if(graph.getReferenceGraph().findEdge(peer, peerDocument) == null) {
							graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter), peer, peerDocument);
							edgeCounter++;
						}
						if(graph.getReferenceGraph().findEdge(peerDocument, document) == null) {
							graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter), peerDocument, document);
							edgeCounter++;
						}
					}
					else if(evt.getType().equals("linkdocument")){
						P2PVertex documentOne = graph.getReferenceGraph().getVertexInGraph(new DocumentVertex(evt.getParam(1)));
						P2PVertex documentTwo = graph.getReferenceGraph().getVertexInGraph(new DocumentVertex(evt.getParam(2)));
						if(graph.getReferenceGraph().findEdge(documentOne, documentTwo) == null) {
							graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeCounter), documentOne, documentTwo);
							edgeCounter++;
						}
					}
				}
				logList.add(evt);
				progress(++counter);
			}
		}
	}
	
	private void graphBuilder(Document networkDoc) {
		if(networkDoc.getRootElement().getName().equals("network")) {
			int edgeCounter=0; //to keep track of indexing the edges
			P2PNetworkGraph startGraph = new P2PNetworkGraph();
			Element networkElem = networkDoc.getRootElement();
			
			Element graphElem = networkElem.getChild("graph");
			if(graphElem != null) {//Create Graph
				taskStarted(1,"Start Building Graph");
				//Add Vertices to graph
				buildGraphVertices(graphElem.getChild("nodemap"), startGraph);

				//Add Edges to graph
				edgeCounter = buildGraphEdges(graphElem.getChild("edgemap"), startGraph);
			}

			//Create Logs
			buildLogEvents(networkElem.getChild("logevents"), startGraph, edgeCounter);
			
			graph.setDynamicGraph(startGraph);
			taskComplete();
		}
	}

	private void addEventsToGraph(Document networkDoc, long timeOffset) {
		if(networkDoc.getRootElement().getName().equals("network")) {
			int edgeCounter=graph.getReferenceGraph().getEdgeCount();
			Element networkElem = networkDoc.getRootElement();
			Element logElem = networkElem.getChild("logevents");
			if(logElem != null) {
				taskStarted(logElem.getChildren().size(), "Events");
				for(Object o : logElem.getChildren()) {

					Element event = (Element)o;
					String type = event.getAttribute("type").getValue();
					if(type.equals("start") || type.equals("end")) {
						continue;
					}
					long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
					long eventTime = timeDifference+timeOffset;
					int paramOne = Integer.parseInt(event.getChildText("param1"));
					int paramTwo = 0;
					int paramThree = 0;
					if(LogEvent.typeHasParamTwo(type)) {
						paramTwo = Integer.parseInt(event.getChildText("param2"));
					}
					if(LogEvent.typeHasParamThree(type)) {
						paramThree = Integer.parseInt(event.getChildText("param3"));
					}
					LogEvent evt = new LogEvent(eventTime, type, paramOne, paramTwo, paramThree);
					if(evt.isConstructing()) {
						if (evt.getType().equals("online")){
							graph.getReferenceGraph().addVertex(new PeerVertex(evt.getParam(1)));
						} else if(evt.getType().equals("connect")){
							P2PVertex from = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(1)));
							P2PVertex to = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(2)));
							if(graph.getReferenceGraph().findEdge(to, from) == null) {
								P2PConnection edgeOne = new P2PConnection(P2PConnection.P2P,edgeCounter);
								edgeCounter++;
								P2PConnection edgeTwo = new P2PConnection(P2PConnection.P2P,edgeCounter);
								edgeCounter++;
								graph.getReferenceGraph().addEdge(edgeOne, from, to);
								graph.getReferenceGraph().addEdge(edgeTwo, to, from);
							}
							// else the edge already exists
						} else if(evt.getType().equals("publish")){
							P2PVertex document = new DocumentVertex(evt.getParam(2));
							P2PVertex peerDocument = new PeerDocumentVertex(evt.getParam(1), evt.getParam(2));
							P2PVertex peer = graph.getReferenceGraph().getVertexInGraph(new PeerVertex(evt.getParam(1)));

							if(!graph.getReferenceGraph().containsVertex(document)) {
								graph.getReferenceGraph().addVertex(document);
							}
							else {
								document = graph.getReferenceGraph().getVertexInGraph(document);
							}
							graph.getReferenceGraph().addVertex(peerDocument);

							if(graph.getReferenceGraph().findEdge(peer, document) == null) {
								graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter), peer, document);
								edgeCounter++;
							}
							if(graph.getReferenceGraph().findEdge(peer, peerDocument) == null) {
								graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter), peer, peerDocument);
								edgeCounter++;
							}
							if(graph.getReferenceGraph().findEdge(peerDocument, document) == null) {
								graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter), peerDocument, document);
								edgeCounter++;
							}
						}
						else if(evt.getType().equals("linkdocument")){
							P2PVertex documentOne = graph.getReferenceGraph().getVertexInGraph(new DocumentVertex(evt.getParam(1)));
							P2PVertex documentTwo = graph.getReferenceGraph().getVertexInGraph(new DocumentVertex(evt.getParam(2)));
							if(graph.getReferenceGraph().findEdge(documentOne, documentTwo) == null) {
								graph.getReferenceGraph().addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeCounter), documentOne, documentTwo);
								edgeCounter++;
							}
						}
					}
					logList.add(evt);
				}
			}
		}
	}


	public List<LogEvent> getLogList() {
		return logList;
	}
	public ReferencedNetworkGraph getGraph() {
		return graph;
	}

	/**
	 * Brings up a JFileChooser for the choice of selecting a file to open for loading.
	 * 
	 * Filters the files visible by the passed extensions and brings up error dialogs if
	 * an incorrect file type is selected, or an error occurs.
	 * @param filterDescription The description of what file extensions are being filtered
	 * @param acceptedExtensions The file extensions to filter
	 * @return a <code>File</code> containing the information to load to the graph.
	 */
	public static File chooseLoadFile(String filterDescription, String[] acceptedExtensions) {
		JFileChooser fileNamer = new JFileChooser();
		fileNamer.setFileFilter(new ExtensionFileFilter(filterDescription, acceptedExtensions));
		int returnVal = fileNamer.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for(String extension : acceptedExtensions) {
				if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(extension)) {

					return fileNamer.getSelectedFile();
				}
			}
			JOptionPane.showMessageDialog(null, "Error: Incorrect extension.", "Error", JOptionPane.ERROR_MESSAGE);

			return null;
		}
		else if (returnVal == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(null, "Error: Could not load file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}


	public static P2PNetworkGraphLoader buildGraph(InputStream inStream, HTTPClient client) throws JDOMException, IOException {

		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(inStream);
		
		long currentTime = doc.getRootElement().getAttribute("currentTime").getLongValue();
		client.setZeroTime(currentTime);

		P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
		loader.logList.add(0,LogEvent.getStartEvent());
		loader.logList.add(LogEvent.getEndEvent(loader.logList.get(loader.logList.size()-1)));
		loader.graphBuilder(doc);

		return loader;
	}


	public static List<LogEvent> buildLogs(InputStream inStream, HTTPClient client, ReferencedNetworkGraph graph) throws JDOMException, IOException {
		
		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(inStream);
		long requestTime = doc.getRootElement().getAttribute("requestTime").getIntValue();
		long currentTime = doc.getRootElement().getAttribute("currentTime").getLongValue();
		
		client.setLatestTime(currentTime);
		
		P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
		loader.graph = graph;
		loader.addEventsToGraph(doc, requestTime);
		return loader.logList;
	}

}

