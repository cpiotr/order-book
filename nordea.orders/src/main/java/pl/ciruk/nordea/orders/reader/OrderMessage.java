package pl.ciruk.nordea.orders.reader;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Preconditions;

/**
 * Abstract Order Message.
 * @author piotrci
 *
 */
public abstract class OrderMessage {
	public static final OrderMessage EMPTY = new OrderMessage() {
		@Override
		public boolean isDeleteMessage() {
			return false;
		}
		
		@Override
		public boolean isAddMessage() {
			return false;
		}
		
		public String toString() {
			return "EMPTY";
		}
	};
	
	private String bookId;
	
	private long orderId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public abstract boolean isAddMessage();
	
	public abstract boolean isDeleteMessage();
	
	public AddOrderMessage asAddMessage() {
		Preconditions.checkState(this instanceof AddOrderMessage, "This message is not an instance of AddOrderMessage");
		return (AddOrderMessage) this;
	}
	
	public DeleteOrderMessage asDeleteOrderMessage() {
		Preconditions.checkState(this instanceof DeleteOrderMessage, "This message is not an instance of DeleteOrderMessage");
		return (DeleteOrderMessage) this;
	}
	
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	public String getBookId() {
		return bookId;
	}
	
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	
	public long getOrderId() {
		return orderId;
	}
}
