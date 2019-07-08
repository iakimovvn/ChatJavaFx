package client.chat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class OtherMessageHBox extends HBox {

    public OtherMessageHBox(String nickname, String message) {
        setPrefWidth(350);
        setAlignment(Pos.CENTER_RIGHT);


        Label messageLbl = new Label(message);
        messageLbl.setFont(Font.font("Arial",14));
        messageLbl.setAlignment(Pos.CENTER_RIGHT);
        messageLbl.setTextFill(Color.BLUE);
        messageLbl.setMaxWidth(280);
        messageLbl.setBackground(new Background(new BackgroundFill(Color.rgb(191,255,250), CornerRadii.EMPTY, Insets.EMPTY)));

        this.getChildren().add(messageLbl);

        Label nickLabel = new Label(" :"+nickname);
        nickLabel.setFont(Font.font("Arial",16));
        nickLabel.setAlignment(Pos.CENTER);
        nickLabel.setTextFill(Color.GREEN);
        nickLabel.setMaxWidth(140);
        this.getChildren().add(nickLabel);

        Pane pane = new Pane();
        pane.setPrefWidth(10);
        this.getChildren().add(pane);

    }
}
