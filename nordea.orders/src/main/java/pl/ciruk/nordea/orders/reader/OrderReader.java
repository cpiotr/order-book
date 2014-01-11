package pl.ciruk.nordea.orders.reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Serialized orders processor. <br/>
 * Reads an input file using streaming API.
 * 
 * @author piotr.ciruk
 * 
 */
public class OrderReader implements Closeable, AutoCloseable {
	private XMLEventReader eventReader;
	
	/** Names of XML nodes. */
	class ElementNames {
		static final String ADD_ORDER = "AddOrder";
		
		static final String DELETE_ORDER = "DeleteOrder";
	}
	
	/** XML Attributes' names. */
	class AttributesLocalParts {
		static final String BOOK = "book";
		
		static final String ORDER_ID = "orderId";
		
		static final String OPERATION = "operation";
		
		static final String PRICE = "price";
		
		static final String VOLUME = "volume";
	}
	
	/** Creates a reader to get data from given file. */
	public static OrderReader from(File file) {
		Preconditions.checkArgument(file != null, "Input file cannot be null");
		Preconditions.checkArgument(file.isFile(), "Input file must point to an OS file");

		try {
			return new OrderReader(file);
		} catch (FileNotFoundException | XMLStreamException e) {
			throw new OrderReaderException(e);
		}
	}
	
	/** Creates a reader to get data from given stream. */
	public static OrderReader from(InputStream inputStream) {
		Preconditions.checkArgument(inputStream != null, "InputStream cannot be null");
		
		try {
			return new OrderReader(inputStream);
		} catch (XMLStreamException e) {
			throw new OrderReaderException(e);
		}
	}
	
	private OrderReader(File file) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		eventReader = factory.createXMLEventReader(new FileReader(file));
	}
	
	private OrderReader(InputStream inputStream) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		eventReader = factory.createXMLEventReader(new InputStreamReader(inputStream));
	}
	
	/** Indicates whether there are orders left for reading. */
	public boolean hasNext() {
		return eventReader.hasNext();
	}
	
	/** 
	 * Reads next {@link OrderMessage}. <br/>
	 * Should be called along with {@link #hasNext()} method.
	 */
	@SuppressWarnings("unchecked")
	public OrderMessage next() {
		OrderMessage message = OrderMessage.EMPTY;
		
		try {
			XMLEvent event = null;
			
			// Read first XML start element
			do {
				event = eventReader.nextEvent();
			} while (eventReader.hasNext() && !event.isStartElement());
				
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (ElementNames.ADD_ORDER.equals(startElement.getName().getLocalPart())) {
					message = new AddOrderMessage();
					
					populateOrderMessage(message.asAddMessage(), startElement.getAttributes());
				} else if (ElementNames.DELETE_ORDER.equals(startElement.getName().getLocalPart())) {
					message = new DeleteOrderMessage();
					
					populateOrderMessage(message, startElement.getAttributes());
				}
			}
		} catch (XMLStreamException e) {
			throw new OrderReaderException(e);
		}
		
		return message;
	}
	
	/** Closes this reader and frees associated resources. */
	public void close() {
		try {
			eventReader.close();
		} catch (XMLStreamException e) {
			throw new OrderReaderException(e);
		}
	}
	
	private void populateOrderMessage(OrderMessage message, Iterator<Attribute> attributes) {
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			switch (attribute.getName().getLocalPart()) {
				case AttributesLocalParts.BOOK :
					message.setBookId(attribute.getValue());
					break;
				case AttributesLocalParts.ORDER_ID:
					if (!Strings.isNullOrEmpty(attribute.getValue())) {
						message.setOrderId(Long.valueOf(attribute.getValue()));
					}
					break;
				default:
					break;
			}
		}
	}
	
	private void populateOrderMessage(AddOrderMessage message, Iterator<Attribute> attributes) {
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (Strings.isNullOrEmpty(attribute.getValue())) {
				continue;
			}
			
			switch (attribute.getName().getLocalPart()) {
				case AttributesLocalParts.BOOK :
					message.setBookId(attribute.getValue());
					break;
				case AttributesLocalParts.ORDER_ID:
					message.setOrderId(Long.valueOf(attribute.getValue()));
					break;
				case AttributesLocalParts.OPERATION:
					message.setOperation(attribute.getValue());
					break;
				case AttributesLocalParts.PRICE:
					BigDecimal price = BigDecimal.valueOf(Double.valueOf(attribute.getValue()));
					message.setPrice(price);
					break;
				case AttributesLocalParts.VOLUME:
					Long volume = Long.valueOf(attribute.getValue());
					message.setVolume(volume);
					break;
				default:
					throw new OrderReaderException();
			}
		}
	}
}
