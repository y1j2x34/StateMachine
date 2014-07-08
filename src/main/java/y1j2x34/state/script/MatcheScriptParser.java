package y1j2x34.state.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcheScriptParser {
	private static ValueScriptMatcher $(Object value){
		return new ValueScriptMatcherImpl(value);
	}
	private static boolean isString(String str){
		return checkSE(str, 39,39);
	}
	private static boolean isProp(String str){
		return checkSE(str, 123,125);
	}
	private static boolean isSomeValue(String str){
		return isString(str) || isProp(str) || "null".equals(str);
	}
	private static boolean checkSE(String str,int sc,int ec){
		if(str == null || str.length() < 2){
			return false;
		}
		int len = str.length();
		return str.charAt(0)== sc && str.charAt(len-1) == ec;
	}
	private static Object getVal(String text,Context context){
		if(isString(text)){
			return text.substring(1, text.length()-1);
		}else if(isProp(text)){
			String key = text.substring(1, text.length()-1);
			return context.get(key);
		}else{
			return text;
		}
	}
	
	/**
	 * sth is sth
	 * sth is not sth
	 * sth like sth
	 * sth not like sth
	 * sth match sth
	 * sth not match sth
	 * and ...
	 * and sth ...
	 * or ....
	 * or sth ...
	 * @param script
	 * @return
	 * @throws SyntaxException
	 */
	public static boolean parse(String script,Context context){
		if(script == null){
			throw new SyntaxException("script == null!");
		}
		Pattern p = Pattern.compile("(\\'[^\\']*\\')|(\\{[^\\{\\}]+\\})|\\w+");
		Matcher m = p.matcher(script.trim());
		List<String> splited = new ArrayList<String>();
		
		while(m.find()){
			splited.add(m.group());
		}
		String start = splited.get(0);
		Object startVal = getVal(start, context);
		if(startVal == start){
			throw new SyntaxException(start);
		}
		ScriptMatcher $m = $(startVal);
		String prev = null;
		for(int i=1;i<splited.size();i++){
			String cur = splited.get(i);
			if($m instanceof ValueScriptMatcher){
				
			}else if($m instanceof LogicScriptMatcher){
				if(isSomeValue(cur)){
					
				}else{
					
				}
			}
			prev = cur;
		}
		return false;
	}
}
