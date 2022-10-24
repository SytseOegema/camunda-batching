package controllers;

import javax.inject.*;

import play.mvc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletionStage;
import java.util.List;
import java.util.Optional;
import models.ProcessModel;
import models.ProcessRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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

}
