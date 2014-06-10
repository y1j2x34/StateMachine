package y1j2x34.state;

public class RegexCondition extends DefaultCondition{

	private static final long serialVersionUID = 1L;
	public RegexCondition(Object value) {
		super(value);
	}
	
	@Override
	public boolean is(Object o) {
		if(o == null) return false;
		String regex = String.valueOf(getValue());
		String other = String.valueOf(o);
		return other.matches(regex);
	}
}
