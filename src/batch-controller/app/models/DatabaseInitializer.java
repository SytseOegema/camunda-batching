package models;

import com.google.inject.AbstractModule;

import javax.inject.*;

import play.db.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DatabaseInitializer {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public DatabaseInitializer(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
    init();
  }

  public CompletionStage<Void> init() {
    return CompletableFuture.runAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          assertTablesArePresent(connection);
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return;
      },
      executionContext
    );
  }

  private void assertTablesArePresent(Connection connection) {
    List<String> tables = new ArrayList<String>();
    tables.add("process");
    tables.add("process_activity");
    tables.add("activity");
    tables.add("process_instance");
    tables.add("batch_activity_connector");
    tables.add("batch_activity_connector_condition");
    tables.add("batch_model");
    tables.add("batch_model_group_by");
    tables.add("batch_cluster");
    tables.add("batch_cluster_instance");
    String sql = "SELECT tablename FROM pg_tables " +
      "where tablename in ("  +
      "'process'," +
      "'process_activity'," +
      "'activity'," +
      "'process_instance'," +
      "'batch_activity_connector'," +
      "'batch_activity_connector_condition'," +
      "'batch_model'," +
      "'batch_model_group_by'," +
      "'batch_cluster'," +
      "'batch_cluster_instance'" +
      ")";

    try {
      Statement st = connection.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while(rs.next()) {
        int idx = tables.indexOf(rs.getString("tablename").toLowerCase());
        if(idx != -1) {
          tables.remove(idx);
        }
      }

      st = connection.createStatement();

      for (String table : tables) {
        System.out.println(table);
        switch (table) {
          case "process":
              st.addBatch(createProcessesTable());
            break;
          case "activity":
              st.addBatch(createActivitiesTable());
            break;
          case "process_activity":
              st.addBatch(createProcessActivitiesTable());
            break;
          case "process_instance":
              st.addBatch(createProcessInstancesTable());
            break;
          case "batch_activity_connector":
              st.addBatch(createBatchActivityConnectorTable());
            break;
          case "batch_activity_connector_condition":
              st.addBatch(createBatchActivityConnectorConditionTable());
            break;
          case "batch_model":
              st.addBatch(createBatchModelTable());
            break;
          case "batch_model_group_by":
              st.addBatch(createBatchModelGroupByTable());
            break;
          case "batch_cluster":
              st.addBatch(createBatchClusterTable());
            break;
          case "batch_cluster_instance":
              st.addBatch(createBatchClusterInstanceTable());
            break;
          default:
            break;
        }
      }
      st.executeBatch();
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  private String createProcessesTable() {
    return "CREATE TABLE process " +
      "(id VARCHAR(255) not NULL, " +
      " name VARCHAR(255), " +
      " PRIMARY KEY ( id ))";
  }

  private String createActivitiesTable() {
    return "CREATE TABLE activity " +
      "(id VARCHAR(255) not NULL, " +
      " name VARCHAR(255), " +
      " activity_type VARCHAR(255), " +
      " PRIMARY KEY ( id ))";
  }

  private String createProcessActivitiesTable() {
    return "CREATE TABLE process_activity " +
      "(process_id VARCHAR(255) not NULL, " +
      " activity_id VARCHAR(255), " +
      " PRIMARY KEY ( process_id, activity_id ))";
  }

  private String createProcessInstancesTable() {
    return "CREATE TABLE process_instance " +
      "(id SERIAL, " +
      " process_instance_key BIGINT not NULL, " +
      " element_instance_key BIGINT not NULL, " +
      " bpmn_process_id VARCHAR(255), " +
      " process_version INT, " +
      " process_definition_key BIGINT, " +
      " element_id VARCHAR(255), " +
      " element_type VARCHAR(255), " +
      " flow_scope_key BIGINT, " +
      " variables TEXT, " +
      " intent VARCHAR(255), " +
      " PRIMARY KEY ( id ))";
  }

  private String createBatchActivityConnectorTable() {
    return "CREATE TABLE batch_activity_connector " +
      "(connector_id SERIAL not NULL, " +
      " active BOOLEAN, " +
      " validity TIMESTAMP, " +
      " activity_id VARCHAR(255), " +
      " batch_model_id INT, " +
      " PRIMARY KEY ( connector_id ))";
  }

  private String createBatchActivityConnectorConditionTable() {
    return "CREATE TABLE batch_activity_connector_condition " +
      "(condition_id SERIAL not NULL, " +
      " connector_id INT, " +
      " field_name VARCHAR(255), " +
      " field_type VARCHAR(255), " +
      " compare_operator VARCHAR(255), " +
      " compare_value VARCHAR(255), " +
      " PRIMARY KEY ( condition_id ))";
  }

  private String createBatchModelTable() {
    return "CREATE TABLE batch_model " +
      "(batch_model_id SERIAL not NULL, " +
      " name VARCHAR(255), " +
      " max_batch_size INT, " +
      " execute_parallel BOOLEAN, " +
      " activation_threshold_cases INT, " +
      " activation_threshold_time INT, " +
      " batch_executor_URI VARCHAR(255), " +
      " PRIMARY KEY ( batch_model_id ))";
  }

  private String createBatchModelGroupByTable() {
    return "CREATE TABLE batch_model_group_by " +
      "(group_by_id SERIAL not NULL, " +
      " batch_model_id INT, " +
      " field_name VARCHAR(255), " +
      " PRIMARY KEY ( group_by_id ))";
  }

  private String createBatchClusterTable() {
    return "CREATE TABLE batch_cluster " +
      "(batch_cluster_id SERIAL not NULL, " +
      " batch_model_id INT, " +
      " created_at TIMESTAMP, " +
      " state VARCHAR(255), " +
      " PRIMARY KEY ( batch_cluster_id ))";
  }


    private String createBatchClusterInstanceTable() {
      return "CREATE TABLE batch_cluster_instance " +
        " (batch_cluster_id INT, " +
        " process_instance_id BIGINT, " +
        " process_instance_key BIGINT, " +
        " PRIMARY KEY ( batch_cluster_id, process_instance_id ))";
    }
}
