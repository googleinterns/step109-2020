import java.util.ArrayList;

private class UserInfo {
  private ArrayList<StudySet> userStudySets;
  private Long id;
  private String email;
  private String name;
  private String userName;
  private String university;
  private String bio;
  private Boolean verifiedStatus;

  public UserInfo(
    ArrayList<StudySet> userStudySets,
    Long id,
    String email,
    String name,
    String userName,
    String university,
    String bio,
    Boolean verifiedStatus
  ) {
    this.userStudySets = userStudySets;
    this.id = id;
    this.email = email;
    this.name = name;
    this.userName = userName;
    this.university = university;
    this.bio = bio;
    this.verifiedStatus = verifiedStatus;
  }
}
