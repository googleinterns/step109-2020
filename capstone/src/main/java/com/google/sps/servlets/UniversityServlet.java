/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Card;
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

@WebServlet("/university")
public class UniversityServlet extends HttpServlet {
    private static final String SEARCH_UNIVERSITY_TABLE_SQL_STATEMENT = "SELECT * FROM university;";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setContentType("application/json;");
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");

    String uniArrayResult;
    try {
      ArrayList<HashMap<String, String>> universityArray = getUniArray(
        pool,
        response
      );
      uniArrayResult = new Gson().toJson(universityArray);
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
    response.getWriter().println(uniArrayResult);
  }

  public ArrayList<HashMap<String, String>> getArrays(
    DataSource pool,
    HttpServletResponse response
  )
    throws SQLException, IOException {
    ArrayList<HashMap<String, String>> returnArray = new ArrayList<HashMap<String, String>>();
    try (Connection conn = pool.getConnection()) {
      String getUniTable = SEARCH_UNIVERSITY_TABLE_SQL_STATEMENT;
      PreparedStatement uniStatement = conn.prepareStatement(getUniTable);
      ResultSet result = uniStatement.executeQuery();

      while (result.next()) {
        HashMap<String, String> University = new HashMap<>();
        University.put("id", result.getString("id"));
        University.put("name", result.getString("name"););
        University.put("state", result.getString("state"));
        returnArray.add(University);
      }
      
      return returnArray;
    } catch (SQLException ex) {
      throw new RuntimeException("Unable to verify Connection", ex);
    }
  }
}
