package com.rahmed.redhat.demo.rest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "activemq")
public class AMQSetup {

	final static Logger LOG = LoggerFactory.getLogger(AMQSetup.class);
	/**
	 * The host name of the kie service.
	 */
	private String host;

	/**
	 * The port of the kie service.
	 */
	private Integer port;

	/**
	 * The username of the kie service.
	 */
	private String username;

	/**
	 * The password of the kie service.
	 */
	private String password;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private String getBrokerUrl() {
		return "tcp://" + host + ":" + port;
	}

	@Bean
	ActiveMQConnectionFactory amqConnectionFactory() {
		LOG.info("host=" + host + ", port=" + port + ", username=" + username + ", password=" + password);
		return new ActiveMQConnectionFactory(username, password, getBrokerUrl());

	}

	@Bean
	JmsTransactionManager jmsTransactionManager(@Autowired ActiveMQConnectionFactory amqConnectionFactory) {
		return new JmsTransactionManager(amqConnectionFactory);

	}

	@Bean
	ActiveMQComponent activemq(@Autowired JmsTransactionManager jmsTransactionManager,
			@Autowired ActiveMQConnectionFactory connectionFactory) {
		ActiveMQComponent activemq = new ActiveMQComponent();
		activemq.setTransactionManager(jmsTransactionManager);
		activemq.setConnectionFactory(connectionFactory);
		// activemq.setBrokerURL(getBrokerUrl());
		// activemq.setUserName(username);
		// activemq.setPassword(password);
		return activemq;

	}

}
