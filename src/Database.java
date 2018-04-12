import network_data.AuthUser;
import network_data.Friend;

import java.io.*;
import java.util.Hashtable;

public class Database {

    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static Hashtable<String, AuthUser> authUsers;
    private static Hashtable<String, Friend> friends;

    public static Database instance = new Database();

    private Database() {
        authUsers = getAuthUsers();
        friends = getFriends();
    }

    public void addAuthUser(AuthUser authUser) {
        authUsers.put(authUser.getUsername(), authUser);
        try {
            saveAuthUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hashtable<String, AuthUser> getAuthUsers() {
        try {
            ois = new ObjectInputStream(new FileInputStream("authUsers.DB"));
            Hashtable<String, AuthUser> au = (Hashtable<String, AuthUser>) ois.readObject();
            ois.close();
            return au;
        } catch (EOFException e) {
            authUsers = new Hashtable<>();
            return authUsers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveAuthUsers() {
        try {
            oos = new ObjectOutputStream(new FileOutputStream("authUsers.DB"));
            oos.writeObject(authUsers);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFriend(Friend friend) {
        friends.put(friend.getUsername(),friend);
        try {
            saveFriends();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hashtable<String, Friend> getFriends() {
        try {
            ois = new ObjectInputStream(new FileInputStream("friends.DB"));
            Hashtable<String, Friend> fr = (Hashtable<String, Friend>) ois.readObject();
            ois.close();
            return fr;
        } catch (EOFException e) {
            friends = new Hashtable<>();
            return friends;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveFriends() {
        try {
            oos = new ObjectOutputStream(new FileOutputStream("friends.DB"));
            oos.writeObject(friends);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
