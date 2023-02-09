package workers;

import play.db.*;
import managers.ProcessInstanceActivityManager;
import managers.ProcessInstanceFlowManager;
import models.BatchCluster.BatchClusterState;
import models.ProcessInstanceModel;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import javax.inject.Singleton;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BatchClusterInvoker {
  private Database db;
  private Timer timer;
  private BatchClusterInvokerContext executionContext;
  private Logger logger = LoggerFactory.getLogger("Invoker");

  @Inject
    public BatchClusterInvoker(
      Database db,
      BatchClusterInvokerContext executionContext
    ) {
      this.db = db;
      this.executionContext = executionContext;
  }

  public CompletableFuture<Void> invokeBatchClusters(Map<String, List<Integer>> map) {
    return CompletableFuture.runAsync(() -> {
      logger.info("starting future of invokeBatchClustersssssss");
      for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
        if(entry.getKey() == BatchClusterState.EXECUTING.getStateName()) {
          for (int id : entry.getValue()) {
            invokeBatchCluster(id, true).thenApply(this::updateStateToFinished);
          }
        } else if(entry.getKey() == BatchClusterState.RESUMING.getStateName()) {
          for (int id : entry.getValue()) {
            invokeBatchCluster(id, false).thenApply(this::updateStateToResumed);
          }
        }
      }
    });
  }

  public CompletableFuture<Integer> invokeBatchCluster(int batchClusterId, boolean isBatchExecution) {
    final String sql = "SELECT "
      + "process_instance.process_instance_key ,"
      + "process_instance.element_instance_key ,"
      + "process_instance.bpmn_process_id ,"
      + "process_instance.process_version ,"
      + "process_instance.process_definition_key ,"
      + "process_instance.element_id ,"
      + "process_instance.element_type ,"
      + "process_instance.flow_scope_key ,"
      + "process_instance.variables, "
      + "process_instance.intent "
      + " FROM process_instance"
      + " JOIN batch_cluster_instance"
      + " ON batch_cluster_instance.process_instance_id = process_instance.id"
      + " WHERE batch_cluster_instance.batch_cluster_id = %d";

    final String sql2 = "SELECT "
      + "batch_model.batch_executor_URI, "
      + "batch_model_group_by.group_by_id, "
      + "batch_model_group_by.field_name "
      + " FROM batch_model"
      + " JOIN batch_cluster"
      + " ON batch_cluster.batch_model_id = batch_model.batch_model_id"
      + " LEFT JOIN batch_model_group_by"
      + " ON batch_model_group_by.batch_model_id = batch_model.batch_model_id"
      + " WHERE batch_cluster.batch_cluster_id = %d";

    return CompletableFuture.supplyAsync(
      () -> {
        logger.info("starting future of BatchCluster");
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(String.format(sql, batchClusterId));

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

          String executorURI = "";
          if (isBatchExecution) {
            rs = st.executeQuery(String.format(sql2, batchClusterId));
            List<String> groupBy = new ArrayList<String>();
            while(rs.next()) {
              executorURI = rs.getString("batch_executor_URI");
              if (rs.getInt("group_by_id") != 0) {
                groupBy.add(rs.getString("field_name"));
              }
            }
            processes = executeBatch(processes, executorURI, groupBy);
            ProcessInstanceFlowManager.continueBatchActivityFlow(processes);
          } else {
            ProcessInstanceFlowManager.resumeBatchActivityFlow(processes);
          }
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return batchClusterId;
      },
      executionContext
    );
  }

  private List<ProcessInstanceModel> executeBatch(
    List<ProcessInstanceModel> processes,
    String executorURI,
    List<String> groupBy
  ) {
    if (groupBy.size() == 0) {
      for (ProcessInstanceModel process: processes) {
        String body = "{\"content\":" + process.variables + "}";
        logger.info("joehoe!");
        logger.info(body);
        Unirest.config().verifySsl(false);
        HttpResponse<String> response = Unirest.post(executorURI)
        .header("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOmFiY3pPM3haQ0xyTU42djJCS0sxZFhZRnBYbFBrY2NPRnFtMTJDZEFzTWdSVTRWck5aOWx5R1ZDR3VNREdJd1A=")
        .header("Content-Type", "application/json")
        .body(body)
        .asString();

        logger.info("response: ");
        logger.info(response.getBody());
        process.variables = ProcessInstanceActivityManager
          .updateVariables(process.variables, response.getBody());
      }
      return processes;
    }

    // create map based on value in the group-by parameter
    // !!! currently this only supports a single group-by parameter !!!
    Map<String, List<ProcessInstanceModel>> map =
      groupBy(processes, groupBy.get(0));

    List<ProcessInstanceModel> resultProcesses = new ArrayList<ProcessInstanceModel>();

    for (Map.Entry<String, List<ProcessInstanceModel>> entry : map.entrySet()) {
      logger.info(entry.getKey() + " / " + entry.getValue().size());
      String variableString = buildGroupedVariableBody(entry.getValue());
      logger.info(variableString);

      Unirest.config().verifySsl(false);
      HttpResponse<String> response = Unirest.post(executorURI)
        .header("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOmFiY3pPM3haQ0xyTU42djJCS0sxZFhZRnBYbFBrY2NPRnFtMTJDZEFzTWdSVTRWck5aOWx5R1ZDR3VNREdJd1A=")
        .header("Content-Type", "application/json")
        .body(variableString)
        .asString();

      logger.info("response: ");
      logger.info(response.getBody());
      for (ProcessInstanceModel process: entry.getValue()) {
        logger.info("wiew1: ");
        process.variables = ProcessInstanceActivityManager
          .updateVariables(process.variables, response.getBody());
        resultProcesses.add(process);
        logger.info(String.valueOf(resultProcesses.size()));
      }
    }
    return resultProcesses;
  }

  private String buildGroupedVariableBody(List<ProcessInstanceModel> processes) {
    String body = "{\"content\":[";
    for (ProcessInstanceModel process: processes) {
      body += process.variables;
      body += ",";
    }
    // remove last ','
    body = body.substring(0, body.length() - 1);
    body += "]}";
    return body;
  }

  private Map<String, List<ProcessInstanceModel>> groupBy(
    List<ProcessInstanceModel> processes,
    String groupByVariable)
  {
    Map<String, List<ProcessInstanceModel>> map = new HashMap<String, List<ProcessInstanceModel>>();
    for (ProcessInstanceModel process: processes) {
      String value = ProcessInstanceActivityManager.getVariableValue(process, groupByVariable);
      // if the key is already there add the process to the list.
      if (map.containsKey(value)) {
        List<ProcessInstanceModel> instances = map.get(value);
        instances.add(process);
        map.replace(value, instances);
      } else {
        List<ProcessInstanceModel> instances = new ArrayList<ProcessInstanceModel>();
        instances.add(process);
        map.put(value, instances);
      }
    }
    return map;
  }

  public CompletableFuture<Map<String, List<Integer>>> updateStates(Map<String, List<Integer>> map) {
    List<String> queries = new ArrayList<String>();
    for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
      if (entry.getValue().size() > 0) {
        queries.add(constructStateQuery(entry.getKey(), entry.getValue()));
      }
    }

    if (queries.size() == 0) {
      return CompletableFuture.completedFuture(map);
    }

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          for (String sql : queries) {
            logger.info(sql);
            st.executeUpdate(sql);
          }
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
          System.out.println("Error: " + e.getSQLState());
        }
        return map;
      },
      executionContext
    );
  }

  private CompletableFuture<Void> updateStateToFinished(int batchClusterId) {
    return updateStates(BatchClusterState.FINISHED.getStateName(), batchClusterId);
  }

  private CompletableFuture<Void> updateStateToResumed(int batchClusterId) {
    return updateStates(BatchClusterState.RESUMED.getStateName(), batchClusterId);
  }

  private CompletableFuture<Void> updateStates(String state, int batchClusterId) {
    logger.info("updateStates - " + state);
    List<Integer> list = new ArrayList<Integer>();
    list.add(batchClusterId);
    final String sql = constructStateQuery(state, list);

    return CompletableFuture.runAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          logger.info(sql);
          st.executeUpdate(sql);
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
          System.out.println("Error: " + e.getSQLState());
        }
      },
      executionContext
    );
  }

  private String constructStateQuery(String state, List<Integer> ids) {
    String query = "UPDATE batch_cluster ";
    query += "SET state = '%s' ";
    query += "WHERE batch_cluster_id in (";
    for (int id : ids) {
      query += String.valueOf(id) + ",";
    }
    query = query.substring(0, query.length() - 1);
    query += ")";

    return String.format(query, state);
  }

}
