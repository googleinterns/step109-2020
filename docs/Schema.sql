CREATE TABLE card(
    id INT PRIMARY KEY,
    study_id INT,
    front TEXT,
    back TEXT
);

CREATE TABLE study_set(
    id INT PRIMARY KEY,
    owner_userid INT,
    title TEXT,
    description TEXT,
    university_id INT,
    professor TEXT,
    academic_time_period TEXT,
    course_name TEXT,
    creation_time DATE,
    update_time DATE
);

CREATE TABLE university(
    id INT PRIMARY KEY,
    name TEXT,
    state TEXT,
    url  TEXT
);

CREATE TABLE user_info(
   id INT,
   user_name TEXT,
   full_name TEXT,
   email TEXT,
   verified_status BOOLEAN,
   university_id INT,
   study_set_url TEXT [],
   PRIMARY KEY (id)
 );