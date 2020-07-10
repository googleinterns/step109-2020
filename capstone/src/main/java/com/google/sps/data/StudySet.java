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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collection;

public final class StudySet {
  private ArrayList<Card> Cards;
  private Long id;
  private String email;
  private String title;
  private Long userID;
  private String description;
  private String university;
  private String subject;
  private String professor;
  private String timePeriodSystem; // Quarter/Semester/Trimester
  private String courseName;
  private Long creationTime;

  public StudySet(
    ArrayList<Card> Cards,
    Long id,
    String email,
    String title,
    Long userID,
    String description,
    String university_id,
    String subject,
    String professor,
    String timePeriodSystem,
    String courseName,
    Long creationTime
  ) {
    this.Cards = Cards;
    this.id = id;
    this.email = email;
    this.title = title;
    this.userID = userID;
    this.description = description;
    this.university = university;
    this.subject = subject;
    this.professor = professor;
    this.timePeriodSystem = timePeriodSystem;
    this.courseName = courseName;
    this.creationTime = creationTime;
  }

  public Integer getSetSize() {
    return this.Cards.size();
  }

  public void addCard(Card newCard) {
    this.Cards.add(newCard);
  }

  public void addCard(Integer cardID, String front, String back, Integer studySetId) {
    Card newCard = new Card(cardID,front, back, studySetId);
    this.Cards.add(newCard);
  }
}
