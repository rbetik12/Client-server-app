package multithread_classes;

import base_code.DBMS;
import base_code.Server;

import java.io.*;
import java.net.Socket;

public class FileLoaderThread extends Thread {
    private final Socket socket;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;

    public FileLoaderThread(Socket socket) throws IOException {
        this.socket = socket;
        socketIn = new DataInputStream(socket.getInputStream());
        socketOut = new DataOutputStream(socket.getOutputStream());
        start();
    }

    @Override
    public void run() {
        String filename = null;
        try {
            filename = socketIn.readUTF();
            System.out.println(filename);
        } catch (IOException e) {
            close();
        }
        if (filename == null) {
            close();
        } else {
            try {
                String action = socketIn.readUTF();
                if (action.equals("6")) {
                    DBMS.checkDir("files");
                    OutputStream fileOutput = new FileOutputStream(new File("files/" + filename));
                    byte[] buffer = new byte[4096];
                    int countOfBytes;
                    while ((countOfBytes = socketIn.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, countOfBytes);
                    }
                    fileOutput.close();
                    DBMS.writeFilename(filename);
                    System.out.println(filename + " was successfully written to server");
                } else {
                    if (DBMS.findFilename(filename)) {
                        socketOut.writeUTF("okay");
                        DBMS.checkDir("files");
                        InputStream fileInput = new FileInputStream(new File("files/" + filename));
                        byte[] buffer = new byte[4096];
                        int countOfBytes;
                        while ((countOfBytes = fileInput.read(buffer)) > 0) {
                            socketOut.write(buffer, 0, countOfBytes);
                        }
                        fileInput.close();
                        System.out.println(filename + " was sent to client");
                    } else
                        socketOut.writeUTF("null");
                    close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    private synchronized void close() {
        try {
            if (!socket.isClosed()) {
                socketOut.close();
                socketIn.close();
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
