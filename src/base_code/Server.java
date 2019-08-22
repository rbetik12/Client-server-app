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

    public static class ServerListener extends Thread {
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
                        removeDeadThreads(serverThreads);
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

    public static class FileLoaderListener extends Thread {
        private ServerSocket serverSocket;
        private static LinkedList<FileLoaderThread> fileLoaderThreads;

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
                        removeDeadThreads(fileLoaderThreads);
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

    private static <T extends Thread> LinkedList<T> removeDeadThreads(LinkedList<T> threadsList) {
        for (T el : threadsList) {
            if (!el.isAlive())
                threadsList.remove(el);
        }
        return threadsList;
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(45777, 45778);
    }
}