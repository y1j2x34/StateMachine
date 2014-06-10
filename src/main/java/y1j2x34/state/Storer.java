package y1j2x34.state;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Storer<Action> {
	private static final String DTD_URL = "src/test/resources/machine.dtd";
	private static Logger log = Logger.getLogger(StateMachine.class.getName());
	
	private StateMachine<Action> machine;
	public Storer(StateMachine<Action> machine) {
		this.machine = machine;
	}
	public void storeToXml(OutputStream os,String encoding) throws IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            assert(false);
        }
        Document doc = db.newDocument();
        
        Element machineElm = doc.createElement("machine");
//        machineElm.setAttribute("judgement", machine.getJudge().getClass().getName());
        
        //初始化标签
        Element initElm = createInitElm(machine, doc);
        
        //状态定义标签
        Map<String,State<Action>> nameStates = new HashMap<String,State<Action>>();
		Map<Link,Boolean> linkTags = new HashMap<Link,Boolean>();
		Element defElm = doc.createElement("def");
		State<Action> initState = machine.getInitialState();
		nameStates.put(initState.getName(), initState);
		
		List<Element> stateElms = createStateElms(initState, doc, nameStates, linkTags);
		for(Element elm:stateElms){
			defElm.appendChild(elm);
		}
		//关系设定标签
		
		Element confElm = doc.createElement("conf");
		
		for(Link link:linkTags.keySet()){
			Element linkElm = doc.createElement("link");
			linkElm.setAttribute("from", link.from);
			linkElm.setAttribute("to", link.to);
			linkElm.setAttribute("val", String.valueOf(link.condition.getValue()));
			linkElm.setAttribute("condition", link.condition.getClass().getName());
			confElm.appendChild(linkElm);
		}
		
		//当前状态
		Element curElm = doc.createElement("cur");
		curElm.setTextContent(machine.current().getName());
		
		machineElm.appendChild(initElm);
		machineElm.appendChild(defElm);
		machineElm.appendChild(confElm);
		machineElm.appendChild(curElm);
		//监听器
		StateListener<Action> lst = machine.getStateListener();
		if(lst != machine && lst != null){
			Element lsrElm = doc.createElement("listener");
			Class<?> cls = machine.getStateListener().getClass();
			try{
				if(cls.getConstructor() != null){
					lsrElm.setAttribute("class",cls.getName());
					machineElm.appendChild(lsrElm);
				}
			}catch(Throwable t){
				log.warning("state listener class <"+cls.getName() + "> cannot be instantiated!");
			}
//			if(lst instanceof Serializable){
//				ByteArrayOutputStream bout = IOUtils.serialize(lst);
//				lsrElm.setTextContent(new String(bout.toByteArray(),"UTF-8"));
//			}
		}
		
		doc.appendChild(machineElm);
		emitDocument(doc, os, encoding);
	}
	private void emitDocument(Document doc,OutputStream os,String encoding) throws IOException{
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        try {
            t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DTD_URL);
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
        } catch (TransformerConfigurationException tce) {
            assert(false);
        }
        DOMSource doms = new DOMSource(doc);
        StreamResult sr = new StreamResult(os);
        try {
            t.transform(doms, sr);
        } catch (TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
	}
	
	private Element createInitElm(StateMachine<Action> machine,Document doc){
		State<Action> init = machine.getInitialState();
		
		Element initElm = doc.createElement("init");
		initElm.appendChild(createStateElm(init, doc));
		return initElm;
	}
	
	private Element createStateElm(State<Action> state,Document doc){
		Element stateElm = doc.createElement("state");
		stateElm.setAttribute("name", state.getName());
		stateElm.setAttribute("action", state.getCallback().getClass().getName());
		return stateElm;
	}
	 
	private List<Element> createStateElms(State<Action> state,Document doc,Map<String,State<Action>> nameStates,Map<Link,Boolean> linkTags){
		List<Element> stateElms = new LinkedList<Element>();
		
		List<State<Action>.LinkData> linkDatas = state.getLinks();
		for(State<Action>.LinkData linkData:linkDatas){
			State<Action> stateI = linkData.getState();
			Link link = new Link(state.getName(),stateI.getName(),linkData.getCondition());
			Boolean li = linkTags.get(link);
			if(li != null && li){
				continue;
			}
			if(stateI != state){
				if(!nameStates.containsKey(stateI.getName())){
					Element stateElm = doc.createElement("state");
					stateElm.setAttribute("name", stateI.getName());
					stateElm.setAttribute("action", stateI.getCallback().getClass().getName());
					stateElms.add(stateElm);
					nameStates.put(stateI.getName(), stateI);
				}
				linkTags.put(link, true);
				stateElms.addAll(createStateElms(stateI, doc, nameStates, linkTags));
			}
		}
		return stateElms;
	}
	private static final class Link{
		private String from;
		private String to;
		private StateCondition condition;
		public Link(String from,String to, StateCondition condition) {
			this.from = from;
			this.to = to;
			this.condition = condition;
		}
		@Override
		public int hashCode() {
			int result = 17;
			result += 31*result + (from == null?0:from.hashCode());
			result += 31*result + (to == null?0:to.hashCode());
			result += 31*result + (condition == null?0:condition.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;
			if(!(obj instanceof Link))return false;
			Link other = (Link)obj;
			return eq(from,other.from) && eq(to,other.to) && eq(condition,other.condition);
		}
		private boolean eq(Object a,Object b){
			return a != null && a.equals(b);
		}
	}
}
