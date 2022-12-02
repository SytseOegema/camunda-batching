package models.BatchActivityConnector;

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
public class BatchActivityConnectorRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public BatchActivityConnectorRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<BatchActivityConnectorModel>>> list() {
    final String sql = "SELECT "
      + "batch_activity_connector_condition.condition_id, "
      + "batch_activity_connector_condition.field_name, "
      + "batch_activity_connector_condition.field_type, "
      + "batch_activity_connector_condition.compare_operator, "
      + "batch_activity_connector_condition.compare_value, "
      + "batch_activity_connector.connector_id, "
      + "batch_activity_connector.active, "
      + "batch_activity_connector.validity, "
      + "batch_activity_connector.activity_id, "
      + "batch_activity_connector.batch_model_id "
      + " FROM batch_activity_connector"
      + " LEFT JOIN batch_activity_connector_condition ON"
      + " batch_activity_connector_condition.connector_id = batch_activity_connector.connector_id";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          HashMap<Integer, BatchActivityConnectorModel> map = new HashMap<Integer, BatchActivityConnectorModel>();
          List<BatchActivityConnectorConditionModel> conditions = new ArrayList<BatchActivityConnectorConditionModel>();
          while(rs.next()) {
            map.putIfAbsent(rs.getInt("connector_id"),
              new BatchActivityConnectorModel(
                rs.getInt("connector_id"),
                rs.getBoolean("active"),
                rs.getObject("validity",LocalDateTime.class)
                  .toString(),
                new ArrayList<BatchActivityConnectorConditionModel>(),
                rs.getString("activity_id"),
                rs.getInt("batch_model_id")
              )
            );

            if (rs.getInt("condition_id") != 0) {
              conditions.add(
                new BatchActivityConnectorConditionModel(
                  rs.getInt("condition_id"),
                  rs.getInt("connector_id"),
                  rs.getString("field_name"),
                  rs.getString("field_type"),
                  rs.getString("compare_operator"),
                  rs.getString("compare_value")
                )
              );
            }
          }

          for (BatchActivityConnectorConditionModel condition: conditions) {
            BatchActivityConnectorModel connector = map.get(condition.connectorId);
            connector.conditions.add(condition);
            map.replace(condition.connectorId, connector);
          }

          connection.close();
          return Optional.of(new ArrayList<BatchActivityConnectorModel>(map.values()));
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletableFuture<Optional<List<BatchActivityConnectorModel>>> listByActivityId(String activityId) {
    final String sql = "SELECT "
      + "batch_activity_connector_condition.condition_id, "
      + "batch_activity_connector_condition.field_name, "
      + "batch_activity_connector_condition.field_type, "
      + "batch_activity_connector_condition.compare_operator, "
      + "batch_activity_connector_condition.compare_value, "
      + "batch_activity_connector.connector_id, "
      + "batch_activity_connector.active, "
      + "batch_activity_connector.validity, "
      + "batch_activity_connector.activity_id, "
      + "batch_activity_connector.batch_model_id "
      + " FROM batch_activity_connector"
      + " LEFT JOIN batch_activity_connector_condition ON"
      + " batch_activity_connector_condition.connector_id = batch_activity_connector.connector_id"
      + " WHERE batch_activity_connector.activity_id = '%s'";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(String.format(sql, activityId));

          HashMap<Integer, BatchActivityConnectorModel> map = new HashMap<Integer, BatchActivityConnectorModel>();
          List<BatchActivityConnectorConditionModel> conditions = new ArrayList<BatchActivityConnectorConditionModel>();
          while(rs.next()) {
            map.putIfAbsent(rs.getInt("connector_id"),
              new BatchActivityConnectorModel(
                rs.getInt("connector_id"),
                rs.getBoolean("active"),
                rs.getObject("validity",LocalDateTime.class)
                  .toString(),
                new ArrayList<BatchActivityConnectorConditionModel>(),
                rs.getString("activity_id"),
                rs.getInt("batch_model_id")
              )
            );

            if (rs.getInt("condition_id") != 0) {
              conditions.add(
                new BatchActivityConnectorConditionModel(
                  rs.getInt("condition_id"),
                  rs.getInt("connector_id"),
                  rs.getString("field_name"),
                  rs.getString("field_type"),
                  rs.getString("compare_operator"),
                  rs.getString("compare_value")
                )
              );
            }
          }

          for (BatchActivityConnectorConditionModel condition: conditions) {
            BatchActivityConnectorModel connector = map.get(condition.connectorId);
            connector.conditions.add(condition);
            map.replace(condition.connectorId, connector);
          }

          connection.close();
          return Optional.of(new ArrayList<BatchActivityConnectorModel>(map.values()));
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return Optional.empty();
      },
      executionContext
    );
  }

  public CompletionStage<Boolean> add(BatchActivityConnectorModel connector) {
    String query = "INSERT INTO batch_activity_connector  (";
    query += "active ,";
    query += "validity, ";
    query += "activity_id, ";
    query += "batch_model_id ";
    query += ") VALUES ('%b', '%s', '%s', '%d') ";
    query += "RETURNING connector_id";
    final String sql = String.format(query,
      connector.active,
      connector.validity,
      connector.activityId,
      connector.batchModelId);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);
          while(rs.next()) {
            addConditions(
              connection,
              rs.getInt("connector_id"),
              connector.conditions);
          }
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

  private void addConditions(Connection connection, int connectorId, List<BatchActivityConnectorConditionModel> conditions) {
    String sql = "INSERT INTO batch_activity_connector_condition (";
    sql += "connector_id, field_name, field_type, compare_operator, compare_value) VALUES ";
    String values = "('%d','%s', '%s', '%s', '%s')";

    for (BatchActivityConnectorConditionModel condition : conditions) {
      sql += String.format(
        values,
        connectorId,
        condition.fieldName,
        condition.fieldType.value(),
        condition.compareOperator.value(),
        condition.compareValue);
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

  public CompletionStage<Boolean> update(BatchActivityConnectorModel connector) {
    String query = "UPDATE batch_activity_connector ";
    query += "SET active = %b, ";
    query += "validity = '%s', ";
    query += "activity_id = '%s', ";
    query += "batch_model_id = %d ";
    query += "WHERE connector_id = %d";
    final String sql = String.format(
      query,
      connector.active,
      connector.validity,
      connector.activityId,
      connector.batchModelId,
      connector.connectorId);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
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

  public CompletionStage<Boolean> updateConditions(BatchActivityConnectorConditionModel condition) {
    String query = "UPDATE batch_activity_connector_condition ";
    query += "SET field_name = '%s', ";
    query += "field_type = '%s', ";
    query += "compare_operator = '%s', ";
    query += "compare_value = '%s' ";
    query += "WHERE condition_id = %d";
    final String sql = String.format(
      query,
      condition.fieldName,
      condition.fieldType,
      condition.compareOperator,
      condition.compareValue,
      condition.conditionId);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
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

  public CompletionStage<Boolean> delete(int connectorId) {
    String query = "DELETE FROM batch_activity_connector_condition ";
    query += "WHERE connector_id = %d";
    final String sql1 = String.format(query, connectorId);

    String query2 = "DELETE FROM batch_activity_connector ";
    query2 += "WHERE connector_id = %d";
    final String sql2 = String.format(query2, connectorId);


    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          st.executeUpdate(sql1);
          st.executeUpdate(sql2);
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
}
