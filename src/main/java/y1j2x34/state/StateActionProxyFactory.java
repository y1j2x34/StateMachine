package y1j2x34.state;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

final class StateActionProxyFactory{
	private StateActionProxyFactory(){}
	
	private static class StateInvocationHandler<Action> implements InvocationHandler,Serializable{
		private static final long serialVersionUID = 1L;
		private Action target;
		private StateMachine<Action> machine;
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			ActionMethod method_ann = method.getAnnotation(ActionMethod.class);
			boolean ct = true;
			check:if(method_ann == null){
				try{
					ActionMethod am = target.getClass().getAnnotation(ActionMethod.class);
					String[] names = am.name();
					if(names == null || names.length == 0){
						ct = false;
						break check;
					}
					ct = false;
					String methodName = method.getName();
					for(String name:am.name()){
						if(methodName.equals(name)){
							ct = true;
							break check;
						}
					}
				}catch(NullPointerException e){
					return method.invoke(target, args);
				}
			}
			if(!ct){
				return method.invoke(target, args);
			}
			StateListener<Action> listener = null;
			State<Action> current = machine.current();
			listener = machine.getStateListener();
			//事件发生前
			if(listener != null){
				listener.onBeforeEvent(machine, current);
			}
			method.setAccessible(true);
			Object result = method.invoke(target, args);
			//事件发生后
			listener = machine.getStateListener();
			if(listener != null){
				listener.onAfterEvent(machine, current,result);
			}
			return result;
		}
	}
	private static final Class<?>[] getAllInterfaces(Class<?> cls){
		Collection<Class<?>> interfaces = new HashSet<Class<?>>();
		Class<?> c = cls;
		while(c != null && c != Object.class ){
			interfaces.addAll(Arrays.asList(c.getInterfaces()));
			c = c.getSuperclass();
		}
		return interfaces.toArray(new Class<?>[interfaces.size()]);
	}
	@SuppressWarnings("unchecked")
	static <TargetAction> TargetAction createActionProxy(TargetAction action,StateMachine<TargetAction> stateMachine){
		StateInvocationHandler<TargetAction> sih = new StateInvocationHandler<TargetAction>();
		sih.target = action;
		sih.machine = stateMachine;
		Object proxy = Proxy.newProxyInstance(action.getClass().getClassLoader(), getAllInterfaces(action.getClass()), sih);
		return (TargetAction)proxy;
	}
}
