package controllers;

import javax.inject.*;

import play.mvc.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import models.ProcessInstanceModel;
import models.ProcessInstanceRepository;
import models.BatchCluster.BatchClusterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class ProcessInstanceController extends Controller {
  private ProcessInstanceRepository repository;
  private BatchClusterRepository batchClusterRepository;
  private ObjectMapper mapper;
  private String zeebeAddress = "zeebe:26500";

  @Inject
  public ProcessInstanceController(ProcessInstanceRepository repository,
    BatchClusterRepository batchClusterRepository
  ) {
    this.repository = repository;
    this.batchClusterRepository = batchClusterRepository;
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

  public CompletionStage<Result> resumeFlow(int id) {
    return repository
      .get(id)
      .thenApplyAsync(instance -> {
        if(instance.isPresent()) {
          return resumeBatchActivityFlow(instance.get(), id);
        }
        return internalServerError("Server error: could not get instance for process with id: " + id);
    });
  }

  private Result resumeBatchActivityFlow(ProcessInstanceModel instance, int id) {
    ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
      .gatewayAddress(zeebeAddress)
      .usePlaintext();

    try (final ZeebeClient client = clientBuilder.build()) {

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

      batchClusterRepository.removeInstanceFromCluster(id).toCompletableFuture().join();

      return ok(response.getSuccess());
    } catch (Exception e) {
      e.printStackTrace();
      return internalServerError("Server error: could not resume flow for processInstanceKey: " + instance.processInstanceKey);
    }
  }
}
