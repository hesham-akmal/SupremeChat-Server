import network_data.AuthUser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;


public class Server extends Thread {
    //Port must be forwarded
    //Create separate ports for signing in and signing up
    private static final int PORT_NUMBER = 3000;
    protected Socket socket;

    private Hashtable<String, AuthUser> authUsers;

    private Server(Socket socket) {
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        this.start();
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;
        ObjectInputStream oin;
        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();
            oin = new ObjectInputStream(in);

            AuthUser a = (AuthUser) oin.readObject();

            if (a.isLogin())
            {
                //Check if username exists
                if(authUsers.containsKey(a.getUsername())){
                    //Username does exist, compare passwords
                    if( authUsers.get(a.getUsername()).getPassword().equals(a.getPassword()) ){
                        //Success, return stuff

                    }
                }
                else
                {
                    //username doesnt exist
                }

            } else { //sign up

                //check existing username
                if(authUsers.containsKey(a.getUsername()))
                {
                    //username already exists
                }
                else
                    {//sign up success
                    authUsers.put(a.getUsername() , a);
                    //return success
                }
            }

            //System.out.println(a.toString());

        } catch (IOException var13) {
            var13.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                this.socket.close();
            } catch (IOException var12) {
                var12.printStackTrace();
            }

        }

    }

    public static void main(String[] args) {
        ServerSocket server = null;


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
}
