package com.rahmed.redhat.demo.rest;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentREST extends RouteBuilder {

	@Override
	public void configure() {

		rest("/payment").description("Payment service")

				.post("/").type(Payment.class).description("Create a new Payment").route().routeId("process-payment")
				.tracing().log("Payment Id is ${body.id}").setHeader("id", simple("${body.id}"))
				.log("Payment Id is ${header.id} whole body is ${body}")
				.log("Posting to Kie")
				.bean(PaymentService.class, "executeRules(${body})")
				.choice()
					.when().simple("${body.approved} == true")
					 	.log("Payment Approved")
					 	//I need to create a JMS
					 	.bean(ProcessorBean.class, "processJMSMessage(${body})")
					 	.setExchangePattern(ExchangePattern.InOnly)
					 	.to("activemq:queue:TESTQUEUE")
						.transform(constant("OK"))
					.otherwise()
						.log("Payment Rejected")
						.transform(constant("NO"))
		         .end()
				.endRest();

	}
}
