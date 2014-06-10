package y1j2x34.state.action;

public class DanceSportAction implements SportAction{
	private String danceName;
	@Override
	public void sport() {
		System.out.println("正在跳"+danceName+"舞");
	}

}
