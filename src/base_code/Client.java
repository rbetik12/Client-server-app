package base_code;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client {
    private final String login;

    public Client(String ip, int port, String login) {
        this.login = login;
        int messagesCounter = 0;
        try (Socket socket = new Socket(ip, port);
             BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            socketOutput.write(login + "\n");
            socketOutput.flush();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                clearScreen();
                drawMenu();
                String action;
                Pattern actionPatter = Pattern.compile("[1-7]");
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
                        System.out.println(socketInput.readLine());
                        break;
                    case ("2"):
                        clearScreen();
                        socketOutput.write("2\n");
                        socketOutput.flush();
                        String jsonMessage = socketInput.readLine();
                        drawMessagesTable(parseMessagesList(jsonMessage));
                        System.out.println("Press enter to continue");
                        scanner.nextLine();
                        System.out.println(socketInput.readLine());
                        break;
                    case ("3"):
                        clearScreen();
                        System.out.println("Please enter id of message you want to delete: ");
                        int messageID = scanner.nextInt();
                        socketOutput.write("3\n");
                        socketOutput.write(messageID + "\n");
                        socketOutput.flush();
                        System.out.println(socketInput.readLine());
                        break;
                    case ("4"):
                        clearScreen();
                        socketOutput.write("4\n");
                        socketOutput.flush();
                        System.out.println("Choose how you want to sort table by username (u) or date (d)");
                        ArrayList<Message> usersMessages = parseMessagesList(socketInput.readLine());
                        String sortType;
                        while (true) {
                            sortType = scanner.nextLine();
                            if (sortType.equals("u") || sortType.equals("d"))
                                break;
                            System.out.println("Please enter right type of sort");
                        }
                        if (sortType.equals("u")) {
                            Comparator<Message> compareByUsername = (o1, o2) -> o1.username.compareTo(o2.username);
                            usersMessages.sort(compareByUsername);
                        } else {
                            Comparator<Message> compareByDate = (o1, o2) -> Long.compare(o1.date, o2.date);
                            usersMessages.sort(compareByDate.reversed());
                        }
                        drawMessagesTable(usersMessages);
                        String filesJSON = socketInput.readLine();
                        drawFilesTable(parseFilesList(filesJSON));
                        System.out.println("Press enter to continue");
                        scanner.nextLine();
                        System.out.println(socketInput.readLine());
                        break;
                    case ("5"):
                        clearScreen();
                        System.out.println("To exit please enter your username, if you want to go back write back");
                        String input = scanner.nextLine();
                        if (input.equals(login)) {
                            socketOutput.write("5\n");
                        } else {
                            action = "back";
                            socketOutput.write("7\n");
                        }
                        socketOutput.flush();
                        if (!action.equals("back")) {
                            System.out.println(socketInput.readLine());
                        }
                        break;
                    case ("6"):
                        clearScreen();
                        System.out.println("Enter file name: ");
                        String filename = scanner.nextLine();
                        try (Socket fileSocket = new Socket("127.0.0.1", 45778);
                             InputStream fileInput = new FileInputStream(new File(filename));
                             DataOutputStream socketOut = new DataOutputStream(fileSocket.getOutputStream())) {
                            socketOut.writeUTF(filename);
                            socketOut.writeUTF("6");
                            byte[] buffer = new byte[4096];
                            int countOfBytes;
                            int result = 0;
                            while ((countOfBytes = fileInput.read(buffer)) > 0) {
                                result += countOfBytes;
                                socketOut.write(buffer, 0, countOfBytes);
                            }
                            System.out.println(result);
                        } catch (FileNotFoundException ignored) {
                            System.out.println("File doesn't exist, press enter to continue");
                            scanner.nextLine();
                        }
                        break;
                    case ("7"):
                        clearScreen();
                        System.out.println("Enter file name: ");
                        String filename1 = scanner.nextLine();
                        try (Socket getFileSocket = new Socket("127.0.0.1", 45778);
                             DataOutputStream socketOut = new DataOutputStream(getFileSocket.getOutputStream());
                             DataInputStream socketIn = new DataInputStream(getFileSocket.getInputStream())) {
                            socketOut.writeUTF(filename1);
                            socketOut.writeUTF("7");
                            if (socketIn.readUTF().equals("null")) {
                                System.out.println("File wasn't found on a server");
                                System.out.println("Press enter to continue");
                                scanner.nextLine();
                            } else {
                                OutputStream fileOutput = new FileOutputStream(new File(filename1));
                                byte[] buffer = new byte[4096];
                                int countOfBytes;
                                int result = 0;
                                while ((countOfBytes = socketIn.read(buffer)) > 0) {
                                    result += countOfBytes;
                                    fileOutput.write(buffer, 0, countOfBytes);
                                }
                                System.out.println(result);
                                fileOutput.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                if (action.equals("5")) {
                    break;
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

    private ArrayList<String> parseFilesList(String json) {
        ArrayList<String> lexems = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            lexems.add(matcher.group(1));
        }
        lexems.remove(0);
        return lexems;
    }

    private void drawMenu() {
        System.out.println("==========================Menu==========================");
        System.out.println("Hey, " + login);
        System.out.println("1. Write new message");
        System.out.println("2. Show my messages");
        System.out.println("3. Delete my message");
        System.out.println("4. Show all users messages and files");
        System.out.println("5. Exit");
        System.out.println("6. Load file on server");
        System.out.println("7. Get file from server");
        System.out.println("Enter number of action you want to do...");
    }

    private void drawFilesTable(ArrayList<String> files) {
        System.out.println("==========================Files==========================");
        for (String file : files) {
            System.out.println(file);
        }
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void drawMessagesTable(ArrayList<Message> messagesList) {
        System.out.println("==========================Messages==========================");
        System.out.println("ID===========Username========Date===============Text========");
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        for (Message messageFromList : messagesList) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(messageFromList.date));
            System.out.println(messageFromList.id + " || " + messageFromList.username + " || " + dateFormat.format(messageFromList.date) + " || " + messageFromList.text);
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
            System.out.println("Username you entered doesn't match the pattern");
        }
        Client client = new Client("127.0.0.1", 45777, login);
    }
}