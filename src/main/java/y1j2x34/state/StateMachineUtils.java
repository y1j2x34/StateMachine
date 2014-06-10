package y1j2x34.state;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

class StateMachineUtils {
	static <Action> void load(StateMachine<Action> machine,InputStream stream,Map<String,Object> context) throws IOException,InvalidFormatException{
		new Loader<Action>(machine,context).load(stream);
	}
	static <Action> void storeToXml(StateMachine<Action> machine,OutputStream os,String encoding) throws IOException{
		new Storer<Action>(machine).storeToXml(os, encoding);
	}
}
