package base_code;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class DBMS {
    private static FileReader fileReader;
    private static FileWriter fileWriter;

    public static boolean findUsername(String username) throws IOException {
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

    public static void writeUsername(String username) throws IOException {
        fileWriter = new FileWriter("users.txt", true);
        fileWriter.write("\n" + username);
        fileWriter.close();
    }

    public static ArrayList<String> readAllUsernames() throws IOException {
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

    public static void writeMessages(String login, ArrayList<Message> messages) throws IOException {
        try {
            fileWriter = new FileWriter(login + ".txt");
        } catch (IOException e) {
            Files.write(Paths.get(login + ".txt"), "\n".getBytes());
        } finally {
            for (Message message : messages) {
                fileWriter.write(message.id + "\n");
                fileWriter.write(message.username + "\n");
                fileWriter.write(String.valueOf(message.date) + "\n");
                fileWriter.write(message.text + "\n");
            }
            fileWriter.close();
        }
    }

    public static ArrayList<Message> readMessages(String login) {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            fileReader = new FileReader(login + ".txt");
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

    public static void writeID(int id) throws IOException {
        try {
            fileWriter = new FileWriter("ids.txt", true);
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), "".getBytes());
        } finally {
            fileWriter.write(String.valueOf(id) + "\n");
            fileWriter.close();
        }
    }

    public static boolean findID(int id) throws IOException {
        try {
            fileReader = new FileReader("ids.txt");
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equals(String.valueOf(id)))
                    return true;
            }
            return false;
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), String.valueOf(id).getBytes());
            return false;
        } finally {
            fileReader.close();
        }
    }

    public static void removeID(int id) throws IOException {
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
            boolean delete = ids.delete();
            boolean b = temp.renameTo(ids);
        } catch (IOException e) {
            Files.write(Paths.get("ids.txt"), "".getBytes());
        }
    }
}
