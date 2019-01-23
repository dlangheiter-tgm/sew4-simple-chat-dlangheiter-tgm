package simplechat.communication.socket.client;

import javafx.application.Platform;
import simplechat.client.SimpleChat;
import simplechat.communication.MessageProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import static java.util.logging.Level.*;
import static simplechat.communication.MessageProtocol.Commands.EXIT;
import static simplechat.communication.MessageProtocol.Commands.PRIVATE;

/**
 * SimpleChatClient connects to SimpleChatServer with the choosen communication protocol and initiates a UI.
 * <br>
 * Default settings for the main attributes will be: name="Client" host="localhost" and port=5050
 */
public class SimpleChatClient extends Thread {

    private String name = "Client";
    private String host = "localhost";
    private Integer port = 5050;

    private InetSocketAddress socketAddress;
    private Socket socket = null;
    private PrintWriter out;
    private BufferedReader in;

    private boolean listening = false;
    private String currentMessage;

    private SimpleChat client;

    /**
     * Initializes host, port and callback for UserInterface interactions.
     *
     * @param name   String representation of chatName
     * @param host   String representation of hostname, on which the server should listen
     * @param port   Integer for the listening port
     * @param client UserInterface callback reference for user interactions
     */
    public SimpleChatClient(String name, String host, Integer port, SimpleChat client) {
        if (name != null)
            this.name = name;
        if (host != null)
            this.host = host;
        if (port != null)
            this.port = port;
        if (host != null && port != null)
            this.socketAddress = new InetSocketAddress(host, port);
        this.client = client;
        SimpleChat.clientLogger.log(INFO, "Init: host=" + this.host + " port="
                + this.port + " chatName=" + this.name);
    }

    /**
     * Initiating the Socket with already defined Parameters (host, port). Also a timeout of 2000 ms is set at connect.
     * The {@link java.net.Socket#setKeepAlive(boolean)} is set to true.
     * <br>
     * After activating {@link #listening}, the Chatname will be sent to the Server and the reading loop is started,
     * checking for the {@link BufferedReader#readLine()} and the {@link #listening} flag.
     * <br>
     * In case of an Exception the Thread will be interrupted and if the socket was connected and bound,
     * the {@link #shutdown()} method will be called.
     */
    public void run() {
        try {
            this.socket = new Socket();
            // Connect to server with timeout
            this.socket.connect(this.socketAddress, 2000);
            // Setup input and output
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            // Set listening to true
            this.listening = true;
            // Send server the chatname
            this.send("!CHATNAME " + this.name);
            while (this.listening && (this.currentMessage = this.in.readLine()) != null) {
                this.received();
            }
        } catch (IOException e) {
            SimpleChat.clientLogger.log(SEVERE, "Exception in socket thread: " + e.getMessage());
            this.shutdown();
        }
        Platform.exit();
    }

    /**
     * Analyzing received messages.
     * <br>
     * If Server sends proper {@link simplechat.communication.MessageProtocol.Commands} this method will act accordingly.
     * <br>
     * {@link simplechat.communication.MessageProtocol.Commands#EXIT} will set listening to false
     * and then calls {@link #shutdown()}
     * <br>
     * If there is now Command (no "!" as first character),
     * the message will be passed to {@link simplechat.client.SimpleChat#incomingMessage(String)}
     */
    private void received() {
        if (this.currentMessage.startsWith("!")) {
            SimpleChat.clientLogger.log(INFO, "Received command from server: " + this.currentMessage);
            String[] split = this.currentMessage.trim().split(" ");
            MessageProtocol.Commands cmd;
            try {
                cmd = MessageProtocol.getCommand(split[0]);
                SimpleChat.clientLogger.log(WARNING, "Command: " + cmd);
            } catch (IllegalArgumentException e) {
                SimpleChat.clientLogger.log(WARNING, "Unknown command: " + this.currentMessage);
                return;
            }
            switch (cmd) {
                case EXIT:
                    this.listening = false;
                    this.shutdown();
                    break;
                default:
                    SimpleChat.clientLogger.log(WARNING, "Unhandled command: " + cmd);
            }
        } else {
            SimpleChat.clientLogger.log(INFO, "Received msg from server: " + this.currentMessage);
            client.incomingMessage(this.currentMessage);
        }
    }

    /**
     * Sending message to the server through network
     *
     * @param message Public message for server intercommunication
     */
    public void send(String message) {
        SimpleChat.clientLogger.log(INFO, "Send message to server: " + message);
        try {
            out.println(message);
        } catch (Exception e) {
            SimpleChat.clientLogger.log(SEVERE, "Error while sending message: " + e);
        }
    }

    /**
     * Sending message to the server through network for private Message
     *
     * @param message  Private message for client-to-client intercommunication
     * @param chatName Name of receiver
     */
    public void send(String message, String chatName) {
        SimpleChat.clientLogger.log(INFO, "Send private message to " + chatName + ": " + message);
        this.send(MessageProtocol.getMessage(PRIVATE) + " (" + chatName + ") " + message);
    }

    /**
     * Clean shutdown of Client
     * <br>
     * If listening was still true, we are sending a {@link MessageProtocol.Commands#EXIT} to the server.
     * Finally we are closing all open resources.
     */
    public void shutdown() {
        SimpleChat.clientLogger.log(INFO, "Shutting down Client ... listening=" + listening);
        if (this.listening) {
            this.listening = false;
            this.send(MessageProtocol.getMessage(EXIT));
        }
        this.currentMessage = "Server disconnected.";
        this.received();
        try {
            this.out.close();
            this.in.close();
            this.socket.close();
        } catch (IOException e) {
            SimpleChat.clientLogger.log(WARNING, "Error while closing connection: " + e.getMessage());
        }
    }

    /**
     * @return True if still listening and online
     */
    public boolean isListening() {
        return listening;
    }
}
