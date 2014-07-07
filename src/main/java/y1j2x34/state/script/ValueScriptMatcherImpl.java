package y1j2x34.state.script;
class ValueScriptMatcherImpl implements ValueScriptMatcher{
	private final Object value;
	private final Boolean leftValue;
	private final int opr;
	public ValueScriptMatcherImpl(Object value) {
		this(value,null,LogicScriptMatcher.OPR_NONE);
	}
	public ValueScriptMatcherImpl(Object value,Boolean leftValue,int opr) {
		this.value = value;
		this.leftValue = leftValue;
		this.opr = opr;
	}
	private boolean transform(boolean b){
		boolean left = leftValue == null?b:leftValue.booleanValue();
		switch (opr) {
		case LogicScriptMatcher.OPR_OR:
			return left || b;
		case LogicScriptMatcher.OPR_AND:
			return left && b;
		default:
			return b;
		}
	}
	private boolean equals0(Object value){
		return value != null && value.equals(value);
	}
	private boolean like0(String code){
		return match0(code.replaceAll("_", ".").replaceAll("\\%", ".*").replaceAll("\\[\\!", "[^"));
	}
	private boolean checkStr(String str){
		return value == null || String.class != value.getClass() || str == null || str.isEmpty();
	}
	private boolean match0(String regex){
		return checkStr(regex)?false:String.valueOf(value).matches(regex);
	}
	@Override
	public LogicScriptMatcher is(Object value) {
		return new LogicScriptMatcherImpl(transform(equals0(value)),value);
	}
	@Override
	public LogicScriptMatcher isNumeric() {
		return new LogicScriptMatcherImpl(transform(match0("\\d+(\\.\\d+)?")), leftValue);
	}
	@Override
	public LogicScriptMatcher isNotNumeric() {
		return new LogicScriptMatcherImpl(transform(!match0("\\d+(\\.\\d+)?")), leftValue);
	}
	@Override
	public LogicScriptMatcher isNot(Object value) {
		return new LogicScriptMatcherImpl(transform(!equals0(value)),value);
	}
	@Override
	public LogicScriptMatcher like(String code) {
		return new LogicScriptMatcherImpl(transform(like0(code)),value);
	}
	@Override
	public LogicScriptMatcher notLike(String code) {
		return new LogicScriptMatcherImpl(transform(!like0(code)),value);
	}
	@Override
	public LogicScriptMatcher match(String regex) {
		return new LogicScriptMatcherImpl(transform(match0(regex)),value);
	}
	@Override
	public LogicScriptMatcher notMatch(String regex) {
		return new LogicScriptMatcherImpl(transform(!match0(regex)),value);
	}
	@Override
	public LogicScriptMatcher isNull() {
		return new LogicScriptMatcherImpl(transform(value == null),value);
	}
	@Override
	public LogicScriptMatcher isNotNull() {
		return new LogicScriptMatcherImpl(transform(value != null),value);
	}
}