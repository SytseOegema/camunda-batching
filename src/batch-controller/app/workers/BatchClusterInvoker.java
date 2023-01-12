package workers;

import play.db.*;
import managers.ProcessInstanceActivityManager;
import managers.ProcessInstanceFlowManager;
import models.BatchCluster.BatchClusterState;
import models.ProcessInstanceModel;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
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

  public CompletableFuture<Void> invokeBatchCluster(List<Integer> batchClusterIds) {
    if (batchClusterIds.size() == 0) {
      return CompletableFuture.completedFuture(null);
    }
    int batchClusterId = batchClusterIds.get(0);
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
      + "batch_executor_URI"
      + " FROM batch_model"
      + " JOIN batch_cluster"
      + " ON batch_cluster.batch_model_id = batch_model.batch_model_id"
      + " WHERE batch_cluster.batch_cluster_id = %d";

    return CompletableFuture.runAsync(
      () -> {
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
          rs = st.executeQuery(String.format(sql2, batchClusterId));
          while(rs.next()) {
            executorURI = rs.getString("batch_executor_URI");
          }
          connection.close();

          logger.info("before loop!");
          for (ProcessInstanceModel process: processes) {
            logger.info("go with process instance " + process.processInstanceKey);
            // Unirest.config().verifySsl(false);
            // HttpResponse<String> response = Unirest.post(executorURI)
            //   .header("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOmFiY3pPM3haQ0xyTU42djJCS0sxZFhZRnBYbFBrY2NPRnFtMTJDZEFzTWdSVTRWck5aOWx5R1ZDR3VNREdJd1A=")
            //   .header("Content-Type", "application/json")
            //   .body(process.variables)
            //   .asString();
            //
            // logger.info(response.getBody());
            // process.variables = ProcessInstanceActivityManager
            //   .updateVariables(process.variables, response.getBody());


            process.variables = ProcessInstanceActivityManager
              .updateVariables(process.variables, "{\"result\":\"mooi\"}");

            logger.info("process.variables");
            logger.info(process.variables);
            ProcessInstanceFlowManager.continueBatchActivityFlow(process);
          }
        } catch(SQLException e) {
          e.printStackTrace();
        }
      },
      executionContext
    );
  }
}
