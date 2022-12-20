package com.example.springproject.github;

import com.example.springproject.config.Druid;
import com.example.springproject.domain.Commit;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class commitsPipeline implements Pipeline {
  public static Connection con = null;
  public static long tot = 0;
  public static PreparedStatement githubRepo_stmt = null;
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  public Lock balanceChangeLock = new ReentrantLock();

  public static void openDB(String dbname) {
    try {
      //
      Class.forName("org.postgresql.Driver");
    } catch (Exception e) {
      System.err.println("Cannot find the Mysql driver. Check CLASSPATH.");
      System.exit(1);
    }

    try {
      con = Druid.getConnection();
      con.setAutoCommit(false);
    } catch (SQLException e) {
      System.err.println("Database connection failed");
      System.err.println(e.getMessage());
      System.exit(1);
    }


    try {
      githubRepo_stmt = con.prepareStatement("insert into commit values(?,?,?)");
    } catch (SQLException e) {
      System.err.println("Insert statement failed");
      System.err.println(e.getMessage());
      closeDB();
      System.exit(1);
    }
  }

  public static void commit() {
    try {
      con.commit();
    } catch (SQLException throwables) {
      System.err.println("SQL error: " + throwables.getMessage());
      try {
        con.rollback();
      } catch (Exception e2) {
      }
      closeDB();
      System.exit(1);
    }
  }

  public static void closeDB() {
    if (con != null) {
      try {
        if (githubRepo_stmt != null) {
          githubRepo_stmt.close();
        }
        Druid.closeAll(con);
      } catch (Exception e) {
        // Forget about it
      }
    }
  }

  public static void load_Githubrepo(Commit tmp) {
    tot++;
    try {
      if (con != null) {
        githubRepo_stmt.setLong(1, tot);
        githubRepo_stmt.setString(2, tmp.getRepoName());
        githubRepo_stmt.setDate(3, tmp.getTime());
        githubRepo_stmt.addBatch();
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  @Override
  public void process(ResultItems resultItems, Task task) {
    balanceChangeLock.lock();
    try {
      openDB("cs209_project");
      List<Commit> commits = resultItems.get("commits");
      commits.stream().forEach(o -> load_Githubrepo(o));
      try {
        githubRepo_stmt.executeBatch();
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
      commit();
      closeDB();
    } finally {
      balanceChangeLock.unlock();
    }

  }
}
