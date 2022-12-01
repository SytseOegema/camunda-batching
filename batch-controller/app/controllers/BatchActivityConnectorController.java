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
import models.BatchActivityConnectorModel;
import models.BatchActivityConnectorConditionModel;
import models.BatchActivityConnectorRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class BatchActivityConnectorController extends Controller {
  private BatchActivityConnectorRepository repository;
  private ObjectMapper mapper;

  @Inject
  public BatchActivityConnectorController(BatchActivityConnectorRepository repository) {
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
      BatchActivityConnectorModel connector = new BatchActivityConnectorModel(
        0,
        json.get("active").asBoolean(),
        json.get("validity").asText(),
        mapper.readValue(json.get("conditions").toString(), new TypeReference<List<BatchActivityConnectorConditionModel>>() {}),
        json.get("activityId").asText(),
        json.get("batchModelId").asInt());

      return repository
      .add(connector)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Connector created");
        }
        return internalServerError("Server could not create connector.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

  public CompletionStage<Result> put(int connectorId, Http.Request request) {
    JsonNode json = request.body().asJson();
    try {
      BatchActivityConnectorModel connector = new BatchActivityConnectorModel(
        connectorId,
        json.get("active").asBoolean(),
        json.get("validity").asText(),
        new ArrayList<BatchActivityConnectorConditionModel>(),
        json.get("activityId").asText(),
        json.get("batchModelId").asInt());

      return repository
      .update(connector)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Connector updated");
        }
        return internalServerError("Server could not update connector.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

  public CompletionStage<Result> putCondition(int connectorId, Http.Request request) {
    JsonNode json = request.body().asJson();
    try {
      BatchActivityConnectorConditionModel condition = new BatchActivityConnectorConditionModel(
        json.get("conditionId").asInt(),
        connectorId,
        json.get("fieldName").asText(),
        json.get("fieldType").asText(),
        json.get("compareOperator").asText(),
        json.get("compareValue").asText());

      return repository
      .updateConditions(condition)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Condition updated");
        }
        return internalServerError("Server could not update condition.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

  public CompletionStage<Result> delete(int connectorId) {
    try {
      return repository
      .delete(connectorId)
      .thenApplyAsync(success -> {
        if (success) {
          return ok("Connector deleted");
        }
        return internalServerError("Server could not delete connector.");
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.supplyAsync(() ->
      internalServerError("Server Error - Oops something went wrong"));
  }

}
