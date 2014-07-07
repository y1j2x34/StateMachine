package y1j2x34.state;

import y1j2x34.state.script.Context;

public class ScriptCondition extends DefaultCondition{
	private static final long serialVersionUID = 1L;
	
	/**
	 * <pre>
	 * key worlds: or,and,is,not,like,match,null
	 * {propertyName} {@code --> 返回上下文属性值}
	 * syntax: {@code {param} is null and {param0} like ''}
	 * </pre>
	 * @param script
	 */
	public ScriptCondition(String script,Context context) {
		super(script);
		this.contetxt = context;
	}
	
	@Override
	public boolean test(Object o) {
		Context ctx = new Context();
		ctx.putAll(o);
		ctx.putAll(contetxt);
		
		return super.test(o);
	}
	private Context contetxt = new Context();
	
	public void setContetxt(Context contetxt) {
		this.contetxt = contetxt;
	}
	public Context getContetxt() {
		return contetxt;
	}
}
