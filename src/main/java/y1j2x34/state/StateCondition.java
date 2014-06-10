package y1j2x34.state;

import java.io.Serializable;

public interface StateCondition extends Serializable{
	public boolean is(Object o);
	public Object getValue();
	public void setValue(Object value);
}
