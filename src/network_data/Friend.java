package network_data;

import java.io.Serializable;

public class Friend implements Serializable {
    private static final long serialVersionUID = 6519685098267757690L;
    private String username;
    private Boolean online;
    private String lastLogin;
    private String IP;

    public Friend(String username, Boolean online, String lastLogin, String IP) {
        this.username = username;
        this.online = online;
        this.lastLogin = lastLogin;
        this.IP = IP;
    }

    public String getUsername() {
        return username;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}