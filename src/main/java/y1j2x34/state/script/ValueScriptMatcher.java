package y1j2x34.state.script;

public interface ValueScriptMatcher extends ScriptMatcher{
	LogicScriptMatcher is(Object value);
	LogicScriptMatcher isNot(Object value);
	LogicScriptMatcher like(String code);
	LogicScriptMatcher notLike(String code);
	LogicScriptMatcher match(String regex);
	LogicScriptMatcher notMatch(String regex);
}