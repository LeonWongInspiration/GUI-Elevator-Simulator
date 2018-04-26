package cn.leonwong.ElevatorSimulator;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class View extends Application {
    private Building b;
    private Controller c;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
        primaryStage.setTitle("Elevator Simulator - 1652795 王陆洋");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    TextField numberOfLevelsText;

    @FXML
    TextField numberOfElevatorsText;

    @FXML
    Button createBuildingButton;

    @FXML
    TextField capacityText;

    @FXML
    ChoiceBox<Integer> startingLevelChoiceBox;

    @FXML
    ChoiceBox<Integer> destLevelChoiceBox;

    @FXML
    Button addPassengerButton;

    @FXML
    ListView<String> strategyListView;
}
