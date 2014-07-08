package y1j2x34.state;

public class RegexGuards extends DefaultGuards{

	private static final long serialVersionUID = 1L;
	public RegexGuards(Object value) {
		super(value);
	}
	
	@Override
	public boolean test(Object o) {
		if(o == null) return false;
		String regex = String.valueOf(getValue());
		String other = String.valueOf(o);
		return other.matches(regex);
	}
}
