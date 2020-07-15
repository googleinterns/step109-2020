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

public final class Card{
  private Integer id;
  private String front;
  private String back;
  private Integer study_set_id; 

  public Card(Integer id, String front, String back, Integer study_set_id){
    this.id = id;
    this.front = front;
    this.back = back;
    this.study_set_id = study_set_id;
  }

  public Card(String front, String back){
    this.front = front;
    this.back = back;
  }

  public Integer getID(){
    return this.id;
  }

  public String getFront(){
    return this.front;
  }

  public String getBack(){
    return this.back;
   } 
  
  public Integer getStudySetId(){
    return this.study_set_id;
  }

  public void setFront(String front){
    this.front = front;
  }

  public void setBack(String back){
    this.back = back;
  }
    
  //For Testing Purposes Only
  public String toString(){
    return "Front of Card: " + front + " Back of Card: " + back;
  }
}