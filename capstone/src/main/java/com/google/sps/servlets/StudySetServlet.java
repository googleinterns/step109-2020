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
import com.google.gson.internal.LinkedTreeMap;
import com.google.sps.data.Card;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    HashMap<String, Object> result_par = new Gson()
    .fromJson(request.getReader(), HashMap.class);
    String owner_id = result_par.get("user_id").toString();
    String title = result_par.get("title").toString();
    String subject = result_par.get("subject").toString();
    String description = result_par.get("description").toString();
    String university = result_par.get("university_id").toString();
    String professor = result_par.get("professor").toString();
    String academic_time = result_par.get("academic_time").toString();
    String course_name = result_par.get("course_name").toString();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    String creation_time = formatter.format(date);
    String update_time = creation_time;

    ArrayList<LinkedTreeMap<String, String>> cards = (ArrayList) result_par.get(
      "cards"
    );
    String studyID = "";

    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    if (pool == null) {
      System.err.println(
        "Connection to SQL database not working because servlet is not conntect to the database"
      );
      return;
    }

    ResultSet result;
    try (Connection conn = pool.getConnection()) {
      String statement = String.format(
        "INSERT INTO study_set (owner_id, title, subject, description, university_id, professor, academic_time_period, course_name, creation_time, update_time)" +
        "Values (%1$s, '%2$s', '%3$s', '%4$s', %5$s, '%6$s', '%7$s', '%8$s', '%9$s', '%10$s');",
        owner_id,
        title,
        subject,
        description,
        university,
        professor,
        academic_time,
        course_name,
        creation_time,
        update_time
      );
      PreparedStatement updateTable = conn.prepareStatement(statement);
      updateTable.execute();

      String getStudyID = String.format(
        "SELECT study_set.id FROM study_set WHERE study_set.owner_id = %1$s AND study_set.creation_time = '%2$s';",
        owner_id,
        creation_time
      );
      PreparedStatement studyIDStatement = conn.prepareStatement(getStudyID);
      result = studyIDStatement.executeQuery();

      while (result.next()) {
        studyID = result.getString("id");
      }
      if (studyID.equals("")) {
        System.err.println("Unable to get study_set ID");
        return;
      }
      creatCard(cards, studyID, conn);
    } catch (SQLException ex) {
      throw new RuntimeException("Unable to verify Connection", ex);
    }

    response.sendRedirect("/createStudySet.html");
  }

  private void creatCard(
    ArrayList<LinkedTreeMap<String, String>> cards,
    String studyID,
    Connection conn
  )
    throws SQLException {
    for (int i = 0; i < cards.size(); ++i) {
      String frontText = cards.get(i).get("front");
      String backText = cards.get(i).get("back");
      if (!frontText.equals("") && !backText.equals("")) {
        String cardStatement = String.format(
          "INSERT INTO CARD (study_set_id, front, back) VALUES(%1$s, '%2$S', '%3$s');",
          studyID,
          frontText,
          backText
        );
        PreparedStatement updateCardTable = conn.prepareStatement(
          cardStatement
        );
        updateCardTable.execute();
      }
    }
  }
}
