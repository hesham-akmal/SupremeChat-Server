import javafx.beans.property.ReadOnlyLongProperty;
import network_data.AuthUser;
import network_data.Command;
import network_data.Friend;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;


public class Server extends Thread {
    //Port must be forwarded
    //Create separate ports for signing in and signing up
    private static final int PORT_NUMBER = 3000;
    protected Socket socket;

    private Server(Socket socket) {
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        this.start();
    }

    public void run() {
        ObjectInputStream ois;
        ObjectOutputStream oos;
        Command command;
        try {
            //Creating ObjectOutputStream before ObjectInputStream IS A MUST to avoid blocking
            oos = new ObjectOutputStream(this.socket.getOutputStream());
            ois = new ObjectInputStream(this.socket.getInputStream());
            System.out.println("Reading command from user");
            //Read command from user
            command = (Command) ois.readObject();
            System.out.println("command read: " + command);

            //send success ack
            oos.writeObject(Command.success);

            AuthUser a = (AuthUser) ois.readObject();

            if (command == Command.signIn) {
                //Username does exist, compare passwords
                if (Database.instance.getAuthUsers().containsKey(a.getUsername())) {
                    if (Database.instance.getAuthUsers().get(a.getUsername()).getPassword().equals(a.getPassword())) {
                        //Success. Username found and pass correct
                        System.out.println("Success. Username found and pass correct");
                        oos.writeObject(Command.success);
                        //Update friend in Database //////////
                        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
                        Friend f = Database.instance.getFriends().get(a.getUsername());
                        f.setStatus(Friend.Status.Online);
                        f.setLastLogin(timeStamp);
                        f.setIP(a.getIP());
                    } else {
                        //Fail. Username found but pass incorrect
                        oos.writeObject(Command.fail); //
                        System.out.println("Fail. Username found but pass incorrect");
                    }
                } else {
                    //Username does not exists
                    oos.writeObject(Command.fail);
                    System.out.println("Username does not exists");
                }
            } else if (command == Command.signUp) { //sign up
                //check existing username
                if (Database.instance.getAuthUsers().containsKey(a.getUsername())) {
                    oos.writeObject(Command.fail);
                    System.out.println("existing username");
                } else {//sign up success
                    //Insert authUser in Database //////////
                    Database.instance.addAuthUser(a);
                    oos.writeObject(Command.success);
                    System.out.println("created user");
                    //Insert friend in Database //////////
                    String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
                    Database.instance.addFriend(new Friend(a.getUsername(), Friend.Status.Online, timeStamp, a.getIP()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException var12) {
                var12.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ServerSocket server = null;

        testPrintAll();
        //Database.instance.addFriend(new Friend("asd", Friend.Status.Online, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a").format(Calendar.getInstance().getTime()),
        //        "232.32.32.2"));

        try {
            server = new ServerSocket(PORT_NUMBER);

            while (true) {
                new Server(server.accept());
            }
        } catch (IOException var10) {
            System.out.println("Unable to start server.");
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException var9) {
                var9.printStackTrace();
            }
        }
    }

    private static void testPrintAll() {
        System.out.println("Test print all authUsers:\n");
        Set<String> keys = Database.instance.getAuthUsers().keySet();
        for (String key : keys) {
            AuthUser au = Database.instance.getAuthUsers().get(key);
            System.out.println("Value of " + key + " is: " + au.getUsername() + "," + au.getIP());
        }
        System.out.println("\n\n");

        System.out.println("Test print all friends:\n");
        keys = Database.instance.getFriends().keySet();
        for (String key : keys) {
            Friend f = Database.instance.getFriends().get(key);
            System.out.println("Value of " + key + " is: " + f.getUsername() + " , " + f.getLastLogin() + " , " + f.getStatus());
        }
    }
}
