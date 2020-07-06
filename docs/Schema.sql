 /*
    The tables are in the order they will need to be created in our code dude to references.
 */


 /*
    This is table on each university in the United States. study_set and user_info will 
    link to it because it is better than having all the information about a university 
    in multiple tables
*/
CREATE TABLE university(
    id INT PRIMARY KEY,
    name TEXT,
    state TEXT,
);

/*
    This table holds the relevant information for each user. university_id  holds the 
    id of the university the user is currently enrolled in. The study_set_id array 
    contains the id of all study_set created by the user.
*/
CREATE TABLE user_info(
   id INT,
   user_name TEXT,
   full_name TEXT,
   email TEXT,
   verified BOOLEAN,
   university_id INT REFERENCES university(id),
   study_set_id INT [],
   PRIMARY KEY (id)
 );

/* 
    This is a table that holds information on a study set which will be displayed on
    the frontend when the user is viewing a specific study set. This table will be 
    used to show ownership of each card and organizes all the rows in the card table.
*/
CREATE TABLE study_set(
    id INT PRIMARY KEY,
    owner_id INT REFERENCES user_info(id),
    title TEXT,
    description TEXT,
    university_id INT REFERENCES university(id),
    professor TEXT,
    academic_time_period TEXT,
    course_name TEXT,
    creation_time DATE,
    update_time DATE
);

/*
    This is the table that holds information on a card, every card will have a row 
    when created in this table. When each cardis created it will have to have a 
    study_set_id in which it belongs to
*/
CREATE TABLE card(
    id INT PRIMARY KEY,
    study_set_id INT REFERENCES study_set(id),
    front TEXT,
    back TEXT
);
