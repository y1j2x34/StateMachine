package y1j2x34.state;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import y1j2x34.state.action.SportAction;
import static org.junit.Assert.*;

public class AppTest
{
	@Test()
    public void testApp()
    {
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("haha", "hello world");
    	map.put("danceName", "芭蕾舞");
    	StateMachine<SportAction> machine = new StateMachine<SportAction>();
    	try {
			machine.loadFromXml(getClass().getClassLoader().getResourceAsStream("machine.xml"),map);
		} catch (InvalidFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
    	assertEquals("dance", machine.current().getName());
    	machine.setStateListener(new StateAdapter<SportAction>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onBeforeEvent(StateMachine<SportAction> machine,
					State<SportAction> current) {
				assertEquals("dance", current.getName());
			}
			@Override
			public void onAfterEvent(StateMachine<SportAction> machine,
					State<SportAction> current, Object actionReturned) {
				assertNull(actionReturned);
			}
    	});
    	machine.current().getAction().sport();
    }
}
