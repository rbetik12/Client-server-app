import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private Socket clientSocket;
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    private ArrayList<Message> messages;
    private String login;
    private int idCounter;

    public Server(int port) throws IOException {
        System.out.println("Server started");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            clientSocket = serverSocket.accept();
            socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            login = socketInput.readLine();
            idCounter = DBMS.readIDCounter();
            if (DBMS.findUsername(login))
                System.out.println("Greetings to old 'nown " + login);
            else
                System.out.println("Yo we've got a newcomer " + login);
            messages = DBMS.readMessages(login);
            while (true) {
                String action = socketInput.readLine();
                System.out.println(action);
                switch (action) {
                    case ("1"):
                        String jsonMessage = socketInput.readLine();
                        Message newMessage = parse(jsonMessage);
                        messages.add(newMessage);
                        ++idCounter;
                        socketOutput.write("Message was successfully received and saved\n");
                        break;
                    case ("2"):
                        StringBuilder jsonMessagesList = new StringBuilder("{\"messages\": [");
                        for (int i = 0; i < messages.size(); i++) {
                            String listJsonMessage = String.format("{\"id\": \"%s\", \"username\": \"%s\", \"date\": \"%s\", \"text\": \"%s\"}",
                                    messages.get(i).id, messages.get(i).username, messages.get(i).date, messages.get(i).text);
                            if (i != messages.size() - 1)
                                listJsonMessage += ",";
                            jsonMessagesList.append(listJsonMessage);
                        }
                        jsonMessagesList.append("]}\n");
                        socketOutput.write(jsonMessagesList.toString());
                        socketOutput.write("Messages list query successfully completed\n");
                        break;
                    case ("3"):
                        boolean messageWasFound = false;
                        int messageID = Integer.parseInt(socketInput.readLine());
                        for (Message message: messages) {
                            if (message.id == messageID) {
                                messages.remove(message);
                                messageWasFound = true;
                                break;
                            }
                        }
                        if (messageWasFound)
                            socketOutput.write("Message with ID: " + messageID + " was successfully removed\n");
                        else
                            socketOutput.write("Message with ID: " + messageID + " doesn't exist\n");
                        break;
                    case ("4"):
                        clientSocket.close();
                        socketOutput.write("Closing connection...\n");
                        break;
                }
                socketOutput.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server is down...");
            System.out.println("Saving user's messages to db...");
            DBMS.writeMessages(login, messages);
            DBMS.writeIDCounter(idCounter);
        }
    }

    private Message parse(String message) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(message);
        String[] info = new String[8];
        int i = 0;
        while (matcher.find()) {
            info[i] = matcher.group(1);
            i++;
        }
        return new Message(idCounter, info[3], Long.parseLong(info[5]), info[7]);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(45777);
    }
}