package y1j2x34.state.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcheScriptParser {
	private static ValueScriptMatcher $(Object value){
		return new ValueScriptMatcherImpl(value);
	}
	enum Block{
		STRING,CONTEXTPROPERTY,NORMAL
	}
	/**
	 * @param script
	 * @return
	 * @throws SyntaxException
	 */
	public static boolean parse(String script,Context context){
		if(script == null){
			throw new SyntaxException("script == null!");
		}
		Pattern p = Pattern.compile("[^\\s\\w]|\\w+");
		Matcher m = p.matcher(script);
		List<String> splited = new ArrayList<String>();
		while(m.find()){
			splited.add(m.group());
		}
		ValueScriptMatcher $m = null;
		
		return false;
	}
}
