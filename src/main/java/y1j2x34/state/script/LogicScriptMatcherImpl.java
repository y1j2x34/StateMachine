package y1j2x34.state.script;

class LogicScriptMatcherImpl implements LogicScriptMatcher {
	private boolean bool;
	private Object leftValue;

	public LogicScriptMatcherImpl(boolean bool, Object leftValue) {
		this.bool = bool;
		this.leftValue = leftValue;
	}

	@Override
	public ValueScriptMatcher and(Object value) {
		return new ValueScriptMatcherImpl(value, this.bool, OPR_AND);
	}

	@Override
	public ValueScriptMatcher or(Object value) {
		return new ValueScriptMatcherImpl(value, this.bool, OPR_OR);
	}

	@Override
	public ValueScriptMatcher andThat() {
		return and(leftValue);
	}

	@Override
	public ValueScriptMatcher orThat() {
		return or(leftValue);
	}

	@Override
	public boolean reduce() {
		return bool;
	}
	@Override
	public LogicScriptMatcher not() {
		return new LogicScriptMatcherImpl(!this.bool,leftValue);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return reduce()+"";
	}
}