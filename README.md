# thrift-chat

Simple chat application using Apache Thrift.

### Prerequisites

1. Install MongoDB. Ensure `mongod` is running on port 27017
2. Install Java JDK 1.8
3. Install Gradle, add it to your PATH environment variables.
4. Ensure gradle can works by typing `gradle` in your command prompt.


### Compile to jar

1. Run `gradle clientJar serverJar` on root project dir
2. Ensure MongoDB is running
3. In `build/libs` folder, run `java -jar chat-server-1.0.jar` and `java -jar chat-client-1.0.jar`

