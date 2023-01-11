package workers;

import play.db.*;
import models.BatchCluster.BatchClusterState;
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


@Singleton
public class BatchClusterActivatorWorker {
  private Database db;
  private Timer timer;
  private BatchClusterActivatorContext executionContext;

  @Inject
    public BatchClusterActivatorWorker(
      Database db,
      BatchClusterActivatorContext executionContext
    ) {
      this.db = db;
      this.executionContext = executionContext;
      this.timer = new Timer();

      // run function every 60 seconds
      timer.schedule(new TimerTask() {
          public void run() {
             checkClusters();
          }
       }, 0, 60*1000);
  }

  private void checkClusters() {
      final String searchQuery = "SELECT "
        + "batch_cluster.batch_cluster_id, "
        + "batch_cluster.created_at, "
        + "batch_model.max_batch_size, "
        + "batch_model.activation_threshold_time, "
        + "COUNT(batch_cluster_instance.process_instance_id) as cluster_size "
        + " FROM batch_cluster"
        + " JOIN batch_cluster_instance"
        + " ON batch_cluster_instance.batch_cluster_id = batch_cluster.batch_cluster_id"
        + " JOIN batch_model"
        + " ON batch_model.batch_model_id = batch_cluster.batch_model_id"
        + " WHERE batch_cluster.state = '%s'"
        + " GROUP BY batch_cluster.batch_cluster_id";

    CompletableFuture.supplyAsync(
      () -> {
        List<Integer> batchClusterIds = new ArrayList<Integer>();
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(String.format(searchQuery, BatchClusterState.READY.getStateName()));

          while(rs.next()) {
            if(rs.getInt("cluster_size") >= rs.getInt("max_batch_size")) {
              batchClusterIds.add(rs.getInt("batchClusterId"));
              continue;
            }

            // TO-DO add check for time passed

          }

          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return batchClusterIds;
      },
      executionContext
    );
      // .thenApplyAsync(batchClusterIds -> System.out.println("batchClusterIds"));
  }
}
