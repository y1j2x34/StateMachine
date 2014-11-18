/**
 * 
 */
package y1j2x34.state.script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <pre>
 * sth is sth
 * sth is not sth
 * sth like sth
 * sth not like sth
 * sth match sth
 * sth not match {property}
 * '' match {property}
 * and ...
 * and sth ...
 * or ....
 * or sth ...
 * </pre>
 * @author yangjianxin
 * @date 2014年11月18日
 */
public class Script {
	private Script(){}
	
	private static final Map<String,Method> methods;
	static{
		methods = new HashMap<String,Method>();
		Method[] ms = ValueScriptMatcher.class.getDeclaredMethods();
		for(Method m:ms){
			methods.put(m.getName().toLowerCase(Locale.ENGLISH), m);
		}
		ms = LogicScriptMatcher.class.getDeclaredMethods();
		for(Method m:ms){
			methods.put(m.getName().toLowerCase(Locale.ENGLISH), m);
		}
		methods.remove("reduce");
		methods.put("isnotlike", methods.get("notlike"));
		methods.put("are", methods.get("is"));
		methods.put("am", methods.get("is"));
		methods.put("==", methods.get("is"));
		methods.put("!=", methods.get("isnot"));
	}
	private static interface Unit{
		/**
		 * 在语句中位置
		 * @return
		 */
		int getIndex();
		String origin();
		ScriptMatcher execute(ScriptMatcher left,Unit right,Context context);
		Object getValue(Context context);
		boolean isVerb();
	}
	private static abstract class Base implements Unit{
		private int index;
		private String origin;
		protected Object value;
		public Base(String origin,Object value,int index) {
			this.origin = origin;
			this.index = index;
			this.value = value;
		}
		@Override
		public int getIndex() {
			return index;
		}
		@Override
		public String origin() {
			return origin;
		}
		@Override
		public ScriptMatcher execute(ScriptMatcher left, Unit right,
				Context context) {
			return null;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName()+"["+origin()+"]";
		}
		@Override
		public boolean isVerb() {
			return false;
		}
		@Override
		public Object getValue(Context context) {
			return value;
		}
	}
	private static class STRING extends Base implements Unit{
		public STRING(String origin,String value,int index) {
			super(origin,value,index);
		}
	}
	private static class NUMBER extends Base implements Unit{
		public NUMBER(String origin,Number value,int index) {
			super(origin,value,index);
		}
	}
	private static class PROPERTY extends Base{
		private String name;
		public PROPERTY(String origin,String name,int index) {
			super(origin,null,index);
			this.name = name;
		}
		@Override
		public Object getValue(Context context) {
			return context==null?null:context.get(name);
		}
	}
	private static class BOOLEAN extends Base{
		public BOOLEAN(String origin, String value, int index) {
			super(origin, Boolean.valueOf(value), index);
		}
	}
	private static class ELEMENT extends Base{
		public ELEMENT(String origin, Element value, int index) {
			super(origin, value, index);
		}
	}
	private static class Verb extends Base{
		public Verb(String origin,String name,int index) {
			super(origin,name,index);
		}
		@Override
		public ScriptMatcher execute(ScriptMatcher left, Unit right,
				Context context) {
			String name = (String)getValue(context);
			Method m = methods.get(name);
			if(m == null){
				throw new RuntimeException(name);
			}
			try{
				if(right != null){
					return (ScriptMatcher) m.invoke(left, right.getValue(context));
				}else{
					return (ScriptMatcher) m.invoke(left);
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		@Override
		public boolean isVerb() {
			return true;
		}
	}
	private static class TEMP{
		private List<Unit> units = new ArrayList<Unit>();
		private LinkedList<Verb> verbs = new LinkedList<Verb>();
		
		void addUnit(Unit unit){
			if(unit.isVerb()){
				if("that".equals(unit.origin())){
					end();
					units.add(units.get(units.size() - 1));//炸弹
				}else{
					verbs.add((Verb)unit);
				}
			}else{
				if(!units.isEmpty() && verbs.isEmpty()){
					throw new SyntaxException("at " + unit.getIndex());
				}
				end();
				units.add(unit);
			}
		}
		void end(){
			StringBuilder methodName = new StringBuilder();
			StringBuilder origin = new StringBuilder();
			for(Verb v:verbs){
				methodName.append(v.origin());
				origin.append(v.origin());
				origin.append(' ');
			}
			if(origin.length() > 0 && methodName.length() > 0){
				origin.deleteCharAt(origin.length() - 1);
				if(!methods.containsKey(methodName.toString())){
					throw new SyntaxException("Unexpected token: "+origin+", at "+verbs.getFirst().getIndex());
				}else{
					units.add(new Verb(origin.toString(), methodName.toString(), verbs.getFirst().getIndex()));
					verbs.clear();
				}
			}
		}
	}
	private final List<Unit> units = new ArrayList<Unit>();
	/**
	 * 
	 * @param script
	 * @param context
	 * @throws SyntaxException
	 * @return
	 */
	public static Script compile(String script){
		TEMP tmp = new TEMP();
		
		if(script == null){
			throw new SyntaxException("script == null.");
		}
		script = new StringBuilder(script.length()+2).append(' ').append(script).append(' ').toString();
		if(script.length() < 1){
			throw new SyntaxException("empty script.");
		}
		Script result = new Script();
		int len = script.length();
		
		for(int i=0;i<len;i++){
			char ch = script.charAt(i);
			if(ch == '\''){//字符串
				int idx = script.indexOf('\'',i+1);
				if(idx == -1){
					throw new SyntaxException("EOF while scanning string literal,at "+(i-1));
				}else{
					tmp.addUnit(new STRING(script.substring(i, idx+1),script.substring(i+1,idx), i-1));
					i = idx;
				}
			}else if(ch == '{'){//参数
				int idx =script.indexOf('}',i+1);
				if(idx == -1){
					throw new SyntaxException("EOF while scanning property literal,at "+(i-1));
				}else{
					tmp.addUnit(new PROPERTY(script.substring(i,idx+1),script.substring(i+1, idx), i-1));
					i = idx;
				}
			}else if(Character.isSpaceChar(ch)){//空格
				int j=i+1;
				while(j < len && Character.isSpaceChar(script.charAt(j))){
					j++;
				}
				i = j-1;
			}else{
				int j = i;
				while(j < len && !Character.isSpaceChar(script.charAt(j))){
					j++;
				}
				String origin = script.substring(i, j);
				i = j-1;
				if("true".equals(origin) || "false".equals(origin)){
					tmp.addUnit(new BOOLEAN(origin,origin,j-1));
				}else{
					try{
						Element e = Element.valueOf(origin.toUpperCase(Locale.ENGLISH));
						tmp.addUnit(new ELEMENT(origin, e,j - 1));
					}catch(IllegalArgumentException e){
						Number n = parseNumber(origin);
						if(n != null){
							tmp.addUnit(new NUMBER(origin, n, j-1));
						}else{
							tmp.addUnit(new Verb(origin, origin, j-1));
						}
					}
				}
			}
		}
		tmp.end();
		result.units.addAll(tmp.units);
		return result;
	}
	private static Number parseNumber(String n){
		n = n.trim();
		int len = n.length();
		boolean isDecimail = false;
		double result = 0;
		for(int i=len-1,k=1;i>-1;i--){
			char ch = n.charAt(i);
			if(!isDecimail && ch == '.'){
				isDecimail = true;
				result = result / k;
				k = 1;
			}else if(Character.isDigit(ch)){
				result = (ch-'0') * k + result;
				k*=10;
			}else{
				return null;
			}
		}
		if(isDecimail){
			if(result <= Float.MAX_VALUE && result >= Float.MIN_VALUE){
				return (float)result;
			}else{
				return result;
			}
		}else{
			if(result <= Integer.MAX_VALUE && result >= Integer.MIN_VALUE){
				return (int)result;
			}else{
				return (long)result;
			}
		}
	}
	
	public boolean execute(Context context){
		int size = units.size();
		if(size > 0){
			ScriptMatcher left = new ValueScriptMatcherImpl(units.get(0).getValue(context));
			for(int i=1;i<size;i++){
				Unit unit = units.get(i);
				if(unit.isVerb()){
					Method m = methods.get(unit.getValue(context));
					Class<?>[] ptypes = m.getParameterTypes();
					if(ptypes != null && ptypes.length == 1){
						i++;
						left = (ScriptMatcher)unit.execute(left, units.get(i),context);
					}else{
						left = (ScriptMatcher)unit.execute(left, null,context);
					}
				}
			}
			if(left instanceof LogicScriptMatcher){
				return ((LogicScriptMatcher) left).reduce();
			}
		}else{
			throw new RuntimeException("empty script.");
		}
		return false;
	}
	/**
	 * @return the units
	 */
	public List<Unit> getUnits() {
		return units;
	}
}
