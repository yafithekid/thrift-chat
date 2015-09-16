package com.if4031;

import com.mongodb.MongoClient;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class ChatServer {
    public static ChatHandler handler;
    public static ChatService.Processor processor;

    public static void main(String [] args) {
        System.out.println("Contacting MongoDB on localhost:27017...");
        MongoClient client = new MongoClient("localhost",27017);
        try {
            handler = new ChatHandler(client.getDB("if4031"));
            processor = new ChatService.Processor(handler);
            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor);
                }
            };

            new Thread(simple).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    public static void simple(ChatService.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            System.out.println("Server started on port 9090");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
