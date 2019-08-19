package base_code;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client {
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    private int messagesCounter;
    private String login;

    public Client(String ip, int port, String login) {
        this.login = login;
        messagesCounter = 0;
        try (Socket socket = new Socket(ip, port)) {
            socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socketOutput.write(login + "\n");
            socketOutput.flush();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                clearScreen();
                drawMenu();
                String action = "";
                Pattern actionPatter = Pattern.compile("[1-5]");
                while (true) {
                    action = scanner.nextLine();
                    Matcher matcher = actionPatter.matcher(action);
                    if (matcher.find())
                        break;
                    else
                        System.out.println("Incorrect action was entered, enter new one");
                }

                switch (action) {
                    case ("1"):
                        clearScreen();
                        System.out.println("Write message text below: ");
                        String messageText = scanner.nextLine();
                        String message = String.format("{\"id\": \"%s\", \"username\": \"%s\", \"date\": \"%s\", \"text\": \"%s\"}", messagesCounter++, login, new Date().getTime(), messageText);
                        socketOutput.write("1\n");
                        socketOutput.write(message + "\n");
                        socketOutput.flush();
                        break;
                    case ("2"):
                        clearScreen();
                        socketOutput.write("2\n");
                        socketOutput.flush();
                        String jsonMessage = socketInput.readLine();
                        drawMessagesTable(jsonMessage);
                        System.out.println("Press enter to continue");
                        scanner.nextLine();
                        break;
                    case ("3"):
                        clearScreen();
                        System.out.println("Please enter id of message you want to delete: ");
                        int messageID = scanner.nextInt();
                        socketOutput.write("3\n");
                        socketOutput.write(messageID + "\n");
                        socketOutput.flush();
                        break;
                    case ("4"):
                        clearScreen();
                        socketOutput.write("4\n");
                        socketOutput.flush();
                        drawMessagesTable(socketInput.readLine());
                        System.out.println("Press enter to continue");
                        scanner.nextLine();
                        break;
                    case ("5"):
                        clearScreen();
                        System.out.println("To exit please enter your username, if you want to go back enter back");
                        String input = scanner.nextLine();
                        if (input.equals(login)) {
                            socketOutput.write("5\n");
                            close();
                        } else
                            action = "back";
                        break;
                }
                if (action.equals("5"))
                    break;
                else if (!action.equals("back")) {
                    System.out.println(socketInput.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection closed");
        }
    }

    private ArrayList<Message> parseMessagesList(String jsonMessagesList) {
        LinkedList<String> lexems = new LinkedList<>();
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonMessagesList);
        while (matcher.find()) {
            lexems.add(matcher.group(1));
        }
        lexems.pollFirst();
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < lexems.size(); i += 8) {
            messages.add(new Message(Integer.parseInt(lexems.get(i + 1)), lexems.get(i + 3), Long.parseLong(lexems.get(i + 5)), lexems.get(i + 7)));
        }
        return messages;
    }

    private void drawMenu() {
        System.out.println("==========================Menu==========================");
        System.out.println("Hey, " + login);
        System.out.println("1. Write new message");
        System.out.println("2. Show my messages");
        System.out.println("3. Delete my message");
        System.out.println("4. Show all users messages");
        System.out.println("5. Exit");
        System.out.println("Enter number of action you want to do...");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void close() throws IOException {
        socketInput.close();
        socketOutput.close();
    }

    private void drawMessagesTable(String jsonMessage) {
        ArrayList<Message> messagesList = parseMessagesList(jsonMessage);
        System.out.println("==========================Messages==========================");
        System.out.println("ID===========Username========Date===============Text========");
        for (Message messageFromList : messagesList) {
            System.out.println(messageFromList.id + " || " + messageFromList.username + " || " + messageFromList.date + " || " + messageFromList.text);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username it should contain only lowercase latin letters and be from 1 to 4 symbols length");
        String login;
        while (true) {
            login = scanner.next();
            if (Pattern.matches("[a-z]{1,4}", login)) {
                clearScreen();
                System.out.println("Logging in...");
                break;
            }
            System.out.println("Username you entered doesn\"t match the pattern");
        }
        Client client = new Client("127.0.0.1", 45777, login);
    }
}