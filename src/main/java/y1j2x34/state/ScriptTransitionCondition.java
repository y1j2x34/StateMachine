package y1j2x34.state;

import y1j2x34.state.script.Context;
import y1j2x34.state.script.Script;

public class ScriptTransitionCondition extends DefaultTransitionCondition{
	private static final long serialVersionUID = 1L;
	private Script script;
	/**
	 * <pre>
	 * keywords: or,and,is,not,like,match,null
	 * {propertyName}  --> 上下文属性值
	 * 语法: <code>{param} is null and {param0} like ''</code>
	 * </pre>
	 * @param script
	 */
	public ScriptTransitionCondition(String script,Context context) {
		super(script);
		this.context = context;
	}
	
	@Override
	public boolean can(Object o) {
		Context ctx = new Context();
		ctx.putAll(o);
		ctx.putAll(context);
		if(script == null){
			script = Script.compile((String)getValue());
		}
		return script.execute(ctx);
	}
	private Context context = new Context();
	
	public void setContetxt(Context contetxt) {
		this.context = contetxt;
	}
	public Context getContetxt() {
		return context;
	}
}
