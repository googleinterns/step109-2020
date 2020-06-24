private class Card{
  private String front;
  private String back;

  public Card(String infront, String inback){
    this.front = infront;
    this.back = inback;
  }

  public String getFront(){
    return this.front;
  }

  public String getBack(){
    return this.back;
   } 

  public void setFront(String infront){
    this.front = infront;
  }

  public void setBack(String inback){
    this.back = inback;
  }
    
  //For Testing Purposes Only
  public String toString(){
    return "Front of Card: " + front + " Back of Card: " + back;
  }
}