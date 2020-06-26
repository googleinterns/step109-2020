public final class Card{
  private Long id;
  private String front;
  private String back;

  public Card(Long id, String infront, String inback){
    this.id = id;
    this.front = infront;
    this.back = inback;
  }

  public Long getID(){
    return this.id;
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