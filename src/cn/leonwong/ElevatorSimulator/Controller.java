package cn.leonwong.ElevatorSimulator;

import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Building;
import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Elevator;
import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Message;
import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Passenger;

import java.util.ArrayList;
import java.util.Vector;

public class Controller extends Thread {
    public static class DispatchingStrategy{
        public static final int SpeedFirst = 0;
        public static final int LoadBalancing = 1;
        public static final int PowerSaving = 2;
    }
    private int strategy;
    private Vector<Message> messageCenter;
    private View view;
    private Thread t;
    private Building building;

    public Controller(){
        this.strategy = DispatchingStrategy.SpeedFirst;
    }

    @Override
    public void start(){
        if (this.t == null){
            this.t = new Thread(this, "Controller");
            t.start();
        }
    }

    @Override
    public void run(){
        while (true){
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
                    else if (tmp.mode == Message.elevatorChangeFloor)
                        System.out.printf("Controller: Elevator #%d goes to #%d floor.\n", tmp.destElevator, tmp.destLevel);
                    else if (tmp.mode == Message.elevatorIsIdle)
                        System.out.printf("Controller: Elevator #%d is idle.\n", tmp.destElevator);
                    else
                        System.out.println("Controller received wrong message!");
                }
                this.messageCenter.remove(0);

            }
        }
    }

    public void setMessageCenter(Vector<Message> mc){
        this.messageCenter = mc;
    }

    public void setView(View v){
        this.view = v;
    }

    public void addPassenger(int from, int to){
        this.building.levelList.get(from).add(new Passenger(to));
        this.arrangeForElevator(from, to);
    }

    public void setBuilding(Building b){
        this.building = b;
    }

    public void createBuilding(int levs, int elevs, int cap){
        this.building = new Building(levs, elevs, cap);
        this.messageCenter = this.building.messageCenter;
    }

    public boolean isBuildingCreated(){
        return this.building != null;
    }

    public int getLevels(){
        return this.building.getLevels();
    }

    public int getElevators(){
        return this.building.getElevators();
    }

    private void arrangeForElevator(int from, int to){
        if (this.strategy == DispatchingStrategy.SpeedFirst){
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                if (e.getDirection() * (to - from) >= 0)
                    waitingTime.add(Math.abs(from - e.getLevel()));
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2);
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from);
                }
            }
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
        else if (this.strategy == DispatchingStrategy.LoadBalancing){
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                if (e.getDirection() * (to - from) >= 0)
                    waitingTime.add(Math.abs(from - e.getLevel()) + e.getPassengers());
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2 + e.getPassengers());
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from + e.getPassengers());
                }
            }
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
        else if (this.strategy == DispatchingStrategy.PowerSaving){
            ArrayList<Integer> waitingTime = new ArrayList<>();
            for (Elevator e : this.building.elevatorList){
                if (e.isIdle()){
                    waitingTime.add(Integer.MAX_VALUE);
                }
                else if (e.getDirection() * (to - from) >= 0)
                    waitingTime.add(Math.abs(from - e.getLevel()));
                else {
                    if (e.getDirection() == Elevator.Direction.Upward)
                        waitingTime.add(e.getMaxDestination() * 2 - e.getLevel() - from + 2);
                    else if (e.getDirection() == Elevator.Direction.Downward)
                        waitingTime.add(e.getMinDestination() + from);
                }
            }
            int fastestIndex = 0;
            for (int i = 0; i < this.building.getElevators(); ++i){
                if (waitingTime.get(i) < waitingTime.get(fastestIndex))
                    fastestIndex = i;
            }
            this.building.elevatorList.get(fastestIndex).addDestination(from);
        }
    }
}
