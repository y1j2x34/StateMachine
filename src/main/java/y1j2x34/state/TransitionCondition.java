package y1j2x34.state;

import java.io.Serializable;
/**
 * 转换条件
 * @author y1j2x34
 */
public interface TransitionCondition extends Serializable{
	public boolean can(Object o);
	public Object getValue();
	public void setValue(Object value);
}
