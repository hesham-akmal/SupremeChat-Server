package network_data;

import java.io.Serializable;

public class AuthUser implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private String username;
    private String password;
    private String IP;

    public AuthUser(String username, String password,String IP) {
        this.username = username;
        this.password = password;
        this.IP = IP;
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