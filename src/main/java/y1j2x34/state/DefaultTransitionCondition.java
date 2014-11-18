package y1j2x34.state;

public class DefaultTransitionCondition implements TransitionCondition{
	
	private static final long serialVersionUID = 1L;
	
	private Object value;
	public DefaultTransitionCondition(Object value){
		this.value = value;
	}
	
	public boolean accept(Object o){
		return o==value || (value != null && value.equals(o));
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
