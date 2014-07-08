package y1j2x34.state;

public class DefaultGuards implements Guards{
	
	private static final long serialVersionUID = 1L;
	
	private Object value;
	public DefaultGuards(Object value){
		this.value = value;
	}
	
	public boolean test(Object o){
		return o==value || (value != null && value.equals(o));
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
