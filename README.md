# thrift-chat

Simple chat application using Apache Thrift.


## Environment Setup (Just around 1-2 minute, internet download time excluded)

1. Install gradle and add it to your PATH environment variables.
2. Ensure gradle can works by typing `gradle` in your command prompt.
3. Type "gradle build" to download the dependencies and save them to gradle local folder.
4. Open IDEA and open the com.if4031.HelloWorld file, the org.apache.thrift should be class not found.
5. At your IDEA, File -> Project Structure -> Libraries -> "plus sign" -> Java.
6. Search for needed **.jar** in your .gradle local folder (it should be around `C:/Users/%USER_PROFILE%/.gradle/caches/modules-2/files-2.1`)
7. Repeat step no. 5 until all class not found problem solved.
8. There is a "hello world" gradle task to create a helloworld jar. Please check the "task helloWorldJar" in `build.gradle`
9. Type `gradle helloWorldJar` in command prompt, at your root project directory
10. Open build/libs. Run java -jar chat-helloworld-1.0.jar (or similar like that)
