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

@WebServlet("/view_studyset")
public class ViewStudySetServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext servletContext = getServletContext();
    String studySetID = request.getParameter("id");
    DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
    try {
      HashMap<String, Object> studySetDetails = runSqlQuery(pool, studySetID);
      response.setContentType("application/json;");
      Gson gson = new Gson();
      String studySetDetailsGSON = gson.toJson(studySetDetails);
      response.getWriter().println(studySetDetailsGSON);
    } catch (SQLException ex) {
      throw new RuntimeException(
        "There is an error with your sql statement ... ",
        ex
      );
    }
  }

  public String query_study_set_by_id(String studySetID) {
    String queryString =
      " SELECT card.front, card.back, study_set.title, study_set.description, study_set.subject, university.name, user_info.user_name " +
      " FROM study_set" +
      " JOIN university ON university.id = study_set.university_id" +
      " JOIN user_info ON study_set.owner_id = user_info.id" +
      " JOIN card ON card.study_set_id = study_set.id" +
      " WHERE  study_set.id=" +
      studySetID +
      " GROUP BY card.id, study_set.id, university.id, user_info.id;";

    return queryString;
  }

  public HashMap<String, Object> runSqlQuery(
    DataSource pool,
    String studySetID
  )
    throws SQLException {
    HashMap<String, Object> studySetDetails = new HashMap<>();
    ArrayList<Card> studySetCards = new ArrayList<>();

    try (Connection conn = pool.getConnection()) {
      try (
        PreparedStatement queryStatement = conn.prepareStatement(
          query_study_set_by_id(studySetID)
        )
      ) {
        ResultSet result = queryStatement.executeQuery();
        while (result.next()) {
          if(result.getRow()==1){
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
}
