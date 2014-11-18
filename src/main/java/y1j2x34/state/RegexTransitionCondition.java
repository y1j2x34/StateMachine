package y1j2x34.state;

public class RegexTransitionCondition extends DefaultTransitionCondition{

	private static final long serialVersionUID = 1L;
	public RegexTransitionCondition(Object value) {
		super(value);
	}
	
	@Override
	public boolean can(Object o) {
		if(o == null) return false;
		String regex = String.valueOf(getValue());
		String other = String.valueOf(o);
		return other.matches(regex);
	}
}
