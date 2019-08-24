package base_code;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 */
public class Client {
    private final String login;

    /**
     * Opens new socket connection and output and input channels. Handles all communication with server.
     *
     * @param ip    socket ip
     * @param port  socket port
     * @param login user's login
     */
    public Client(String ip, int port, String login) {
        this.login = login;
        try (Socket socket = new Socket(ip, port);
             BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            socketOutput.write(login + "\n");
            socketOutput.flush();
            Scanner scanner = new Scanner(System.in);
            while (true) {
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
                        writeMessage(socketInput, socketOutput, scanner);
                        break;
                    case ("2"):
                        getUserMessagesList(socketInput, socketOutput, scanner);
                        break;
                    case ("3"):
                        deleteMessage(socketInput, socketOutput, scanner);
                        break;
                    case ("4"):
                        getUsersMessagesAndFilesList(socketInput, socketOutput, scanner);
                        break;
                    case ("5"):
                        exit(socketInput, socketOutput, scanner);
                        break;
                    case ("6"):
                        loadFile(scanner);
                        break;
                    case ("7"):
                        getFile(scanner);
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

    /**
     * Send message that user wrote to server, in JSON format.
     * Example:
     * {
     * "id": "1",  #Will be unique and set on server
     * "username": "User's login",
     * "date": "Current date",
     * "text": "Text that was written by user"
     * }
     *
     * @param socketInput  Socket input stream
     * @param socketOutput Socket output stream
     * @param scanner      User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void writeMessage(BufferedReader socketInput, BufferedWriter socketOutput, Scanner scanner) throws IOException {
        System.out.println("Write message text below: ");
        String messageText = scanner.nextLine();
        String message = String.format("{\"id\": \"%s\", \"username\": \"%s\", \"date\": \"%s\", \"text\": \"%s\"}", 0, login, new Date().getTime(), messageText);
        socketOutput.write("1\n");
        socketOutput.write(message + "\n");
        socketOutput.flush();
        System.out.println(socketInput.readLine());
    }


    /**
     * Receives messages list from a server in JSON format, parses it with parseMessagesList()
     *
     * @param socketInput  Socket input stream
     * @param socketOutput Socket output stream
     * @param scanner      User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void getUserMessagesList(BufferedReader socketInput, BufferedWriter socketOutput, Scanner scanner) throws IOException {
        socketOutput.write("2\n");
        socketOutput.flush();
        String jsonMessage = socketInput.readLine();
        drawMessagesTable(parseMessagesList(jsonMessage));
        System.out.println("Press enter to continue");
        scanner.nextLine();
        System.out.println(socketInput.readLine());
    }

    /**
     * Sends to server ID of message that should be deleted, message ID is retrieved from user's input
     *
     * @param socketInput  Socket input stream
     * @param socketOutput Socket output stream
     * @param scanner      User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void deleteMessage(BufferedReader socketInput, BufferedWriter socketOutput, Scanner scanner) throws IOException {
        System.out.println("Please enter id of message you want to delete: ");
        int messageID = scanner.nextInt();
        socketOutput.write("3\n");
        socketOutput.write(messageID + "\n");
        socketOutput.flush();
        System.out.println(socketInput.readLine());
    }

    /**
     * Retrieves messages and files list from server, sorts it and shows it to user
     *
     * @param socketInput  Socket input stream
     * @param socketOutput Socket output stream
     * @param scanner      User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void getUsersMessagesAndFilesList(BufferedReader socketInput, BufferedWriter socketOutput, Scanner scanner) throws IOException {
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
    }

    /**
     * Initiates closing procedure for client socket and server thread.
     *
     * @param socketInput  Socket input stream
     * @param socketOutput Socket output stream
     * @param scanner      User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void exit(BufferedReader socketInput, BufferedWriter socketOutput, Scanner scanner) throws IOException {
        System.out.println("To exit please enter your username, if you want to go back write back");
        String input = scanner.nextLine();
        if (input.equals(login)) {
            socketOutput.write("5\n");
        } else {
            input = "back";
            socketOutput.write("7\n");
        }
        socketOutput.flush();
        if (!input.equals("back")) {
            System.out.println(socketInput.readLine());
        }
    }

    /**
     * Reads file that user asked to load to server, if that file wasn't found on a local computer then it writes an
     * error to console. Reads file in a small buffer of 4096 bytes and sends it to a server
     *
     * @param scanner User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void loadFile(Scanner scanner) throws IOException {
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
            System.out.println("Total bytes send: " + result);
        } catch (FileNotFoundException ignored) {
            System.out.println("File doesn't exist, press enter to continue");
            scanner.nextLine();
        }
    }

    /**
     * If user entered file name that doesn't exist on a server, writes an error to console. Otherwise, starts file
     * reading from a server stream in a buffer of 4096 bytes.
     *
     * @param scanner User's input reader
     * @throws IOException Whether any problem with IO occurs
     */
    private void getFile(Scanner scanner) throws IOException {
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
    }

    /**
     * Parses string of json and returns a list of messages
     *
     * @param jsonMessagesList String of JSON list with messages
     * @return ArrayList of messages
     */
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

    /**
     * Parses string of json and returns a list of files
     *
     * @param json String of JSON list with file names
     * @return ArrayList of file names
     */
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

    /**
     * Draws menu
     */
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

    /**
     * Draws files table
     *
     * @param files List of file names
     */
    private void drawFilesTable(ArrayList<String> files) {
        System.out.println("==========================Files==========================");
        for (String file : files) {
            System.out.println(file);
        }
    }

    /**
     * Draws message table
     * Example of message:
     * id || username || date || text
     *
     * @param messagesList List of messages objects
     */
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
                System.out.println("Logging in...");
                break;
            }
            System.out.println("Username you entered doesn't match the pattern");
        }
        Client client = new Client("127.0.0.1", 45777, login);
    }
}