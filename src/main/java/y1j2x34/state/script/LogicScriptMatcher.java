package y1j2x34.state.script;
public interface LogicScriptMatcher extends ScriptMatcher{
	int OPR_NONE = -1;
	int OPR_AND = 0;
	int OPR_OR = 1;
	ValueScriptMatcher and(Object value);
	ValueScriptMatcher or(Object value);
	ValueScriptMatcher andThat();
	ValueScriptMatcher orThat();
	boolean reduce();
}