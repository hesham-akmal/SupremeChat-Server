package network_data;

import java.io.Serializable;
import java.util.List;

public class MessagePacket implements Serializable {
    private static final long serialVersionUID = 6529685098200757690L;

    private String sender, receiver, text;
    private List<String> listOfRecievers;
    private boolean isGroupMsg = false;

    public MessagePacket(String sender, String receiver, String text, boolean isGroupMsg) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.isGroupMsg = isGroupMsg;
    }

    public MessagePacket(String sender, List<String> listOfRecievers, String text, boolean isGroupMsg) {
        this.sender = sender;
        this.listOfRecievers = listOfRecievers;
        this.text = text;
        this.isGroupMsg = isGroupMsg;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public boolean IsGroupMSG(){return isGroupMsg;}

    public List<String> getListOfRecievers(){return listOfRecievers;}

    public String getText() {
        return text;
    }
}
