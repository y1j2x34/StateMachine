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
	 * @param guard
	 * @param state
	 * @return self
	 */
	public State<Action> link(Object guard,State<Action> state){
		return this.link(new DefaultTransitionCondition(guard), state);
	}
	public State<Action> link(TransitionCondition guard,State<Action> state){
		links.add(new LinkData(guard, state));
		return this;
	}
	/**
	 * 与自身建立条件连接
	 * @param guard
	 * @return this
	 */
	public final State<Action> round(Object guard){
		return link(guard,this);
	}
	final State<Action> transition(Object guard){
		StateListener<Action> listener = mMachine.getStateListener();
		State<Action> st = findNext(guard);
		if(st != null && listener != null){
			listener.onBeforeState(mMachine, st, this, guard);
		}
		return st;
	}
	private State<Action> findNext(Object guard){
		for(LinkData link:links){
			if(link.cond.accept(guard)){
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
		private TransitionCondition cond;
		private State<Action> state;
		public LinkData(TransitionCondition cond,State<Action> state) {
			this.cond = cond;
			this.state = state;
		}
		public State<Action> getState() {
			return state;
		}
		public TransitionCondition getGuard() {
			return cond;
		}
	}
}
