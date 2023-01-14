package workers;

import play.db.*;
import models.BatchCluster.BatchClusterState;
import models.ProcessInstanceModel;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BatchClusterActivatorWorker {
  private Database db;
  private Timer timer;
  private BatchClusterActivatorContext executionContext;
  private BatchClusterInvoker invoker;
  private Logger logger = LoggerFactory.getLogger("BatchClusterActivatorWorker");


  @Inject
    public BatchClusterActivatorWorker(
      Database db,
      BatchClusterActivatorContext executionContext,
      BatchClusterInvoker invoker
    ) {
      this.db = db;
      this.executionContext = executionContext;
      this.invoker = invoker;
      this.timer = new Timer();

      // run function every 60 seconds
      timer.schedule(new TimerTask() {
          public void run() {
             checkClusters();
          }
       }, 60*1000, 60*1000);
  }

  private void checkClusters() {
    logger.info("checkClusters()");
    final String searchQuery = "SELECT "
      + "batch_cluster.batch_cluster_id, "
      + "batch_cluster.created_at, "
      + "batch_model.max_batch_size, "
      + "batch_model.activation_threshold_cases, "
      + "batch_model.activation_threshold_time, "
      + "COUNT(batch_cluster_instance.process_instance_id) as cluster_size "
      + " FROM batch_cluster"
      + " JOIN batch_cluster_instance"
      + " ON batch_cluster_instance.batch_cluster_id = batch_cluster.batch_cluster_id"
      + " JOIN batch_model"
      + " ON batch_model.batch_model_id = batch_cluster.batch_model_id"
      + " WHERE batch_cluster.state = '%s'"
      + " GROUP BY batch_cluster.batch_cluster_id, batch_model.max_batch_size, batch_model.activation_threshold_cases, batch_model.activation_threshold_time";

    try {
      logger.info("try");

      CompletableFuture.supplyAsync(
      () -> {
        List<Integer> continueClusterIds = new ArrayList<Integer>();
        List<Integer> resumeClusterIds = new ArrayList<Integer>();
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(String.format(searchQuery, BatchClusterState.READY.getStateName()));
          while(rs.next()) {
            if(rs.getInt("cluster_size") >= rs.getInt("max_batch_size")) {
              System.out.println("now add batch cluster id" + rs.getInt("batch_cluster_id"));
              continueClusterIds.add(rs.getInt("batch_cluster_id"));
              continue;
            }


            Timestamp deadline = rs.getTimestamp("created_at");
            //convert minutes to milliseconds
            int extraTime = rs.getInt("activation_threshold_time") * 60 * 1000;
            deadline.setTime(deadline.getTime() + extraTime);
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // check if batchmodel deadline has passed.
            if (now.after(deadline)) {
              System.out.println("now after deadline");
              if(rs.getInt("cluster_size") >= rs.getInt("activation_threshold_cases")) {
                System.out.println("now add batch cluster id" + rs.getInt("batch_cluster_id"));
                continueClusterIds.add(rs.getInt("batch_cluster_id"));
              } else {
                resumeClusterIds.add(rs.getInt("batch_cluster_id"));
              }
            }
          }

          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
        map.put(BatchClusterState.EXECUTING.getStateName(), continueClusterIds);
        map.put(BatchClusterState.RESUMING.getStateName(), resumeClusterIds);
        return map;
      })
        .thenCompose(invoker::updateStates)
        .thenCompose(invoker::invokeBatchClusters)
        .get();
    } catch (Exception e) {
      e.printStackTrace();
      logger.info("lekker een catch.");
    }
    logger.info("einde.");
  }
}
