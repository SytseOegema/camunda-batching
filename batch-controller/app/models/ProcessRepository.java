package models;

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
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.messages.ActivityDTO;
import io.camunda.batching.messaging.messages.ActivityType;

@Singleton
public class ProcessRepository {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public ProcessRepository(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
  }

  public CompletionStage<Optional<List<ProcessDTO>>> list() {
    final String sql = "SELECT "
      + "process.id AS process_id, "
      + "process.name AS process_name, "
      + "activity.id AS activity_id, "
      + "activity.name AS activity_name, "
      + "activity.activity_type AS activity_type "
      + " FROM process"
      + " LEFT JOIN process_activity"
      + " ON process_activity.process_id = process.id"
      + " LEFT JOIN activity"
      + " ON activity.id = process_activity.activity_id";

      // wel anders

    return CompletableFuture.supplyAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          Statement st = connection.createStatement();
          ResultSet rs = st.executeQuery(sql);
          HashMap<String, ProcessDTO> map = new HashMap<String, ProcessDTO>();

          while(rs.next()) {
            if(map.containsKey(rs.getString("process_id"))) {
              ProcessDTO process = map.get(rs.getString("process_id"));
              process.activities.add(new ActivityDTO(
                rs.getString("activity_id"),
                rs.getString("activity_name"),
                getType(rs.getString("activity_type"))));
              map.replace(rs.getString("process_id"), process);
            } else {
              List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
              activities.add(new ActivityDTO(
                  rs.getString("activity_id"),
                  rs.getString("activity_name"),
                  getType(rs.getString("activity_type"))));

              ProcessDTO process = new ProcessDTO(
                rs.getString("process_id"),
                rs.getString("process_name"),
                activities);

              map.put(rs.getString("process_id"), process);
            }
          }
          connection.close();
          return Optional.of(new ArrayList<ProcessDTO>(map.values()));
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

  private ActivityType getType(String type) {
    ActivityType activityType = ActivityType.UNSPECIFIED;
    switch(type) {
      case "serviceTask":
        activityType = ActivityType.SERVICE_TASK;
        break;
      case "receiveTask":
        activityType = ActivityType.RECEIVE_TASK;
        break;
      case "userTask":
        activityType = ActivityType.USER_TASK;
        break;
      case "manualTask":
        activityType = ActivityType.MANUAL_TASK;
        break;
      case "businessRuleTask":
        activityType = ActivityType.BUSINESS_RULE_TASK;
        break;
      case "scriptTask":
        activityType = ActivityType.SCRIPT_TASK;
        break;
      case "sendTask":
        activityType = ActivityType.SEND_TASK;
        break;
      default:
        activityType = ActivityType.UNSPECIFIED;
        break;
    }
    return activityType;
  }
}
