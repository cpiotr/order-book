package pl.ciruk.nordea.orders.book;

import java.io.PrintStream;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class OrderBookContainer {
	private Map<String, OrderBook> books = Maps.newHashMap();
	
	public OrderBook get(String bookId) {
		if (!books.containsKey(bookId)) {
			books.put(bookId, new OrderBook());
		}
		
		return books.get(bookId);
	}
	
	public void printContent(PrintStream out) {
		Preconditions.checkArgument(out != null, "OutputStream cannot be null");
		
		for (Map.Entry<String, OrderBook> bookEntry : books.entrySet()) {
			out.println("book: " + bookEntry.getKey());
			bookEntry.getValue().printContent(out);
			out.println();
			out.flush();
		}
	}
}
