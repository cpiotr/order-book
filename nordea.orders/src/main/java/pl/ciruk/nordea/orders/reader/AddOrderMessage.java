package pl.ciruk.nordea.orders.reader;

import java.math.BigDecimal;

public class AddOrderMessage extends OrderMessage {

	private String operation;
	
	private BigDecimal price;
	
	private long volume;
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	@Override
	public boolean isAddMessage() {
		return true;
	}

	@Override
	public boolean isDeleteMessage() {
		return false;
	}

}
