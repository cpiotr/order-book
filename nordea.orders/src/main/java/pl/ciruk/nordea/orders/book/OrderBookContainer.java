package pl.ciruk.nordea.orders.book;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public class OrderBookContainer {
	private List<OrderBook> books = new ArrayList<>();
	private Map<String, BlockingQueue<Order>> queues = new HashMap<>();
	private ExecutorService executor = Executors.newFixedThreadPool(4);
	
	/**
	 * Dispatches given order to book with given id.
	 * @param bookId
	 * @param order
	 */
	public void process(String bookId, Order order) {
		if (!queues.containsKey(bookId)) {
			// Queue for passing orders
			BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
			queues.put(bookId, queue);
			
			OrderBook book = new OrderBook(bookId, queue);
			books.add(book);
			executor.execute(book);
		}
		
		try {
			queues.get(bookId).put(order);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Notify underlying order book queues of end of processing.
	 * @throws InterruptedException
	 */
	public void finishProcessing() throws InterruptedException {
		for (BlockingQueue<Order> queue : queues.values()) {
			queue.put(Order.EMPTY);
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	/**
	 * Print books and their content to given stream.
	 * @param out
	 */
	public void printContent(PrintStream out) {
		Preconditions.checkArgument(out != null, "OutputStream cannot be null");
		
		for (OrderBook book : books) {
			out.println("book: " + book);
			book.printContent(out);
			out.println();
			out.flush();
		}
	}
}
