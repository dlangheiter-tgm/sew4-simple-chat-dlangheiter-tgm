package simplechat.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {

    private SimpleChat simpleChat;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    private ListView listView;

    @FXML
    private Text actionTarget = null;

    @FXML
    protected void handleMessageButtonAction(ActionEvent event) {
        event.consume();
        this.simpleChat.sendMessage(this.textField.getText());
        this.textField.setText("");
    }

    @FXML
    protected void handleRemoveButtonAction(ActionEvent event) {
        // TODO: handleRemoveButtonAction
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
        this.textArea.setText(this.textArea.getText() + "\n" + text);
    }

    public void addUser(String user) {
        Platform.runLater(() -> this.listView.getItems().add(user));
    }

    public void removeUser(String user) {
        Platform.runLater(() -> this.listView.getItems().remove(user));
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
