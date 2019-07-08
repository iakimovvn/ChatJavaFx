package client.chat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;


public class MyMessageHBox extends HBox {


    public MyMessageHBox(String nickname, String message) {
        setPrefWidth(350);
        setAlignment(Pos.CENTER_LEFT);

        Pane pane = new Pane();
        pane.setPrefWidth(10);
        this.getChildren().add(pane);

        Label nickLabel = new Label(nickname+": ");
        nickLabel.setFont(Font.font("Arial",16));
        nickLabel.setAlignment(Pos.CENTER);
        nickLabel.setTextFill(Color.RED);
        nickLabel.setMaxWidth(140);
        this.getChildren().add(nickLabel);

        Label messageLbl = new Label(message);
        messageLbl.setFont(Font.font("Arial",14));
        messageLbl.setAlignment(Pos.CENTER_LEFT);
        messageLbl.setTextFill(Color.DARKBLUE);
        messageLbl.setBackground(new Background(new BackgroundFill(Color.rgb(255,246,148), CornerRadii.EMPTY, Insets.EMPTY)));

        messageLbl.setMaxWidth(280);
        this.getChildren().add(messageLbl);
    }


}
