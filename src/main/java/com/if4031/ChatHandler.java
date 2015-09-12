package com.if4031;

import com.mongodb.*;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class ChatHandler implements ChatService.Iface {
    //DB Schema
    //{_id,nickname,channel,content,timestamp}
    static final String MESSAGE_COLLECTION_NAME = "messages";
    //{_id,nickname,channel}
    static final String USER_CHANNEL_COLLECTION_NAME = "user_channels";
    //{_id,nickname,last_fetch}
    static final String USER_LAST_FETCH_COLLECTION_NAME = "user_last_fetch";
    DB db;
    public ChatHandler(DB db){
        this.db = db;
        createIndex();
    }

    @Override
    public Response join(String nickname, String channel) throws TException {
        DBCollection userChannelCollection = this.getUserChannelCollection();
        BasicDBObject dbObject = new BasicDBObject()
                .append("nickname",nickname)
                .append("channel", channel);

        //check if user already joined
        if (userChannelCollection.findOne(dbObject) != null){
            return new Response("ERROR","user "+nickname+" already joined "+channel);
        } else {
            //set the last fetch to current timestamp
            DBCollection lastFetchCollection = getUserLastFetchCollection();
            lastFetchCollection.update(new BasicDBObject().append("nickname",nickname),new BasicDBObject().append("nickname", nickname).append("last_fetch", System.currentTimeMillis()),true,false,WriteConcern.ACKNOWLEDGED);

            userChannelCollection.insert(dbObject, WriteConcern.ACKNOWLEDGED);
            return new Response("OK",nickname+" has joined "+channel);
        }
    }

    @Override
    public Response leave(String nickname, String channel) throws TException {
        DBCollection userChannelCollection = this.getUserChannelCollection();
        DBObject dbObject = new BasicDBObject()
                .append("nickname", nickname)
                .append("channel", channel);
        WriteResult writeResult = userChannelCollection.remove(dbObject, WriteConcern.ACKNOWLEDGED);
        if (writeResult.getN() == 0){
            return new Response("ERROR","user "+nickname+" doesn't join "+channel);
        } else {
            return new Response("OK","removed");
        }
    }

    @Override
    public Response send(String nickname, String channel, String message) throws TException {
        //check if user has joined to channel
        DBCollection userChannelCollection = getUserChannelCollection();
        DBObject one = userChannelCollection.findOne(new BasicDBObject().append("nickname", nickname).append("channel", channel));
        if (one == null){
            return new Response("ERROR","User "+nickname+" not joined "+channel);
        } else {
            DBCollection messageCollection = this.getMessageCollection();
            DBObject dbObject = new BasicDBObject()
                    .append("nickname",nickname)
                    .append("channel", channel)
                    .append("content", message)
                    .append("timestamp", System.currentTimeMillis());
            messageCollection.insert(dbObject,WriteConcern.ACKNOWLEDGED);
            return new Response("OK","");
        }


    }

    @Override
    public Response sendAll(String nickname, String message) throws TException {
        //get user channels
        List<String> channels = fetchUserChannels(nickname);
        for(String channel: channels){
            send(nickname,channel,message);
        }
        return new Response("OK","Channels affected : "+channels.size());
    }

    @Override
    public ChatResponse recv(String nickname, String channel) throws TException {
        throw new UnsupportedOperationException("Use recvAll instead.");
    }

    @Override
    public ChatResponse recvAll(String nickname) throws TException {
        //{'last_fetch':{'$gte':lastFetch},'channel':{'or':[....]}}
        long lastFetch = fetchUserLastFetchAndUpdate(nickname);
        BasicDBObject messageQuery = new BasicDBObject().append("timestamp", new BasicDBObject("$gte", lastFetch));

        List<String> channels = fetchUserChannels(nickname);
        messageQuery.append("channel",new BasicDBObject().append("$in", channels));

        BasicDBObject sortQuery = new BasicDBObject().append("channel",1).append("timestamp",1);

        DBCollection messageCollection = getMessageCollection();
        DBCursor cursor = messageCollection.find(messageQuery).sort(sortQuery);
        int counts = cursor.count();

        List<Message> messages = new ArrayList<>();
        while (cursor.hasNext()){
            DBObject dbObject = cursor.next();
            messages.add(new Message(
                    (String) dbObject.get("nickname"),
                    (String) dbObject.get("channel"),
                    (String) dbObject.get("content"),
                    (long) dbObject.get("timestamp")
            ));
        }
        return new ChatResponse("ok","Got "+counts+" messages",messages);
    }

    private DBCollection getUserChannelCollection(){
        return db.getCollection(USER_CHANNEL_COLLECTION_NAME);
    }

    private DBCollection getMessageCollection(){
        return db.getCollection(MESSAGE_COLLECTION_NAME);
    }

    private DBCollection getUserLastFetchCollection(){
        return db.getCollection(USER_LAST_FETCH_COLLECTION_NAME);
    }

    private void createIndex(){
        DBCollection messageCollection = getMessageCollection();
        //create index for messages
        messageCollection.createIndex(new BasicDBObject().append("channel", 1));
        messageCollection.createIndex(new BasicDBObject().append("timestamp", -1));

        DBCollection userChannelCollection = getUserChannelCollection();
        userChannelCollection.createIndex(new BasicDBObject().append("nickname",1).append("channel", 1),null,true);

        DBCollection userLastFetchCollection = getUserLastFetchCollection();
        userLastFetchCollection.createIndex(new BasicDBObject().append("nickname",1),null,true);

    }

    /**
     * Get when user last fetch a chat message
     * @param nickname user nickname
     * @return timestamp in millis
     */
    private long fetchUserLastFetchAndUpdate(String nickname){
        long lastFetch = fetchUserLastFetch(nickname);
        updateLastFetch(nickname);
        return lastFetch;
    }

    private void updateLastFetch(String nickname){
        DBCollection userLastFetchCollection = getUserLastFetchCollection();
        DBObject userChannelQuery = new BasicDBObject().append("nickname", nickname);
        userLastFetchCollection.update(userChannelQuery,
                new BasicDBObject().append("nickname",nickname).append("last_fetch",System.currentTimeMillis()));
    }

    private long fetchUserLastFetch(String nickname){
        DBCollection userLastFetchCollection = getUserLastFetchCollection();
        DBObject userQuery = new BasicDBObject().append("nickname", nickname);
        DBObject userDBObject = userLastFetchCollection.findOne(userQuery);
        return (long) userDBObject.get("last_fetch");
    }

    /**
     * get all messages started last fetch
     * @param lastFetch start of last fetch
     * @param channel selected channel. if null, fetch from all channel
     * @return messages
     */
    private List<Message> getMessages(long lastFetch,String channel){
        //get all messages
        List<Message> messages = new ArrayList<>();
        BasicDBObject messageQuery = new BasicDBObject()
                .append("timestamp", new BasicDBObject().append("\\$gte", lastFetch));
        if (channel != null){
            messageQuery.append("channel",channel);
        }
        DBCollection messageCollection = getMessageCollection();

        DBCursor cursor = messageCollection.find(messageQuery).sort(new BasicDBObject().append("timestamp", 1));
        while (cursor.hasNext()){
            DBObject dbObject = cursor.next();
            Message message = new Message(
                    (String) dbObject.get("nickname"),
                    (String) dbObject.get("channel"),
                    (String) dbObject.get("content"),
                    (long) dbObject.get("timestamp"));
            messages.add(message);
        }
        return messages;
    }

    private List<String> fetchUserChannels(String nickname){
        List<String> channels = new ArrayList<>();
        //get user channels
        DBCollection userChannelCollection = getUserChannelCollection();
        DBCursor cursor = userChannelCollection.find(new BasicDBObject().append("nickname", nickname));

        //for each channels
        while (cursor.hasNext()){
            DBObject dbObject = cursor.next();
            String channel = (String) dbObject.get("channel");
            channels.add(channel);
        }
        return channels;
    }
}
