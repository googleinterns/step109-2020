// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

//import statements for the Users API
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
//import statements for SQL Interaction
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/validate_email")
public class ValidateEmailServlet extends HttpServlet {
  private final String GET_USER_ROW_QUERY =
    "SELECT user_info.id, user_info.email,  user_info.full_name, " +
    "user_info.user_name, university.name " +
    "FROM user_info JOIN university ON university.id = user_info.university_id " +
    "WHERE user_info.email = ?";

  private final String URL_TO_REDIRECT_AFTER_USER_LOGS_OUT = "/";
  private final String URL_TO_REDIRECT_AFTER_USER_LOGS_IN = "/login";

  /**
   * This method checks if the email supplied is a student email by confirming the '.edu' tld.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param get_user_row_query This is an SQL statement to query our database for the needed user's information.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   */
  public void checkForStudentEmail(
    DataSource pool,
    UserService userService,
    String get_user_row_query,
    HttpServletResponse response,
    HttpServletRequest request
  )
    throws SQLException, IOException {
    String email = userService.getCurrentUser().getEmail();
    String logoutUrl = userService.createLogoutURL(
      URL_TO_REDIRECT_AFTER_USER_LOGS_OUT
    );
    if (!email.endsWith(".edu")) {
      response.setStatus(412);
      response.sendRedirect(
        "/badLogin.html?error=requires-student-email&logoutUrl=" + logoutUrl
      );
      return;
    }
    checkIfUserIsAlreadyRegistered(
      pool,
      userService,
      get_user_row_query,
      response,
      request
    );
  }

  /**
   * This method checks if the current user is already in  our database, if not we redirect them to fill in their info.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param get_user_row_query This is an SQL statement to query our database for the needed user's information.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @return void.
   */
  public void checkIfUserIsAlreadyRegistered(
    DataSource pool,
    UserService userService,
    String get_user_row_query,
    HttpServletResponse response,
    HttpServletRequest request
  )
    throws SQLException, IOException {
    String email = userService.getCurrentUser().getEmail();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    try (
      Connection conn = pool.getConnection();
      PreparedStatement queryStatement = conn.prepareStatement(
        get_user_row_query
      )
    ) {
      queryStatement.setString(1, email);
      ResultSet result = queryStatement.executeQuery();
      if (result.next() == false) {
        response.setStatus(303);
        response.sendRedirect("/initialLogin.html?email=" + email);
        return;
      }
      response.setStatus(200);
      response.sendRedirect("/dashboard.html");
    }
  }

  /**
   * This method is called when a GET request is sent to the "/users" endpoint
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @return void
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    response.setContentType("application/json");
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
    }
    try {
      checkForStudentEmail(
        pool,
        userService,
        GET_USER_ROW_QUERY,
        response,
        request
      );
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
  }
}
