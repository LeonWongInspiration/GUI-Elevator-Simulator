package cn.leonwong.ElevatorSimulator;

import java.util.Vector;

public class Controller extends Thread {
    private Vector<Message> messageCenter;
    private View view;
    private Thread t;
    public Controller(Vector<Message> mc, View v){
        this.messageCenter = mc;
        this.view = v;
    }

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
}
