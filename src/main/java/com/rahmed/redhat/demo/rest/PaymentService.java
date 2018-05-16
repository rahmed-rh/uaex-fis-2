/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.rahmed.redhat.demo.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.command.CommandFactory;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.redhat.consulting.domain.InFact;

@Component
@ConfigurationProperties(prefix = "kie")
public class PaymentService {

	final static Logger LOG = LoggerFactory.getLogger(PaymentService.class);

	private final String IN_HANDLE = "KIE_IN_HANDLE";
	private final String RESULTS_HANDLE = "resultsList";
	private final String OBJECT_HANDLE = "KIE_OBJECT_HANDLE";
	private final String APPROVAL_OUT_IDENTIFIER = "KIE_RESULT";

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

	/**
	 * The password of the containerId service.
	 */
	private String containerId;

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

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public void executeRules(Payment payment) {
		List<String> result = kieRestAPI(payment.getAmount());

		payment.setApproved(result != null && result.size() > 0 && result.get(0).equalsIgnoreCase("APPROVED"));
	}

	private String getKieUrl() {
		return "http://" + host + ":" + port + "/kie-server/services/rest/server";
	}

	private List<String> kieRestAPI(Double value) {

		KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(getKieUrl(), username,
				password);
		Set<Class<?>> allClasses = new HashSet<Class<?>>();
		allClasses.add(com.redhat.consulting.domain.InFact.class);
		configuration.addExtraClasses(allClasses);

		KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
		RuleServicesClient ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
		KieCommands commandsFactory = KieServices.Factory.get().getCommands();

		List<Command> cmds = new ArrayList<>();
		cmds.add(CommandFactory.newSetGlobal(RESULTS_HANDLE, new ArrayList<String>(), true));
		cmds.add(CommandFactory.newInsert(new InFact(value), IN_HANDLE));
		// cmds.add( CommandFactory.newInsert(new String(), "out" ));
		GetObjectsCommand getObjectsCommand = new GetObjectsCommand();
		cmds.add(commandsFactory.newGetObjects(OBJECT_HANDLE));
		cmds.add(commandsFactory.newFireAllRules());
		BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(cmds);
		ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(containerId, batchExecution);

		LOG.info("KIESERVER results identifiers ----->: " + response.getResult().getIdentifiers()
				+ "\n -------------------------------------");

		LOG.info(" APPROVALS List " + response.getResult().getValue(RESULTS_HANDLE)); // returns the ArrayList

		

		return response.getResult().getValue(RESULTS_HANDLE) != null
				? (ArrayList<String>) response.getResult().getValue(RESULTS_HANDLE)
				: null;
	}

}