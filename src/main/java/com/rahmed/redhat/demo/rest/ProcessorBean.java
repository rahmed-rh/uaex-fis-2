package com.rahmed.redhat.demo.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

public class ProcessorBean {
	public Map<String, Object> processJMSMessage(Payment payment) {

		Map<String, Object> map = new HashMap<String, Object>();

		//Object body = (Object) exchange.getIn().getBody();

		// map.put("ERROR_ID",111);
		// map.put("ERROR_CODE",headers.get("ERROR-CODE"));
		// map.put("ERROR_MESSAGE",headers.get("ERROR-MESSAGE"));
		// map.put("MESSAGE", body.toString());
		// map.put("STATUS", "ERROR");
		map.put("PAYMENT_ID", payment.getId());
		map.put("PAYEE_ID", payment.getPayeeId());
		map.put("PAYMENT_AMOUNT", payment.getAmount());
		return map;
	}
}
