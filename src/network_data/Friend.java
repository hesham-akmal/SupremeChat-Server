package network_data;

import java.io.Serializable;

public class Friend implements Serializable {
    private static final long serialVersionUID = 6519685098267757690L;

    public enum Status {
        Offline,
        Idle,
        Online
    }

    private String username;
    private Status status;
    private String lastLogin;
    private String IP;

    public Friend(String username, Status status, String lastLogin, String IP) {
        this.username = username;
        this.status = status;
        this.lastLogin = lastLogin;
        this.IP = IP;
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public String getIP() {
        return IP;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}