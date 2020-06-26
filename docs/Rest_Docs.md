If the user is not logged in: user can go about the study_set endpoints, but homepage has no endpoints. 


## The first time the user clicks the “Sign In” button in our homepage , we go through the Google Sign-On process, and redirect the user to the page where they enter university, username (NON_MVP-->and can optionally make their bio).  This info will be sent to this endpoint (/users/) to create an entry in the Users Info entity.

/users/ → POST request (body attr: username, university, bio) → NON-MVP-->bio can be initialized as null.

`{
	status_code: #,
}`


## But if the user is automatically logged in(you can get the id → and like save it in the JS file) and , you redirect them to the dashboard html page and to populate that page we use this endpoint (/users/#id).

Dashboard.html calls -->/users/id#
/users and /users/“id” modeled after  https://api.github.com/users/“username” → GET request

`{
	id : #,
email: “ ”,
name: “ ”,
username: “ ”,
university: “ ”,
study_sets_url: [ “ ” ],
bio: “ ”;
verified_status: bool,
profile_url: “ ”
}`

## Creating Study Set which is a post request

/study_set → POST request (body param: Collection(of cards front/back), title, description, and subject)

`{
          Status_code: # (tells if the request was successful)
}`

## Finding  Study Sets --doGet
/study_sets?search_term={userinput}

`[	
	id:#;
	title: “”;
	set_description: “ “;
	study_set_length: #;
	user_author: “ “;
	study_set_url: “ “;
	cards: [
				{
					id: #,
					‘front’: text, 
					‘back’: back_text
				},
				{
					id: #,
					‘front’: text_2, w
					‘back’: back_text_2
				}
			],

  ]`

## View  Study Set
/study_sets/#id → GET request.
`{
	id: #,
	title: “ ”,
	user_author: “ ”,(Author of study set)
	university: “ ”,
            description: “”,
	subject: “ ”,
	period_of_upload: “Quarter/Semester/Trimester”,
	course_name: “ ”,
	professor: “ ”,
	created_at: “ ”,
	cards: [
				{
					id: #,
					‘front’: text, 
					‘back’: back_text
				},
				{
					id: #,
					‘front’: text_2, w
					‘back’: back_text_2
				}
		]
}`


###Non MVP UPDATING CARDS 

`
	NON-MVP:
	rating: “ ”,
	amount_of_people_rated : #,
	favorite_number: #
`

