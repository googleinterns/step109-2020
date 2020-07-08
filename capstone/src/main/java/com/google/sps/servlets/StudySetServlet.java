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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.sql.ResultSet;
import com.google.sps.data.Card;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/study_set")
public class StudySetServlet extends HttpServlet {
    public ArrayList<HashMap<String, String>> runSqlQuery(DataSource pool, String sqlStatement, String search_word) throws SQLException {
        ArrayList<HashMap<String, String>> studySets =  new ArrayList<>();
        try (Connection conn = pool.getConnection()) {
            try (
                PreparedStatement queryStatement = conn.prepareStatement(
                "SELECT study_set.id, study_set.title, study_set.description, study_set.subject,  university.name, user_info.user_name FROM study_set " + 
                "JOIN university ON university.id = study_set.university_id JOIN user_info ON study_set.owner_id = user_info.id " +
                "ORDER BY study_set.update_time DESC LIMIT 10"
                )
            ) {
                // queryStatement.setString(1, search_word);
                ResultSet result = queryStatement.executeQuery();
                while(result.next()) {
                    HashMap<String, String> newEntry = new HashMap<>();
                    String id = Integer.toString(result.getInt("id"));
                    newEntry.put("id", id);
                    String title = result.getString("title");
                    newEntry.put("title", title);
                    String description = result.getString("description");
                    newEntry.put("description", description);
                    String subject = result.getString("subject");
                    newEntry.put("subject", subject);
                    String user_author = result.getString("user_name");
                    newEntry.put("user_author", user_author);
                    String university = result.getString("name");
                    newEntry.put("university", university);
                    try( PreparedStatement anotherqueryStatement = conn.prepareStatement(
                        "SELECT COUNT(card.study_set_id), card.study_set_id FROM card WHERE card.study_set_id =" + id + " GROUP BY card.study_set_id"
                        )
                    ) {
                        ResultSet anotherResult = anotherqueryStatement.executeQuery();
                        if (anotherResult.next() == false) {
                            newEntry.put("study_set_length", "0");
                        } else {
                            do {
                                String study_set_length = Integer.toString(anotherResult.getInt("count"));
                                newEntry.put("study_set_length", study_set_length);
                            } while (anotherResult.next());
                        }
                    }
                    studySets.add(newEntry);
                }
                return studySets;
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String search_word = "";
        //request.getParameter("search_word");
        String sqlStatement = "";// you can place your sql statements here.

        ServletContext servletContext = getServletContext();
        DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
        try {
            ArrayList<HashMap<String, String>> studySets = runSqlQuery(pool, sqlStatement, search_word);
            response.setContentType("application/json;");
            Gson gson = new Gson();
            String studySetsResult = gson.toJson(studySets);
            response.getWriter().println(studySetsResult);
        } catch (SQLException ex) {
            throw new RuntimeException( "There is an error with your sql statement ... " , ex);
        }
    }


    //TODO doPost for creating and storing a study set

}