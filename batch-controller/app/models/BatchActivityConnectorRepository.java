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
      + "connector_id ,"
      + "active ,"
      + "validity, "
      + "activity_id, "
      + "batchModel_id "
      + " FROM batch_activity_connector";

      // TODO make join in query such that also connection properties are returned
      // then use a hashset to get the unique connectors and therafter add the
      // conditions to the connectors in the hashset.

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          List<BatchActivityConnectorModel> connectors = new ArrayList<BatchActivityConnectorModel>();
          while(rs.next()) {
            connectors.add(
              new BatchActivityConnectorModel(
                rs.getInt("connector_id"),
                rs.getBoolean("active"),
                rs.getObject("validity",LocalDateTime.class)
                  .toString(),
                new ArrayList<BatchActivityConnectorConditionModel>(),
                rs.getString("activity_id"),
                rs.getInt("batchModel_id")));
          }
          connection.close();
          return Optional.of(connectors);
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
    query += "batchModel_id ";
    query += ") VALUES ('%b', '%s', '%s', '%d') ";
    query += "RETURNING connector_id";
    final String sql = String.format(
      query,
      connector.active,
      connector.validity,
      connector.activityId,
      connector.batchModelId);
    System.out.println(sql);

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);
          while(rs.next()) {
            System.out.println("jajaajajaja " + rs.getInt("connector_id"));
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
        condition.connectorId,
        condition.fieldName,
        condition.fieldType.value(),
        condition.compareOperator.value(),
        condition.compareValue);
      sql += ",";
    }
    // remove last ',' behind the final values from the query
    sql = sql.substring(0, sql.length() - 1);
    System.out.println(sql);

    try {
      Statement st = connection.createStatement();
      st.executeUpdate(sql);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }
}
