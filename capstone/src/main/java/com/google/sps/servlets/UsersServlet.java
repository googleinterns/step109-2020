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

@WebServlet("/users/*")
public class UsersServlet extends HttpServlet {
  private final String CREATE_NEW_USER_QUERY =
    "INSERT INTO user_info (user_name, full_name, email, verified, university_id) VALUES (?, ?, ?, true, ?)";

  private final String URL_TO_REDIRECT_AFTER_USER_LOGS_OUT = "/";
  private final String URL_TO_REDIRECT_AFTER_USER_LOGS_IN = "/login";

  private final String GET_USER_ROW_QUERY =
    "SELECT user_info.id, user_info.email,  user_info.full_name, " +
    "user_info.user_name, university.name " +
    "FROM user_info JOIN university ON university.id = user_info.university_id " +
    "WHERE user_info.email = ?";

  /**
   * This method checks if the current user is already registered in our database.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @return Boolean
   */
  public Boolean IsUserRegistered(
    DataSource pool,
    UserService userService,
    HttpServletResponse response,
    HttpServletRequest request
  )
    throws SQLException, IOException {
    String email = userService.getCurrentUser().getEmail();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    try (
      Connection conn = pool.getConnection();
      PreparedStatement queryStatement = conn.prepareStatement(
        GET_USER_ROW_QUERY
      )
    ) {
      queryStatement.setString(1, email);
      ResultSet result = queryStatement.executeQuery();
      if (result.next() == false) {
        return false;
      }
      return true;
    }
  }

  /**
   * This method retrieves all the neccessary information associated with the current user from our database.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @return HashMap<String, String> saves a key-value pair of all neccessary user details derived.
   */
  public HashMap<String, String> retrieveExistingUserInfo(
    DataSource pool,
    UserService userService,
    HttpServletResponse response,
    HttpServletRequest request
  )
    throws SQLException, IOException {
    HashMap<String, String> userDetails = new HashMap<>();
    String email = userService.getCurrentUser().getEmail();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    try (
      Connection conn = pool.getConnection();
      PreparedStatement queryStatement = conn.prepareStatement(
        GET_USER_ROW_QUERY
      )
    ) {
      queryStatement.setString(1, email);
      ResultSet result = queryStatement.executeQuery();
      while (result.next()) {
        userDetails.put("user_status", Boolean.toString(isUserLoggedIn));
        userDetails.put("id", Integer.toString(result.getInt("id")));
        userDetails.put("email", email);
        userDetails.put("full_name", result.getString("full_name"));
        userDetails.put("user_name", result.getString("user_name"));
        userDetails.put("university", result.getString("name"));
        userDetails.put(
          "logoutUrl",
          userService.createLogoutURL(URL_TO_REDIRECT_AFTER_USER_LOGS_OUT)
        );
      }
      response.setStatus(200);
      return userDetails;
    }
  }

  /**
   * This method is called when a GET request is sent to the "/users/*" endpoint
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
    if (!request.getPathInfo().equals("/current")) {
      response.sendError(404, "Invalid url supplied by the client.");
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    Boolean IsUserRegistered;
    if (!isUserLoggedIn) {
      response.sendRedirect("/login");
      return;
    }
    try {
      IsUserRegistered = IsUserRegistered(pool, userService, response, request);
      if (!IsUserRegistered) {
        response.setStatus(300);
        return;
      }
    } catch (SQLException ex) {
      throw new RuntimeException("There is an error with your sql statement ... ", ex);
    }

    String userResult;
    try {
      HashMap<String, String> userDetails = retrieveExistingUserInfo(pool, userService, response, request);
      userResult = new Gson().toJson(userDetails);
    } catch (SQLException ex) {
      throw new RuntimeException("There is an error with your sql statement ... ", ex);
    }
    response.getWriter().println(userResult);
  }

  /**
   * This method checks if the university id provided by the client is valid i.e exists in our database.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @param resultParameters  A HashMap object that holds all the information sent in the "body" of the user's request.
   * @return Boolean
   */
  public Boolean checkUniversityIdExists(
    DataSource pool,
    UserService userService,
    HttpServletResponse response,
    HashMap<String, String> resultParameters
  )
    throws SQLException, IOException {
    Integer university_id = Integer.parseInt(
      resultParameters.get("university")
    );
    try (
      Connection conn = pool.getConnection();
      PreparedStatement queryStatement = conn.prepareStatement("SELECT * FROM university WHERE university.id = ?")
    ) {
      queryStatement.setInt(1, university_id);
      ResultSet result = queryStatement.executeQuery();
      if (result.next() == false) {
        response.sendError(403, "University's id sent by client does not exist.");
        return false;
      }
    }
    return true;
  }

  /**
   * This method saves the user's information in the request parameters to our user_info table.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param resultParameters  A HashMap object that holds all the information sent in the "body" of the user's request.
   * @param create_new_user_query A string object that contain the SQL query to create a new user in our database.
   * @return void
   */
  public void saveUserDetails(
    DataSource pool,
    UserService userService,
    HashMap<String, String> resultParameters,
    String create_new_user_query
  )
    throws SQLException {
    try (
      Connection conn = pool.getConnection();
      PreparedStatement queryStatement = conn.prepareStatement(
        create_new_user_query
      )
    ) {
      String user_email = userService.getCurrentUser().getEmail();
      queryStatement.setString(1, resultParameters.get("user_name"));
      queryStatement.setString(2, resultParameters.get("full_name"));
      queryStatement.setString(3, user_email);
      queryStatement.setInt(
        4,
        Integer.parseInt(resultParameters.get("university"))
      );
      queryStatement.execute();
    }
  }

  /**
   * This method is called when a POST request is sent to "/users" endpoint.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @return void
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.setContentType("application/json");
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    UserService userService = UserServiceFactory.getUserService();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    if (!isUserLoggedIn) {
      response.sendRedirect("/login");
      return;
    }
    String email = userService.getCurrentUser().getEmail();
    String logoutUrl = userService.createLogoutURL(
      URL_TO_REDIRECT_AFTER_USER_LOGS_OUT
    );
    if (!email.endsWith(".edu")) {
      response.sendRedirect("/badLogin.html?error=requires-student-email&logoutUrl=" + logoutUrl);
      return;
    }
    HashMap<String, String> resultParameters = new Gson().fromJson(request.getReader(), HashMap.class);
    Boolean isUniversityIdValid;
    try {
      isUniversityIdValid = checkUniversityIdExists(pool, userService, response, resultParameters);
    } catch (SQLException ex) {
      throw new RuntimeException("There is an error with your sql statement ... ", ex);
    }
    if (isUniversityIdValid) {
      try {
        saveUserDetails(pool, userService, resultParameters, CREATE_NEW_USER_QUERY);
      } catch (SQLException ex) {
        throw new RuntimeException("There is an error with your sql statement ... ", ex);
      }
    }
  }
}
