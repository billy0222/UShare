package lix5.ushare;

import java.util.Date;

/**
 * Created by Kevin on 3/4/2018.
 */

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String messageTime;

    public ChatMessage(String messageText, String messageUser){
        this.messageText = messageText;
        this.messageUser = messageUser;
        messageTime = new Date().toString();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getMessageUser() {
        return messageUser;
    }
}
