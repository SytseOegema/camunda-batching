package controllers;

import javax.inject.*;

import play.mvc.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import models.BatchCluster.BatchClusterModel;
import models.BatchCluster.BatchClusterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class BatchClusterController extends Controller {
  private BatchClusterRepository repository;
  private ObjectMapper mapper;

  @Inject
  public BatchClusterController(BatchClusterRepository repository) {
    this.repository = repository;
    this.mapper = new ObjectMapper();
  }

  public CompletionStage<Result> get() {
    return repository
      .list()
      .thenApplyAsync(clusters -> {
        if(clusters.isPresent()) {
          try {
            return ok(mapper.writeValueAsString(clusters.get()));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return internalServerError("Server error: could not get model list.");
    });
  }

}
