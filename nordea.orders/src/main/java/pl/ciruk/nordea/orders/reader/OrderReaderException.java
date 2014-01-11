package pl.ciruk.nordea.orders.reader;

/**
 * Indicates an error during the reading of order messages.
 * @author piotr.ciruk
 *
 */
public class OrderReaderException extends RuntimeException {
	
	/** */
	private static final long serialVersionUID = -633819091963884091L;

	public OrderReaderException() {
		
	}
	
	public OrderReaderException(Throwable cause) {
		super(cause);
	}
}
