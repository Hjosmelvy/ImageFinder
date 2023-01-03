# ImageFinder
A Java web app that, upon entering a URL, parses through all sub-URLs, displays all images, and highlights the ones with human faces in them.

### Requirements
Before beginning, make sure you have the following installed and ready to use
- Maven 3.5 or higher
- Java 8

### Setup

To start, open a terminal window and navigate to wherever you unzipped to the root directory. To build the project, run the command:

>`mvn package`

If all goes well you should see some lines that ends with "BUILD SUCCESS". When you build the project, maven should build it in the `target` directory. To clear this, you may run the command:

>`mvn clean`

To run the project, use the following command to start the server:

>`mvn clean test package jetty:run`

You should see a line at the bottom that says "Started Jetty Server". Now, if you enter `localhost:8080` into your browser, you should see the `index.html` welcome page.
