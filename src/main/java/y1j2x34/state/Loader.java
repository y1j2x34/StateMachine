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
	private static final String ELM_NAME_MACHINE = "machine";
	private static final String ELM_NAME_IMPORT = "import";
	private static final String ELM_NAME_INIT = "init";
	private static final String ELM_NAME_DEF = "def";
	private static final String ELM_NAME_LISTENER = "listener";
	private static final String ELM_NAME_CUR = "cur";
	private static final String ELM_NAME_STATE = "state";
	private static final String ELM_NAME_LINK = "link";
	private static final String ELM_NAME_CONF = "conf";
	private static final String ELM_NAME_ACTION = "action";
	private static final String ELM_NAME_PROPERTY = "property";
	private static final String ATTR_NAME_FROM = "from";
	private static final String ATTR_NAME_TO = "to";
	private static final String ATTR_NAME_VAL = "val";
	private static final String ATTR_NAME_NAME = "name";
	private static final String ATTR_NAME_CLASS = "class";
	private static final String ATTR_NAME_GUARDS = "guards";
	private static final String ATTR_NAME_REF = "ref";
	private static final String ATTR_NAME_ACTION = "action";
	private static final String ATTR_NAME_VALUE = "value";
	
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
		Element machineElement = (Element) doc.getElementsByTagName(ELM_NAME_MACHINE)
				.item(0);
		importStateMachine(machineElement);
	}

	private void importStateMachine(Element machineElement)
			throws InvalidFormatException {

		Map<String, State<Action>> nameStates = new HashMap<String, State<Action>>();
		NodeList importElements = machineElement.getElementsByTagName(ELM_NAME_IMPORT);
		Element initElement = (Element) machineElement.getElementsByTagName(
				ELM_NAME_INIT).item(0);
		Element defElement = (Element) machineElement.getElementsByTagName(
				ELM_NAME_DEF).item(0);
		Element curElement = (Element) machineElement.getElementsByTagName(
				ELM_NAME_CUR).item(0);
		Element listenerElement = (Element) machineElement
				.getElementsByTagName(ELM_NAME_LISTENER).item(0);
		
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
			NodeList stateElements = defElement.getElementsByTagName(ELM_NAME_STATE);
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
		NodeList confElements = machineElement.getElementsByTagName(ELM_NAME_CONF);
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
			if (listenerElement.hasAttribute(ATTR_NAME_CLASS)) {
				String class_ = listenerElement.getAttribute(ATTR_NAME_CLASS);
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
						if (ELM_NAME_STATE.equals(item.getNodeValue())) {
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
			NodeList states = initElement.getElementsByTagName(ELM_NAME_STATE);
			switch (states.getLength()) {
			case 0:
				throw new InvalidFormatException("not initial state");
			case 1:
				Element stateElement = (Element) states.item(0);
				Action action = importAction(stateElement);
				State<Action> state = machine.newState(action,
						stateElement.hasAttribute(ATTR_NAME_NAME)?stateElement.getAttribute(ATTR_NAME_NAME):"initialize");
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
		String confFrom = confElement.getAttribute(ATTR_NAME_FROM);
		String confTo = confElement.getAttribute(ATTR_NAME_TO);
		String confVal = confElement.getAttribute(ATTR_NAME_VAL);
		String confCondCls = confElement.getAttribute(ATTR_NAME_GUARDS);
		confCondCls = StrUtil.isEmpty(confCondCls) ? DefaultTransitionCondition.class.getName() : confCondCls;
		
		NodeList children = confElement.getElementsByTagName(ELM_NAME_LINK);
		int len = children.getLength();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				Node item = children.item(i);
				Element linkElement = (Element) item;
				String from = checkLostAttr(linkElement, ATTR_NAME_FROM, confFrom);
				String to = checkLostAttr(linkElement, ATTR_NAME_TO, confTo);
				String val = checkLostAttr(linkElement, ATTR_NAME_VAL, confVal);
				String condCls = checkLostAttr(linkElement,ATTR_NAME_GUARDS,confCondCls);
				
				State<Action> sFrom = nameStates.get(from);
				
				if (sFrom == null) {
					throw new RuntimeException("from state not found :"
							+ from);
				}
				State<Action> sTo = nameStates.get(to);
				if (sTo == null) {
					throw new RuntimeException("to state not found :" + to);
				}
				TransitionCondition condition = newConditionInstance(condCls, val);
				importProperty(condition, linkElement);
				sFrom.link(condition, sTo);
			}
		}
	}

	private void importStateLink(State<Action> pState, Element stateElement,
			Map<String, State<Action>> nameStates)
			throws InvalidFormatException {
		NodeList children = stateElement.getElementsByTagName(ELM_NAME_LINK);
		int len = children.getLength();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				Node item = children.item(i);
//				if (ELM_NAME_LINK.equals(item.getNodeName())) {
					Element condElement = (Element) item;
					checkLostAttr(condElement, ATTR_NAME_VAL, null);
					String conditionClass = checkLostAttr(condElement,
							ATTR_NAME_GUARDS, DefaultTransitionCondition.class.getName());
					String to = condElement.getAttribute(ATTR_NAME_TO);
					String val = condElement.getAttribute(ATTR_NAME_VAL);

					State<Action> sTo = nameStates.get(to);
					if (sTo == null) {
						throw new RuntimeException("to state not found!");
					}
					TransitionCondition cond = newConditionInstance(conditionClass, val);
					importProperty(cond, (Element)item);
					pState.link(cond, sTo);
//				}
			}
		}
	}
	private void importProperty(Object target,Element owner){
		NodeList propsElement = owner.getElementsByTagName(ELM_NAME_PROPERTY);
		int len = propsElement.getLength();
		for(int i=0;i<len;i++){
			Element propElement = (Element)propsElement.item(i);
			String name = propElement.getAttribute(ATTR_NAME_NAME);
			Object value = propElement.getAttribute(ATTR_NAME_VALUE);
			String ref = propElement.getAttribute(ATTR_NAME_REF);
			if(StrUtil.isEmpty(name)){
				continue;
			}
			if(StrUtil.isNotEmpty(ref)){
				value = context.get(ref);
			}else if(StrUtil.isEmpty((String)value)){
				value = context.get(name);
			}
			if(!ReflectUtils.set(target, name, value)){
				System.out.printf(">>参数设置失败！ 参数名称:'%s'，参数值:%s\n",name,String.valueOf(value));
			}
		}
	}
	private State<Action> importState(Element stateElement,
			Map<String, State<Action>> nameStates)
			throws InvalidFormatException {

		if (!stateElement.hasAttribute(ATTR_NAME_NAME)) {
			throw new InvalidFormatException("state name not defined!");
		}

		String name = stateElement.getAttribute(ATTR_NAME_NAME);

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
		NodeList actionChildren = stateElement.getElementsByTagName(ELM_NAME_ACTION);
		Element actionElement = null;
		if(actionChildren.getLength() == 1){
			actionElement = (Element) actionChildren.item(0);
			actionClass = actionElement.getAttribute(ATTR_NAME_CLASS);
		}else{
			actionClass = stateElement.getAttribute(ATTR_NAME_ACTION);
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

	private TransitionCondition newConditionInstance(String cls, String val) {
		TransitionCondition conditionObj = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends TransitionCondition> condCls = (Class<? extends TransitionCondition>) ReflectUtils
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