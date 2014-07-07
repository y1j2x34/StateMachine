package y1j2x34.state.script;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Context extends HashMap<String,Object>{
	private static final long serialVersionUID = 1L;
	public Context() {}
	public Context(int capcity) {
		super(capcity);
	}
	public Context(String key,Object value){
		put(key, value);
	}
	public Context(Map<String,Object> map){
		super(map);
	}
	public Context(Object pojo){
		putAll(pojo);
	}
	public void putAll(Object pojo){
		if(pojo == null){
			return;
		}
		Method[] methods = pojo.getClass().getMethods();
		for(Method method:methods){
			String methodName = method.getName();
			if(methodName.startsWith("get") && method.getParameterTypes().length == 0 && method.getReturnType() == void.class){
				String paramName = methodName.substring(3);
				char[] chars = paramName.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				paramName = new String(chars);
				try{
					method.setAccessible(true);
					Object val = method.invoke(pojo,new Object[]{});
					put(paramName, val);
				}catch(Exception e){}
			}
		}
	}
}
