package controllers;

import javax.inject.*;

import play.mvc.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import models.ProcessInstanceModel;
import models.ProcessInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class ProcessInstanceController extends Controller {
  private ProcessInstanceRepository repository;
  private ObjectMapper mapper;
  private String zeebeAddress = "zeebe:26500";

  @Inject
  public ProcessInstanceController(ProcessInstanceRepository repository) {
    this.repository = repository;
    this.mapper = new ObjectMapper();
  }

  public CompletionStage<Result> get() {
    return repository
      .list()
      .thenApplyAsync(instances -> {
        if(instances.isPresent()) {
          try {
            return ok(mapper.writeValueAsString(instances.get()));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return internalServerError("Server error: could not get model list.");
    });
  }

  public CompletionStage<Result> resumeFlow(long processInstanceKey) {
    return repository
      .get(processInstanceKey)
      .thenApplyAsync(instance -> {
        if(instance.isPresent()) {
          return resumeBatchActivityFlow(instance.get());
        }
        return internalServerError("Server error: could not get instance for processInstanceKey: " + processInstanceKey);
    });
  }

  private Result resumeBatchActivityFlow(ProcessInstanceModel instance) {
    ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
      .gatewayAddress(zeebeAddress)
      .usePlaintext();

    try (final ZeebeClient client = clientBuilder.build()) {

      System.out.println("Creating process instance");

      final ResumeBatchActivityResponse response =
        client
          .newResumeBatchActivityCommand()
          .isBatchExecuted(false)
          .addProcessInstance(
            instance.processInstanceKey,
            instance.elementInstanceKey,
            instance.bpmnProcessId,
            instance.processVersion,
            instance.processDefinitionKey,
            instance.elementId,
            instance.elementType,
            instance.flowScopeKey,
            instance.variables)
          .send()
          .join();

      return ok(response.getSuccess());
    } catch (Exception e) {
      e.printStackTrace();
      return internalServerError("Server error: could not resume flow for processInstanceKey: " + instance.processInstanceKey);
    }
  }
}
