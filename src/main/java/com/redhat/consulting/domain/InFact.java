package com.redhat.consulting.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement(name = "com.redhat.consulting.domain.InFact")
@XmlAccessorType(XmlAccessType.FIELD)
public class InFact implements Serializable {

	@org.kie.api.definition.type.Label("paymentValue")
	@XmlElement
	private Double paymentValue;

	public InFact() {
		this.paymentValue = 0.0;
	}

	public InFact(Double paymentValue) {
		this.paymentValue = paymentValue;
	}

	public Double getPaymentValue() {
		return paymentValue;
	}

	public void setPaymentValue(Double paymentValue) {
		this.paymentValue = paymentValue;
	}

}
