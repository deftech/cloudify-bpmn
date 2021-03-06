package co.cloudify.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(MockitoJUnitRunner.class)
public class RunWorkflowDelegateTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8888);

	@Mock
	DelegateExecution execution;

	@Test
	public void TestRunWorkflow() throws Exception{
		CloudifyRunWorkflowDelegate d = new CloudifyRunWorkflowDelegate();
		when(execution.getVariable("InputCfy_deployment")).thenReturn("test");
		when(execution.hasVariable("InputCfy_deployment")).thenReturn(true);
		when(execution.getVariable("InputCfy_workflow")).thenReturn("install");
		when(execution.hasVariable("InputCfy_workflow")).thenReturn(true);
				
		Map<String,String> creds = new HashMap<>();
		creds.put("username","admin");
		creds.put("password","admin");
		creds.put("url","http://localhost:8888");
		creds.put("tenant","default_tenant");
		when(execution.getVariable("InputCfy_credentials")).thenReturn(creds);
		when(execution.hasVariable("InputCfy_credentials")).thenReturn(true);

		//wiremock
		stubFor(get(urlMatching("/.*")).willReturn(aResponse().withStatus(200)));
		stubFor(get(urlMatching("/.*executions.*")).willReturn(aResponse().withBody("{\"items\":[{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2019-01-01\", \"status\": \"terminated\"}]}").withHeader("content-type", "application/json").withStatus(200)));
		stubFor(get(urlMatching("/.*executions/[a-zA-Z0-9]*")).willReturn(aResponse().withBody("{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2019-01-01\", \"status\": \"terminated\"}").withHeader("content-type", "application/json").withStatus(200)));
		stubFor(post(urlMatching("/.*executions.*")).willReturn(aResponse().withBody("{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2019-01-01\", \"status\": \"terminated\"}").withHeader("content-type", "application/json").withStatus(200)));
		d.execute(execution);
		
		verify(postRequestedFor(urlMatching("/.*executions.*")).withRequestBody(containing("\"workflow_id\":\"install\"")));
		verify(postRequestedFor(urlMatching("/.*executions.*")).withRequestBody(containing("\"deployment_id\":\"test\"")));
	}


	@Test(expected = java.lang.Exception.class)
	public void TestMissingCreds() throws Exception{
		CloudifyRunWorkflowDelegate d = new CloudifyRunWorkflowDelegate();
		d.execute(execution);

	}
}
