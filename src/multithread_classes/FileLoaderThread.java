package multithread_classes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())){
            byte[] buffer = new byte[4096];
            int countOfBytes = 1;
            int totalBytes = 0;
            while(true) {
                countOfBytes = dataInputStream.read(buffer);
                if (countOfBytes <= 0)
                    break;
                totalBytes += countOfBytes;
            }
            System.out.println(totalBytes);
            socket.close();
        }catch (IOException ignored){}
    }
}
