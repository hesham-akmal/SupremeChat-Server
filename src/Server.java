import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Server extends Thread {
    //Port must be forwarded, allowed inbound rules in firewall
    private static final int PORT_NUMBER = 8109;
    protected Socket socket;

    private Server(Socket socket) {
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        this.start();
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String request;
            while ((request = br.readLine()) != null) {
                System.out.println("Message received:" + request);
                request = request + '\n';
                out.write(request.getBytes());
            }
        } catch (IOException var13) {
            System.out.println("Unable to get streams from client");
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
