package com.if4031;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Scanner;

public class ChatClient {
    public static void main(String [] args){
        try {
            TTransport transport;

            transport = new TSocket("localhost", 9090);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            ChatService.Client client = new ChatService.Client(protocol);
            perform(client);
            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    private static void perform(ChatService.Client client){
        String currNick = "paijo";
        boolean stop = false;
        do{
            Scanner sc = new Scanner(System.in);
            String str = sc.nextLine();
            String[] splited = str.split("\\s+");
            if (splited[0].equals("/NICK")){
                if (splited.length != 2){
                    System.out.println("Usage: /NICK <nickname>");
                } else {
                    currNick = splited[1];
                    System.out.println("[OK] Nickname changed to '"+currNick+"'");
                }

            } else if (splited[0].equals("/JOIN")){
                if (splited.length != 2){
                    System.out.println("Usage: /JOIN <nickname>");
                } else {
                    try {
                        Response response = client.join(currNick, splited[1]);
                        System.out.println("["+response.getStatus()+"] "+response.getMessage());
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }

            } else if (splited[0].equals("/LEAVE")){
                if (splited.length != 2){
                    System.out.println("Usage: /LEAVE <channel>");
                } else {
                    try {
                        Response response = client.leave(currNick,splited[1]);
                        System.out.println("[" + response.getStatus() + "] " + response.getMessage());
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            } else if (splited[0].equals("/EXIT")){
                stop = true;
            } else {
                StringBuffer message = new StringBuffer();
                if (splited[0].startsWith("@")){
                    if (splited.length < 2){
                        System.out.println("Usage: @<channel> <text>");
                    } else {
                        String channelName = splited[0].substring(1);
                        for(int i = 1; i < splited.length; i++){
                            if (i > 1) message.append(" ");
                            message.append(splited[i]);
                        }
                        try {
                            Response response = client.send(currNick,channelName,message.toString());
                            System.out.println("["+response.getStatus()+"] "+response.getMessage());
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for(int i = 0; i < splited.length; i++){
                        if (i > 0) message.append(" ");
                        message.append(splited[i]);

                    }
                    try {
                        Response response = client.sendAll(currNick, message.toString());
                        System.out.println("["+response.getStatus()+"] "+response.getMessage());
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!stop){
                try {
                    ChatResponse chatResponse = client.recvAll(currNick);
                    System.out.println("["+chatResponse.getStatus()+"] "+chatResponse.getMessage());
                    for(Message message:chatResponse.getChats()){
                        System.out.println("["+message.getChannel()+"]("+message.getNickname()+")" + message.getContent());
                    }
                } catch (TException e) {
                    e.printStackTrace();
                }
            }
        } while (!stop);
        System.out.println("bye");

    }

    private static void printMenu(){
        System.out.println("/NICK <nickname>");
        System.out.println("/JOIN <nickname>");
        System.out.println("/LEAVE <channel>");
        System.out.println("/EXIT");
        System.out.println("<text>");
        System.out.println("@<channel> text");
    }
}
