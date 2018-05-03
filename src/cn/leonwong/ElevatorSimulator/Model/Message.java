package cn.leonwong.ElevatorSimulator.Model;

/**
 * a class which defines a message
 */
public class Message {
    /// used to indicate this message is describing a passenger leaving an elevator
    public static final int passengerLeaveElevator = 0;
    /// used to indicate this message is describing a passenger entering an elevator
    public static final int passengerEnterElevator = 1;
    /// used to indicate this message is describing an elevator has moved to another floor
    public static final int elevatorChangeFloor = 2;
    /// used to indicate this message is describing an elevator is idle
    public static final int elevatorIsIdle = 3;

    /// to denote which kind of message this one is
    public int mode;
    /// to denote the elevator that this message took place in
    public int destElevator;
    /// to denote the level that this message took place at
    public int destLevel;
    /// to denote the passenger related to this message
    public Passenger pass;

    /**
     * to build a new messafe
     * @param modeOfMessage the kind of message
     * @param elevatorSender the elevator this message took place in
     * @param levelHappened the level this message took place at
     * @param passenger the related passenger
     */
    public Message(int modeOfMessage, int elevatorSender, int levelHappened, Passenger passenger){
        this.mode = modeOfMessage;
        this.destElevator = elevatorSender;
        this.destLevel = levelHappened;
        this.pass = passenger;
    }
}
