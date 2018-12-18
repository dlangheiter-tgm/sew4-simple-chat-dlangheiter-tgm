package simplechat.communication;

import java.io.Serializable;

public class MessageProtocol implements Serializable {

    /**
     * MessageProtocol Commands for client and server communication
     */
    public enum Commands {
        /**
         * Signals client or server to end communication,
         * the command will be defined as "!EXIT"
         */
        EXIT,
        /**
         * Signals server to change the chatName of given user,
         * the command will be defined as "!CHATNAME chatName"
         */
        CHATNAME,
        /**
         * Signals server to send a message only to the listed users,
         * the command will be defined as "!PRIVATE {chatName, chatName#1} text"
         */
        PRIVATE
    }

    /**
     * Using a easy to recognize encoding for string commands
     *
     * @param command enum of wished command
     * @return textual representation of command, e.g. !EXIT
     */
    public static String getMessage(Commands command) {
        return "!" + command;
    }

    /**
     * Takes a String command and returns a corresponding Command representation
     *
     * @param command String command e.g. "!EXIT"
     * @return Command representation of given String,
     * e.g. {@link simplechat.communication.MessageProtocol.Commands#EXIT}
     * @throws IllegalArgumentException will be thrown if the String is not defined as Command
     */
    public static Commands getCommand(String command) {
        Commands c = Commands.valueOf(command.substring(1));
        return c;
    }

    /**
     * Encapsulates plain message with ChatName, e.g. "[Franz] hallo!"
     *
     * @param plainMessage Plain Messagetext
     * @param chatName     Sender of message
     * @return Messagetext with Sender Chatname "[chatName] plainMessage"
     */
    public static String textMessage(String plainMessage, String chatName) {
        return "[" + chatName + "] " + plainMessage;
    }
}
