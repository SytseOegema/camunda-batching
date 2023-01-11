package models.BatchModel;

import models.DatabaseExecutionContext;
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
public class BatchModelRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public BatchModelRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<BatchModelModel>>> list() {
    final String sql = "SELECT "
      + "batch_model.batch_model_id, "
      + "batch_model.name, "
      + "batch_model.max_batch_size, "
      + "batch_model.execute_parallel, "
      + "batch_model.activation_threshold_cases, "
      + "batch_model.activation_threshold_time, "
      + "batch_model.batch_executor_URI, "
      + "batch_model_group_by.group_by_id, "
      + "batch_model_group_by.field_name "
      + " FROM batch_model"
      + " LEFT JOIN batch_model_group_by"
      + " ON batch_model_group_by.batch_model_id = batch_model.batch_model_id"
      + " ORDER BY group_by_id";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          HashMap<Integer, BatchModelModel> map = new HashMap<Integer, BatchModelModel>();
          BatchModelModel model;
          while(rs.next()) {
            int id = rs.getInt("batch_model_id");
            if(map.containsKey(id)) {
              model = map.get(id);
            } else {
              model = new BatchModelModel(
                id,
                rs.getString("name"),
                rs.getInt("max_batch_size"),
                rs.getBoolean("execute_parallel"),
                rs.getInt("activation_threshold_cases"),
                rs.getInt("activation_threshold_time"),
                rs.getString("batch_executor_URI"),
                new ArrayList<String>()
              );
            }

            if (rs.getInt("group_by_id") != 0) {
              model.groupBy.add(rs.getString("field_name"));
            }

            if (map.containsKey(id)) {
              map.replace(id, model);
            } else {
              map.put(id, model);
            }
          }
          connection.close();
          return Optional.of(new ArrayList<BatchModelModel>(map.values()));
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletionStage<Optional<BatchModelModel>> get(int batchModelId) {
    final String sql = "SELECT "
      + "batch_model.batch_model_id, "
      + "batch_model.name, "
      + "batch_model.max_batch_size, "
      + "batch_model.execute_parallel, "
      + "batch_model.activation_threshold_cases, "
      + "batch_model.activation_threshold_time, "
      + "batch_model.batch_executor_URI, "
      + "batch_model_group_by.group_by_id, "
      + "batch_model_group_by.field_name "
      + " FROM batch_model"
      + " LEFT JOIN batch_model_group_by"
      + " ON batch_model_group_by.batch_model_id = batch_model.batch_model_id"
      + " WHERE batch_model.batch_model_id = '%d'";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(String.format(sql, batchModelId));

          HashMap<Integer, BatchModelModel> map = new HashMap<Integer, BatchModelModel>();
          BatchModelModel model;
          while(rs.next()) {
            int id = rs.getInt("batch_model_id");

            if(map.containsKey(id)) {
              model = map.get(id);
            } else {
              model = new BatchModelModel(
                id,
                rs.getString("name"),
                rs.getInt("max_batch_size"),
                rs.getBoolean("execute_parallel"),
                rs.getInt("activation_threshold_cases"),
                rs.getInt("activation_threshold_time"),
                rs.getString("batch_executor_URI"),
                new ArrayList<String>()
              );
            }

            if (rs.getInt("group_by_id") != 0) {
              model.groupBy.add(rs.getString("field_name"));
            }

            if (map.containsKey(id)) {
              map.replace(id, model);
            } else {
              map.put(id, model);
            }
          }
          connection.close();
          return Optional.of(map.get(batchModelId));
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletionStage<Boolean> add(BatchModelModel model) {
    String query = "INSERT INTO batch_model  (";
    query += "name ,";
    query += "max_batch_size ,";
    query += "execute_parallel, ";
    query += "activation_threshold_cases, ";
    query += "activation_threshold_time, ";
    query += "batch_executor_URI ";
    query += ") VALUES ('%s', '%d', '%b', '%d', '%d', '%s') ";
    query += "RETURNING batch_model_id";
    final String sql = String.format(query,
      model.name,
      model.maxBatchSize,
      model.executeParallel,
      model.activationThresholdCases,
      model.activationThresholdTime,
      model.batchExecutorURI);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);
          while(rs.next()) {
            addGroupBy(
              connection,
              rs.getInt("batch_model_id"),
              model.groupBy);
          }
          connection.close();
          return true;
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return false;
      },
      executionContext
    );
  }

  private void addGroupBy(Connection connection, int batchModelId, List<String> fieldNames) {
    if (fieldNames.size() == 0) {
      return;
    }
    String sql = "INSERT INTO batch_model_group_by (";
    sql += "batch_model_id, field_name) VALUES ";
    String values = "('%d','%s')";

    for (String fieldName : fieldNames) {
      sql += String.format(
        values,
        batchModelId,
        fieldName);
      sql += ",";
    }
    // remove last ',' behind the final values from the query
    sql = sql.substring(0, sql.length() - 1);
    try {
      Statement st = connection.createStatement();
      st.executeUpdate(sql);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  public CompletionStage<Boolean> update(BatchModelModel model) {
    String query = "UPDATE batch_model ";
    query += "SET name = %s, ";
    query += "max_batch_size = %d, ";
    query += "execute_parallel = %b, ";
    query += "activation_threshold_cases = %d, ";
    query += "activation_threshold_time = %d, ";
    query += "batch_executor_URI = '%s' ";
    query += "WHERE batch_model_id = %d";
    final String sql = String.format(
      query,
      model.name,
      model.maxBatchSize,
      model.executeParallel,
      model.activationThresholdCases,
      model.activationThresholdTime,
      model.batchExecutorURI,
      model.batchModelId);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          st.executeUpdate(sql);
          updateGroupBy(connection, model);
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

  public Boolean updateGroupBy(Connection connection, BatchModelModel model) {
      if (!deleteGroupBy(connection, model.batchModelId)) {
        return false;
      }
      addGroupBy(connection, model.batchModelId, model.groupBy);
      return true;
  }

  public CompletionStage<Boolean> delete(int batchModelId) {
    String query = "DELETE FROM batch_model ";
    query += "WHERE batch_model_id = %d";
    final String sql = String.format(query, batchModelId);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          if (!deleteGroupBy(connection, batchModelId)) {
            connection.close();
            return false;
          }
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

  public boolean deleteGroupBy(Connection connection, int batchModelId) {
    String query = "DELETE FROM batch_model_group_by ";
    query += "WHERE batch_model_id = %d";
    final String sql = String.format(query, batchModelId);

    try {
      final Statement st = connection.createStatement();
      st.executeUpdate(sql);
      return true;
    } catch(SQLException e) {
      e.printStackTrace();
      System.out.println("Error: " + e.getSQLState());
    }
    return false;
  }
}
