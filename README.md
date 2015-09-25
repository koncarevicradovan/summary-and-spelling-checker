# Clojure web application summary generator and spelling checker
#Instaling

First you will need to download application, in either way by cloning this repo to desktop or by downloading zip file.

Download and install:

1. Leingen (https://github.com/technomancy/leiningen), dependency management tool for configuration of projects written in the Clojure. It enables its user to create, bulid, test and deploy projects.

2. Cross-platform document-oriented database, MongoDB (http://www.mongodb.org/downloads) which is NoSQL database. To start database open command line, navigate to mongodb/bin folder, and then execute mongod.exe (Windows). To start MongoDB shell, navigate to the same path and execute mongo.exe (on windows). For more detailed instructions on how to start MongoDB, see http://docs.mongodb.org/manual/installation/. The application automaticaly inserts initial users data in database.
Open the Leiningen command prompt, navigate to project folder and type lein run. Leiningen will download all dependencies needed for runing this project.

#Application
This application is designed to work as an web application for generating summary from entered text and for spelling correction for words and text.
Application has different levels of access - admin user and regular user. Initially  there are two users with following credentials:

Regular user - username: usr, password: pwd

Admin user - username: admin, password: admin

When admin user is logged  in he can manage users (there are CRUD operations implemented). Admin user can create new users (regular or admin). Also, he see all users in table and when he click on table row representing some user, popup will open
with appropriate data about that user, then he can change or delete that user. This page represents administration page for managing users.

When regular user is logged in first page is home page and from there he can go to summary generator, spelling corrector or
profile page. 
Summary generator page has three text areas where user should enter at least two paragraphs of text that he want to summarize and if user has more than three paragraphs he can split his text into three equal parts and enter those parts in text areas. After that user choose number of thesis that he want to have in his summary and the he clicks on Create summary. In bottom text area summary will appear with chosen number of thesis. Here is adjusted and implemented algorithm from book "Clojure Data Structures and Algorithms Cookbook" - Chapter 4. This algorithm is inspired by Google's PageRank and is labeled as "LexRank". Algorithm used  term frequency inverse document frequency (TF-IDF) to represent text as vectors. TFIDF is computed using the following formula:
      TFIDF = TF * IDF,
where term frequency (TF) is the measure of how many times in a sentence particular word occurs, and inverse document frequency (IDF) is a measure of how often a particular word is present across all the documents/paragraphs(textual chunks). After that distance between two sentences represented by TFIDF is computed using measure called cosine similarity. Matrix with all the chunks's sentences in both columns and rows is filled with not zero data if cosine similarity is higher than 0.2, that is optimal value for cosine threshold. All this is used for measuring degree centrality of each sentence, but the most important thing is to watch centrality degree. With respect to that using power method we will get measure of how
sentence is salient and algorithm return top N the most salient sentences. After creating summary user can save entered text and created summary (he can view that on his profile page).

Example - if we enter first three paragraphs from book "Clojure programming" - Chapter five summary will be:
- code can be executed repeatedly in a loop; or, code can be grouped as a unit and given
- manual work, a programmer can write code once and treat that code as a reusable unit
- historically, lisps have been described as “programmable programming languages
Solid result.

Spelling corrector page has two tabs word and text. On tab words user can enter some word and then click on Correct word and in bottom text box corrected word will appear. If he is satisfied with correction he can train his dataset with corrected word, or he can enter better word and train his dataset. Dataset can be extended only if file size is less than 100 Mb. On second tab same functionality is implemented for text instead of single word. Here is implemented algorithm for spelling correction from book "Clojure Data Analysis Cookbook" - Chapter 2, which is improved and adjusted. Same algorithm written in Python can be foun on http://norvig.com/spell-correct.html  and data for training is used fro http://norvig.com/big.txt and extended with 1000 most common english words. Depending on training dataset algorithm can work with different languages. Algorithm creates different variations of words and variations of varioations to the second depth level. All the possible edits that can be made to a word are calculated, used are only those edits that are known in the training set, preferably one that appears multiple times.

On the profile page a users can see and change data about their profile and saved texts and generated summaries.

Most of the front end scripting was done using javascript library Knockout JS (with creating ViewModels), including KnockoutValidation (for client validation in addition to server validation) and Notify.js, as well as jQuerry combined with Selmer. Responsive UI style is done using Bootstrap.
