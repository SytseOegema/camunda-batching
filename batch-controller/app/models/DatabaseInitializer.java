package models;

import com.google.inject.AbstractModule;

import javax.inject.*;

import play.db.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DatabaseInitializer {
  private Database db;
  private DatabaseExecutionContext executionContext;

  @Inject
  public DatabaseInitializer(Database db, DatabaseExecutionContext executionContext) {
    this.db = db;
    this.executionContext = executionContext;
    init();
  }

  public CompletionStage<Void> init() {
    return CompletableFuture.runAsync(
      () -> {
        try {
          Connection connection = db.getConnection();
          assertTablesArePresent(connection);
          connection.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
        return;
      },
      executionContext
    );
  }

  private void assertTablesArePresent(Connection connection) {
    List<String> tables = new ArrayList<String>();
    tables.add("process");
    tables.add("process_activity");
    tables.add("activity");
    String sql = "SELECT tablename FROM pg_tables " +
      "where tablename in ("  +
      "'process'," +
      "'process_activity'," +
      "'activity'" +
      ")";

    try {
      Statement st = connection.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while(rs.next()) {
        int idx = tables.indexOf(rs.getString("tablename").toLowerCase());
        if(idx != -1) {
          tables.remove(idx);
        }
      }

      for (String table : tables) {
        switch (table) {
          case "process":
              createProcessesTable(connection);
            break;
          case "activity":
              createActivitiesTable(connection);
            break;
          case "process_activity":
              createProcessActivitiesTable(connection);
            break;
          default:
            break;
        }
      }
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  private void createProcessesTable(Connection connection) {
    String sql = "CREATE TABLE process " +
      "(id VARCHAR(255) not NULL, " +
      " name VARCHAR(255), " +
      " PRIMARY KEY ( id ))";
    try {
      Statement st = connection.createStatement();
      st.executeQuery(sql);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  private void createActivitiesTable(Connection connection) {
    String sql = "CREATE TABLE activity " +
      "(id VARCHAR(255) not NULL, " +
      " name VARCHAR(255), " +
      " activity_type VARCHAR(255), " +
      " PRIMARY KEY ( id ))";
    try {
      Statement st = connection.createStatement();
      st.executeQuery(sql);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }

  private void createProcessActivitiesTable(Connection connection) {
    String sql = "CREATE TABLE process_activity " +
      "(process_id VARCHAR(255) not NULL, " +
      " activity_id VARCHAR(255), " +
      " PRIMARY KEY ( process_id, activity_id ))";
    try {
      Statement st = connection.createStatement();
      st.executeQuery(sql);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }
}
