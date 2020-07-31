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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  private final String INSERT_CARD_STATEMENT =
    "INSERT INTO CARD (study_set_id, front, back) VALUES";

  private final String INSERT_STUDY_SET_STATEMENT =
    "INSERT INTO study_set (owner_id, title, subject, description, university_id, professor, academic_time_period, course_name, creation_time, update_time)" +
    "Values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) Returning id;";

  private final String GET_STUDY_ID_STATEMENT =
    "SELECT study_set.id FROM study_set WHERE study_set.owner_id = ? AND study_set.creation_time = ?;";

  private final String VIEW_STUDY_SET_QUERY =
    " SELECT card.front, card.back, study_set.title, study_set.description, study_set.subject, university.name, user_info.user_name " +
    " FROM study_set" +
    " JOIN university ON university.id = study_set.university_id" +
    " JOIN user_info ON study_set.owner_id = user_info.id" +
    " JOIN card ON card.study_set_id = study_set.id" +
    " WHERE study_set.id = ?" +
    " GROUP BY card.id, study_set.id, university.id, user_info.id;";

  private final String GET_USERID_FROM_EMAIL =
    "SELECT user_info.id FROM user_info WHERE user_info.email = ?;";

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

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, NotLoggedInException {
    HashMap<String, Object> resultsParams = new Gson()
    .fromJson(request.getReader(), HashMap.class);
    String title = resultsParams.get("title").toString();
    String subject = resultsParams.get("subject").toString();
    String description = resultsParams.get("description").toString();
    String university = resultsParams.get("university_id").toString();
    String professor = resultsParams.get("professor").toString();
    String academicTime = resultsParams.get("academic_time").toString();
    String courseName = resultsParams.get("course_name").toString();

    Date date = new Date();
    Timestamp creationTimestamp = new Timestamp(date.getTime());

    ArrayList<LinkedTreeMap<String, String>> cards = (ArrayList) resultsParams.get(
      "cards"
    );

    if (
      !areFieldsFilled(
        title,
        subject,
        description,
        university,
        professor,
        academicTime,
        courseName,
        cards
      )
    ) {
      System.err.println("Data fields are not completely filled in.");
      return;
    }

    ServletContext servletContext = getServletContext();
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    if (pool == null) {
      System.err.println(
        "Connection to SQL database not working because servlet is not conntect to the database"
      );
      return;
    }
    String ownerID = "";
    try {
      ownerID = getOwnerId(pool);
    } catch (NotLoggedInException ex) {
      response.sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "User must be logged in"
      );
    }

    String redirectID = createRow(
      ownerID,
      title,
      subject,
      description,
      university,
      professor,
      academicTime,
      courseName,
      creationTimestamp,
      pool,
      cards
    );
    response.setContentType("application/json;");
    response.getWriter().println(redirectID);
    return;
  }

  private void createCard(
    ArrayList<LinkedTreeMap<String, String>> cards,
    String studyID,
    Connection conn
  )
    throws SQLException {
    PreparedStatement updateCardTable = conn.prepareStatement(
      insertCardString(cards, INSERT_CARD_STATEMENT)
    );
    for (int i = 0; i < cards.size(); ++i) {
      String frontText = cards.get(i).get("front");
      String backText = cards.get(i).get("back");

      updateCardTable.setInt((i * 3) + 1, Integer.parseInt(studyID));
      updateCardTable.setString((i * 3) + 2, frontText);
      updateCardTable.setString((i * 3) + 3, backText);
    }

    updateCardTable.execute();
  }

  private String createRow(
    String ownerID,
    String title,
    String subject,
    String description,
    String university,
    String professor,
    String academicTime,
    String courseName,
    Timestamp creationTimestamp,
    DataSource pool,
    ArrayList<LinkedTreeMap<String, String>> cards
  ) {
    try (Connection conn = pool.getConnection()) {
      PreparedStatement updateTable = conn.prepareStatement(
        INSERT_STUDY_SET_STATEMENT
      );

      updateTable.setInt(1, Integer.parseInt(ownerID));
      updateTable.setString(2, title);
      updateTable.setString(3, subject);
      updateTable.setString(4, description);
      updateTable.setInt(5, Integer.parseInt(university));
      updateTable.setString(6, professor);
      updateTable.setString(7, academicTime);
      updateTable.setString(8, courseName);
      updateTable.setTimestamp(9, creationTimestamp);
      updateTable.setTimestamp(10, creationTimestamp);

      ResultSet resultStudySet = updateTable.executeQuery();

      String studyID = "";
      while (resultStudySet.next()) {
        studyID = resultStudySet.getString("id");
      }

      createCard(cards, studyID, conn);
      return studyID;
    } catch (SQLException ex) {
      throw new RuntimeException("Unable to verify Connection", ex);
    }
  }

  private String insertCardString(
    ArrayList<LinkedTreeMap<String, String>> cards,
    String insertStatement
  ) {
    String fullInsertStatement = insertStatement;

    fullInsertStatement +=
      String.join(", ", Collections.nCopies(cards.size(), "(?, ?, ?)"));
    fullInsertStatement += ";";

    return fullInsertStatement;
  }

  private boolean areFieldsFilled(
    String title,
    String subject,
    String description,
    String university,
    String professor,
    String academicTime,
    String courseName,
    ArrayList<LinkedTreeMap<String, String>> cards
  ) {
    if (
      title.isEmpty() ||
      subject.isEmpty() ||
      description.isEmpty() ||
      university.isEmpty() ||
      professor.isEmpty() ||
      academicTime.isEmpty()
    ) {
      return false;
    }
    for (int i = 0; i < cards.size(); ++i) {
      String frontText = cards.get(i).get("front");
      String backText = cards.get(i).get("back");

      if (frontText.isEmpty() || backText.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private String getOwnerId(DataSource pool) {
    UserService userService = UserServiceFactory.getUserService();
    Boolean isUserLoggedIn = userService.isUserLoggedIn();
    if (!isUserLoggedIn) {
      System.err.println("Need to be logged in");
      throw new NotLoggedInException(
        "User must be logged in to create a new Study Set."
      );
    }
    String email = userService.getCurrentUser().getEmail();

    try (Connection conn = pool.getConnection()) {
      PreparedStatement getUserStatement = conn.prepareStatement(
        GET_USERID_FROM_EMAIL
      );
      getUserStatement.setString(1, email);
      ResultSet result = getUserStatement.executeQuery();
      String emailID = "";
      while (result.next()) {
        emailID = result.getString("id");
      }
      return emailID;
    } catch (SQLException ex) {
      throw new RuntimeException("Unable to verify Connection", ex);
    }
  }
}
