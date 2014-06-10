package y1j2x34.state;

import java.io.Serializable;

/**
 * 状态监听器
 * @author y1j2x34
 * @param <Action>
 * @param <Cond>
 */
public interface StateListener<Action> extends Serializable{
	/**
	 * 任一事件发生之前调用
	 * @param machine 状态机
	 * @param current 事件所属状态
	 */
	void onBeforeEvent(StateMachine<Action> machine,State<Action> current);
	/**
	 * 任一事件结束之后调用
	 * @param machine 状态机
	 * @param current 事件所属状态
	 */
	void onAfterEvent(StateMachine<Action> machine,State<Action> current);
	/**
	 * 进入任一状态时触发
	 * @param machine	状态机
	 * @param ready		准备进入的状态
	 * @param current	当前状态
	 * @param condition TODO
	 */
	void onBeforeState(StateMachine<Action> machine,State<Action> ready,State<Action> current, Object condition);
	/**
	 * 离开任一状态时触发
	 * @param machine	状态机
	 * @param enter		当前进入的状态
	 * @param leave		离开的状态（上一个状态）
	 * @param condition TODO
	 */
	void onAfterState(StateMachine<Action> machine,State<Action> enter,State<Action> leave, Object condition);
	/**
	 * 事件不可能发生时触发
	 * @param machine	状态机
	 * @param current	当前状态
	 * @param condition	触发条件
	 */
	void onErrorChange(StateMachine<Action> machine,State<Action> current,Object condition);
}
