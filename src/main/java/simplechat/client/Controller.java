package simplechat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private SimpleChat simpleChat;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    private Text actionTarget = null;

    @FXML
    protected void handleMessageButtonAction(ActionEvent event) {
        event.consume();
        this.sendMessage();
    }

    public void initialize() {
    }

    public void stop() {
        this.simpleChat.stop();
    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        Platform.runLater(() -> this.textArea.setText(this.textArea.getText() + "\n" + text));
    }

    public void sendMessage() {
        String msg = this.textField.getText();
        if(!msg.trim().isEmpty()) {
            this.simpleChat.sendMessage(msg);
            this.textField.setText("");
        }
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
