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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.drools.compiler.runtime.pipeline.impl.DroolsJaxbHelperProviderImpl;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.command.CommandFactory;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.server.api.marshalling.MarshallingFormat;
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

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.commands.CommandScript;

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

	@PostConstruct
	public void prepareKieConfig() {

		LOG.info("PostConstruct  We should add our prparation here======  ");
	}

	@SuppressWarnings("unchecked")
	private List<String> kieRestAPI(Double value) {

		Marshaller marshaller = null;
		KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(getKieUrl(), username,
				password);
		if (getKieUrl().toLowerCase().startsWith("https")) {
			configuration.setUseSsl(true);
			try {
				forgiveUnknownCert();
			} catch (Exception e) {
				LOG.error("Unable to certify server SSL certifdicate  ", e);
				return null;
			}
		}

		MarshallingFormat marshallingFormat = getMarshallingFormat();
		configuration.setMarshallingFormat(marshallingFormat);
		if (MarshallingFormat.JAXB.equals(marshallingFormat)) {

			Set<Class<?>> classes = new HashSet<Class<?>>();
			classes.add(InFact.class);
			configuration.addExtraClasses(classes);

		}
		if (LOG.isInfoEnabled()) {
			ClassLoader classLoader;
			classLoader = Thread.currentThread().getContextClassLoader() != null
					? Thread.currentThread().getContextClassLoader()
					: CommandScript.class.getClassLoader();
			marshaller = MarshallerFactory.getMarshaller(configuration.getExtraClasses(),
					configuration.getMarshallingFormat(), classLoader);
		}
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
		LOG.info("About to call newBatchExecution");

		BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(cmds);
		if (LOG.isInfoEnabled()) {
			LOG.info("About to call executeCommandsWithResults -- batchExecution=["
					+ marshaller.marshall(batchExecution) + "]");
		}

		ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(containerId, batchExecution);

		LOG.info("KIESERVER results identifiers ----->: " + response.getResult().getIdentifiers()
				+ "\n -------------------------------------");

		LOG.info(" APPROVALS List " + response.getResult().getValue(RESULTS_HANDLE)); // returns the ArrayList

		return response.getResult().getValue(RESULTS_HANDLE) != null
				? (ArrayList<String>) response.getResult().getValue(RESULTS_HANDLE)
				: null;
	}

	private MarshallingFormat getMarshallingFormat() {
		// can use xstream, xml (jaxb), or json
		// String type = System.getProperty("MarshallingFormat", "xstream");
		String type = System.getProperty("MarshallingFormat", MarshallingFormat.JSON.getType());
		if (type.trim().equalsIgnoreCase("jaxb")) {
			type = "xml";
		}
		MarshallingFormat marshallingFormat = MarshallingFormat.fromType(type);
		LOG.debug(String.format("--------->  %s MarshallingFormat.%s", marshallingFormat.getType(),
				marshallingFormat.name()));
		return marshallingFormat;
	}

	// only needed for non-production test scenarios where the TLS certificate isn't
	// set up properly
	private void forgiveUnknownCert() throws Exception {
		KieRemoteHttpRequest.ConnectionFactory connf = new KieRemoteHttpRequest.ConnectionFactory() {
			public HttpURLConnection create(URL u) throws IOException {
				return forgiveUnknownCert((HttpURLConnection) u.openConnection());
			}

			public HttpURLConnection create(URL u, Proxy p) throws IOException {
				return forgiveUnknownCert((HttpURLConnection) u.openConnection(p));
			}

			private HttpURLConnection forgiveUnknownCert(HttpURLConnection conn) throws IOException {
				if (conn instanceof HttpsURLConnection) {
					HttpsURLConnection sconn = HttpsURLConnection.class.cast(conn);
					sconn.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String arg0, SSLSession arg1) {
							return true;
						}
					});
					try {
						SSLContext context = SSLContext.getInstance("TLS");
						context.init(null, new TrustManager[] { new X509TrustManager() {
							public void checkClientTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
							}

							public void checkServerTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
							}

							public X509Certificate[] getAcceptedIssuers() {
								return null;
							}
						} }, null);
						sconn.setSSLSocketFactory(context.getSocketFactory());
					} catch (Exception e) {
						throw new IOException(e);
					}
				}
				return conn;
			}
		};
		Field field = KieRemoteHttpRequest.class.getDeclaredField("CONNECTION_FACTORY");
		field.setAccessible(true);
		field.set(null, connf);
	}

}