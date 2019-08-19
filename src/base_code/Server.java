package base_code;

import multithread_classes.ThreadServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private LinkedList<ThreadServer> serverThreads;

    public Server(int port) throws IOException {
        serverThreads = new LinkedList<>();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    serverThreads.add(new ThreadServer(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(45777);
    }
}