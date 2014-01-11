package pl.ciruk.nordea.orders.reader;

import junit.framework.Assert;

import org.junit.Test;

public class OrderMessageTest {

	@Test
	public void shouldCreateAddMessage() {
		OrderMessage message = new AddOrderMessage();
		Assert.assertTrue(message.isAddMessage());
		Assert.assertFalse(message.isDeleteMessage());
	}
	
	@Test
	public void shouldCreateDeleteMessage() {
		OrderMessage message = new DeleteOrderMessage();
		Assert.assertFalse(message.isAddMessage());
		Assert.assertTrue(message.isDeleteMessage());
	}
	
	@Test
	public void shouldCreateEmptyMessage() {
		OrderMessage message = OrderMessage.EMPTY;
		Assert.assertFalse(message.isAddMessage());
		Assert.assertFalse(message.isDeleteMessage());
		
		OrderMessage newRef = OrderMessage.EMPTY;
		Assert.assertTrue(message == newRef);
	}
}
