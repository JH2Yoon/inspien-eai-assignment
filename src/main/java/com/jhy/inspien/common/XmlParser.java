package com.jhy.inspien.common;

import com.jhy.inspien.scenario1.dto.OrderHeader;
import com.jhy.inspien.scenario1.dto.OrderItem;
import com.jhy.inspien.scenario1.dto.ParsedOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlParser {
    public static List<ParsedOrder> parseOrders(String xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Map<String, OrderHeader> headers = new LinkedHashMap<>();
        NodeList hNodes = doc.getElementsByTagName("HEADER");
        for (int i = 0; i < hNodes.getLength(); i++) {
            Element el = (Element) hNodes.item(i);
            OrderHeader h = new OrderHeader();
            h.setUserId(getText(el, "USER_ID"));
            h.setName(getText(el, "NAME"));
            h.setAddress(getText(el, "ADDRESS"));
            h.setStatus(getText(el, "STATUS"));
            headers.putIfAbsent(h.getUserId(), h);
        }

        List<ParsedOrder> result = new ArrayList<>();
        NodeList iNodes = doc.getElementsByTagName("ITEM");
        for (int i = 0; i < iNodes.getLength(); i++) {
            Element el = (Element) iNodes.item(i);
            OrderItem item = new OrderItem();
            item.setUserId(getText(el, "USER_ID"));
            item.setItemId(getText(el, "ITEM_ID"));
            item.setItemName(getText(el, "ITEM_NAME"));
            item.setPrice(new BigDecimal(getText(el, "PRICE")));

            OrderHeader header = headers.get(item.getUserId());
            if (header != null) {
                result.add(new ParsedOrder(header, item));
            }
        }
        return result;
    }

    private static String getText(Element parent, String tagName) {
        return parent.getElementsByTagName(tagName).item(0).getTextContent().trim();
    }
}