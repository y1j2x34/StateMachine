package y1j2x34.state;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 用于标记能够被代理的事件方法或类，
 * 触发前事件：{@linkplain StateListener#onBeforeEvent(StateMachine, State)} 
 * 触发后事件：{@linkplain StateListener#onAfterEvent(StateMachine, State)}
 * </pre>
 * @author y1j2x34
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD,ElementType.TYPE})
@Inherited
public @interface ActionMethod {
	String[] name() default {};
}
