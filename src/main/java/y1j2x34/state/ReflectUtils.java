package y1j2x34.state;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ReflectUtils {
	private ReflectUtils(){}
	private static final Map<String,Class<?>> PRIMTYPEMAPPING = new HashMap<String,Class<?>>();
	static{
		PRIMTYPEMAPPING.put("int", int.class);
		PRIMTYPEMAPPING.put("string", String.class);
		PRIMTYPEMAPPING.put("char", char.class);
		PRIMTYPEMAPPING.put("byte", byte.class);
		PRIMTYPEMAPPING.put("short", short.class);
		PRIMTYPEMAPPING.put("float", float.class);
		PRIMTYPEMAPPING.put("long", long.class);
		PRIMTYPEMAPPING.put("double", double.class);
		PRIMTYPEMAPPING.put("boolean", boolean.class);
		PRIMTYPEMAPPING.put("void", void.class);
		PRIMTYPEMAPPING.put("class", Class.class);
	}
	
	public static <T> T newInstance(Class<? extends T> clazz,Class<?>[] parameterTypes,Object[] params){
		try{
			Constructor<? extends T> c = clazz.getConstructor(parameterTypes);
			c.setAccessible(true);
			return c.newInstance(params);
		}catch(Throwable t){}
		return null;
	}
	public static <T> T newInstance(Class<? extends T> clazz){
		try{
			Constructor<? extends T> c = clazz.getConstructor();
			c.setAccessible(true);
			return c.newInstance();
		}catch(Throwable t){}
		return null;
	}
	public static Object newInstance(String clazz){
		try{
			return newInstance(Class.forName(clazz));
		}catch(Throwable t){}
		return null;
	}
	public static boolean set(Object obj,String fieldName,Object val){
		if(obj == null) return false;
		Class<?> clazz = obj.getClass();
		try{
			Field field = findField(clazz, fieldName);
			if(field != null){
				field.setAccessible(true);
				field.set(obj, val);
				return true;
			}else{
				if(val == null) return false;
				String setName = setName(fieldName);
				Method setMethod = findMethod(clazz, setName, new Class[]{val.getClass()});
				setMethod.setAccessible(true);
				setMethod.invoke(obj, val);
				return true;
			}
		}catch(Exception e){}
		return false;
	}
	public static String setName(String fieldName){
		if(StrUtil.isEmpty(fieldName)) return null;
		StringBuilder sb = new StringBuilder(fieldName);
		sb.setCharAt(0, Character.toUpperCase(fieldName.charAt(0)));
		sb.insert(0, "set");
		return sb.toString();
	}
	public static Method findMethod(Class<?> clazz,String methodName,Class<?>[] paramsType){
		Class<?> tmp = clazz;
		Method method = null;
		try{
			do{
				method = tmp.getDeclaredMethod(methodName);
				tmp = tmp.getSuperclass();
			}while(method == null && tmp != null);
		}catch(Exception e){}
		return method;
	}
	public static Field findField(Class<?> clazz,String fieldName){
		Class<?> tmp = clazz;
		Field field = null;
		try{
			do{
				field = tmp.getDeclaredField(fieldName);
				tmp = tmp.getSuperclass();
			}while(field == null && tmp != null);
		}catch(Exception e){}
		return field;
	}
	public static Class<?> findClass(Collection<String> imports,String simpleName){
		if(StrUtil.isEmpty(simpleName)){
			return Object.class;
		}
		if(simpleName.length() < 8 && simpleName.length() > 2){
			Class<?> ft = PRIMTYPEMAPPING.get(simpleName);
			if(ft != null){
				return ft;
			}
		}
		URLClassLoader uloader = new URLClassLoader(new URL[]{});
		Class<?> type = null;
		if(!( imports == null || imports.isEmpty())){
			StringBuilder cname = new StringBuilder();
			Iterator<String> it = imports.iterator();
			while(type == null && it.hasNext()){
				String imp=it.next();
				if(imp.endsWith("*")){
					cname.append(imp.substring(0, imp.length()-1)).append(simpleName);
				}else if(imp.endsWith(simpleName)){
					cname.setLength(0);
					cname.append(imp);
				}else{
					cname.append(imp).append('.').append(simpleName);
				}
				try {
					type = uloader.loadClass(cname.toString());
				} catch (ClassNotFoundException e) {}
				catch (NoClassDefFoundError e) {}
				cname.setLength(0);
			}
		}
		if(type == null){
			try {
				type = uloader.loadClass(simpleName);
			} catch (ClassNotFoundException e) {}
		}
		if(type == null){
			try {
				type = Class.forName(simpleName);
			} catch (ClassNotFoundException e) {}
		}
		return type;
	}
}
