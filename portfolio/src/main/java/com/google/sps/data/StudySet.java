import java.util.ArrayList;

private class StudySet {
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
    String university,
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

  public void addCard(String front, String back) {
    Card newCard = new Card(front, back);
    this.Cards.add(newCard);
  }
}
