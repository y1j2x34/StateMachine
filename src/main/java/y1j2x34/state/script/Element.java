/**
 * 
 */
package y1j2x34.state.script;

import java.util.Date;

/**
 * @author yangjianxin
 * @date 2014年11月18日
 */
public enum Element {
	NUMERIC{
		@Override
		boolean check(Object value) {
			return value instanceof Number;
		}
	},NOTNULL{
		@Override
		boolean check(Object value) {
			return value != null;
		}
	},NULL{
		@Override
		boolean check(Object value) {
			return value == null;
		}
	},BOOLEAN{
		@Override
		boolean check(Object value) {
			return value != null && value instanceof Boolean || value.getClass() == boolean.class;
		}
	},INTEGER{
		@Override
		boolean check(Object value) {
			return value != null && value instanceof Integer || value.getClass() == int.class;
		}
	},INT{
		@Override
		boolean check(Object value) {
			return INTEGER.check(value);
		}
	},DECIMAIL{
		@Override
		boolean check(Object value) {
			return value != null 
					&& value instanceof Float || value.getClass() == float.class
					|| value instanceof Double || value.getClass() == double.class;
		}
	},DATE{
		@Override
		boolean check(Object value) {
			if(value == null) return false;
			return value instanceof Date;
		}
	};
	abstract boolean check(Object value);
}
