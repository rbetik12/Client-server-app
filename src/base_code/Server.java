package base_code;


import multithread_classes.FileLoaderThread;
import multithread_classes.ThreadServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class Server {


    public Server(int serverPort, int fileLoaderPort) throws IOException {
        new ServerListener(serverPort);
        new FileLoaderListener(fileLoaderPort);
        System.out.println("Servers started");
    }

    public class ServerListener extends Thread {
        private LinkedList<ThreadServer> serverThreads;
        private ServerSocket serverSocket;

        public ServerListener(int port) throws IOException {
            serverThreads = new LinkedList<>();
            serverSocket = new ServerSocket(port);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    try {
                        serverThreads.add(new ThreadServer(socket));
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class FileLoaderListener extends Thread {
        private LinkedList<FileLoaderThread> fileLoaderThreads;
        private ServerSocket serverSocket;

        public FileLoaderListener(int port) throws IOException {
            fileLoaderThreads = new LinkedList<>();
            serverSocket = new ServerSocket(port);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    try {
                        fileLoaderThreads.add(new FileLoaderThread(socket));
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(45777, 45778);
    }
}