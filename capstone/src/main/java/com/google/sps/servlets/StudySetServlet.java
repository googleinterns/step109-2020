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

@WebServlet(urlPatterns = { "/study_set/*" })
public class StudySetServlet extends HttpServlet {
    
  private final String SEARCH_SQL_STATEMENT =
    "SELECT COUNT(card.study_set_id), study_set.id, study_set.title, study_set.description, " +
    "study_set.subject, university.name, user_info.user_name FROM study_set JOIN university " +
    "ON university.id = study_set.university_id JOIN user_info ON study_set.owner_id = user_info.id " +
    "JOIN card ON card.study_set_id = study_set.id WHERE study_set.subject ILIKE ? OR university.name ILIKE ? OR " +
    "study_set.title ILIKE ? OR study_set.description ILIKE ? OR user_info.user_name ILIKE ? " +
    "GROUP BY study_set.id, university.id, user_info.id";

  private final String VIEW_STUDY_SET_QUERY =
    " SELECT card.front, card.back, study_set.title, study_set.description, study_set.subject, university.name, user_info.user_name " +
    " FROM study_set" +
    " JOIN university ON university.id = study_set.university_id" +
    " JOIN user_info ON study_set.owner_id = user_info.id" +
    " JOIN card ON card.study_set_id = study_set.id" +
    " WHERE study_set.id = ?" +
    " GROUP BY card.id, study_set.id, university.id, user_info.id;";

  public ArrayList<HashMap<String, String>> runSearchStudySetSqlQuery(
    DataSource pool,
    String searchWord
  )
    throws SQLException {
    ArrayList<HashMap<String, String>> studySets = new ArrayList<>();
    try (Connection conn = pool.getConnection()) {
      try (
        PreparedStatement queryStatement = conn.prepareStatement(
          SEARCH_SQL_STATEMENT
        )
      ) {
        long numberOfPlaceholders = SEARCH_SQL_STATEMENT
          .chars()
          .filter(ch -> ch == '?')
          .count();
        for (int i = 1; i <= numberOfPlaceholders; i++) {
          queryStatement.setString(i, "%" + searchWord + "%");
        }

        ResultSet result = queryStatement.executeQuery();
        while (result.next()) {
          HashMap<String, String> newEntry = new HashMap<>();
          String studySetLength = Integer.toString(result.getInt("count"));
          newEntry.put("study_set_length", studySetLength);
          String id = Integer.toString(result.getInt("id"));
          newEntry.put("id", id);
          String title = result.getString("title");
          newEntry.put("title", title);
          String description = result.getString("description");
          newEntry.put("description", description);
          String subject = result.getString("subject");
          newEntry.put("subject", subject);
          String userAuthor = result.getString("user_name");
          newEntry.put("user_author", userAuthor);
          String university = result.getString("name");
          newEntry.put("university", university);
          studySets.add(newEntry);
        }
        return studySets;
      }
    }
  }

  public HashMap<String, Object> runViewStudySetSqlQuery(
    DataSource pool,
    String studySetID
  )
    throws SQLException {
    HashMap<String, Object> studySetDetails = new HashMap<>();
    ArrayList<Card> studySetCards = new ArrayList<>();

    try (Connection conn = pool.getConnection()) {
      try (
        PreparedStatement query_statement = conn.prepareStatement(
          VIEW_STUDY_SET_QUERY
        )
      ) {
        studySetID = studySetID.substring(1);
        query_statement.setInt(1, Integer.parseInt(studySetID));
        ResultSet result = query_statement.executeQuery();
        while (result.next()) {
          if (studySetDetails.containsKey("title") == false) {
            String title = result.getString("title");
            studySetDetails.put("title", title);

            String description = result.getString("description");
            studySetDetails.put("description", description);

            String subject = result.getString("subject");
            studySetDetails.put("subject", subject);

            String user_author = result.getString("user_name");
            studySetDetails.put("user_author", user_author);

            String university = result.getString("name");
            studySetDetails.put("university", university);
          }
          String cardFront = result.getString("front");
          String cardBack = result.getString("back");
          Card card = new Card(cardFront, cardBack);
          studySetCards.add(card);
        }
        studySetDetails.put("cards", studySetCards);
        return studySetDetails;
      }
    }
  }

  public Object getRequestResult(HttpServletRequest request)
    throws ServletException, IOException {
    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    String pathInfo = request.getPathInfo();

    try {
      if (pathInfo == null) {
        String searchWord = request.getParameter("stringToSearchBy");
        return runSearchStudySetSqlQuery(pool, searchWord);
      }
      return runViewStudySetSqlQuery(pool, pathInfo);
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();
    String requestResultJSON = gson.toJson(getRequestResult(request));
    response.getWriter().println(requestResultJSON);
  }
  //TODO doPost for creating and storing a study set

}
