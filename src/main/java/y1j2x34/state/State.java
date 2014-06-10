package y1j2x34.state;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author y1j2x34
 *
 * @param <Action>
 */
public class State<Action> implements Serializable{
	private static final long serialVersionUID = 3558287642143165251L;
	
//	private final StateManager<Action> mMgr;
	
	private final Action mAction;
	private StateMachine<Action> mMachine; 
	private final String mName;
	private final Action mCallback;
	
	private List<LinkData> links = new LinkedList<LinkData>();
	
	State(Action callback,StateMachine<Action> machine,String name) {
		mMachine = machine;
		mName = name;
		mCallback = callback;
		mAction = StateActionProxyFactory.createActionProxy(callback, mMachine);
	}
	/**
	 * 与其他状态建立单向条件连接
	 * @param condition
	 * @param state
	 * @return self
	 */
	public State<Action> link(Object condition,State<Action> state){
		return this.link(new DefaultCondition(condition), state);
	}
	public State<Action> link(StateCondition condition,State<Action> state){
		links.add(new LinkData(condition, state));
		return this;
	}
	/**
	 * 与自身建立条件连接
	 * @param condition
	 * @return this
	 */
	public final State<Action> round(Object condition){
		return link(condition,this);
	}
	final State<Action> change(Object condition){
		StateListener<Action> listener = mMachine.getStateListener();
		State<Action> st = findNext(condition);
		if(st != null && listener != null){
			listener.onBeforeState(mMachine, st, this, condition);
		}
		return st;
	}
	private State<Action> findNext(Object condition){
		for(LinkData link:links){
			if(link.cond.is(condition)){
				return link.state;
			}
		}
		return null;
	}
	protected List<LinkData> getLinks(){
		return links;
	}
	public boolean can(State<Action> state){
		boolean can = false;
		for(LinkData link:links){
			if(link.state == state){
				can = true;
				break;
			}
		}
		return can;
	}
	/**
	 * @return
	 */
	public Action getAction() {
		return mAction;
	}
	/**
	 * @return
	 */
	public Action getCallback() {
		return mCallback;
	}
	public final String getName(){
		return mName;
	}
	@Override
	public String toString() {
		return mName;
	}
//	StateManager<Action> getCondMgr(){
//		return mMgr;
//	}
	class LinkData{
		private StateCondition cond;
		private State<Action> state;
		public LinkData(StateCondition cond,State<Action> state) {
			this.cond = cond;
			this.state = state;
		}
		public State<Action> getState() {
			return state;
		}
		public StateCondition getCondition() {
			return cond;
		}
	}
}
