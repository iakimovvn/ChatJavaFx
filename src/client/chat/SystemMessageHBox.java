package client.chat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SystemMessageHBox extends HBox {

    public SystemMessageHBox(String msg) {

        setPrefWidth(350);
        setAlignment(Pos.CENTER);

        Label messageLbl = new Label(msg);
        messageLbl.setFont(Font.font("Arial",12));
        messageLbl.setAlignment(Pos.CENTER);
        messageLbl.setTextFill(Color.GRAY);
        messageLbl.setMaxWidth(350);
        this.getChildren().add(messageLbl);
    }
}
