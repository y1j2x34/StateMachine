package y1j2x34.state;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 被标记的方法
 * 或
 * 标记到类和接口时设定的name数组里的方法
 * 的调用会触发状态机事件
 * 这里的事件为：
 * 触发前事件：{@linkplain StateListener#onBeforeEvent(StateMachine, State)} 
 * 触发后事件：{@linkplain StateListener#onAfterEvent(StateMachine, State)}
 * </pre>
 * @author y1j2x34
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD,ElementType.TYPE})
public @interface ActionMethod {
	String[] name() default {};
}
