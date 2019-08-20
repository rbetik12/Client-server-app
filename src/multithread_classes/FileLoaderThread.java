package multithread_classes;

import exceptions.ThreadClosed;

import java.io.*;
import java.net.Socket;

public class FileLoaderThread extends Thread {
    private Socket socket;
    private BufferedReader socketIn;

    public FileLoaderThread(Socket socket) throws IOException {
        this.socket = socket;
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        start();
    }

    @Override
    public void run() {
        String filename = null;
        try {
            filename = socketIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (DataInputStream socketFileInput = new DataInputStream(socket.getInputStream());
             OutputStream fileOutput = new FileOutputStream(new File(filename))) {
            byte[] buffer = new byte[4096];
            int countOfBytes = 1;
            while (true) {
                countOfBytes = socketFileInput.read(buffer);
                if (countOfBytes <= 0)
                    break;
                fileOutput.write(buffer, 0 , buffer.length);
            }
            socket.close();
            throw new ThreadClosed();
        } catch (IOException ignored) {
        }
    }
}
