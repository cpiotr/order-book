package pl.ciruk.nordea.orders.book;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.ciruk.nordea.orders.book.Order.OperationType;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.internal.Lists;

public class OrderBookTest {

	private OrderBook book;
	
	private BlockingQueue<Order> queue = new ArrayBlockingQueue<>(10);
	
	private long id = 1;
	
	@Before
	public void setUp() throws Exception {
		book = new OrderBook("ID", queue);
		new Thread(book).start();
	}

	@Test
	public void shouldBeOrderedAsBuys() {
		List<Order> orders = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(100.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(101.5)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(100.25)).volume(30).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(99.5)).volume(45).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(99.75)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(100.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(99.5)).volume(60).build());
		for (Order order : orders) {
			try {
				queue.put(order);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			queue.put(Order.EMPTY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 1; i < book.buys.size(); i++) {
			Order first = book.buys.get(i-1);
			Order second = book.buys.get(i);
			
			boolean ascendingPrices = first.getPrice().compareTo(second.getPrice()) < 0;
			Assert.assertFalse(ascendingPrices);
			
			if (first.getPrice().equals(second.getPrice())) {
				boolean descendingTimestamps = first.getTimestamp().after(second.getTimestamp());
				Assert.assertFalse(descendingTimestamps);
			}
		}
	}
	
	@Test
	public void shouldBeOrderedAsSells() throws InterruptedException {
		List<Order> orders = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(101.5)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.25)).volume(30).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(99.5)).volume(45).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(99.75)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(99.5)).volume(60).build());
		for (Order order : orders) {
			queue.put(order);
		}
		queue.put(Order.EMPTY);
		
		for (int i = 1; i < book.sells.size(); i++) {
			Order first = book.sells.get(i-1);
			Order second = book.sells.get(i);
			
			boolean descendingPrices = first.getPrice().compareTo(second.getPrice()) > 0;
			Assert.assertFalse(descendingPrices);
			
			if (first.getPrice().equals(second.getPrice())) {
				boolean ascendingTimestamps = first.getTimestamp().before(second.getTimestamp());
				Assert.assertFalse(ascendingTimestamps);
			}
		}
	}
	
	@Test
	public void shouldMatchBuyWithAllSells() throws InterruptedException {
		List<Order> sells = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(101.5)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(60).build());
		long totalSellVolume = 0;
		BigDecimal maxSellPrice = BigDecimal.ZERO;
		for (Order sell : sells) {
			queue.put(Order.copyOf(sell));
			totalSellVolume += sell.getVolume();
			maxSellPrice  = maxSellPrice.max(sell.getPrice());
		}
		
		Order buy = new Order.Builder().id(id++).operationType(OperationType.BUY).price(maxSellPrice).volume(totalSellVolume).build();
		queue.put(buy);
		
		queue.put(Order.EMPTY);
		
		Assert.assertTrue(book.buys.isEmpty());
		Assert.assertTrue(book.sells.isEmpty());
	}
	

	
	@Test
	public void shouldMatchBuyWithFirstSell() throws InterruptedException {
		List<Order> sells = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(200.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(60).build());
		for (Order sell : sells) {
			queue.put(Order.copyOf(sell));
		}
		
		// Create a buy order to match first sell (greater price, greater volume)
		Order firstSell = book.sells.get(0);
		Order buy = new Order.Builder()
				.id(id++)
				.operationType(OperationType.BUY)
				.price(firstSell.getPrice().add(BigDecimal.ONE))
				.volume(firstSell.getVolume()*5/4)
				.build();
		queue.put(buy);
		
		Assert.assertFalse(book.buys.isEmpty());
		Assert.assertFalse(book.sells.isEmpty());
		
		Assert.assertEquals(1, book.buys.size());
		Assert.assertEquals(1, book.sells.size());
		
		// Ensure that first sell from the book has price greater than first buy
		Order firstBuy = book.buys.get(0);
		firstSell = book.sells.get(0);
		Assert.assertTrue(firstSell.getPrice().compareTo(firstBuy.getPrice()) > 0);
	}
	
	@Test
	public void shouldMatchBuyWithLastSell() throws InterruptedException {
		List<Order> sells = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(200.0)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(150.0)).volume(30).build());
		for (Order sell : sells) {
			queue.put(Order.copyOf(sell));
		}
		
		// Create a buy order to match first sell (greater price, greater volume)
		Order lastSell = book.sells.get(book.sells.size() - 1);
		Order buy = new Order.Builder()
				.id(id++)
				.operationType(OperationType.BUY)
				.price(lastSell.getPrice().add(BigDecimal.ONE))
				.volume(lastSell.getVolume()*4/5)
				.build();
		queue.put(buy);
		
		queue.put(Order.EMPTY);
		
		Assert.assertTrue(book.buys.isEmpty());
		Assert.assertFalse(book.sells.isEmpty());
	}
	
	@Test
	public void shouldMatchSellWithAllBuys() throws InterruptedException {
		List<Order> buys = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(201.5)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(100.0)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(150.0)).volume(65).build());
		long totalBuyVolume = 0;
		BigDecimal minBuyPrice = BigDecimal.ZERO;
		for (Order buy : buys) {
			queue.put(Order.copyOf(buy));
			totalBuyVolume += buy.getVolume();
			minBuyPrice  = minBuyPrice.min(buy.getPrice());
		}
		
		Order sell = new Order.Builder()
				.id(id++)
				.operationType(OperationType.SELL)
				.price(minBuyPrice.subtract(BigDecimal.valueOf(0.1)))
				.volume(totalBuyVolume)
				.build();
		queue.put(sell);
		
		queue.put(Order.EMPTY);
		
		Assert.assertTrue(book.buys.isEmpty());
		Assert.assertTrue(book.sells.isEmpty());
	}
	
	@Test
	public void shouldMatchSellWithFirstBuy() throws InterruptedException {
		List<Order> buys = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(201.5)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(100.0)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(150.0)).volume(65).build());

		for (Order buy : buys) {
			queue.put(Order.copyOf(buy));
		}
		
		Order firstBuy = book.buys.get(0);
		Order sell = new Order.Builder()
				.id(id++)
				.operationType(OperationType.SELL)
				.price(firstBuy.getPrice().subtract(BigDecimal.ONE))
				.volume(firstBuy.getVolume()*5/4)
				.build();
		queue.put(sell);

		queue.put(Order.EMPTY);
		
		Assert.assertFalse(book.buys.isEmpty());
		Assert.assertFalse(book.sells.isEmpty());

		// Ensure that first sell from the book has price greater than first buy
		firstBuy = book.buys.get(0);
		Order firstSell = book.sells.get(0);
		Assert.assertTrue(firstSell.getPrice().compareTo(firstBuy.getPrice()) > 0);
	}

	@Test
	public void testRemove() throws InterruptedException {
		List<Order> orders = Lists.newArrayList(
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(10)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(20)).volume(30).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(30)).volume(45).build(),
				new Order.Builder().id(id++).operationType(OperationType.BUY).price(BigDecimal.valueOf(40)).volume(60).build(),
				
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(400)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(300)).volume(50).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(200)).volume(60).build(),
				new Order.Builder().id(id++).operationType(OperationType.SELL).price(BigDecimal.valueOf(100.0)).volume(50).build());
		for (Order order : orders) {
			queue.put(order);
		}
		
		// Ensure that no match was performed
		Assert.assertEquals(orders.size(), book.buys.size() + book.sells.size());
		
		// Orders with even IDs will be removed from book
		Collection<Order> toBeRemoved = Collections2.filter(orders, new Predicate<Order>() {
			@Override
			public boolean apply(Order order) {
				return order.getId() % 2 == 0;
			}
		});
		
		for (Order order : toBeRemoved) {
			book.remove(order.getId());
			Assert.assertNull(book.getOrder(order.getId()));
		}
		
		queue.put(Order.EMPTY);
		
		Assert.assertEquals(orders.size() - toBeRemoved.size(), book.sells.size() + book.buys.size());
	}

}
