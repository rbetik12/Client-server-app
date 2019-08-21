package multithread_classes;

import base_code.DBMS;
import base_code.Server;

import java.io.*;
import java.net.Socket;

public class FileLoaderThread extends Thread {
    private Socket socket;
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
                    OutputStream fileOutput = new FileOutputStream(new File(filename));
                    byte[] buffer = new byte[4096];
                    int countOfBytes = 1;
                    int result = 0;
                    while ((countOfBytes = socketIn.read(buffer)) > 0) {
                        result += countOfBytes;
                        fileOutput.write(buffer, 0, countOfBytes);
                    }
                    System.out.println(result);
                    socketIn.close();
                    fileOutput.close();
                    DBMS.writeFilename(filename);
                    close();
                    System.out.println(filename + " was successfully written to server");
                }
                else {
                    if (DBMS.findFilename(filename)){
                        socketOut.writeUTF("okay");
                        InputStream fileInput = new FileInputStream(new File(filename));
                        byte[] buffer = new byte[4096];
                        int countOfBytes;
                        int result = 0;
                        while ((countOfBytes = fileInput.read(buffer)) > 0){
                            result += countOfBytes;
                            socketOut.write(buffer, 0, countOfBytes);
                        }
                        System.out.println(result);
                        socketOut.close();
                        fileInput.close();
                        close();
                        System.out.println(filename + " was sent to client");
                    }
                    else
                        socketOut.writeUTF("null");
                    socketOut.close();
                    close();
                }
            } catch (IOException ignored) {
            }
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
