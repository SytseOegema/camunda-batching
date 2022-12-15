package models;

import com.google.inject.AbstractModule;

import javax.inject.*;

import play.db.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;

@Singleton
public class ProcessInstanceRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public ProcessInstanceRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<ProcessInstanceModel>>> list() {
    final String sql = "SELECT "
      + "process_instance_key ,"
      + "element_instance_key ,"
      + "bpmn_process_id ,"
      + "process_version ,"
      + "process_definition_key ,"
      + "element_id ,"
      + "element_type ,"
      + "flow_scope_key ,"
      + "variables, "
      + "intent "
      + " FROM process_instance";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          List<ProcessInstanceModel> processes = new ArrayList<ProcessInstanceModel>();
          while(rs.next()) {
            processes.add(
              new ProcessInstanceModel(
                rs.getLong("process_instance_key"),
                rs.getLong("element_instance_key"),
                rs.getString("bpmn_process_id"),
                rs.getInt("process_version"),
                rs.getLong("process_definition_key"),
                rs.getString("element_id"),
                rs.getString("element_type"),
                rs.getLong("flow_scope_key"),
                rs.getString("variables"),
                rs.getString("intent"))
            );
          }
          connection.close();
          return Optional.of(processes);
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletionStage<Optional<ProcessInstanceModel>> get(long elementInstanceKey) {
    final String sql = "SELECT "
      + "process_instance_key ,"
      + "element_instance_key ,"
      + "bpmn_process_id ,"
      + "process_version ,"
      + "process_definition_key ,"
      + "element_id ,"
      + "element_type ,"
      + "flow_scope_key ,"
      + "variables, "
      + "intent "
      + " FROM process_instance"
      + " WHERE element_instance_key = " + elementInstanceKey;

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          ProcessInstanceModel instance = null;
          if(rs.next()) {
            instance = new ProcessInstanceModel(
              rs.getLong("process_instance_key"),
              rs.getLong("element_instance_key"),
              rs.getString("bpmn_process_id"),
              rs.getInt("process_version"),
              rs.getLong("process_definition_key"),
              rs.getString("element_id"),
              rs.getString("element_type"),
              rs.getLong("flow_scope_key"),
              rs.getString("variables"),
              rs.getString("intent"));
          }
          connection.close();
          return Optional.of(instance);
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletionStage<Void> add(ProcessInstanceDTO processInstance) {

    System.out.println(processInstance.intent == null);
    System.out.println(processInstance.intent);


    String query = "INSERT INTO process_instance  (";
    query += "process_instance_key ,";
    query += "element_instance_key ,";
    query += "bpmn_process_id ,";
    query += "process_version ,";
    query += "process_definition_key ,";
    query += "element_id ,";
    query += "element_type ,";
    query += "flow_scope_key ,";
    query += "variables, ";
    query += "intent ";
    query += ") VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";
    final String sql = String.format(
      query,
      processInstance.processInstanceKey,
      processInstance.elementInstanceKey,
      processInstance.bpmnProcessId,
      processInstance.processVersion,
      processInstance.processDefinitionKey,
      processInstance.elementId,
      processInstance.elementType.getElementTypeName(),
      processInstance.flowScopeKey,
      processInstance.variables,
      processInstance.intent.getValue());
    System.out.println(sql);

    return CompletableFuture.runAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          st.executeUpdate(sql);
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
          System.out.println("Error: " + e.getSQLState());
        }
        return;
      },
      executionContext
    );
  }
}
