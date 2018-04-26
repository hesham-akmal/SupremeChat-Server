import network_data.AuthUser;
import network_data.Command;
import network_data.Friend;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Server extends Thread {
    //Port must be forwarded
    //Create separate ports for signing and heartbeats
    private static final int SIGNING_PORT_NUMBER = 3000;
    protected Socket socket;
    private AuthUser authUser;

    private boolean heartbeatCheck = true;
    private int disconnectConsecutiveRetries = 1;
    private Timer hearbeatChecktimer;

    private void updateFriendOnline() {
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
        Friend f = Database.instance.getFriends().get(authUser.getUsername());
        f.setStatus(Friend.Status.Online);
        f.setLastLogin(timeStamp);
        f.setIP(authUser.getIP());
        Database.instance.syncFriends();
    }

    private void updateFriendOffline() {
        if (authUser == null) {
            return;
        }
        Friend f = Database.instance.getFriends().get(authUser.getUsername());
        f.setStatus(Friend.Status.Offline);
        Database.instance.syncFriends();
    }

    private void StartStatusCheck() {

        hearbeatChecktimer = new Timer("hearbeatChecktimer");//create a new Timer

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!heartbeatCheck)
                    UserDisconnected();
                else {
                    heartbeatCheck = false;
                    disconnectConsecutiveRetries = 1;
                }
            }
        };

        hearbeatChecktimer.scheduleAtFixedRate(timerTask, 1000, 2000);//this line starts the timer at the same time its executed
    }

    private void UserDisconnected() {
        System.out.println("disconnectConsecutiveRetries: " + disconnectConsecutiveRetries);
        if (disconnectConsecutiveRetries++ < 3)
            return;

        updateFriendOffline();
        System.out.println(authUser.getUsername() + " went offline");
        hearbeatChecktimer.cancel();
        hearbeatChecktimer.purge();
    }

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

            while (true) {

                System.out.print("\n Reading command from user.. ");
                //Read command from user
                command = (Command) ois.readObject();
                System.out.print(" Command read: " + command);

                switch (command) {
                    case signIn:

                        authUser = (AuthUser) ois.readObject();

                        //Username does exist, compare passwords
                        if (Database.instance.getAuthUsers().containsKey(authUser.getUsername())) {

                            String pass1 = Database.instance.getAuthUsers().get(authUser.getUsername()).getPassword();
                            String pass2 = authUser.getPassword();
                            if (pass1.equals(pass2)) {
                                //Success. Username found and pass correct
                                System.out.println("\nSuccess. Username found and pass correct");
                                oos.writeObject(Command.success);
                                oos.flush();
                                //Update friend in Database //////////
                                String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
                                Friend f = Database.instance.getFriends().get(authUser.getUsername());
                                f.setStatus(Friend.Status.Online);
                                f.setLastLogin(timeStamp);
                                f.setIP(authUser.getIP());
                                StartStatusCheck();
                            } else {
                                //Fail. Username found but pass incorrect
                                oos.writeObject(Command.fail);
                                oos.flush();
                                System.out.println("Fail. Username found but pass incorrect");
                            }
                        } else {
                            //Username does not exists
                            oos.writeObject(Command.fail);
                            oos.flush();
                            System.out.println("Username does not exists");
                        }

                        break;

                    case signUp:

                        authUser = (AuthUser) ois.readObject();

                        //check existing username
                        if (Database.instance.getAuthUsers().containsKey(authUser.getUsername())) {
                            oos.writeObject(Command.fail);
                            oos.flush();
                            System.out.println("existing username");
                        } else {//sign up success
                            //Insert authUser in Database //////////
                            Database.instance.addAuthUser(authUser);
                            oos.writeObject(Command.success);
                            oos.flush();
                            System.out.println("created user");
                            //Insert friend in Database //////////
                            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
                            Database.instance.addFriend(new Friend(authUser.getUsername(), Friend.Status.Online, timeStamp, authUser.getIP()));
                            StartStatusCheck();
                        }

                        break;

                    case heartbeat:

                        try {

                            if (authUser == null) {
                                System.out.println("authUser == null");
                                continue;
                            }

                            if (ois.readObject() == Command.heartbeat) {
                                System.out.print(" , " + authUser.getUsername() + " is online\n");
                                updateFriendOnline();
                                heartbeatCheck = true;
                            }

                        } catch (Exception e) {
                        }

                        break;

                    case search:

                        oos.writeObject(Command.success);
                        oos.flush();

                        String query = (String) ois.readObject();
                        System.out.println(query);

                        if (Database.instance.getFriends().containsKey(query)) {
                            oos.writeObject(Command.success);
                            oos.flush();

                            Friend f = Database.instance.getFriends().get(query);
                            oos.writeObject(f);
                            oos.flush();
                        }
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Probable user disconnection.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing thread for " + this.socket.getInetAddress().toString());
                this.socket.close();
            } catch (IOException var12) {
                var12.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ServerSocket server = null;

        testPrintAll();

        try {
            server = new ServerSocket(SIGNING_PORT_NUMBER);

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
        System.out.println("Test print all authUsers:");
        Set<String> keys = Database.instance.getAuthUsers().keySet();
        for (String key : keys) {
            AuthUser au = Database.instance.getAuthUsers().get(key);
            System.out.println("Value of " + key + " is: " + au.getUsername() + "," + au.getIP());
        }
        System.out.println("\n");

        System.out.println("Test print all friends:");
        keys = Database.instance.getFriends().keySet();
        for (String key : keys) {
            Friend f = Database.instance.getFriends().get(key);
            System.out.println("Value of " + key + " is: " + f.getUsername() + " , " + f.getLastLogin() + " , " + f.getStatus());
        }
    }
}
