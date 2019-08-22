package base_code;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class DBMS {
    private static FileReader fileReader;
    private static FileWriter fileWriter;
    private static String messagesDir = "messages";
    private static String filesDir = "files";
    public synchronized static boolean findUsername(String username) throws IOException {
        try {
            fileReader = new FileReader("users.txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equals(username)) {
                    fileReader.close();
                    return true;
                }
            }
            fileReader.close();
            writeUsername(username);
            return false;
        } catch (IOException e) {
            Files.write(Paths.get("users.txt"), username.getBytes());
            return false;
        }
    }

    private synchronized static void writeUsername(String username) throws IOException {
        fileWriter = new FileWriter("users.txt", true);
        fileWriter.write("\n" + username);
        fileWriter.close();
    }

    public synchronized static ArrayList<String> readAllUsernames() throws IOException {
        ArrayList<String> usernames = new ArrayList<>();
        try {
            fileReader = new FileReader("users.txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                usernames.add(scanner.nextLine());
            }
            return usernames;
        } catch (IOException e) {
            Files.write(Paths.get("users.txt"), "".getBytes());
            return new ArrayList<>();
        }
    }

    public synchronized static void writeMessages(String login, ArrayList<Message> messages) throws IOException {
        try {
            checkDir(messagesDir);
            fileWriter = new FileWriter(messagesDir + "/" + login + ".txt");
        } catch (IOException e) {
            Files.write(Paths.get(messagesDir + "/" + login + ".txt"), "\n".getBytes());
        } finally {
            for (Message message : messages) {
                fileWriter.write(message.id + "\n");
                fileWriter.write(message.username + "\n");
                fileWriter.write(message.date + "\n");
                fileWriter.write(message.text + "\n");
            }
            fileWriter.close();
        }
    }

    public synchronized static ArrayList<Message> readMessages(String login) {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            checkDir(messagesDir);
            fileReader = new FileReader(messagesDir + "/" + login + ".txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                String id = scanner.nextLine();
                String username = scanner.nextLine();
                String date = scanner.nextLine();
                String text = scanner.nextLine();
                messages.add(new Message(Integer.parseInt(id), username, Long.parseLong(date), text));
            }
            fileReader.close();
            return messages;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public synchronized static void writeID(int id) throws IOException {
        try {
            fileWriter = new FileWriter("ids.txt", true);
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), "".getBytes());
        } finally {
            fileWriter.write(id + "\n");
            fileWriter.close();
        }
    }

    public synchronized static boolean findID(int id) throws IOException {
        try {
            fileReader = new FileReader("ids.txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equals(String.valueOf(id)))
                    return true;
            }
            fileReader.close();
            return false;
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), String.valueOf(id).getBytes());
            return false;
        }
    }

    public synchronized static void removeID(int id) throws IOException {
        try {
            File temp = new File("temp.txt");
            File ids = new File("ids.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            BufferedReader reader = new BufferedReader(new FileReader(ids));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if (trimmedLine.equals(String.valueOf(id)))
                    currentLine = "";
                writer.write(currentLine + "\n");
            }
            writer.close();
            reader.close();
            ids.delete();
            temp.renameTo(ids);
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), "".getBytes());
        }
    }

    public synchronized static void writeFilename(String filename) throws IOException {
        try {
            fileWriter = new FileWriter( "files.txt", true);
        } catch (IOException e) {
            Files.write(Paths.get("files.txt"), filename.getBytes());
        } finally {
            fileWriter.write(filename + "\n");
            fileWriter.close();
        }
    }

    public synchronized static boolean findFilename(String filename) {
        try {
            fileReader = new FileReader("files.txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equals(filename))
                    return true;
            }
            fileReader.close();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized static ArrayList<String> getFilenames() {
        try {
            fileReader = new FileReader("files.txt");
            Scanner scanner = new Scanner(fileReader);
            ArrayList<String> filenames = new ArrayList<>();
            while (scanner.hasNextLine()) {
                filenames.add(scanner.nextLine());
            }
            fileReader.close();
            return filenames;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public synchronized static void checkDir(String path){
        Path dirPath = Paths.get(path);
        boolean dirExists = Files.exists(dirPath);
        if (!dirExists) {
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
