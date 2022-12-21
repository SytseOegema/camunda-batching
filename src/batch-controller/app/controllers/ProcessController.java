package controllers;

import javax.inject.*;

import play.mvc.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import models.ProcessModel;
import models.ProcessRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class ProcessController extends Controller {
  private ProcessRepository repository;
  private ObjectMapper mapper;

  @Inject
  public ProcessController(ProcessRepository repository) {
    this.repository = repository;
    this.mapper = new ObjectMapper();
  }

  public CompletionStage<Result> get() {
    return repository
      .list()
      .thenApplyAsync(processes -> {
        if(processes.isPresent()) {
          try {
            return ok(mapper.writeValueAsString(processes.get()));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return internalServerError("Server error: could not get model list.");
    });
  }

  public CompletionStage<Result> post(Http.Request request) {
    return CompletableFuture.supplyAsync(() -> {

      JsonNode json = request.body().asJson();

      ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
        .gatewayAddress(json.get("zeebeAddress").asText())
        .usePlaintext();
      try (final ZeebeClient client = clientBuilder.build()) {

        final DeploymentEvent deploymentEvent =
          client
            .newDeployResourceCommand()
            .addResourceStringUtf8(
              json.get("resourceString").asText(),
              json.get("resourceName").asText()
            )
            .send()
            .join();

        List<Process> processes = deploymentEvent.getProcesses();
        if(processes.size() > 0) {
          Process process = processes.get(0);
          return ok("Deployed process: " + process.getBpmnProcessId());
        } else {
          return internalServerError("Server error: could create process.");
        }

      } catch (Exception e) {
        e.printStackTrace();
        return internalServerError("Server error: could create process.");
      }
    });
  }

  public CompletionStage<Result> createInstance(String id, Http.Request request) {
    return CompletableFuture.supplyAsync(() -> {

      JsonNode json = request.body().asJson();

      ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
        .gatewayAddress(json.get("zeebeAddress").asText())
        .usePlaintext();
      try (final ZeebeClient client = clientBuilder.build()) {
        final ProcessInstanceEvent processInstanceEvent =
          client
            .newCreateInstanceCommand()
            .bpmnProcessId(id)
            .latestVersion()
            .variables(json.get("variables").asText())
            .send()
            .join();

        return ok("Deployed process instance: " + processInstanceEvent.getProcessInstanceKey());
      } catch (Exception e) {
        e.printStackTrace();
        return internalServerError("Server error: could create process.");
      }
    });
  }

}
