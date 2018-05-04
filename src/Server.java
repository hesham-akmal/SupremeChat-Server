import network_data.AuthUser;
import network_data.Command;
import network_data.Friend;
import network_data.MessagePacket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server extends Thread {
    //Port must be forwarded
    //Create separate ports for signing and heartbeats
    private static final int SIGNING_PORT_NUMBER = 3000;

    private static HashMap<String, ObjectOutputStream> allOOS = new HashMap<>();

    protected Socket socket;
    private AuthUser authUser;

    private boolean heartbeatCheck = true;
    private int disconnectConsecutiveRetries = 1;
    private Timer hearbeatChecktimer;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private void updateFriendOnline() {
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(Calendar.getInstance().getTime());
        Friend f = Database.instance.getFriends().get(authUser.getUsername());
        f.setOnline(true);
        f.setLastLogin(timeStamp);
        f.setIP(socket.getInetAddress().toString());
        Database.instance.SaveFriendsToDB();
    }

    private void updateFriendOffline() {
        if (authUser == null) {
            return;
        }
        Friend f = Database.instance.getFriends().get(authUser.getUsername());
        f.setOnline(false);
        Database.instance.SaveFriendsToDB();
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

        hearbeatChecktimer.scheduleAtFixedRate(timerTask, 1000, 2000);
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

    private void LoggedInSuccessfully() {
        allOOS.put(this.authUser.getUsername(), this.oos);
        StartStatusCheck();
    }

    public void run() {
        Command command;

        try {
            //Creating ObjectOutputStream before ObjectInputStream IS A MUST to avoid blocking
            oos = new ObjectOutputStream(this.socket.getOutputStream());
            ois = new ObjectInputStream(this.socket.getInputStream());

            while (true) {

                System.out.print("Reading command. ");
                //Read command from user
                command = (Command) ois.readObject();
                System.out.println("Command read: " + command);

                switch (command) {
                    case signIn:

                        authUser = (AuthUser) ois.readObject();

                        //Username does exist, compare passwords
                        if (Database.instance.getAuthUsers().containsKey(authUser.getUsername())) {

                            String pass1 = Database.instance.getAuthUsers().get(authUser.getUsername()).getPassword();
                            String pass2 = authUser.getPassword();
                            if (pass1.equals(pass2)) {
                                //Success. Username found and pass correct
                                System.out.println("Success. Username found and pass correct");
                                oos.writeObject(Command.success);
                                oos.flush();
                                LoggedInSuccessfully();
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

                    case signInAuto:

                        authUser = (AuthUser) ois.readObject();
                        LoggedInSuccessfully();

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
                            Database.instance.addFriend(new Friend(authUser.getUsername(), true, timeStamp, socket.getInetAddress().toString()));
                            LoggedInSuccessfully();
                        }

                        break;

                    case heartbeat:

                        if (authUser == null) {
                            System.out.println("\nauthUser == null");
                            break;
                        }

                        //System.out.print(" , " + authUser.getUsername() + " is online\n");
                        updateFriendOnline();
                        heartbeatCheck = true;

                        break;

                    case search:

                        String query = (String) ois.readObject();
                        System.out.println(query);

                        if (Database.instance.getFriends().containsKey(query)) {
                            oos.writeObject(Command.success);
                            oos.flush();

                            Friend f = Database.instance.getFriends().get(query);
                            oos.writeObject(f);
                            oos.flush();
                        } else {
                            oos.writeObject(Command.fail);
                            oos.flush();
                        }

                        break;

                    case sendFriends:

                        //receive user friend list
                        Hashtable<String, Friend> user_friend_list = (Hashtable<String, Friend>) ois.readObject();

                        //create new friend list, fill it with the user friends, and their latest IPs
                        Hashtable<String, Friend> friend_list = new Hashtable<>();
                        for (Map.Entry<String, Friend> fr : user_friend_list.entrySet()) {

                            if (!Database.instance.getFriends().containsKey(fr.getKey())) {
                                System.out.println("Friend: " + fr.getKey() + " Not found in DB!");
                            } else {
                                Friend f = Database.instance.getFriends().get(fr.getKey());
                                friend_list.put(f.getUsername(), f);
                            }
                        }

                        //Send friend list with latest IPs to user
                        oos.writeObject(friend_list);
                        oos.flush();

                        break;

                    case sendMsg:

                        try {
                            MessagePacket mp = (MessagePacket) ois.readObject();

                            ObjectOutputStream ReceiverOOS = allOOS.get(mp.getReceiver());

                            System.out.println("RECEIVER: " + mp.getReceiver());
                            System.out.println(ReceiverOOS);

                            ReceiverOOS.writeObject(Command.sendMsg);
                            ReceiverOOS.flush();

                            ReceiverOOS.writeObject(mp);
                            ReceiverOOS.flush();

                        } catch (Exception v) {
                            v.printStackTrace();
                            System.out.println("MSG NOT SEND!");
                        }

                        break;

                    case sendGroupMsg:
                        try {
                            MessagePacket mp = (MessagePacket) ois.readObject();

                            for (int i =0; i < mp.getListOfRecievers().size(); i++){
                                ObjectOutputStream ReceiverOOS = allOOS.get(mp.getListOfRecievers().get(i));
                                System.out.println("RECEIVER: " + mp.getReceiver());
                                System.out.println(ReceiverOOS);
                                ReceiverOOS.writeObject(Command.sendMsg);
                                ReceiverOOS.flush();

                                ReceiverOOS.writeObject(mp);
                                ReceiverOOS.flush();
                            }


                        } catch (Exception v) {
                            v.printStackTrace();
                            System.out.println("MSG NOT SEND!");
                        }

                        break;

                    case createNewGroup:


                        ArrayList<String> selectedFriendsNames = (ArrayList<String>) ois.readObject();

                        ArrayList<Friend> selectedFriends = new ArrayList<>();

                        for (String FriendName : selectedFriendsNames)
                            if (Database.instance.getFriends().containsKey(FriendName))
                                selectedFriends.add(Database.instance.getFriends().get(FriendName));

                        for (Friend f : selectedFriends) {
                            try {
                                if (!f.getOnline()) {
                                    System.out.println(f.getUsername() + ":invitation not sent.");
                                    continue;
                                }

                                ObjectOutputStream ReceiverOOS = allOOS.get(f.getUsername());

                                ReceiverOOS.writeObject(Command.createNewGroup);
                                ReceiverOOS.flush();

                                ReceiverOOS.writeObject(selectedFriends);
                                ReceiverOOS.flush();

                                System.out.println(f.getUsername() + ":invitation sent.");
                            } catch (Exception v) {
                                System.out.println(f.getUsername() + ":invitation not sent.");
                            }
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
        System.out.println("Test print all friends:");
        Set<String> keys = Database.instance.getFriends().keySet();
        for (String key : keys) {
            Friend f = Database.instance.getFriends().get(key);
            System.out.println(f.getUsername() + " , " + f.getLastLogin() + " , " + f.getIP() + " , " + f.getOnline());
        }
    }
}
