package y1j2x34.state;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 有限状态机
 * @author y1j2x34
 *
 * @param <Action> 状态机事件
 */
public class StateMachine<Action> implements Serializable,StateListener<Action>{
	private static final long serialVersionUID = 516903296455759060L;
	
	//初始状态
	private State<Action> mInitial;
	
	private StateListener<Action> mListener;
	private State<Action> mCurrent;
	
	private final String STATE_NAME_PREFIX = "state_";
	
	private int mStateCount = 0;
	
	private State<Action> mPreview;
	
	public StateMachine(){
		this.setStateListener(this);
	}
	public void initialize(State<Action> initial){
		mInitial = mCurrent = initial;
	}
	/**
	 * 构建状态变化事件
	 * @param from		开始状态
	 * @param condition	use {@link DefaultCondition} 变化条件
	 * @param to		目标状态
	 */
	public StateMachine<Action> link(State<Action> from,Object condition,State<Action> to){
		from.link(condition, to);
		return this;
	}
	public StateMachine<Action> link(State<Action> from,StateCondition condition,State<Action> to){
		from.link(condition, to);
		return this;
	}
	
	/**
	 * 根据条件变化状态
	 * @param condition 条件
	 * @throws StateException 狀態不能變化時拋出該異常
	 * @return 新的状态
	 */
	public State<Action> change(Object condition) throws StateException{
		State<Action> old = mCurrent;
		State<Action> new_ = mCurrent.change(condition);
		if(new_ != null){
			mCurrent = new_;
			mPreview = old;
			//状态发生了变化，触发变化后的事件
			if(mListener != null){
				mListener.onAfterState(this, mCurrent, old, condition);
			}
		}else{
			//状态无法改变
			mListener.onErrorChange(this, old, condition);
			throw new StateException("can't change state by condition:"+condition);
		}
		return mCurrent;
	}
	public State<Action> gotoPreviewState(){
		State<Action> old = mCurrent;
		mCurrent = mPreview;
		mPreview = old;
		return mCurrent;
	}
	public State<Action> current(){
		return mCurrent;
	}
	public State<Action> reset(){
		return mCurrent = mInitial;
	}
	void set(State<Action> newState){
		this.mCurrent = newState;
	}
	State<Action> getInitialState(){
		return mInitial;
	}
	public boolean is(State<Action> state){
		return current() == state;
	}
	/**
	 * 事件是否能在当前触发
	 * @param state
	 * @return
	 */
	public boolean can(State<Action> state){
		return current().can(state);
	}
	public boolean cannot(State<Action> state){
		return !can(state);
	}
	
	public State<Action> newState(Action act){
		return newState(act, STATE_NAME_PREFIX+mStateCount);
	}
	public State<Action> newState(Action act,String name){
		mStateCount ++;
		return new State<Action>(act, this, name);
	}
	public void setStateListener(StateListener<Action> listener){
		mListener = listener;
	}
	public StateListener<Action> getStateListener(){
		return mListener;
	}
	@Override
	public void onBeforeEvent(StateMachine<Action> machine,State<Action> current) {}

	@Override
	public void onAfterEvent(StateMachine<Action> machine, State<Action> current) {}

	@Override
	public void onBeforeState(StateMachine<Action> machine,State<Action> ready, State<Action> current, Object condition) {}

	@Override
	public void onAfterState(StateMachine<Action> machine, State<Action> enter,State<Action> leave, Object condition) {}

	@Override
	public void onErrorChange(StateMachine<Action> machine,State<Action> current, Object condition) {}

	/**
	 * 
	 * @param xmlStream
	 * @throws IOException
	 * @throws InvalidFormatException 
	 * @throws NullPointerException if xmlStream == null
	 */
	@SuppressWarnings("unchecked")
	public void loadFromXml(InputStream xmlStream) throws IOException,InvalidFormatException{
		loadFromXml(xmlStream, Collections.EMPTY_MAP);
	}
	/**
	 * @param xmlStream
	 * @param context
	 * @throws IOException	
	 * @throws InvalidFormatException
	 * @throws NullException
	 */
	public void loadFromXml(InputStream xmlStream,Map<String,Object> context) throws IOException,InvalidFormatException{
		if(xmlStream == null){
			throw new NullPointerException();
		}
		StateMachineUtils.load(this, xmlStream,context);
	}
	
	public void storeToXml(OutputStream os) throws IOException{
		this.storeToXml(os,"UTF-8");
	}
	public void storeToXml(OutputStream os,String encoding) throws IOException{
		if(os == null){
			throw new NullPointerException();
		}
		StateMachineUtils.storeToXml(this, os, encoding);
	}
	/**
	 * 
	 * @param output
	 * @throws IOException
	 * @throws NullPointerException if output == null
	 */
	public void serializeTo(OutputStream output) throws IOException{
		if(output == null){
			throw new NullPointerException();
		}
		ObjectOutputStream oout = new ObjectOutputStream(output);
		oout.writeObject(this);
	}
}
