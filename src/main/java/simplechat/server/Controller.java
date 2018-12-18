package simplechat.server;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private ListView listView;

    @FXML
    private Text actionTarget = null;

    @FXML
    protected void handleMessageButtonAction(ActionEvent event) {
        this.simpleChat.sendMessage(this.textField.getText());
        this.textField.setText("");
    }

    @FXML
    protected void handleRemoveButtonAction(ActionEvent event) {
    }

    public void initialize() {
    }

    public void stop() {
    }

    public void setSimpleChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    public void updateTextAreaWithText(String text) {
        this.textArea.setText(text);
    }

    public void addUser(String user) {
    }

    public void removeUser(String user) {
    }

    Runnable clearText = () -> {
        actionTarget.setText("");
    };
}
