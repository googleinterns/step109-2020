// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  private final String URL_TO_REDIRECT_AFTER_USER_LOGS_IN = "/login";

  /**
   * This method is called when a GET request is sent to the "/users" endpoint
   * @param request A HttpServletRequest object that holds the information sent by the user.
   * @param response A HttpServletResponse object that holds the information that will be sent back to the user.
   * @return void
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    HashMap<String, String> userDetails = new HashMap<>();
    if (isUserLoggedIn) {
      response.sendRedirect("/validate_email");
    } else {
      response.setStatus(401);
      userDetails.put(
        "loginUrl",
        userService.createLoginURL(URL_TO_REDIRECT_AFTER_USER_LOGS_IN)
      );
      String userResult = new Gson().toJson(userDetails);
      response.getWriter().println(userResult);
    }
  }
}
