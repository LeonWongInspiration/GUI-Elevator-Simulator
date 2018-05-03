package cn.leonwong.ElevatorSimulator.Model;

/**
 * an easy model of a passenger who wants to go to some floor
 */
public class Passenger {
    /// the destination of this passenger
    public int destination;

    /**
     * create a new passenger heading for some floor
     * @param dest the destination of this passenger
     */
    public Passenger(int dest){
        this.destination = dest;
    }

}
