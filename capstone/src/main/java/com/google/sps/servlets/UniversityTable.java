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

@WebServlet("/UniTable")
public class UniversityTable
  extends HttpServlet
  implements ServletContextListener {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      addTable();
    } catch (SQLException Exception) {
      System.out.println("Could not add to Table");
    }
  }

  /**
   * Connects to SQL database and then scans the csv to update the database
   * file with the entries in the csv files
   *
   */
  public void addTable() throws SQLException, FileNotFoundException {
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");

    if (pool == null) {
      System.out.println("Not Working");
      return;
    }

    try (Connection conn = pool.getConnection()) {
      String path = System.getProperty("user.dir");
      path += "/../../src/main/java/com/google/sps/csv/Mock_University_Table.csv";

      if (!isValidPath(path)) {
        System.out.println("Not Valid Path!");
        return;
      }

      File data = new File(path);
      Scanner dataScan = new Scanner(data);

      populateTable(conn, dataScan);
    }
  }

  public boolean isValidPath(String path) {
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
      } else {
        String[] arrayResponse = curLine.split(",", 2);
        String University = arrayResponse[0];
        String State = arrayResponse[1];
        String stmt = String.format("INSERT INTO UNIVERSITY (name, state) Values('%1$s', '%2$s');", University, State);

        try (PreparedStatement addRowStatement = conn.prepareStatement(stmt);) {
          addRowStatement.execute();
        }
      }
    }
  }
  
}
