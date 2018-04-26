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
            syncAuthUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hashtable<String, AuthUser> getAuthUsers() {
        try {
            ois = new ObjectInputStream(new FileInputStream("authUsers.DB"));
            authUsers = (Hashtable<String, AuthUser>) ois.readObject();
            ois.close();
            return authUsers;
        } catch (EOFException e) {
            authUsers = new Hashtable<>();
            return authUsers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void syncAuthUsers() {
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
        friends.put(friend.getUsername(), friend);
        try {
            syncFriends();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hashtable<String, Friend> getFriends() {
        try {
            ois = new ObjectInputStream(new FileInputStream("friends.DB"));
            friends = (Hashtable<String, Friend>) ois.readObject();
            ois.close();
            return friends;
        } catch (EOFException e) {
            friends = new Hashtable<>();
            return friends;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void syncFriends() {
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
