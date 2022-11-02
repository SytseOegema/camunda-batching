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
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.messages.ActivityDTO;

@Singleton
public class ProcessRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public ProcessRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<ProcessModel>>> list() {
    String sql = "SELECT id, name FROM process";

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);

          List<ProcessModel> processes = new ArrayList<ProcessModel>();
          while(rs.next()) {
            processes.add(new ProcessModel(rs.getString("id"), rs.getString("name")));
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

  public CompletionStage<Void> add(ProcessDTO process) {
    final String sql = String.format(
      "INSERT INTO process (id, name) VALUES ('%s', '%s')",
      process.id, process.name
    );

    return CompletableFuture.runAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          final Statement st = connection.createStatement();
          st.executeUpdate(sql);
          addActivities(connection, process.activities);
          addProcessActivities(connection, process.activities, process.id);
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return;
      },
      executionContext
    );
  }

  private void addActivities(Connection connection, List<ActivityDTO> activities) {
    String sql = "INSERT INTO activity (id, name, activity_type) VALUES ";
    String values = "('%s', '%s', '%s')";

    for (ActivityDTO activity : activities) {
      sql += String.format(values, activity.id, activity.name, activity.activityType.getElementTypeName());
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

  private void addProcessActivities(Connection connection, List<ActivityDTO> activities, String processId) {
    String sql = "INSERT INTO process_activity (process_id, activity_id) VALUES ";
    String values = "('%s', '%s')";

    for (ActivityDTO activity : activities) {
      sql += String.format(values, processId, activity.id);
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
}
