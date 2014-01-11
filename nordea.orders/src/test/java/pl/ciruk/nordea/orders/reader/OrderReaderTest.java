package pl.ciruk.nordea.orders.reader;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class OrderReaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFromInputStream() {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("simple_orders.xml");
		OrderReader reader = OrderReader.from(inputStream);
		assertNotNull(reader);
	}
	
	@Test
	public void shouldReadValidNumberOfOrders() {
		// Read content of sample XML with orders
		String xml = null;
		try (InputStream expectedStream = getClass().getClassLoader().getResourceAsStream("simple_orders.xml");) {
			xml = readAll(expectedStream);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		int expectedOrderNumber = countOccurrances("AddOrder", xml) + countOccurrances("DeleteOrder", xml);
		
		List<OrderMessage> messages = Lists.newArrayList();
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("simple_orders.xml");
				OrderReader reader = OrderReader.from(inputStream)) {
			
			while (reader.hasNext()) {
				OrderMessage message = reader.next();
				assertNotNull(message);
				
				if (OrderMessage.EMPTY != message) {
					messages.add(message);
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertEquals(expectedOrderNumber, messages.size());
	}
	
	private String readAll(InputStream stream) throws IOException {
		StringBuffer data = new StringBuffer();
		try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
			int length = -1;
			char[] buffer = new char[1024];
			while ((length = reader.read(buffer)) > -1) {
				data.append(buffer, 0, length);
			}
		}
		return data.toString();
	}
	
	private int countOccurrances(String findWhat, String findWhere) {
		return findWhere.split(findWhat, -1).length-1;
	}
}
