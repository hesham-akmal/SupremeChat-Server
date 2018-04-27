package network_data;

import java.io.Serializable;

public class AuthUser implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private String username;
    private String password;

    public AuthUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}