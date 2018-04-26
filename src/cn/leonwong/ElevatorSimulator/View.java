package cn.leonwong.ElevatorSimulator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;

public class View extends Application {
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
    private TextField numberOfLevelsText;

    @FXML
    private TextField numberOfElevatorsText;

    @FXML
    private Button createBuildingButton;

    @FXML
    private TextField capacityText;

    @FXML
    private ChoiceBox<Integer> startingLevelChoiceBox;

    @FXML
    private ChoiceBox<Integer> destLevelChoiceBox;

    @FXML
    private Button addPassengerButton;

    @FXML
    private ListView<String> strategyListView;

    @FXML
    private void onClickCreateBuildingButton(){
        System.out.println("View: Create a new building.");
        int levels = Integer.parseInt(this.numberOfLevelsText.getCharacters().toString());
        System.out.printf("View: # of levels: %d\n", levels);
        int elevators = Integer.parseInt(this.numberOfElevatorsText.getCharacters().toString());
        System.out.printf("View: # of elevators: %d\n", elevators);
        int maximumCapacity = Integer.parseInt(this.capacityText.getCharacters().toString());
        System.out.printf("View: Capacity of elevators: %d\n", maximumCapacity);
        if (! (levels >= 2 &&
            levels <= 20 &&
            elevators >= 1 &&
            elevators <= 10 &&
            maximumCapacity >= 1 &&
            maximumCapacity <= 20)) {
            Alert errorParamMessage = new Alert(Alert.AlertType.INFORMATION, "Illegal Building Information!\n" +
                    "The number of levels should be between 2 and 20.\n" +
                    "The number of elevators should be between 1 and 10.\n" +
                    "The capacity of each elevator should be between 1 and 20.");
            errorParamMessage.setTitle("Building Parameters Error!");
            errorParamMessage.setHeaderText("Information");
            errorParamMessage.showAndWait();
            System.out.println("View: Building Parameter Error!");
        }
        else {
            this.c = new Controller();
            this.c.setView(this);
            this.c.createBuilding(levels, elevators, maximumCapacity);
            this.startingLevelChoiceBox.setItems(FXCollections.observableArrayList());
            this.destLevelChoiceBox.setItems(FXCollections.observableArrayList());
            for (int i = 1; i <= levels; ++i){
                this.startingLevelChoiceBox.getItems().addAll(i);
                this.destLevelChoiceBox.getItems().addAll(i);
            }
        }
        this.c.start();
    }

    @FXML
    private void onClickAddPassengerButton(){
        System.out.println("View: Add a passenger.");
        if (!this.c.isBuildingCreated()){
            Alert errorNullBuilding = new Alert(Alert.AlertType.INFORMATION, "Please create the building FIRST!");
            errorNullBuilding.setTitle("Building not Created Error!");
            errorNullBuilding.setHeaderText("Information");
            errorNullBuilding.showAndWait();
            System.out.println("View: Building not Created Error!");
        }
        else {
            int from = this.startingLevelChoiceBox.getValue();
            System.out.printf("View: Add a passenger from #%d level,\n", from);
            int to = this.destLevelChoiceBox.getValue();
            System.out.printf("View: heading for #%d level.\n", to);
            if (! (from >= 1 &&
                from <= this.c.getLevels() &&
                to >= 1 &&
                to <= this.c.getLevels() &&
                from != to)){
                Alert errorPassengerParam = new Alert(Alert.AlertType.INFORMATION, "Illegal Passenger Information!\n" +
                        "The starting and destination level should exist in this building!\n" +
                        "And they should be UNEQUAL!");
                errorPassengerParam.setTitle("Passenger Parameter Error!");
                errorPassengerParam.setHeaderText("Information");
                errorPassengerParam.showAndWait();
                System.out.println("View: Passenger Parameter Error!");
            }
            else {
                this.c.addPassenger(from, to);
            }
        }
    }
}
