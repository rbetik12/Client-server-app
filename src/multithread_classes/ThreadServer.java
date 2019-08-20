package multithread_classes;

import base_code.DBMS;
import base_code.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadServer extends Thread {
    private Socket socket;
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    private ArrayList<Message> messages;
    private String login;
    private Random random;

    public ThreadServer(Socket socket) throws IOException {
        random = new Random();
        this.socket = socket;
        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("New thread was successfully created, launching...");
        start();
    }

    @Override
    public void run() {
        System.out.println("Running...");
        try {
            login = socketInput.readLine();
            if (DBMS.findUsername(login))
                System.out.println("Known user connected " + login);
            else
                System.out.println("New user connected " + login);
            messages = DBMS.readMessages(login);
            while (true) {
//                System.out.println("here12");
                String action = socketInput.readLine();
//                System.out.println(action);
                switch (action) {
                    case ("1"):
                        int id = random.nextInt(Integer.MAX_VALUE);
                        String jsonMessage = socketInput.readLine();
                        while (DBMS.findID(id))
                            id = random.nextInt(Integer.MAX_VALUE);
                        Message newMessage = parse(jsonMessage, id);
                        messages.add(newMessage);
                        DBMS.writeID(id);
                        DBMS.writeMessages(login, messages);
                        socketOutput.write("Message was successfully received and saved\n");
                        break;
                    case ("2"):
                        socketOutput.write(buildMessagesJSON(messages));
                        socketOutput.write("Messages list query successfully completed\n");
                        break;
                    case ("3"):
                        boolean messageWasFound = false;
                        int messageID = Integer.parseInt(socketInput.readLine());
                        for (Message message : messages) {
                            if (message.id == messageID) {
                                messages.remove(message);
                                messageWasFound = true;
                                DBMS.removeID(messageID);
                                break;
                            }
                        }
                        if (messageWasFound) {
                            socketOutput.write("Message with ID: " + messageID + " was successfully removed\n");
                            DBMS.writeMessages(login, messages);
                        } else
                            socketOutput.write("Message with ID: " + messageID + " doesn't exist\n");
                        break;
                    case ("4"):
                        ArrayList<String> usernames = DBMS.readAllUsernames();
                        ArrayList<Message> allUsersMessages = new ArrayList<>();
                        for (String username : usernames) {
                            allUsersMessages.addAll(DBMS.readMessages(username));
                        }
                        socketOutput.write(buildMessagesJSON(allUsersMessages));
                        socketOutput.write("Query for all users messages for successfully satisfied\n");
                        break;
                    case ("5"):
//                        System.out.println("Server here0");
                        socketOutput.write("Closing connection...\n");
                        socketOutput.flush();
//                        System.out.println("Server here");
                        close();
                        break;
                }
                socketOutput.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server thread is down...");
            System.out.println("Saving user's messages to db...");
            try {
                DBMS.writeMessages(login, messages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                socketInput.close();
                socketOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildMessagesJSON(ArrayList<Message> messages) {
        StringBuilder jsonMessagesList = new StringBuilder("{\"messages\": [");
        for (int i = 0; i < messages.size(); i++) {
            String listJsonMessage = String.format("{\"id\": \"%s\", \"username\": \"%s\", \"date\": \"%s\", \"text\": \"%s\"}",
                    messages.get(i).id, messages.get(i).username, messages.get(i).date, messages.get(i).text);
            if (i != messages.size() - 1)
                listJsonMessage += ",";
            jsonMessagesList.append(listJsonMessage);
        }
        jsonMessagesList.append("]}\n");
        return jsonMessagesList.toString();
    }

    private Message parse(String message, int id) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(message);
        String[] info = new String[8];
        int i = 0;
        while (matcher.find()) {
            info[i] = matcher.group(1);
            i++;
        }
        return new Message(id, info[3], Long.parseLong(info[5]), info[7]);
    }
}
