package y1j2x34.state;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class Loader<Action> {
	private StateMachine<Action> machine;
	private List<String> imports = new LinkedList<String>();
	private Map<String,Object> context ;
	public Loader(StateMachine<Action> machine,Map<String,Object> context) {
		this.machine = machine;
		this.context = context;
	}

	void load(InputStream stream) throws IOException, InvalidFormatException {
		Document doc = null;
		try {
			doc = getLoadingDoc(stream);
		} catch (SAXException e) {
			throw new InvalidFormatException(e);
		}
		Element machineElement = (Element) doc.getElementsByTagName("machine")
				.item(0);
		importStateMachine(machineElement);
	}

	private void importStateMachine(Element machineElement)
			throws InvalidFormatException {

		Map<String, State<Action>> nameStates = new HashMap<String, State<Action>>();
		NodeList importElements = machineElement.getElementsByTagName("import");
		Element initElement = (Element) machineElement.getElementsByTagName(
				"init").item(0);
		Element defElement = (Element) machineElement.getElementsByTagName(
				"def").item(0);
		Element curElement = (Element) machineElement.getElementsByTagName(
				"cur").item(0);
		Element listenerElement = (Element) machineElement
				.getElementsByTagName("listener").item(0);
		
		//处理包的导入
		if(importElements.getLength() > 0){
			for(int i=0;i<importElements.getLength();i++){
				Node importNode = importElements.item(i);
				String val = importNode.getFirstChild().getNodeValue();
				imports.add(val);
			}
		}
		
		// 导入初始化状态
		State<Action> initalState = importInitState(initElement);
		nameStates.put(initalState.getName(), initalState);
		machine.initialize(initalState);

		// 导入状态定义
		if (defElement != null) {
			NodeList stateElements = defElement.getElementsByTagName("state");
			int stateCount = stateElements.getLength();
			if (stateCount > 0) {
				for (int i = 0; i < stateCount; i++) {
					Element stateElement = (Element) stateElements.item(i);
					State<Action> state = importState(stateElement, nameStates);
					nameStates.put(state.getName(), state);
				}
			}
		}
		// 导入状态关系定义
		NodeList confElements = machineElement.getElementsByTagName("conf");
		importConfs(confElements, nameStates);
		if (curElement != null) {
			Node fchild = curElement.getFirstChild();
			if (fchild instanceof CharacterData) {
				String cur = fchild.getNodeValue();
				State<Action> state = nameStates.get(cur);
				if (state != null) {
					machine.set(state);
				} else {
					throw new InvalidFormatException("state not found :" + cur);
				}
			} else {
				throw new InvalidFormatException("cur>" + fchild);
			}
		}
		if (listenerElement != null) {
			if (listenerElement.hasAttribute("class")) {
				String class_ = listenerElement.getAttribute("class");
				Object listener = null;
				if(imports.isEmpty()){
					
					listener = ReflectUtils.newInstance(class_);
				}else{
					listener = ReflectUtils.newInstance(ReflectUtils.findClass(imports, class_));
				}
				if (listener instanceof StateListener) {
					importProperty(listener, listenerElement);
					@SuppressWarnings("unchecked")
					StateListener<Action> sl = (StateListener<Action>) listener;
					machine.setStateListener(sl);
				}
			} else {
				/*
				 * Node fnode = listenerElement.getFirstChild(); if(fnode !=
				 * null){ String val = fnode.getNodeValue(); if(val != null &&
				 * val.length() > 0){ try { Object lstr =
				 * IOUtils.deserialize(val.getBytes("UTF-8")); if(lstr
				 * instanceof StateListener){
				 * 
				 * @SuppressWarnings("unchecked") StateListener<Action> ls =
				 * (StateListener<Action>)lstr; machine.setStateListener(ls); }
				 * } catch (Exception e){} } }
				 */
			}
		}
	}
	private void importConfs(NodeList confElements,Map<String, State<Action>> nameStates)
			throws InvalidFormatException {
		int confLength = confElements.getLength();
		if (confLength > 0) {
			for (int j = 0; j < confLength; j++) {
				Element confElement = (Element) confElements.item(j);
				NodeList children = confElement.getChildNodes();
				int len = children.getLength();
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						Node item = children.item(i);
						if ("state".equals(item.getNodeValue())) {
							State<Action> state = importState((Element) item,
									nameStates);
							nameStates.put(state.getName(), state);
						}
					}
					importConf(confElement, nameStates);
				}
			}
		}
	}

	private State<Action> importInitState(Element initElement) throws InvalidFormatException {
		if (initElement != null) {
			NodeList states = initElement.getElementsByTagName("state");
			switch (states.getLength()) {
			case 0:
				throw new InvalidFormatException("not initial state");
			case 1:
				Element stateElement = (Element) states.item(0);
				Action action = importAction(stateElement);
				State<Action> state = machine.newState(action,
						stateElement.hasAttribute("name")?stateElement.getAttribute("name"):"initialize");
				return state;
			default:
				throw new InvalidFormatException("too much initial state");
			}
		} else {
			throw new InvalidFormatException("no initial state");
		}
	}
	private void importConf(Element confElement,
			Map<String, State<Action>> nameStates)
			throws InvalidFormatException {
		String confFrom = confElement.getAttribute("from");
		String confTo = confElement.getAttribute("to");
		String confVal = confElement.getAttribute("val");
		String confCondCls = confElement.getAttribute("condition");
		confCondCls = StrUtil.isEmpty(confCondCls) ? DefaultCondition.class.getName() : confCondCls;
		
		NodeList children = confElement.getElementsByTagName("link");
		int len = children.getLength();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				Node item = children.item(i);
				Element linkElement = (Element) item;
				String from = checkLostAttr(linkElement, "from", confFrom);
				String to = checkLostAttr(linkElement, "to", confTo);
				String val = checkLostAttr(linkElement, "val", confVal);
				String condCls = checkLostAttr(linkElement,"condition",confCondCls);
				
				State<Action> sFrom = nameStates.get(from);
				
				if (sFrom == null) {
					throw new RuntimeException("from state not found :"
							+ from);
				}
				State<Action> sTo = nameStates.get(to);
				if (sTo == null) {
					throw new RuntimeException("to state not found :" + to);
				}
				StateCondition condition = newConditionInstance(condCls, val);
				importProperty(condition, linkElement);
				sFrom.link(condition, sTo);
			}
		}
	}

	private void importStateLink(State<Action> pState, Element stateElement,
			Map<String, State<Action>> nameStates)
			throws InvalidFormatException {
		NodeList children = stateElement.getElementsByTagName("link");
		int len = children.getLength();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				Node item = children.item(i);
//				if ("link".equals(item.getNodeName())) {
					Element condElement = (Element) item;
					checkLostAttr(condElement, "val", null);
					String conditionClass = checkLostAttr(condElement,
							"condition", DefaultCondition.class.getName());
					String to = condElement.getAttribute("to");
					String val = condElement.getAttribute("val");

					State<Action> sTo = nameStates.get(to);
					if (sTo == null) {
						throw new RuntimeException("to state not found!");
					}
					StateCondition cond = newConditionInstance(conditionClass, val);
					importProperty(cond, (Element)item);
					pState.link(cond, sTo);
//				}
			}
		}
	}
	private void importProperty(Object target,Element owner){
		NodeList propsElement = owner.getElementsByTagName("property");
		int len = propsElement.getLength();
		for(int i=0;i<len;i++){
			Element propElement = (Element)propsElement.item(i);
			String name = propElement.getAttribute("name");
			Object value = propElement.getAttribute("value");
			String ref = propElement.getAttribute("ref");
			if(StrUtil.isEmpty(name)){
				continue;
			}
			if(StrUtil.isNotEmpty(ref)){
				value = context.get(ref);
			}else if(StrUtil.isEmpty((String)value)){
				value = context.get(name);
			}
			if(!ReflectUtils.set(target, name, value)){
				System.out.printf(">>canot set property! name:%s value:%s\n",name,String.valueOf(value));
			}
		}
	}
	private State<Action> importState(Element stateElement,
			Map<String, State<Action>> nameStates)
			throws InvalidFormatException {

		if (!stateElement.hasAttribute("name")) {
			throw new InvalidFormatException("state name not defined!");
		}

		String name = stateElement.getAttribute("name");

		if (nameStates.containsKey(name)) {
			throw new InvalidFormatException("duplicate state name:" + name);
		}

		Action action = importAction(stateElement);
		
		final State<Action> state = machine.newState(action, name);
		importStateLink(state, stateElement, nameStates);
		return state;
	}
	private Action importAction(Element stateElement){
		String actionClass = null;
		Action action = null;
		NodeList actionChildren = stateElement.getElementsByTagName("action");
		Element actionElement = null;
		if(actionChildren.getLength() == 1){
			actionElement = (Element) actionChildren.item(0);
			actionClass = actionElement.getAttribute("class");
		}else{
			actionClass = stateElement.getAttribute("action");
		}
		try {
			@SuppressWarnings("unchecked")
			Action nAction = (Action) ReflectUtils.newInstance(ReflectUtils.findClass(imports, actionClass));
			if (nAction == null) {
				throw new Exception();
			}
			action = nAction;
		} catch (Exception e) {
			throw new RuntimeException("initial action failed! " + actionClass,e);
		}
		if(actionElement != null){
			importProperty(action, actionElement);
		}
		return action;
	}
	private Document getLoadingDoc(InputStream stream) throws SAXException,
			IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setValidating(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringComments(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new EH());
			InputSource is = new InputSource(stream);
			return db.parse(is);
		} catch (ParserConfigurationException x) {
			throw new Error(x);
		}
	}

	private String checkLostAttr(Element tag, String attrName, String defaultVal) {
		if (!tag.hasAttribute(attrName)) {
			if (defaultVal != null && defaultVal.length() > 0) {
				return defaultVal;
			}
			throw new IllegalArgumentException(String.format("%s.%s == %s",
					tag.getNodeName(), attrName, tag.getAttribute(attrName)));
		}
		return tag.getAttribute(attrName);
	}

	private StateCondition newConditionInstance(String cls, String val) {
		StateCondition conditionObj = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends StateCondition> condCls = (Class<? extends StateCondition>) ReflectUtils
					.findClass(imports, cls);
			conditionObj = ReflectUtils.newInstance(condCls,
					new Class[] { Object.class }, new Object[] { val });
		} catch (Exception e) {}
		
		if (conditionObj == null) {
			throw new RuntimeException("cannot initial condition :" + cls);
		}
		return conditionObj;
	}

	static class EH implements ErrorHandler {
		public void warning(SAXParseException exception) throws SAXException {
			throw exception;
		}

		public void error(SAXParseException exception) throws SAXException {
			throw exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}
	}
}