package models.BatchCluster;

import models.DatabaseExecutionContext;
import models.BatchModel.BatchModelModel;

import com.google.inject.AbstractModule;

import javax.inject.*;

import play.db.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Singleton
public class BatchClusterRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public BatchClusterRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<BatchClusterModel>>> list() {
    final String sql = "SELECT "
      + "batch_cluster.batch_cluster_id, "
      + "batch_cluster.batch_model_id, "
      + "batch_cluster.created_at, "
      + "batch_cluster_instance.process_instance_key, "
      + " FROM batch_cluster"
      + " LEFT JOIN batch_cluster_instance ON"
      + " batch_cluster_instance.batch_cluster_id = batch_cluster.batch_cluster_id";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          HashMap<Integer, BatchClusterModel> map = new HashMap<Integer, BatchClusterModel>();
          List<BatchClusterInstanceModel> instances = new ArrayList<BatchClusterInstanceModel>();
          while(rs.next()) {
            map.putIfAbsent(rs.getInt("batch_cluster_id"),
              new BatchClusterModel(
                rs.getInt("batch_cluster_id"),
                rs.getInt("batch_model_id"),
                new ArrayList<BatchClusterInstanceModel>(),
                rs.getObject("created_at",LocalDateTime.class)
                  .toString()
              )
            );

            if (rs.getLong("process_instance_key") != 0) {
              instances.add(
                new BatchClusterInstanceModel(
                  rs.getInt("batch_cluster_id"),
                  rs.getLong("process_instance_key")
                )
              );
            }
          }

          for (BatchClusterInstanceModel instance: instances) {
            BatchClusterModel cluster = map.get(instance.batchClusterId);
            cluster.instances.add(instance);
            map.replace(instance.batchClusterId, cluster);
          }

          connection.close();
          return Optional.of(new ArrayList<BatchClusterModel>(map.values()));
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  // public CompletionStage<Optional<List<BatchClusterModel>>> get(int batchModelId) {
  //   final String sql = "SELECT "
  //     + "batch_cluster.batch_cluster_id, "
  //     + "batch_cluster.batch_model_id, "
  //     + "batch_cluster.created_at, "
  //     + "batch_cluster_instance.process_instance_key, "
  //     + " FROM batch_cluster"
  //     + " LEFT JOIN batch_cluster_instance ON"
  //     + " batch_cluster_instance.batch_cluster_id = batch_cluster.batch_cluster_id";
  //     + " WHERE batch_cluster.batch_cluster_id = '%s'";
  //
  //   return CompletableFuture.supplyAsync(
  //     () -> {
  //       try {
  //         Connection connection = db.getConnection();
  //         Statement st = connection.createStatement();
  //         ResultSet rs = st.executeQuery(sql);
  //
  //         HashMap<Integer, BatchClusterModel> map = new HashMap<Integer, BatchClusterModel>();
  //         List<Long> instances = new ArrayList<BatchClusterInstanceModel>();
  //         while(rs.next()) {
  //           map.putIfAbsent(rs.getInt("batch_cluster_id"),
  //             new BatchActivityConnectorModel(
  //               rs.getInt("batch_cluster_id"),
  //               rs.getInt("batch_model_id"),
  //               rs.getObject("created_at",LocalDateTime.class)
  //                 .toString(),
  //               new ArrayList<BatchClusterInstanceModel>()
  //             )
  //           );
  //
  //           if (rs.getLong("process_instance_key") != 0) {
  //             instances.add(
  //               new BatchClusterInstanceModel(
  //                 rs.getInt("batch_cluster_id"),
  //                 rs.getLong("process_instance_key")
  //               )
  //             );
  //           }
  //         }
  //
  //         for (BatchClusterInstanceModel instance: instances) {
  //           BatchClusterModel cluster = map.get(instance.batchClusterId);
  //           cluster.instances.add(instance);
  //           map.replace(instance.batchClusterId, cluster);
  //         }
  //
  //         connection.close();
  //         return Optional.of(new ArrayList<BatchActivityConnectorModel>(map.values()));
  //       } catch(SQLException e) {
  //         e.printStackTrace();
  //       }
  //       return Optional.empty();
  //     },
  //     executionContext
  //   );
  // }

  public CompletionStage<Boolean> addInstanceToCluster(
      BatchModelModel model,
      Long processInstanceKey
  ) {
    final String query = "INSERT INTO batch_cluster_instance  ("
    + "batch_cluster_id ,"
    + "process_instance_key "
    + ") VALUES ('%d', '%d') ";


    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          int batchClusterId = getBachClusterId(connection, model);
          String sql = String.format(query,
            batchClusterId,
            processInstanceKey);

          final Statement st = connection.createStatement();
          st.executeUpdate(sql);
          connection.close();
          return true;
        } catch(SQLException e) {
          e.printStackTrace();
          System.out.println("Error: " + e.getSQLState());
        }
        return false;
      },
      executionContext
    );
  }

  private int getBachClusterId(Connection connection, BatchModelModel model) {
    final String searchQuery = "SELECT TOP 1"
      + "batch_cluster.batch_cluster_id, "
      + "COUNT(batch_cluster_instance.process_instance_key) as 'cluster_size', "
      + "created_at, "
      + " FROM batch_cluster"
      + " JOIN batch_cluster_instance"
      + " ON batch_cluster_instance.batch_cluster_id = batch_cluster.batch_cluster_id"
      + " WHERE batch_model_id = '%d'"
      + " GROUP BY batch_cluster.batch_cluster_id"
      + " ORDER BY batch_cluster_id ASC";

    final String createQuery = "INSERT INTO batch_cluster  ("
      + "batch_model_id, "
      + "created_at "
      + ") VALUES ('%d', '%s') "
      + "RETURNING batch_cluster_id";

    try {
      Statement st = connection.createStatement();
      ResultSet rs = st.executeQuery(String.format(searchQuery, model.batchModelId));
      while(rs.next()) {
        if(rs.getInt("cluster_size") < model.maxBatchSize) {
          return rs.getInt("batch_cluster_id");
        }
      }

      rs = st.executeQuery(String.format(createQuery, model.batchModelId, System.currentTimeMillis()));
      while(rs.next()) {
        return rs.getInt("batch_cluster_id");
      }
      return 0;
    } catch(SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }
}
