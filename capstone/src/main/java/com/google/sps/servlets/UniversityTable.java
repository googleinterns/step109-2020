package com.google.sps.servlets;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;




public class UniversityTable{

  /**
   * Connects to SQL database and then scans the csv to update the database
   * file with the entries in the csv files
   *
   */
  public void addTable(ServletContext servletContext) throws SQLException, FileNotFoundException {
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");

    if (pool == null) {
      System.out.println("Connection to SQL database not Working");
      return;
    }

    String path = System.getProperty("user.dir");
    path += "/../../src/main/java/com/google/sps/csv/Mock_University_Table.csv";

    if (!ifFileExist(path)) {
      System.out.println("Not Valid Path!");
      return;
    }

    File data = new File(path);
    Scanner dataScan = new Scanner(data);

    try (Connection conn = pool.getConnection()) {
      populateTable(conn, dataScan);
    }
  }

  public boolean ifFileExist(String path) {
    File data = new File(path);
    return data.exists();
  }

  /**
   * Loops through the csv file using a scanner to make each row in the csv file into a row in the university table
   *
   * @param conn the connection to the sql database
   * @param dataScan a scanner of the file
   */
  public void populateTable(Connection conn, Scanner dataScan)
    throws SQLException {
    while (dataScan.hasNext()) {
      String curLine = dataScan.nextLine();
      if (!curLine.contains(",")) {
        System.out.println("Line does not have comma");
        continue;
      }
      String[] arrayResponse = curLine.split(",", 2);
      if (arrayResponse.length != 2) {
        System.out.println("The array length is incorrect");
        continue;
      }
      String university = arrayResponse[0];
      String state = arrayResponse[1];
      String stmt = String.format(
        "INSERT INTO UNIVERSITY (name, state) Values('%1$s', '%2$s');",
        university,
        state
      );

      try (PreparedStatement addRowStatement = conn.prepareStatement(stmt);) {
        addRowStatement.execute();
      }
    }
  }

}
