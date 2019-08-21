package multithread_classes;

import base_code.Server;

import java.io.*;
import java.net.Socket;

public class FileLoaderThread extends Thread {
    private Socket socket;
    private DataInputStream socketIn;

    public FileLoaderThread(Socket socket) throws IOException {
        this.socket = socket;
        socketIn = new DataInputStream(socket.getInputStream());
        start();
    }

    @Override
    public void run() {
        String filename = null;
        try {
            filename = socketIn.readUTF();
            System.out.println(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStream fileOutput = new FileOutputStream(new File("new" + filename));
            byte[] buffer = new byte[4096];
            int countOfBytes = 1;
            while ((countOfBytes = socketIn.read(buffer)) > 0) {
                System.out.println(countOfBytes);
                fileOutput.write(buffer, 0, countOfBytes);
            }
            socketIn.close();
            fileOutput.close();
            System.out.println(filename + " was successfully written to server");
            close();
        } catch (IOException ignored) {
        }
    }

    private void close() {
        try {
            if (!socket.isClosed()) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
                for (FileLoaderThread thread : Server.getFileLoaderThreads()) {
                    if (thread == this) {
                        thread.interrupt();
                        thread.stop();
                        System.out.println(thread.isAlive());
                        Server.getFileLoaderThreads().remove(this);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
