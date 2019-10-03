#RUNNING THE APP
#The sole dependency this java app has to build/run is a decompression lib
#otherwise just a command prompt should suffice
#
#To build, type this into a command prompt
javac -classpath commons-compress-1.19.jar App.java

#to run, type this into a command prompt
java -classpath commons-compress-1.19.jar App.java



#RUNNING TESTS
#The unit tests rely on maven to build and run.
#If maven is not availble, please inspect test cases at src/test/java/com/mlindner/AppTest.java
#
#to build tests, type this into a command prompt
mvn clean install

#to run tests, type this into a command prompt
mvn test
