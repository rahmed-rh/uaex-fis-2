package com.redhat.consulting.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InFact", propOrder = { "paymentValue" })
@XmlRootElement(name = "InFact")
public class InFact implements Serializable {

	private Double paymentValue;

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