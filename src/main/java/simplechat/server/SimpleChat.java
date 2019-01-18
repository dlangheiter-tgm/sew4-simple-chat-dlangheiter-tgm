package simplechat.server;

import org.apache.commons.cli.*;
import simplechat.communication.socket.server.SimpleChatServer;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * The Server Class choosing the communication framework and user interface
 *
 * @author Michael Borko  {@literal <mborko@tgm.ac.at>}
 * @author David Langheiter {@literal <david@langheiter.com}
 * @version 1.0
 */
public class SimpleChat {

    private SimpleChatServer server;
    private ConcurrentSkipListSet<String> users;

    private ConcurrentLinkedQueue<String> receivedMessages;
    private ConcurrentLinkedQueue<String> sentMessages;

    private Controller controller;

    public static Logger serverLogger = Logger.getLogger("server");

    /**
     * Definition of Server Information
     * <br>
     * There are three optional arguments, which can be parsed through the
     * <a href="https://commons.apache.org/proper/commons-cli/javadocs/api-release/index.html">
     * Apache CommonsCLI Library</a>.
     *
     * @param args <br>
     *             Server hostname, e.g. --host 10.0.15.3 or -h 10.0.15.3 <br>
     *             TCP port to listen on, e.g. --port 1234 or -p 1234 <br>
     *             explaining what is being done, e.g. --verbose or -v <br>
     */
    public static void main(String[] args) {
        serverLogger.setLevel(FINE);
        serverLogger.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(SEVERE);
        serverLogger.addHandler(ch);

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h", "host", true, "Server hostname.");
        options.addOption("p", "port", true, "TCP port to listen.");
        options.addOption("v", "verbose", false, "explain what is being done");

        CommandLine line;
        String host = null;
        Integer port = null;
        try {
            line = parser.parse(options, args);
            host = line.getOptionValue("h");
            port = line.getOptionValue("p") != null ? Integer.parseInt(line.getOptionValue("p")) : null;

            boolean verbose = line.hasOption("v");
            if (verbose) ch.setLevel(ALL);

            serverLogger.log(INFO, "Parameters set by user: " +
                    "host=" + host + " port=" + port + " verbose=" + verbose);
        } catch (ParseException e) {
            serverLogger.log(SEVERE, e.toString());
            System.exit(1);
        }

        SimpleChat simpleChat = new SimpleChat(host, port);
        simpleChat.listen();

        FXApplication fxApplication = new FXApplication();
        fxApplication.setSimpleChat(simpleChat);
        fxApplication.main(args);
    }

    /**
     * Initiating server Thread and the user list {@link #users}.
     *
     * @param host hostname definition for server Thread
     * @param port port on which server Thread should listen
     */
    public SimpleChat(String host, Integer port) {
        server = new simplechat.communication.socket.server.SimpleChatServer(host, port, this);
        users = new ConcurrentSkipListSet<>();
        receivedMessages = new ConcurrentLinkedQueue<>();
        sentMessages = new ConcurrentLinkedQueue<>();
    }

    /**
     * @param controller UI Controller for message and configuration interaction
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * After successfully initiating the server Thread, here the concurrent execution will be started.
     */
    public void listen() {
        serverLogger.log(INFO, "Initiating SimpleChatServer ...");
        this.server.start();
    }

    /**
     * Gracefully shutdown of server Thread calling {@link SimpleChatServer#shutdown()}
     */
    public void stop() {
        this.server.shutdown();
    }

    /**
     * @return checks if server Thread is still alive
     */
    public boolean isConnected() {
        return this.server.isAlive();
    }

    /**
     * Passing message to networkHandler, only if the Thread is still alive
     *
     * @param message plain message
     */
    public void sendMessage(String message) {
        serverLogger.log(INFO, "UI gave me this message: " + message);
        if(this.isConnected()) {
            this.server.send(message);
            this.sentMessages.add(message);
            this.controller.updateTextAreaWithText(message);
        }
    }

    /**
     * Passing message to networkHandler, only if the Thread is still alive
     *
     * @param message  plain message
     * @param chatName receiver
     */
    public void sendMessage(String message, String chatName) {
        serverLogger.log(INFO, "UI gave me this message: " + message + " for this user: " + chatName);
        if(this.isConnected()) {
            this.server.send(message, chatName);
            this.sentMessages.add(message);
        }
    }

    /**
     * Got a new message from communication framework
     *
     * @param message Message sent by Client
     */
    public void incomingMessage(String message) {
        serverLogger.log(INFO, "Socket gave me this message: " + message);
        this.receivedMessages.add(message);
        this.controller.updateTextAreaWithText(message);
    }

    /**
     * Returns list of connected Users
     *
     * @return Array of unique chatNames of connected Clients
     */
    public synchronized String[] getClients() {
        return users.toArray(new String[0]);
    }

    /**
     * Adds a Client to the userList. The check of the username must be synchronized!
     *
     * @param chatName Client which will be added
     * @return New unique ChatName. If the given Name was unique the same as the {@code chatName}
     * or an adapted new name (e.g. Franz#1)
     */
    public synchronized String addClient(String chatName) {
        chatName = chatName.equals("") ? "Client" : chatName;
        if(this.users.contains(chatName)) {
            for(int i = 0;;i++) {
                chatName = chatName + "#" + i;
                if(!this.users.contains(chatName)) {
                    break;
                }
            }
        }
        serverLogger.log(INFO, "Add Client: " + chatName);
        users.add(chatName);
        this.controller.addUser(chatName);
        return chatName;
    }

    /**
     * Renames Client in local userlist {@link #users} by removing the oldChatName and inserting the newChatName
     *
     * @param oldChatName Clientname which will be removed from list
     * @param newChatName Clientname which should be added by {@link simplechat.server.SimpleChat#addClient(String)}
     * @return New unique ChatName. If the given Name was unique the same as the {@code newChatName}
     * or an adapted new name (e.g. Franz#1)
     */
    public synchronized String renameClient(String oldChatName, String newChatName) {
        serverLogger.log(INFO, "Rename Client from " + oldChatName + " to " + newChatName);
        if(oldChatName.equals(newChatName)) {
            return newChatName;
        }
        if(this.users.contains(oldChatName)) {
            this.removeClient(oldChatName);
            return this.addClient(newChatName);
        }
        return null;
    }

    /**
     * If chatName exists in userlist {@link #users}, user will be informed of removal.
     * Afterwards Client will be removed from userlist
     * and also the UserInterface method {@link Controller#removeUser(String)} will be called.
     *
     * @param chatName Client which will be removed from Userlist
     */
    public void removeClient(String chatName) {
        serverLogger.log(INFO, "Remove Client: " + chatName);
        if(this.users.remove(chatName)) {
            this.controller.removeUser(chatName);
        }
    }

    /**
     * Calls {@link simplechat.communication.socket.server.SimpleChatServer#removeClient(String)} to shutdown client
     * and remove it internally by calling {@link #removeClient(String)}.
     *
     * @param chatName Client which will be informed of shutdown
     */
    public void shutdownClient(String chatName) {
        server.removeClient(chatName);
        removeClient(chatName);
    }

    /**
     * @return Queue of current received messages.
     */
    public Queue<String> getReceivedMessages() {
        return new LinkedList<>(receivedMessages);
    }

    /**
     * @return Queue of current sent messages.
     */
    public Queue<String> getSentMessages() {
        return new LinkedList<>(sentMessages);
    }
}
