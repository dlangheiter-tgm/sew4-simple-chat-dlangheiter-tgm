package simplechat.client;

import org.apache.commons.cli.*;
import simplechat.communication.socket.client.SimpleChatClient;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * The Client Class choosing the communication framework and user interface
 *
 * @author Kai Hoeher {@literal <khoeher@tgm.ac.at>}
 * @author Michael Borko {@literal <mborko@tgm.ac.at>}
 * @version 1.0
 */
public class SimpleChat {

    SimpleChatClient client;
    private Controller controller;

    public static Logger clientLogger = Logger.getLogger("client");

    /**
     * Definition of Client Information
     * <br>
     * There are three optional arguments, which can be parsed through the
     * <a href="https://commons.apache.org/proper/commons-cli/javadocs/api-release/index.html">
     * Apache CommonsCLI Library</a>.
     *
     * @param args <br>
     *             Server hostname to be connected to, e.g. --host 10.0.15.3 or -h 10.0.15.3 <br>
     *             TCP port to connecting to, e.g. --port 1234 or -p 1234 <br>
     *             ChatName for Client identification, e.g. --name Franz or -n Franz <br>
     *             explaining what is being done, e.g. --verbose or -v <br>
     */
    public static void main(String[] args) {
        clientLogger.setLevel(FINE);
        clientLogger.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(SEVERE);
        clientLogger.addHandler(ch);

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("n", "name", true, "User name.");
        options.addOption("h", "host", true, "Server hostname.");
        options.addOption("p", "port", true, "TCP port.");
        options.addOption("v", "verbose", false, "explain what is being done");

        CommandLine line = null;
        String host = null;
        String chatName = null;
        Integer port = null;
        try {
            line = parser.parse(options, args);
            host = line.getOptionValue("h");
            chatName = line.getOptionValue("n");
            port = line.getOptionValue("p") != null ? Integer.parseInt(line.getOptionValue("p")) : null;

            boolean verbose = line.hasOption("v");
            if (verbose)
                ch.setLevel(ALL);

            clientLogger.log(INFO, "Parameters set by user: " +
                    "host=" + host + " port=" + port + " chatName=" + chatName + " verbose=" + verbose);
        } catch (ParseException e) {
            clientLogger.log(SEVERE, e.toString());
            System.exit(1);
        }

        SimpleChat simpleChat = new SimpleChat(chatName, host, port);
        simpleChat.listen();

        FXApplication fxApplication = new FXApplication();
        fxApplication.setSimpleChat(simpleChat);
        fxApplication.main(args);
    }

    /**
     * Initiating client Thread.
     *
     * @param name ChatName of client for identification
     * @param host hostname definition for server connection
     * @param port port for connection
     */
    public SimpleChat(String name, String host, Integer port) {
        client = new SimpleChatClient(name, host, port, this);
    }

    /**
     * @param controller UI Controller for message and configuration interaction
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * After successfully initiating the client Thread, here the concurrent execution will be started.
     */
    public void listen() {
        clientLogger.log(INFO, "Initiating SimpleChatClient ...");
        this.client.start();
    }

    /**
     * Gracefully shutdown of client Thread calling {@link SimpleChatClient#shutdown()}
     */
    public void stop() {
        this.client.shutdown();
    }

    /**
     * @return checks if client Thread is still alive
     */
    public boolean isConnected() {
        return this.client.isListening();
    }

    /**
     * Sending message to the network clientHandler only if the Thread is alive
     *
     * @param message Public message for server intercommunication
     */
    public void sendMessage(String message) {
        clientLogger.log(INFO, "UI gave me this message: " + message);
        if(this.isConnected()) {
            this.client.send(message);
        }
    }

    /**
     * Sending message to the network clientHandler only if the Thread is alive
     *
     * @param message  Private message for client-to-client intercommunication
     * @param chatName Name of receiver
     */
    public void sendMessage(String message, String chatName) {
        if(this.isConnected()) {
            clientLogger.log(INFO, "UI gave me this message: " + message + " for this user: " + chatName);
            this.client.send(message, chatName);
        }
    }

    /**
     * Got a new message from communication framework.
     * Updates GUI with new message, only if the controller is set.
     *
     * @param message Message sent by Server
     */
    public void incomingMessage(String message) {
        if(this.controller != null) {
            this.controller.updateTextAreaWithText(message);
        }
    }

}
