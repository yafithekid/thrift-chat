package com.if4031;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.thrift.TException;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Set;

import static org.junit.Assert.*;

public class ChatHandlerTest {
    ChatHandler chatHandler;
    DB testDB;
    PrintStream sysout = System.out;

    @org.junit.Before
    public void setUp() throws Exception {
        MongoClient client = new MongoClient("localhost",27017);
        testDB = (DB) client.getDB("if4031-test");
        Set<String> collectionNames = testDB.getCollectionNames();
        for(String name: collectionNames){
            System.out.println(name);
        }
        chatHandler = new ChatHandler(testDB);
    }

    @org.junit.After
    public void tearDown() throws Exception {
        MongoClient client = new MongoClient("localhost",27017);
        client.dropDatabase("if4031-test");
    }

    @Test
    public void testHelloWorld(){
        sysout.print("Hello world");
    }

    @Test
    public void testJoinLeave() throws TException {
        Response response = chatHandler.join("calvin", "mboh");
        assertEquals(response.getStatus(), "OK");
        sysout.println(response.getMessage());

        Response response2 = chatHandler.join("calvin", "mboh");
        assertEquals(response2.getStatus(), "ERROR");
        sysout.println(response2.getMessage());

        Response response3 = chatHandler.leave("calvin", "mboh");
        assertEquals(response3.getStatus(), "OK");
        sysout.println(response3.getMessage());

        Response response4 = chatHandler.leave("calvin","mboh");
        assertEquals(response4.getStatus(),"ERROR");
        sysout.println(response4.getMessage());
    }

    @Test
    public void testSendReceiveMessageOneChannel() throws TException {
        chatHandler.join("calvin", "mboh");
        chatHandler.join("yafi", "mboh");

        chatHandler.send("calvin", "mboh", "Saya calvin");
        chatHandler.send("calvin", "mboh", "Saya geje");
        ChatResponse yafiResponse = chatHandler.recvAll("yafi");

        assertEquals(yafiResponse.getChats().size(), 2);
        ChatResponse yafiAgainResponse = chatHandler.recvAll("yafi");
        assertEquals(yafiAgainResponse.getChats().size(),0);

        chatHandler.send("calvin", "mboh", "Saya calvin lagi");
        ChatResponse yafiAgainAgainResponse = chatHandler.recvAll("yafi");
        assertEquals(yafiAgainAgainResponse.getChats().size(),1);

        chatHandler.join("sadewa", "mboh");
        ChatResponse calvinResponse = chatHandler.recvAll("sadewa");
        assertEquals(calvinResponse.getChats().size(), 0);
    }

    @Test
    public void testInvalidChannel() throws TException {
        chatHandler.join("anggur", "buah");
        Response response = chatHandler.send("wortel", "buah", "halo aku wortel");
        assertEquals(response.getStatus(),"ERROR");
        ChatResponse anggurResponse = chatHandler.recvAll("anggur");
        assertEquals(anggurResponse.getChats().size(),0);
    }

    @Test
    public void testSendReceiveMultipleChannel() throws TException{
        chatHandler.join("bayam","sayur");
        chatHandler.join("wortel", "sayur");
        chatHandler.join("tomat", "sayur");
        chatHandler.join("tomat", "buah");
        chatHandler.join("anggur", "buah");

        chatHandler.send("bayam", "sayur", "halo aku bayam");
        chatHandler.send("wortel", "sayur", "halo aku wortel");
        chatHandler.send("tomat","sayur","halo aku tomat");

        chatHandler.send("tomat", "buah", "halo aku tomat");
        chatHandler.send("anggur", "buah", "halo aku anggur");

        ChatResponse bayamResponse = chatHandler.recvAll("bayam");
        assertEquals(bayamResponse.getChats().size(),3);
        ChatResponse anggurResponse = chatHandler.recvAll("anggur");
        assertEquals(anggurResponse.getChats().size(),2);
        ChatResponse tomatResponse = chatHandler.recvAll("tomat");
        assertEquals(tomatResponse.getChats().size(),5);


    }

    @Test
    public void testBroadcast() throws TException {
        chatHandler.join("tomat","sayur");
        chatHandler.join("tomat","buah");
        chatHandler.join("bayam","sayur");
        chatHandler.join("anggur", "buah");
        chatHandler.sendAll("tomat", "halo aku tomat");

        ChatResponse bayamResponse = chatHandler.recvAll("bayam");
        assertEquals(bayamResponse.getChats().size(),1);
        ChatResponse anggurResponse = chatHandler.recvAll("anggur");
        assertEquals(anggurResponse.getChats().size(),1);

    }
}