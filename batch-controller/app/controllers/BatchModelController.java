package controllers;

import javax.inject.*;

import play.mvc.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import models.BatchModel.BatchModelModel;
import models.BatchModel.BatchModelRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class BatchModelController extends Controller {
  private BatchModelRepository repository;
  private ObjectMapper mapper;

  @Inject
  public BatchModelController(BatchModelRepository repository) {
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

  public CompletionStage<Result> post(Http.Request request) {
    JsonNode json = request.body().asJson();
    try {
      BatchModelModel model = new BatchModelModel(
        0,
        json.get("maxBatchSize").asInt(),
        json.get("executeParallel").asBoolean(),
        json.get("activationThresholdCases").asInt(),
        json.get("activationThresholdTime").asInt(),
        json.get("batchExecutorURI").asText(),
        mapper.readValue(json.get("groupBy").toString(), new TypeReference<List<String>>() {})
      );

      return repository
      .add(model)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Batch model created");
        }
        return internalServerError("Server could not create Batch model.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

  public CompletionStage<Result> put(int batchModelId, Http.Request request) {
    JsonNode json = request.body().asJson();
    try {
      BatchModelModel model = new BatchModelModel(
        batchModelId,
        json.get("maxBatchSize").asInt(),
        json.get("executeParallel").asBoolean(),
        json.get("activationThresholdCases").asInt(),
        json.get("activationThresholdTime").asInt(),
        json.get("batchExecutorURI").asText(),
        mapper.readValue(json.get("groupBy").toString(), new TypeReference<List<String>>() {})
      );

      return repository
      .update(model)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Batch model updated");
        }
        return internalServerError("Server could not update Batch model.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

  public CompletionStage<Result> delete(int batchModelId) {
    try {
      return repository
      .delete(batchModelId)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Batch model deleted");
        }
        return internalServerError("Server could not delete Batch model.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

}
