package y1j2x34.state;

import java.io.Serializable;
/**
 * 检测器
 * @author y1j2x34
 */
public interface Guards extends Serializable{
	public boolean test(Object o);
	public Object getValue();
	public void setValue(Object value);
}
