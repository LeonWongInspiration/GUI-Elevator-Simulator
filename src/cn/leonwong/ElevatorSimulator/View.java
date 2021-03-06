package cn.leonwong.ElevatorSimulator;

import cn.leonwong.ElevatorSimulator.Model.Elevator;
import cn.leonwong.ElevatorSimulator.Model.Passenger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

/**
 * the view of this app
 */
public class View extends Application {
    /// controller Object
    private Controller c;
    /// contains buttons for elevators
    private ArrayList<Button> GUIElevatorButtons;
    /// contains labels for elevators
    private ArrayList<Label> GUIElevatorLabels;
    /// contains buttons for levels
    private ArrayList<Button> GUILevelButtons;
    /// if there are two pages, indicate which one is being shown
    private Boolean isFirstPageShown;

    /**
     * stop all threads on exit
     */
    private class ExitHandler implements EventHandler<WindowEvent> {
        /// denote the view
        private View view;

        /**
         * create exit handler
         * @param v the view
         */
        public ExitHandler(View v){
            System.out.println("View: Exit Handler Created");
            this.view = v;
        }

        /**
         * when exit triggered, kill all threads
         * @param e the event
         */
        @Override
        public void handle(WindowEvent e){
            e.consume();
            System.out.println("View: Exit Triggered");
            this.view.stopThread();
        }
    }

    /**
     * show information of an elevator when clicking the elevator button
     */
    private class ElevatorButtonOnClickHandler implements EventHandler<javafx.event.ActionEvent> {
        /// the index of this elevator
        private int indexOfElevator;
        /// an Object refering to the elevator
        private Elevator elevator;

        /**
         * constructor
         * @param index the index of this elevator
         * @param elev the Object of elevator
         */
        public ElevatorButtonOnClickHandler(int index, Elevator elev){
            this.indexOfElevator = index;
            this.elevator = elev;
        }

        /**
         * show information when clicking the elevator button
         * @param event the event
         */
        @Override
        public void handle(javafx.event.ActionEvent event){
            event.consume();
            StringBuilder sb = new StringBuilder();
            sb.append("This is the #");
            sb.append(this.indexOfElevator + 1);
            sb.append(" Elevator.\n");
            if (this.elevator.isIdle()){
                sb.append("This elevator is idle.");
            }
            else {
                sb.append("There is(are) ");
                sb.append(this.elevator.getPassengers());
                sb.append(" passenger(s) in this elevator.\n");
                sb.append("The destination level(s) of this elevator is(are):\n");
                sb.append(this.elevator.getDestinations());
            }
            Alert elevatorInfo = new Alert(Alert.AlertType.INFORMATION, sb.toString());
            elevatorInfo.setTitle("Elevator #" + (this.indexOfElevator+1));
            elevatorInfo.setHeaderText("Elevator Information:");
            elevatorInfo.showAndWait();
        }
    }

    /**
     * show information when clicking the level button
     */
    private class LevelButtonOnClickHandler implements EventHandler<javafx.event.ActionEvent>{
        /// the index of this level
        private int indexOfLevel;
        /// the passenger list of this level
        private Vector<Passenger> level;

        /**
         * constructor
         * @param i the index of this level
         * @param vp the vector of passenger at this level
         */
        public LevelButtonOnClickHandler(int i, Vector<Passenger> vp){
            this.indexOfLevel = i;
            this.level = vp;
        }

        /**
         * show information when clicking the level button
         * @param event the event
         */
        @Override
        public void handle(javafx.event.ActionEvent event){
            event.consume();
            StringBuilder sb = new StringBuilder();
            sb.append("This is the #");
            sb.append(this.indexOfLevel + 1);
            sb.append(" Level.\n");
            if (this.level.isEmpty()){
                sb.append("This level is empty.");
            }
            else {
                sb.append("There are ");
                sb.append(this.level.size());
                sb.append(" Passengers waiting at the level.");
                sb.append("And they are heading for (sorted by the order of time):\n");
                sb.append(this.parseString(this.level));
            }
            sb.append("The level indicator is:\n");
            sb.append(this.decideUpwardIndicator());
            sb.append('\n');
            sb.append(this.decideDownwardIndicator());
            Alert levelAlert = new Alert(Alert.AlertType.INFORMATION, sb.toString());
            levelAlert.setTitle("Level #" + this.indexOfLevel + 1);
            levelAlert.setHeaderText("Information");
            levelAlert.showAndWait();
        }

        /**
         * describe a level with its passengers in a String form
         * @param lev the passenger list of this level
         * @return the String which contains the passengers' destinations of this level
         */
        private String parseString(Vector<Passenger> lev){
            ArrayList<Integer> ai = new ArrayList<>();
            for (Passenger pass : lev){
                ai.add(pass.destination);
            }
            return ai.toString();
        }

        /**
         * decide whether the upward indicator at a level is shown
         * @return the upward indicator
         */
        private char decideUpwardIndicator(){
            for (Passenger pass : this.level){
                if (pass.destination > this.indexOfLevel + 1)
                return '▲';
            }
            return '△';
        }

        /**
         * decide whether the downward indicator at a level is shown
         * @return the downward indicator
         */
        private char decideDownwardIndicator(){
            for (Passenger pass : this.level){
                if (pass.destination < this.indexOfLevel + 1)
                    return '▼';
            }
            return '▽';
        }
    }

    /**
     * change the page showing
     */
    private class PageButtonOnClickHandler implements EventHandler<javafx.event.ActionEvent>{
        /// denote whether this button is a page up button
        private boolean pageUp;
        /// refer to level buttons
        private ArrayList<Button> levelButtons;
        /// refer to elevator labels
        private ArrayList<Label> elevLabels;
        /// refer to elevator list
        private Vector<Elevator> elevs;
        /// refer to the canvas which shows the building
        private AnchorPane canvas;
        /// refer to the view
        View view;

        /**
         * construct a page button
         * @param isPageUpButton whether this button is page up button
         * @param v the view
         */
        public PageButtonOnClickHandler(boolean isPageUpButton, View v){
            this.pageUp = isPageUpButton;
            this.levelButtons = v.GUILevelButtons;
            this.elevLabels = v.GUIElevatorLabels;
            this.elevs = v.c.getElevatorList();
            this.canvas = v.buildingCanvas;
            this.view = v;
        }

        /**
         * handle the page changing event
         * @param event the event
         */
        @Override
        public void handle(javafx.event.ActionEvent event){
            event.consume();
            // if this is the page up button, show the levels and elevators at the #11+ floors
            if (this.pageUp){
                this.view.setIsFirstPageShown(false);
                for (int i = 0; i < 10; ++i){
                    this.canvas.getChildren().remove(this.levelButtons.get(i));
                }
                for (int i = 10; i < this.levelButtons.size(); ++i){
                    Button tmpBuildingButton = this.levelButtons.get(i);
                    tmpBuildingButton.setLayoutX(10);
                    tmpBuildingButton.setLayoutY(768 - (i - 10 + 1) * 60 + 10);
                    this.canvas.getChildren().add(tmpBuildingButton);
                }
                for (int i = 0; i < elevs.size(); ++i){
                    if (elevs.get(i).getLevel() > 10){
                        this.view.showElevator(elevLabels.get(i));
                    }
                    else {
                        this.view.unshowElevator(elevLabels.get(i));
                    }
                }
            }
            // else show those at #10- floors
            else {
                this.view.setIsFirstPageShown(true);
                for (int i = 10; i < this.levelButtons.size(); ++i) {
                    this.canvas.getChildren().remove(this.levelButtons.get(i));
                }
                for (int i = 0; i < 10; ++i){
                    Button tmpBuildingButton = this.levelButtons.get(i);
                    tmpBuildingButton.setLayoutX(10);
                    tmpBuildingButton.setLayoutY(768 - (i + 1) * 60 + 10);
                    this.canvas.getChildren().add(tmpBuildingButton);
                }
                for (int i = 0; i < elevs.size(); ++i){
                    if (elevs.get(i).getLevel() > 10){
                        this.view.unshowElevator(elevLabels.get(i));
                    }
                    else {
                        this.view.showElevator(elevLabels.get(i));
                    }
                }
            }
        }
    }

    /**
     * stop this thread
     */
    public void stopThread(){
        if (this.c != null)
            this.c.stopThread();
        System.exit(0);
    }

    /**
     * start this application
     * @param primaryStage the main stage
     * @throws Exception if the application faces some errors starting itself, there will be some exceptions thrown
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
        primaryStage.setTitle("Elevator Simulator - 1652795 王陆洋");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.setOnCloseRequest(new ExitHandler(this));
        primaryStage.show();
//        Runtime.getRuntime().addShutdownHook(new ExitHandler(this));
    }

    /**
     * nothing to comment here...
     * @param args command line arguments which are not used
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * initialize elevators on the canvas
     * @param elevs the number of elevators
     */
    private void initGUIElevators(int elevs){
        this.GUIElevatorButtons = new ArrayList<>();
        this.GUIElevatorLabels = new ArrayList<>();

        for (int i = 0; i < elevs; ++i){
            Line tmpLine = new Line();
            tmpLine.setStartX(700 - 60 * (elevs - i));
            tmpLine.setEndX(700 - 60 * (elevs - i));
            tmpLine.setStartY(0);
            tmpLine.setEndY(768);
            this.buildingCanvas.getChildren().add(tmpLine);

            Button tmpButton = new Button("#" + (i + 1));
            tmpButton.setPrefSize(50, 40);
            tmpButton.setLayoutX(700 - 60 * (elevs - i) - 25);
            tmpButton.setLayoutY(0);
            tmpButton.setOnAction(new ElevatorButtonOnClickHandler(i, this.c.getElevatorList().get(i)));
            this.buildingCanvas.getChildren().add(tmpButton);
            GUIElevatorButtons.add(tmpButton);

            Image tmpLabelImage = new Image("ElevatorIcon_60_60.png");
            Label tmpLabel = new Label(null, new ImageView(tmpLabelImage));
            tmpLabel.setPrefSize(60, 60);
            tmpLabel.setLayoutX(700 - 60 * (elevs - i) - 30);
            tmpLabel.setLayoutY(768 - 60);
            this.buildingCanvas.getChildren().add(tmpLabel);
            GUIElevatorLabels.add(tmpLabel);

        }
    }

    /**
     * initialize levels on the canvas, should create page buttons according to the number of levels
     * @param levs the number of levels
     */
    private void initGUILevels(int levs){
        this.GUILevelButtons = new ArrayList<>();

        for (int i = 0; i < 10; ++i){
            Line tmpBuildingLine = new Line();
            tmpBuildingLine.setStartX(0);
            tmpBuildingLine.setEndX(700);
            tmpBuildingLine.setStartY(768 - 60 * (i + 1));
            tmpBuildingLine.setEndY(768 - 60 * (i + 1));
            this.buildingCanvas.getChildren().add(tmpBuildingLine);
        }
        if (levs <= 10) {
            for (int i = 0; i < levs; ++i) {
                Button tmpBuildingButton = new Button("#" + (i + 1));
                tmpBuildingButton.setPrefSize(60, 40);
                tmpBuildingButton.setLayoutX(10);
                tmpBuildingButton.setLayoutY(768 - (i + 1) * 60 + 10);
                tmpBuildingButton.setOnAction(new LevelButtonOnClickHandler(i, this.c.getLevelList().get(i)));
                this.buildingCanvas.getChildren().add(tmpBuildingButton);
                this.GUILevelButtons.add(tmpBuildingButton);
            }
        }
        else {
            for (int i = 0; i < 10; ++i){
                Button tmpBuildingButton = new Button("#" + (i + 1));
                tmpBuildingButton.setPrefSize(60, 40);
                tmpBuildingButton.setLayoutX(10);
                tmpBuildingButton.setLayoutY(768 - (i + 1) * 60 + 10);
                tmpBuildingButton.setOnAction(new LevelButtonOnClickHandler(i, this.c.getLevelList().get(i)));
                this.buildingCanvas.getChildren().add(tmpBuildingButton);
                this.GUILevelButtons.add(tmpBuildingButton);
            }
            for (int i = 10; i < levs; ++i){
                Button tmpBuildingButton = new Button("#" + (i + 1));
                tmpBuildingButton.setPrefSize(60, 40);
                tmpBuildingButton.setLayoutX(10);
                tmpBuildingButton.setLayoutY(768 - (i - 10 + 1) * 60 + 10); 
                tmpBuildingButton.setOnAction(new LevelButtonOnClickHandler(i, this.c.getLevelList().get(i)));
                this.GUILevelButtons.add(tmpBuildingButton);
            }
            Button pageUpButton = new Button("PageUp");
            pageUpButton.setOnAction(new PageButtonOnClickHandler(true,this));
            Button pageDownButton = new Button("PageDown");
            pageDownButton.setOnAction(new PageButtonOnClickHandler(false, this));
            pageUpButton.setPrefSize(100, 60);
            pageDownButton.setPrefSize(100, 60);
            pageUpButton.setLayoutX(10);
            pageDownButton.setLayoutX(120);
            pageUpButton.setLayoutY(60);
            pageDownButton.setLayoutY(60);
            this.buildingCanvas.getChildren().addAll(pageUpButton, pageDownButton);
        }
    }

    /**
     * re-draw an elevator at some level, should show or un-show them according to the page
     * @param index the index of the elevator
     * @param lev the new level the elevator is at
     */
    public void moveElevator(int index, int lev){
        if (this.c.getLevels() <= 10) {
            --index;
            this.GUIElevatorLabels.get(index).setLayoutY(768 - 60 * lev);
            System.out.printf("View: Elevator#%d moved to #%d Level.\n", index + 1, lev);
        }
        else {
            --index;
            if (this.isFirstPageShown){
                if (lev > 10){
                    this.GUIElevatorLabels.get(index).setLayoutY(768 - 60 * (lev - 10));
                    this.unshowElevator(this.GUIElevatorLabels.get(index));
                    System.out.printf("View: Elevator#%d moved to #%d Level, and not shown.\n", index + 1, lev);
                }
                else {
                    this.GUIElevatorLabels.get(index).setLayoutY(768 - 60 * lev);
                    this.showElevator(this.GUIElevatorLabels.get(index));
                    System.out.printf("View: Elevator#%d moved to #%d Level, and shown.\n", index + 1, lev);
                }
            }
            else {
                if (lev <= 10){
                    this.GUIElevatorLabels.get(index).setLayoutY(768 - 60 * lev);
                    this.unshowElevator(this.GUIElevatorLabels.get(index));
                    System.out.printf("View: Elevator#%d moved to #%d Level, and not shown.\n", index + 1, lev);
                }
                else {
                    this.GUIElevatorLabels.get(index).setLayoutY(768 - 60 * (lev - 10));
                    this.showElevator(this.GUIElevatorLabels.get(index));
                    System.out.printf("View: Elevator#%d moved to #%d Level, and shown.\n", index + 1, lev);
                }
            }
        }
    }

    /**
     * show an elevator
     * @param e the elevator label
     */
    private void showElevator(Label e){
        Platform.runLater(() -> {
            if (!this.buildingCanvas.getChildren().contains(e)){
                this.buildingCanvas.getChildren().add(e);
            }
        });
    }

    /**
     * un-show an elevator
     * @param e the label of the elevator
     */
    private void unshowElevator(Label e){
        Platform.runLater(() -> {
            if (this.buildingCanvas.getChildren().contains(e)){
                this.buildingCanvas.getChildren().remove(e);
            }
        });
    }

    /**
     * set the page showing
     * @param isFirstPageShownIndicator true if the first page is being shown, false otherwise
     */
    private void setIsFirstPageShown(boolean isFirstPageShownIndicator){
        this.isFirstPageShown = isFirstPageShownIndicator;
    }

    /// to write down the number of levels for the user
    @FXML
    private TextField numberOfLevelsText;

    /// to write down the number of elevators for the user
    @FXML
    private TextField numberOfElevatorsText;

    /// the button of creating a new building
    @FXML
    private Button createBuildingButton;

    /// to write down the capacity of the elevators for the user
    @FXML
    private TextField capacityText;

    /// when adding a new passenger, choose its starting level here
    @FXML
    private ChoiceBox<Integer> startingLevelChoiceBox;

    /// when adding a new passenger, choose its destination here
    @FXML
    private ChoiceBox<Integer> destLevelChoiceBox;

    /// the button to add a new passegner
    @FXML
    private Button addPassengerButton;

    /// list the three strategies and make the user choose by this ListView
    @FXML
    private ListView<String> strategyListView;

    /// show the help information of strategies button
    @FXML
    private Button strategyHelpButton;

    /// the button to add random passengers
    @FXML
    private Button testPassengerListButton;

    /// the canvas to draw the whole building
    @FXML
    private AnchorPane buildingCanvas;

    /**
     * when click on the create building button, create the building but firstly check the parameters
     */
    @FXML
    private void onClickCreateBuildingButton(){
        if (this.c != null)
            this.c.stopThread();
        if (this.buildingCanvas != null){
            this.buildingCanvas.getChildren().clear();
        }
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
            return;
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
            this.strategyListView.setItems(FXCollections.observableArrayList("Speed First (Default)", "Load Balancing", "Power Saving"));
        }
        this.isFirstPageShown = true;
//        this.c.randomizeElevators();
        this.initGUIElevators(elevators);
        this.initGUILevels(levels);
        this.c.start();
    }

    /**
     * handle the event that the user requires to add a new passenger
     */
    @FXML
    private void onClickAddPassengerButton(){
        System.out.println("View: Add a passenger.");
        // if the building has not been created...
        if (this.c == null || !this.c.isBuildingCreated()){
            Alert errorNullBuilding = new Alert(Alert.AlertType.INFORMATION, "Please create the building FIRST!");
            errorNullBuilding.setTitle("Building not Created Error!");
            errorNullBuilding.setHeaderText("Information");
            errorNullBuilding.showAndWait();
            System.out.println("View: Building not Created Error!");
        }
        else {
            if (this.startingLevelChoiceBox.getValue() == null || this.destLevelChoiceBox.getValue() == null){
                System.out.println("View: Bad Passenger!");
                return;
            }
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

    /**
     * show the help info about strategies
     */
    @FXML
    private void onClickStrategyHelpButton(){
        StringBuilder sb = new StringBuilder();
        sb.append("Help:\n\n");
        sb.append("Here are three strategies on controlling elevators:\n\n");
        sb.append("Speed First:\n\n");
        sb.append("On Speed First mode, every passenger waiting for elevators can get on an elevator as fast as possible.\n\n\n");
        sb.append("Load Balancing:\n\n");
        sb.append("On Load Balancing mode, when a passenger comes, the system will consider both the waiting time of the passenger and the current load of each elevator.\n\n\n");
        sb.append("Power Saving:\n\n");
        sb.append("On Power Saving mode, the system will try to make as less elevators running as possible (but not making only one elevator running).");
        Alert strategyHelpMessage = new Alert(Alert.AlertType.INFORMATION, sb.toString());
        strategyHelpMessage.setTitle("Dispatching Strategy: Help");
        strategyHelpMessage.setHeaderText("Help:");
        strategyHelpMessage.showAndWait();
    }

    /**
     * add random passengers
     */
    @FXML
    private void onClickTestPassengerListButton(){
        System.out.println("View: Test-case passenger.");
        if (this.c == null || !this.c.isBuildingCreated()){
            Alert errorNullBuilding = new Alert(Alert.AlertType.INFORMATION, "Please create the building FIRST!");
            errorNullBuilding.setTitle("Building not Created Error!");
            errorNullBuilding.setHeaderText("Information");
            errorNullBuilding.showAndWait();
            System.out.println("View: Building not Created Error!");
        }
        else {
            System.out.println("View: Test-case passengers added!");
            Alert testCaseMessage = new Alert(Alert.AlertType.INFORMATION, "There will be a list of random passengers added to the building!");
            testCaseMessage.setTitle("Test-case Starting!");
            testCaseMessage.setHeaderText("Information");
            testCaseMessage.showAndWait();
            Random rd = new Random(System.currentTimeMillis());
            ArrayList<Integer> randomNums = new ArrayList<>();
            for (int i = 0; i < 10; ++i){
                randomNums.add(Math.abs(rd.nextInt() % (this.c.getLevels() - 1)) + 1);
            }
            System.out.println("View: Added test-case passenger with random numbers:");
            System.out.println(randomNums.toString());
            for (int i = 2; i <= this.c.getLevels(); ++i){
                for (int j = 0; j < (i < 5 ? i : 5); ++j){
                    if (i != randomNums.get(j))
                        this.c.addPassenger(randomNums.get(j), i);
                }
                for (int j = 5; j < (i + 5 < 10 ? i + 5 : 10); ++j){
                    if (i != randomNums.get(j))
                        this.c.addPassenger(i, randomNums.get(j));
                }
            }
        }
    }

    /**
     * change the strategy
     */
    @FXML
    private void onChangingStrategyList(){
        int strategy = this.strategyListView.getSelectionModel().getSelectedIndex();
        this.c.setStrategy(strategy);
        System.out.printf("View: Dispatching Strategy changed: %d\n", strategy);
    }
}
