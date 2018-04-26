package cn.leonwong.ElevatorSimulator;

import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Building;
import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Message;
import cn.leonwong.ElevatorSimulator.cn.leonwong.ElevatorSimulator.Model.Passenger;

import java.util.Vector;

public class Controller extends Thread {
    private Vector<Message> messageCenter;
    private View view;
    private Thread t;
    private Building building;

    public Controller(){

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
}
