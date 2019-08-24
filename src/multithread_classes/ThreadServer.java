package multithread_classes;

import base_code.DBMS;
import base_code.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * That thread is responsible for serving user requests for messages
 */
public class ThreadServer extends Thread {
    private final Socket socket;
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    private ArrayList<Message> messages;
    private String login;
    private final Random random;

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
                String action = socketInput.readLine();
                switch (action) {
                    case ("1"):
                        saveMessage();
                        break;
                    case ("2"):
                        sendUsersMessagesList();
                        break;
                    case ("3"):
                        deleteMessage();
                        break;
                    case ("4"):
                        sendAllMessagesAndFiles();
                        break;
                    case ("5"):
                        closeSocket();
                        break;
                }
                if (action.equals("5"))
                    break;
                socketOutput.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server thread is down...");
            System.out.println("Saving " + login + " messages to db...");
            try {
                DBMS.writeMessages(login, messages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes all socket connections
     *
     * @throws IOException when IO error occurs
     */
    private void closeSocket() throws IOException {
        socketOutput.write("Closing connection...\n");
        socketOutput.flush();
        close();
    }

    /**
     * Sends to client all users messages and all files that stored on server
     *
     * @throws IOException when IO occurs
     */
    private void sendAllMessagesAndFiles() throws IOException {
        ArrayList<String> usernames = DBMS.readAllUsernames();
        ArrayList<Message> allUsersMessages = new ArrayList<>();
        for (String username : usernames) {
            allUsersMessages.addAll(DBMS.readMessages(username));
        }
        socketOutput.write(buildMessagesJSON(allUsersMessages));
        socketOutput.write(buildFilesJSON(DBMS.getFilenames()));
        socketOutput.write("Query for all users messages and files successfully satisfied\n");
    }

    /**
     * Receives ID of message that should be deleted and tries to delete it. Result of this operation is send back
     * to client
     *
     * @throws IOException when IO error occurs
     */
    private void deleteMessage() throws IOException {
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
    }

    /**
     * Sends JSON messages list to client
     *
     * @throws IOException when IO error occurs
     */
    private void sendUsersMessagesList() throws IOException {
        socketOutput.write(buildMessagesJSON(messages));
        socketOutput.write("Messages list query successfully completed\n");
    }

    /**
     * Closes socket and its streams
     */
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

    /**
     * Builds json list of messages stored on server
     *
     * @param messages messages list
     * @return messages JSON
     */
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

    /**
     * Builds JSON list of files stored on server
     *
     * @param files List of files
     * @return JSON string
     */
    private String buildFilesJSON(ArrayList<String> files) {
        StringBuilder jsonFilesList = new StringBuilder("{\"files\": [");
        for (int i = 0; i < files.size(); i++) {
            String jsonFile = String.format("\"%s\"", files.get(i));
            if (i != files.size() - 1)
                jsonFile += ",";
            jsonFilesList.append(jsonFile);
        }
        jsonFilesList.append("]}\n");
        return jsonFilesList.toString();
    }

    /**
     * Parses JSON string and returns new message object
     *
     * @param message message in json format
     * @param id      message id
     * @return message object
     */
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

    /**
     * Saves received from client message to DB
     *
     * @throws IOException when IO error occurs
     */
    private void saveMessage() throws IOException {
        int id = random.nextInt(Integer.MAX_VALUE);
        String jsonMessage = socketInput.readLine();
        while (DBMS.findID(id))
            id = random.nextInt(Integer.MAX_VALUE);
        Message newMessage = parse(jsonMessage, id);
        messages.add(newMessage);
        DBMS.writeID(id);
        DBMS.writeMessages(login, messages);
        socketOutput.write("Message was successfully received and saved\n");
    }
}
