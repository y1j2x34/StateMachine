package y1j2x34.state;

import java.io.IOException;
import java.io.NotSerializableException;

public class InvalidFormatException extends IOException{

	private static final long serialVersionUID = 1L;
	public InvalidFormatException(Throwable cause) {
		super(cause == null?null:cause.toString());
		this.initCause(cause);
	}
	public InvalidFormatException(String message) {
		super(message);
	}
	
    private void writeObject(java.io.ObjectOutputStream out)
        throws NotSerializableException 
    {
        throw new NotSerializableException("Not serializable.");
    }

    private void readObject(java.io.ObjectInputStream in)
        throws NotSerializableException 
    {
        throw new NotSerializableException("Not serializable.");
    }
}
