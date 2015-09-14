namespace java com.if4031
struct Message {
	1:string nickname,
	2:string channel,
	3:string content,
	4:i64 timestamp
}

struct Response {
	1:string status,
	2:string message,
}

struct ChatResponse {
	1:string status,
	2:string message,
	3:list<Message> chats
}

service ChatService
{
	Response join(1:string nickname,2:string channel),
	Response leave(1:string nickname,2:string channel),
	
	Response send(1:string nickname,2:string channel,3:string message),
	Response sendAll(1:string nickname,2:string message),
	
	ChatResponse recv(1:string nickname,2:string channel),
	ChatResponse recvAll(1:string nickname)
}