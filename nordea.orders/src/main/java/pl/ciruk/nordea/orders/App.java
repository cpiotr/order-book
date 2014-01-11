package pl.ciruk.nordea.orders;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pl.ciruk.nordea.orders.book.Order;
import pl.ciruk.nordea.orders.book.Order.OperationType;
import pl.ciruk.nordea.orders.book.OrderBook;
import pl.ciruk.nordea.orders.book.OrderBookContainer;
import pl.ciruk.nordea.orders.reader.AddOrderMessage;
import pl.ciruk.nordea.orders.reader.OrderMessage;
import pl.ciruk.nordea.orders.reader.OrderReader;

import com.google.common.base.Function;

/**
 * Main application with CLI.
 *
 */
public class App {
	public static void main(String[] args) {
		CommandLineParser parser = new BasicParser();
		try {
			Options options = options();
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption('f')) {
				long start = System.currentTimeMillis();
				processOrders(Paths.get(cmd.getOptionValue('f')));
				System.out.format("Time: %d ms\n", (System.currentTimeMillis() - start));
			} else {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("nordea.orders", options);
			}
		} catch (ParseException e) {
			// Well, that's a pitty...
			System.err.println("Parsing failed. Caused by: " + e.getMessage());
		}
	}

	private static void processOrders(Path ordersFile) {
		try (OrderReader reader = OrderReader.from(ordersFile.toFile())) {
			OrderBookContainer books = new OrderBookContainer();
			
			// Read and process one order at a time
			while (reader.hasNext()) {
				OrderMessage msg = reader.next();
				if (OrderMessage.EMPTY != msg) {
					OrderBook book = books.get(msg.getBookId());
					
					if (msg.isDeleteMessage()) {
						book.remove(msg.getOrderId());
					} else if (msg.isAddMessage()) {
						Order order  = MESSAGE_TO_ORDER.apply(msg.asAddMessage());
						
						if (order.getOperationType() == OperationType.BUY) {
							book.buy(order);
						} else if (order.getOperationType() == OperationType.SELL) {
							book.sell(order);
						}
					}
				}
			}
			
			// Print results
			books.printContent(System.out);
		}
	}

	private static Options options() {
		Options options = new Options();
		options.addOption("f", true, "Path to a XML document containing orders");
		options.addOption("h", false, "Prints program usage");
		return options;
	}

	/** OrderMessage to Order adapter. */
	private static final Function<AddOrderMessage, Order> MESSAGE_TO_ORDER = new Function<AddOrderMessage, Order>() {
		@Override
		public Order apply(AddOrderMessage message) {
			Order order = null;
			
			if (message != null) {
				order = new Order.Builder()
					.id(message.getOrderId())
					.operationType(Order.OperationType.valueOf(message.getOperation()))
					.price(message.getPrice())
					.volume(message.getVolume())
					.build();
			}
			
			return order;
		}
	};
}
