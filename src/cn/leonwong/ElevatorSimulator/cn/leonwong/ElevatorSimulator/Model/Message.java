package cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model;

public class Message {

    public static final int passengerLeaveElevator = 0;
    public static final int passengerEnterElevator = 1;
    public static final int elevatorChangeFloor = 2;
    public static final int elevatorIsIdle = 3;

    public int mode;
    public int destElevator;
    public int destLevel;
    public Passenger pass;

    public Message(int modeOfMessage, int elevatorSender, int levelHappened, Passenger passenger){
        this.mode = modeOfMessage;
        this.destElevator = elevatorSender;
        this.destLevel = levelHappened;
        this.pass = passenger;
    }
}
