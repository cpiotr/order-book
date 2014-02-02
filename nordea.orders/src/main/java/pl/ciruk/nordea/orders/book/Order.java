package pl.ciruk.nordea.orders.book;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Representation of order operation of a certain type. <br/>
 * Instances could be created either by using provided {@link Order.Builder} or {@link #copyOf(Order)}.
 * @author piotr.ciruk
 *
 */
public class Order {
	/** NullObject. */
	public static final Order EMPTY = new Builder().build();
	
	/** Type of operation. */
	public enum OperationType {
		BUY, SELL, DELETE;
	}
	
	/** Creates deep copy of given order. */
	public static Order copyOf(Order order) {
		return new Builder()
				.id(order.id)
				.operationType(order.operationType)
				.price(order.price)
				.volume(order.volume)
				.build();
	}
	
	public static class Builder {
		private long id;
		
		private OperationType operationType;
		
		private BigDecimal price;
		
		private long volume;
		
		public Builder() {
		}
		
		public Order.Builder id(long id) {
			this.id = id;
			return this;
		}
		
		public Order.Builder operationType(OperationType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		public Order.Builder price(BigDecimal price) {
			this.price = price;
			return this;
		}
		
		public Order.Builder volume(long volume) {
			this.volume = volume;
			return this;
		}
		
		public Order build() {
			return new Order(this);
		}
	}
	
	private Order(Order.Builder builder) {
		this.id = builder.id;
		this.operationType = builder.operationType;
		this.price = builder.price;
		this.volume = builder.volume;
		
		timestamp = new Date();
	}
	
	private long id;
	
	private OperationType operationType;
	
	private BigDecimal price;
	
	private long volume;
	
	private Date timestamp;

	public void decreaseVolume(long delta) {
		volume = getVolume() - delta;
	}

	@Override
	public String toString() {
		return String.format("[%s] %d; %f; %d", operationType.toString(), id, price, volume);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public long getId() {
		return id;
	}
	
	public OperationType getOperationType() {
		return operationType;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public long getVolume() {
		return volume;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
}
