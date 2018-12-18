package simplechat.communication.socket.server;

import simplechat.communication.MessageProtocol;
import simplechat.server.SimpleChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static simplechat.communication.MessageProtocol.Commands.EXIT;


/**
 * SimpleChatServer listens to incoming SimpleChatClients with the choosen communication protocol and initiates a UI.
 * <br>
 * Default settings for the main attributes will be: host="localhost" port=5050 and backlog=5
 */
public class SimpleChatServer extends Thread {

    private Integer port = 5050;
    private String host = "localhost";
    private final Integer backlog = 5;
    private ServerSocket serverSocket = null;

    private boolean listening = false;
    private SimpleChat server;

    private ConcurrentHashMap<ClientWorker, String> workerList = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Initializes host, port and callback for UserInterface interactions.
     *
     * @param host   String representation of hostname, on which the server should listen
     * @param port   Integer for the listening port
     * @param server UserInterface callback reference for user interactions
     */
    public SimpleChatServer(String host, Integer port, SimpleChat server) {
        if (host != null)
            this.host = host;
        if (port != null)
            this.port = port;
        this.server = server;
        this.listening = true;
        SimpleChat.serverLogger.log(INFO, "Init: host=" + this.host + " port=" + this.port);
    }

    /**
     * Initiating the ServerSocket with already defined Parameters and starts accepting incoming
     * requests. If client connects to the ServerSocket a new ClientWorker will be created and passed
     * to the ExecutorService for immediate concurrent action.
     */
    public void run() {
        SimpleChat.serverLogger.log(INFO, "... starting Thread ...");
        try {
            this.serverSocket = new ServerSocket(this.port, this.backlog);
        } catch (IOException e) {
            SimpleChat.serverLogger.log(SEVERE, "Could not initialize ServerSocket: " + e.getMessage());
            return;
        }
        while (this.listening) {
            try {
                ClientWorker cw = new ClientWorker(this.serverSocket.accept(), this);
                String name = server.addClient("");
                SimpleChat.serverLogger.log(INFO, "New client: " + name);
                workerList.put(cw, name);
                executorService.execute(cw);
            } catch (IOException e) {
                SimpleChat.serverLogger.log(SEVERE, "Error on acception client.");
                e.printStackTrace();
            }
        }
        SimpleChat.serverLogger.log(INFO, "... exited Thread ...");
    }

    /**
     * Callback method for client worker to inform server of new message arrival
     *
     * @param plainMessage MessageText sent to server without Client information
     * @param sender       {@link ClientWorker} which received the message
     */
    public void received(String plainMessage, ClientWorker sender) {
        SimpleChat.serverLogger.log(INFO, "Received message: " + plainMessage);
        String message = MessageProtocol.textMessage(plainMessage, this.workerList.get(sender));
        this.send(message);
        this.server.incomingMessage(message);
    }

    /**
     * Sending messages to clients through communication framework
     *
     * @param message MessageText with sender ChatName
     */
    public void send(String message) {
        SimpleChat.serverLogger.log(INFO, "Send message to all: " + message);
        for (ClientWorker cw : this.workerList.keySet()) {
            cw.send(message);
        }
    }

    /**
     * Sending message to one client through communication framework
     *
     * @param message  MessageText with sender ChatName
     * @param receiver ChatName of receiving Client
     */
    public void send(String message, Object receiver) {
        this.getWorker(receiver).send(message);
    }

    private ClientWorker getWorker(Object chatName) {
        String sName = chatName.toString();
        for (Map.Entry<ClientWorker, String> e : this.workerList.entrySet()) {
            if (e.getValue().equals(sName)) {
                return e.getKey();
            }
        }
        return null;
    }

    /**
     * ClientWorker has the possibility to change the ChatName. This method asks the UI
     * to rename the Client and stores the returned Name in the ClientWorker-Collection
     *
     * @param chatName new Name of Client
     * @param worker   ClientWorker Thread which was initiating the renaming
     */
    void setName(String chatName, ClientWorker worker) {
        String name = this.server.renameClient(this.workerList.get(worker), chatName);
        this.workerList.replace(worker, name);
    }

    /**
     * Remove only this worker from the list,
     * shutdown the ClientWorker and also inform GUI about removal.
     *
     * @param worker ClientWorker which should be removed
     */
    void removeClient(ClientWorker worker) {
        this.server.removeClient(workerList.get(worker));
        worker.shutdown();
        this.workerList.remove(worker);
    }

    /**
     * Gets the ClientWorker of the given chatName and calls the private Method {@link #removeClient(String)}
     * This method will remove the worker from the list shutdown the ClientWorker and also inform GUI about removal.
     *
     * @param chatName Client name which should be removed
     */
    public void removeClient(String chatName) {
        this.removeClient(this.getWorker(chatName));
    }

    /**
     * Clean shutdown of all connected Clients.<br>
     * ExecutorService will stop accepting new Thread inits.
     * After notifying all clients, ServerSocket will be closed and ExecutorService will try to shutdown all
     * active ClientWorker Threads.
     */
    public void shutdown() {
    }
}

/**
 * Thread for client socket connection.<br>
 * Every client has to be handled by an own Thread.
 */
class ClientWorker implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;

    private SimpleChatServer callback;
    private boolean listening = true;

    /**
     * Init of ClientWorker-Thread for socket intercommunication
     *
     * @param client   Socket got from ServerSocket.accept()
     * @param callback {@link simplechat.communication.socket.server.SimpleChatServer} reference
     * @throws IOException will be throwed if the init of Input- or OutputStream fails
     */
    ClientWorker(Socket client, SimpleChatServer callback) throws IOException {
        this.client = client;
        this.out = new PrintWriter(client.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.callback = callback;
    }

    /**
     * MessageHandler for incoming Messages on Client Socket
     * <br>
     * The InputSocket will be read synchronous through readLine()
     * Incoming messages first will be checked if they start with any Commands, which will be executed properly.
     * Otherwise text messages will be delegated to the {@link SimpleChatServer#received(String, ClientWorker)} method.
     */
    @Override
    public void run() {
        try {
            while (this.listening) {
                String message = this.in.readLine();
                if (message.startsWith("!")) {
                    String[] split = message.split(" ", 2);
                    String cmd = split[0];
                    String param = split.length > 1 ? split[1] : "";
                    MessageProtocol.Commands command;
                    try {
                        command = MessageProtocol.getCommand(cmd);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    switch (command) {
                        case EXIT:
                            this.shutdown();
                            break;
                        case CHATNAME:
                            if(!param.trim().isEmpty()) {
                                this.callback.setName(split[1], this);
                            }
                            break;
                    }
                } else {
                    this.callback.received(message, this);
                }
            }
            this.out.close();
        } catch (IOException e) {

        } finally {
            this.callback.removeClient(this);
        }
    }

    /**
     * Clean shutdown of ClientWorker
     * <br>
     * If listening was still true, we are sending a {@link MessageProtocol.Commands#EXIT} to the client.
     * Finally we are closing all open resources.
     */
    void shutdown() {
        if(listening) {
            SimpleChat.serverLogger.log(INFO, "Shutting down ClientWorker ... listening=" + listening);
            this.send(MessageProtocol.getMessage(EXIT));
        }
    }

    /**
     * Sending message through Socket OutputStream {@link #out}
     *
     * @param message MessageText for Client
     */
    void send(String message) {
        this.out.println(message);
    }
}
