package network_data;

import java.io.Serializable;

public class MessagePacket implements Serializable {
    private static final long serialVersionUID = 6529685098200757690L;

    private String sender, receiver, text;

    public MessagePacket(String sender, String receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }
}
