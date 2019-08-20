package base_code;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
                Pattern actionPatter = Pattern.compile("[1-6]");
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
                        String sortType = "";
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
                        System.out.println("Press enter to continue");
                        scanner.nextLine();
                        System.out.println(socketInput.readLine());
                        break;
                    case ("5"):
                        clearScreen();
                        System.out.println("To exit please enter your username, if you want to go back enter back");
                        String input = scanner.nextLine();
                        if (input.equals(login)) {
                            socketOutput.write("5\n");
                        } else {
                            action = "back";
                            socketOutput.write("7\n");
                        }
                        socketOutput.flush();
//                        System.out.println("here");
                        if (!action.equals("back")) {
//                            System.out.println("here1");
                            System.out.println(socketInput.readLine());
                        }
                        break;
                    case ("6"):
//                        socketOutput.write("6\n");
                        String filename = scanner.nextLine();
                        try (Socket fileSocket = new Socket("127.0.0.1", 45778);
                             InputStream fileInput = new FileInputStream(new File("ids.txt"));
                             DataOutputStream fileSocketOut = new DataOutputStream(fileSocket.getOutputStream());
                             BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(fileSocket.getOutputStream()))) {
                            socketOut.write(filename);
                            socketOut.flush();
                            byte[] buffer = new byte[4096];
                            int countOfBytes = 1;
                            while(true) {
                                countOfBytes = fileInput.read(buffer);
                                if (countOfBytes <= 0)
                                    break;
                                fileSocketOut.write(buffer, 0, buffer.length);
                            }
                        }
                }
                if (action.equals("5")) {
//                    System.out.println("here2");
                    break;
                }
//                System.out.println("here3");
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
        System.out.println("6. Load file on server");
        System.out.println("Enter number of action you want to do...");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void close() throws IOException {
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
            System.out.println("Username you entered doesn\"t match the pattern");
        }
        Client client = new Client("127.0.0.1", 45777, login);
    }
}