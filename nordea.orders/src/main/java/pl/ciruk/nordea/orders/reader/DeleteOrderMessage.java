package pl.ciruk.nordea.orders.reader;

public class DeleteOrderMessage extends OrderMessage {

	@Override
	public boolean isAddMessage() {
		return false;
	}

	@Override
	public boolean isDeleteMessage() {
		return true;
	}

}
