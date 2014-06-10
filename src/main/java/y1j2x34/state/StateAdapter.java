package y1j2x34.state;

public abstract class StateAdapter<Action> implements StateListener<Action> {
	private static final long serialVersionUID = 1L;

	@Override
	public void onBeforeEvent(StateMachine<Action> machine,
			State<Action> current) {
	}

	@Override
	public void onAfterEvent(StateMachine<Action> machine, State<Action> current,Object actionReturned) {

	}

	@Override
	public void onBeforeState(StateMachine<Action> machine,
			State<Action> ready, State<Action> current, Object condition) {

	}

	@Override
	public void onAfterState(StateMachine<Action> machine, State<Action> enter,
			State<Action> leave, Object condition) {

	}

	@Override
	public void onErrorChange(StateMachine<Action> machine,
			State<Action> current, Object condition) {

	}

}
