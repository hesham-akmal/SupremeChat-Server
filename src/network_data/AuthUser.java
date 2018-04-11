package network_data;

import java.io.Serializable;

public class AuthUser implements Serializable {

    private boolean login;
    private String username;
    private String password;
    private String IP;

    public AuthUser(boolean login, String username, String password, String IP) {
        this.login = login;
        this.username = username;
        this.password = password;
        this.IP = IP;
    }

    public boolean isLogin() {
        return login;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIP() {
        return IP;
    }
}