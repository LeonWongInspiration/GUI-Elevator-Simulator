package cn.leonwong.ElevatorSimulator.Model;

import java.util.ArrayList;
import java.util.Vector;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Elevator Thread
 * @author 1652795 Leon Wong
 */
public class Elevator extends Thread {

    /// time interval for going to another floor
    public static final int FLOOR_INTERVAL = 500;
    /// time interval for a passenger entering/leaving
    public static final int PASS_INTERVAL = 200;

    /**
     * A class contains to directions in order to indicate direction
     */
    public static final class Direction {
        public static final int Upward = 1;
        public static final int changingDirection = 0;
        public static final int Downward = -1;
    }
    /// Note for the direction this elevator is heading
    private int direction;
    /// When changing direction is needed, add another flag
    private int nextDire;
    /// Thread for an elevator
    private Thread t;
    /// Note for  the index of this elevator
    private int index;
    /// Note for person waiting for elevators in each level
    private Vector< Vector<Passenger> > levelList;
    /// Note for max level available
    private int maxLevel;
    /// Note for maximum numbers of passengers available in this elevator
    private int maxPassenger;
    /// Note for which level this elevator is at
    private int level;
    /// Note for messageCenter
    private Vector<Message> messageCenter;
    /// Note for each passenger in this elevator
    private Vector<Passenger> passengerList;
    /// Note for each level that this elevator should stop and open
    private TreeSet<Integer> destinations;
    /// Note for the lock used to lock levelList
    private ReentrantLock lock;

    /**
     * Start this Thread
     */
    @Override
    public void start(){
        System.out.printf("Elevator #%d starting.\n", this.index);
        if (this.t == null){
            this.t = new Thread(this, "Elevator #" + this.index);
            this.t.start();
        }
    }

    /**
     * Run function
     */
    @Override
    public void run(){
        while (!this.stop){
            try {
                Thread.sleep(FLOOR_INTERVAL);
            }
            catch (InterruptedException e){
                System.out.println("Interrupted: " + this.index);
            }
            this.decideDestinations();
            // if this elevator is idle...
            if (this.isIdle()){
                // just do nothing and go on...
                System.out.println("Elevator: Elevator #" + this.index + " is idle.");
                this.messageCenter.add(new Message(
                        Message.elevatorIsIdle, this.index, 0, null
                ));
            }
            // else...
            else {
                // move the elevator according to the direction
                this.level += this.direction;
                System.out.printf("Elevator: Elevator #%d goes %+d.\n", this.index, this.direction);
                // for each passenger who wants to get down at this level...
                this.messageCenter.add(new Message(
                        Message.elevatorChangeFloor, this.index, this.level, null
                ));
                ArrayList<Passenger> leaving = new ArrayList<>();
                for (Passenger pass : this.passengerList){
                    if (pass.destination == this.level)
                        leaving.add(pass);
                }
                // remove them from the passenger level
                for (Passenger pass : leaving){
                    this.passengerLeaveElevator(this.level, pass);
                }
                // and remove this level from destinations
                this.destinations.remove(this.level);
                // for each passenger at this level...
                // lock the levelList first
                this.lock.lock();
                try {
                    ArrayList<Passenger> gettingOn = new ArrayList<>();
                    for (Passenger pass : this.levelList.get(this.level)) {
                        // if the passenger has the SAME direction as the elevator...
                        if ((pass.destination - this.level) * this.direction >= 0 || this.isIdle())
                            // this passenger gets on this elevator
                            gettingOn.add(pass);
                    }
                    for (Passenger pass : gettingOn) {
                        this.passengerEnterElevetor(this.level, pass);
                    }
                }
                finally {
                    this.lock.unlock();
                }

                this.decideDirection();
            }
        }
    }

    @Override
    public String toString(){
        return "This is the #" + this.index + " Elevator.";
    }

    /**
     * create a new elevator
     * @param name the in dex of this elevator
     * @param max the max level of the building
     * @param maxPass the capacity of this elevator
     * @param levs a list for passengers in each level
     * @param mess message center
     * @param l a reentrantlock to lock level list
     */
    public Elevator(int name, int max, int maxPass, Vector< Vector<Passenger> > levs, Vector<Message> mess, ReentrantLock l){
        this.index = name;
        this.maxLevel = max;
        this.maxPassenger = maxPass;
        this.levelList = levs;
        this.messageCenter = mess;
        this.lock = l;
        this.level = 1;
        this.direction = Direction.changingDirection;
        this.passengerList = new Vector<>();
        this.destinations = new TreeSet<>();
        this.nextDire = Direction.Upward;
        this.stop = false;
    }

    /**
     * add one passenger into this elevator
     * @param lev where the passenger entered the elevator
     * @param pass an object refering to the passenger
     * @return true if the passenger entered this elevator successfully, false otherwise
     */
    private synchronized boolean passengerEnterElevetor(int lev, Passenger pass){
        if (this.isFull()) {
            System.out.printf("Elevator: Elevator #%d is full!\n", this.index);
            return false;
        }
        else {
            this.passengerList.add(pass);
            this.messageCenter.add(
                    new Message(Message.passengerEnterElevator, this.index, pass.destination, pass)
            );
            this.destinations.add(pass.destination);
            try {
                Thread.sleep(PASS_INTERVAL);
            }
            catch (InterruptedException e){
                System.out.println("Interrupted: " + this.index);
            }
            System.out.printf("Elevator: A passenger heading for #%d floor entered elevator #%d at level #%d.\n", pass.destination, this.index, this.level);
            this.levelList.get(lev).remove(pass);
            return true;
        }
    }

    /**
     * remove a passenger from this elevator
     * @param lev the level that the passenger leaves at
     * @param pass an object refering to the passenger
     * @return true if the passenger removed successfully, false otherwise
     */
    private synchronized boolean passengerLeaveElevator(int lev, Passenger pass){
        if (this.isEmpty())
            return false;
        else {
            this.passengerList.remove(pass);
            this.messageCenter.add(
                    new Message(Message.passengerLeaveElevator, this.index, lev, pass)
            );
            try {
                Thread.sleep(PASS_INTERVAL);
            }
            catch (InterruptedException e){
                System.out.println("Interrupted: " + this.index);
            }
            System.out.printf("Elevator: A passenger left the elevator #%d at #%d floor.\n", this.index, this.level);
            return true;
        }
    }

    /**
     * decide whether this elevator is full
     * @return true if this elevator is full, false otherwise
     */
    public boolean isFull(){
        return this.passengerList.size() == this.maxPassenger;
    }

    /**
     * decide whether this elevator is empty
     * @return true if this elevator is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.passengerList.isEmpty();
    }

    /**
     * decide whether this elevator is idle
     * @return true if this elevator is idle, false otherwise
     */
    public boolean isIdle(){
        return this.destinations.isEmpty();
    }

    /**
     * add a destination for this elevator
     * @param dest the destination level to be added
     */
    public void addDestination(int dest){
        this.destinations.add(dest);
    }

    /**
     * getter for the current level this elevator is at
     * @return the current level this elevator is at
     */
    public int getLevel(){
        return this.level;
    }

    /**
     * decide what direction this elevator should go after a level's all work done
     */
    private synchronized void decideDirection(){
        // if the elevator has just changed its direction...
        if (this.direction == Direction.changingDirection){
            // use the direction decided just now
            this.direction = this.nextDire;
            return;
        }
        if (this.level == this.maxLevel){
            this.direction = Direction.Downward;
            return;
        }
        if (this.level == 1){
            this.direction = Direction.Upward;
            return;
        }
        // or if the elevator is still running towards the same direction...
        for (int i = this.level + this.direction; i != 0 && i != maxLevel + 1; i = i + this.direction){
            // find if it is needed to go on towards the current direction
            if (this.destinations.contains(i))
                return;
        }
        // Otherwise, change its direction
        this.nextDire = -this.direction;
        this.direction = Direction.changingDirection;
    }

    /**
     * getter for the current direction of this elevator
     * @return the current direction of this elevator
     */
    public int getDirection() {
        return direction;
    }

    /**
     * getter for the biggest level this elevator is heading for
     * @return the biggest number of level in destination list
     */
    public int getMaxDestination(){
        int maxLev = 0;
        for (Passenger p : this.passengerList)
            if (p.destination > maxLev)
                maxLev = p.destination;
        for (Integer i : this.destinations)
            if (i > maxLev)
                maxLev = i;
        return maxLev;
    }

    /**
     * getter for the lowest level this elevator is heading for
     * @return the smallest number of level in destination list
     */
    public int getMinDestination(){
        int minLev = this.maxLevel;
        for (Passenger p : this.passengerList)
            if (p.destination < minLev)
                minLev = p.destination;
        for (Integer i : this.destinations)
            if (i < minLev)
                minLev = i;
        return minLev;
    }

    /**
     * getter for the numbers of passengers in this elevator
     * @return the number of passengers in this elevator
     */
    public int getPassengers(){
        return this.passengerList.size();
    }

    /**
     * use this to ensure that every passenger's destination is in this destination list
     */
    private void decideDestinations(){
        for (Passenger pass : this.passengerList)
            this.destinations.add(pass.destination);
    }

    /**
     * directly move this elevator to a level
     * @param l the destination level
     */
    public void setLevel(int l){
        this.level = l;
    }

    /// decide whether this thread has been stopped
    private boolean stop;

    /**
     * used to stop this thread
     */
    public void stopThread(){
        this.stop = true;
    }

    /**
     * getter for a list of destinations of this elevator in a String
     * @return the String of list of destinations
     */
    public String getDestinations(){
        return this.destinations.toString();
    }

    /**
     * getter for the destination list's size
     * @return this size of this destination list
     */
    public int getDestinationSize(){
        return this.destinations.size();
    }
}
