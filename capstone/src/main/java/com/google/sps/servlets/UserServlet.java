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

@WebServlet("/users")
public class UserServlet extends HttpServlet {
  private final String get_user_row_query =
    "SELECT user_info.id, user_info.email,  user_info.full_name, " +
    "user_info.user_name, university.name FROM user_info JOIN university " +
    "ON university.id = user_info.university_id WHERE user_info.email = ?";

  private final String create_new_user_query =
    "INSERT INTO user_info (user_name, full_name, email, verified, university_id) VALUES (?, ?, ?, true, ?)";

  /**
   * This method gets the related information associated with a user based on the email provided by the userService parameter.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param get_user_row_query This is an SQL statement to query our database for the needed user's information.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @return HashMap<String, String> saves a key-value pair of all neccessary user details derived.
   */
  public HashMap<String, String> getUserDetails(
    DataSource pool,
    UserService userService,
    String get_user_row_query,
    HttpServletResponse response
  )
    throws SQLException, IOException {
    String urlToRedirectToAfterUserLogsOut = "";
    String urlToRedirectToAfterUserLogsIn = "";

    HashMap<String, String> userDetails = new HashMap<>();

    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    if (isUserLoggedIn) {
      String email = userService.getCurrentUser().getEmail();
      if (!email.endsWith(".edu")) {
        userDetails.put("status_code", "412");
        String logoutUrl = userService.createLogoutURL(
          urlToRedirectToAfterUserLogsOut
        );
        userDetails.put("logoutUrl", logoutUrl);
        return userDetails;
      }
      try (Connection conn = pool.getConnection()) {
        try (
          PreparedStatement queryStatement = conn.prepareStatement(
            get_user_row_query
          )
        ) {
          queryStatement.setString(1, email);
          ResultSet result = queryStatement.executeQuery();
          if (result.next() == false) {
            response.sendRedirect("/userDetails.html");
            userDetails.put("status_code", "307");
            return userDetails;
          } else {
            do {
              String user_status = Boolean.toString(isUserLoggedIn);
              userDetails.put("user_status", user_status);
              String id = Integer.toString(result.getInt("id"));
              userDetails.put("id", id);
              userDetails.put("email", email);
              String full_name = result.getString("full_name");
              userDetails.put("full_name", full_name);
              String user_name = result.getString("user_name");
              userDetails.put("user_name", user_name);
              String university = result.getString("name");
              userDetails.put("university", university);
              String logoutUrl = userService.createLogoutURL(
                urlToRedirectToAfterUserLogsOut
              );
              userDetails.put("logoutUrl", logoutUrl);
              userDetails.put("status_code", "200");
            } while (result.next());
            return userDetails;
          }
        }
      }
    } else {
      String user_status = Boolean.toString(isUserLoggedIn);
      userDetails.put("user_status", user_status);
      String loginUrl = userService.createLoginURL(
        urlToRedirectToAfterUserLogsIn
      );
      userDetails.put("loginUrl", loginUrl);
      userDetails.put("status_code", "401");
      return userDetails;
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
    try {
      HashMap<String, String> userDetails = getUserDetails(
        pool,
        userService,
        get_user_row_query,
        response
      );
      String userResult = new Gson().toJson(userDetails);
      response.getWriter().println(userResult);
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
  }

  /**
   * This method saves the user's information in the request parameters to our user_info table.
   * @param pool It is a DataSource object that serves to interact with our database connection.
   * @param userService This is a UserService object that holds information about the current user.
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @return void
   */
  public void saveUserDetails(
    DataSource pool,
    UserService userService,
    HttpServletRequest request
  )
    throws SQLException {
    try (Connection conn = pool.getConnection()) {
      try (
        PreparedStatement queryStatement = conn.prepareStatement(
          create_new_user_query
        )
      ) {
        String full_name = request.getParameter("full_name");
        String user_name = request.getParameter("user_name");
        Integer university_id = Integer.valueOf(
          request.getParameter("university")
        );
        String user_email = userService.getCurrentUser().getEmail();
        queryStatement.setString(1, user_name);
        queryStatement.setString(2, full_name);
        queryStatement.setString(3, user_email);
        queryStatement.setInt(4, university_id);
        queryStatement.execute();
      }
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
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    UserService userService = UserServiceFactory.getUserService();

    try {
      saveUserDetails(pool, userService, request);
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
  }
}
