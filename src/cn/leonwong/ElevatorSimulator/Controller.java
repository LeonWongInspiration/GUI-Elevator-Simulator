package cn.leonwong.ElevatorSimulator;

import cn.leonwong.ElevatorSimulator.Model.Building;
import cn.leonwong.ElevatorSimulator.Model.Elevator;
import cn.leonwong.ElevatorSimulator.Model.Message;
import cn.leonwong.ElevatorSimulator.Model.Passenger;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

/**
 * a thread controls all the models
 */
public class Controller extends Thread {
    /**
     * an enum for dispathching strategies
     */
    public static class DispatchingStrategy{
        public static final int SpeedFirst = 0;
        public static final int LoadBalancing = 1;
        public static final int PowerSaving = 2;
    }
    /// the current strategy
    private int strategy;
    /// message center
    private Vector<Message> messageCenter;
    /// an object trefering to view
    private View view;
    /// the controller's thread
    private Thread t;
    /// denote the building
    private Building building;

    /**
     * default constructor, set strategy to "Speed First" and thread running
     */
    public Controller(){
        this.strategy = DispatchingStrategy.SpeedFirst;
        this.stop = false;
    }

    /**
     * start this thread
     */
    @Override
    public void start(){
        if (this.t == null){
            this.t = new Thread(this, "Controller");
            t.start();
        }
    }

    /**
     * run this thread
     */
    @Override
    public void run(){
        while (!this.stop){
            if (this.messageCenter == null){
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e){
                    System.out.println("Message Center is NULL !! And sleep INTERRUPTED!!");
                }
                System.out.println("Message Center not initialized!");
                continue;
            }
            while (!this.messageCenter.isEmpty()){
                Message tmp = this.messageCenter.firstElement();
                if (tmp != null) {
                    if (tmp.mode == Message.passengerLeaveElevator)
                        System.out.printf("Controller: A passenger left elevator #%d at #%d floor.\n", tmp.destElevator, tmp.destLevel);
                    else if (tmp.mode == Message.passengerEnterElevator)
                        System.out.printf("Controller: A passenger heading for #%d floor entered elevator #%d.\n", tmp.destLevel, tmp.destElevator);
                    else if (tmp.mode == Message.elevatorChangeFloor) {
                        System.out.printf("Controller: Elevator #%d goes to #%d floor.\n", tmp.destElevator, tmp.destLevel);
                        this.view.moveElevator(tmp.destElevator, tmp.destLevel);
                    }
                    else if (tmp.mode == Message.elevatorIsIdle)
                        System.out.printf("Controller: Elevator #%d is idle.\n", tmp.destElevator);
                    else
                        System.out.println("Controller received wrong message!");
                }
                this.messageCenter.remove(0);

            }
        }
    }

    /**
     * setter for message center
     * @param mc the message center
     */
    public void setMessageCenter(Vector<Message> mc){
        this.messageCenter = mc;
    }

    /**
     * setter fr view
     * @param v the view
     */
    public void setView(View v){
        this.view = v;
    }

    /**
     * add a new passenger
     * @param from the starting level of the passenger
     * @param to the destination of the passenger
     */
    public void addPassenger(int from, int to){
        this.building.levelList.get(from).add(new Passenger(to));
        this.arrangeForElevator(from, to);
    }

    /**
     * setter for building
     * @param b the building
     */
    public void setBuilding(Building b){
        this.building = b;
    }

    /**
     * create a new building using the corrected params
     * @param levs the total levels of this building
     * @param elevs the numbers of elevators of this building
     * @param cap the capacity of each elevator
     */
    public void createBuilding(int levs, int elevs, int cap){
        this.building = new Building(levs, elevs, cap);
        this.messageCenter = this.building.messageCenter;
    }

    /**
     * determine whether the building has been created
     * @return true if the building has been created, false otherwise
     */
    public boolean isBuildingCreated(){
        return this.building != null;
    }

    /**
     * getter for total levels
     * @return the number of levels
     */
    public int getLevels(){
        return this.building.getLevels();
    }

    /**
     * getter for the numbers of elevators
     * @return the number of elevators
     */
    public int getElevators(){
        return this.building.getElevators();
    }

    /**
     * arrage for an elevator to some level according to the dispatching strategy
     * @param from the starting level
     * @param to the destination level
     */
    private void arrangeForElevator(int from, int to){
        // if the strategy is Speed First...
        if (this.strategy == DispatchingStrategy.SpeedFirst){
            // create an ArrayList to take down estimated time of elevator arrival
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                // if the elevator is full...
                if (e.isFull())
                    // add a maxInteger to the time list, i.e. ignoring this elevator
                    waitingTime.add(Integer.MAX_VALUE);
                // if the elevator's direction is the same as the passenger's...
                else if (e.getDirection() * (to - from) >= 0) {
                    // and the elevator can arrive at the passenger's level without changing ite direction...
                    if ((e.getDirection() >= Elevator.Direction.changingDirection && from >= e.getLevel()) ||
                            (e.getDirection() <= Elevator.Direction.changingDirection && from <= e.getLevel()))
                        // the estimated time is just the floors which need a stop between
                        waitingTime.add(Math.abs(from - e.getLevel()) + e.getDestinationSize());
                    else
                        waitingTime.add(2 * this.getLevels() - Math.abs(from - e.getLevel()));
                }
                // else the elevator will change its direction once and pick up the passenger
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2 + e.getDestinationSize());
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from + e.getDestinationSize());
                }
            }
            // arrange for the fastest elevator
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
                else if (waitingTime.get(i).equals(waitingTime.get(fastestIndex)))
                    if (Math.abs(new Random(System.currentTimeMillis()).nextInt() % 100) >= 50)
                        fastestIndex = i;
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
        // if the strategy is Load Balancing...
        // In this mode, the number of passsengers in an elevator will also "increase" the estimated time
        else if (this.strategy == DispatchingStrategy.LoadBalancing){
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                // if the elevator is full...
                if (e.isFull())
                    // add a maxInteger to the time list, i.e. ignoring this elevator
                    waitingTime.add(Integer.MAX_VALUE);
                    // if the elevator's direction is the same as the passenger's...
                else if (e.getDirection() * (to - from) >= 0) {
                    // and the elevator can arrive at the passenger's level without changing ite direction...
                    if ((e.getDirection() >= Elevator.Direction.changingDirection && from >= e.getLevel()) ||
                            (e.getDirection() <= Elevator.Direction.changingDirection && from <= e.getLevel()))
                        // the estimated time is just the floors which need a stop between
                        waitingTime.add(Math.abs(from - e.getLevel()) + e.getDestinationSize() + e.getPassengers());
                    else
                        waitingTime.add(2 * this.getLevels() - Math.abs(from - e.getLevel()) + e.getPassengers());
                }
                // else the elevator will change its direction once and pick up the passenger
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2 + e.getDestinationSize() + e.getPassengers());
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from + e.getDestinationSize() + e.getPassengers());
                }
            }
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
                else if (waitingTime.get(i).equals(waitingTime.get(fastestIndex)))
                    if (Math.abs(new Random(System.currentTimeMillis()).nextInt() % 100) >= 50)
                        fastestIndex = i;
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
        // if the strategy is Power Saving...
        // in this mode, idle elevators will keep idle
        else if (this.strategy == DispatchingStrategy.PowerSaving){
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                // if the elevator is full OR IDLE
                if (e.isFull() || e.isIdle())
                    // add a maxInteger to the time list, i.e. ignoring this elevator
                    waitingTime.add(Integer.MAX_VALUE);
                    // if the elevator's direction is the same as the passenger's...
                else if (e.getDirection() * (to - from) >= 0) {
                    // and the elevator can arrive at the passenger's level without changing ite direction...
                    if ((e.getDirection() >= Elevator.Direction.changingDirection && from >= e.getLevel()) ||
                            (e.getDirection() <= Elevator.Direction.changingDirection && from <= e.getLevel()))
                        // the estimated time is just the floors which need a stop between
                        waitingTime.add(Math.abs(from - e.getLevel()) + e.getDestinationSize());
                    else
                        waitingTime.add(2 * this.getLevels() - Math.abs(from - e.getLevel()));
                }
                // else the elevator will change its direction once and pick up the passenger
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2 + e.getDestinationSize());
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from + e.getDestinationSize());
                }
            }
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
                else if (waitingTime.get(i).equals(waitingTime.get(fastestIndex)))
                    if (Math.abs(new Random(System.currentTimeMillis()).nextInt() % 100) >= 50)
                        fastestIndex = i;
            }
            // if all running elevators are full or idle...
            if (waitingTime.get(fastestIndex) == Integer.MAX_VALUE){
                for (int i = 0; i < this.building.getElevators(); ++i)
                    if (this.getElevatorList().get(i).isIdle()){
                        fastestIndex = i;
                        break;
                    }
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
    }

    /**
     * setter for strategy
     * @param str the strategy
     */
    public void setStrategy(int str){
        this.strategy = str;
    }

    /**
     * randomly place elevators
     */
    public void randomizeElevators(){
        for (Elevator e : this.building.elevatorList)
            e.setLevel(Math.abs(new Random(System.currentTimeMillis()).nextInt() % (this.getLevels() - 1)) + 1);
    }

    /// denote if this thread has been ended
    private boolean stop;

    /**
     * used to stop this thread and its elevators
     */
    public void stopThread(){
        for (Elevator e : this.building.elevatorList)
            e.stopThread();
        this.stop = true;
    }

    /**
     * getter for elevator list
     * @return the elevator list
     */
    public Vector<Elevator> getElevatorList(){
        return this.building.elevatorList;
    }

    /**
     * getter for passenger list
     * @return the passenger list
     */
    public Vector< Vector<Passenger> > getLevelList(){
        return this.building.levelList;
    }
}
