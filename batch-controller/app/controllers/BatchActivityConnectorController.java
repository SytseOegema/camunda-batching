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
    System.out.println(json.get("conditions").toString());
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

}
