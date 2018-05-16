package com.redhat.consulting.domain;

import java.io.Serializable;


public class InFact implements Serializable {

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