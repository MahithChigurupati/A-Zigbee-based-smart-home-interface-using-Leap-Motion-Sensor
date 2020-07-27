package samp1;
import java.io.IOException;
import java.lang.Math;
import com.leapmotion.leap.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

class SampleListener extends Listener {
    /**
     * Device 1 On = A
     * DEvice 1 Off= B
     * 
     * Device 2 On = C
     * Device 2 Off = D
     * 
     * Device 3 
     * 
     * intensity level 1-9
     * 
     * 
     */
    
    char device[]={'A','B','C','D'};
    char dimmer[]={'0','1','2','3','4','5','6','7','8','9'};
    int curDevice=0;
    int curStatus=0;
    int intensity=-1;
    int action=-1;
    long TIME_DELAY=2000;
    long delay=0;
    int lastValue=-1;
     private  SerialPort serialPort;
    long time=0;
    
    
    public SampleListener(){
        serialPort = new SerialPort("com5");
        try{
           serialPort.openPort();

            serialPort.setParams(SerialPort.BAUDRATE_9600, //port is capable for transmiting 9600 bits per second
                                 SerialPort.DATABITS_8,  
                                 SerialPort.STOPBITS_1,  //One trailing bit is added to mark the end of the word.
                                 SerialPort.PARITY_NONE);
        }catch(Exception e){
            e.printStackTrace();
        } 
    }
    
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE,true);
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE,true);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP,true);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP,true);
        
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }
long tmpTime=0;
    
    public void onFrame(Controller controller){
          Frame frame = controller.frame();
          
          InteractionBox iBox = controller.frame().interactionBox();
Pointable pointable = controller.frame().pointables().frontmost();

Vector leapPoint = pointable.stabilizedTipPosition();
Vector normalizedPoint = iBox.normalizePoint(leapPoint, true);

//        System.out.println("Frame id: " + frame.id()
//                         + ", timestamp: " + frame.timestamp()
//                         + ", hands: " + frame.hands().count()
//                         + ", fingers: " + frame.fingers().count()
//                         +" X = "+normalizedPoint.getX()
//                         +" Y = "+normalizedPoint.getY()
//                         +" Z = "+normalizedPoint.getY());
        if(frame.hands().count()==0)
            return;
        action=0;
        for(Gesture gesture : frame.gestures()){
            if(gesture.type() == Gesture.Type.TYPE_SWIPE){
                SwipeGesture swipe = new SwipeGesture(gesture);
                tmpTime =getTime();
                    if(swipe.direction().get(0) >0.5 && time< tmpTime){
                        time=tmpTime+1000;
                   
                        curDevice+=2;
                        if(curDevice>=4)
                            curDevice=4;
                             System.out.println("Right "+curDevice);
                    }
                    tmpTime =getTime();
                    if(swipe.direction().get(0) <-0.5 && time<getTime()){
                        time=tmpTime+1000;
                      
                            curDevice-=2;
                        if(curDevice<=0)
                            curDevice=0;
                          System.out.println("Left "+curDevice);
                    }
//                      tmpTime =getTime();
//                    if(swipe.direction().get(1) >0.5 && time<getTime()){
//                        time=tmpTime+1000;
//                        System.out.println("Forward");
//                    }
//                      tmpTime =getTime();
//                    if(swipe.direction().get(1) <-0.5 && time<getTime()){
//                        time=tmpTime+1000;
//                        System.out.println("Backward");
//                    }
//                    
            }
            if(gesture.type() == Gesture.Type.TYPE_SCREEN_TAP && time<getTime()){
                        time=tmpTime+3000;
                      //  System.out.println("Backward tap");
                                              curStatus = (curStatus==1)?0:1;
                        System.out.println("Backward tap "+curStatus);
                        action=1;

            }else
            if(gesture.type() == Gesture.Type.TYPE_KEY_TAP && time<getTime()){
                        time=tmpTime+3000;
                       
                        curStatus = (curStatus==1)?0:1;
                        System.out.println("Backward ktap "+curStatus);
                        action=1;
            }
            
        }
        if(curDevice<4){
            if(action==1){
             System.out.println("Device Stat "+device[curDevice+curStatus]);
                try {
                    serialPort.writeByte((byte)device[curDevice+curStatus]);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }
        else{
            int inten=0;
            for(int k=0;k<10;k++){
             inten += (int)(normalizedPoint.getY()*10.0);
            }
            inten = inten/10;
            if(lastValue==-1){
                lastValue=inten;
                try {
                    if(inten>0 && inten<10)
                    serialPort.writeByte((byte)dimmer[inten]);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
                System.out.println("DEV 3 inten "+inten + " curDev "+curDevice);
            }
            delay+=1;
            if(delay>TIME_DELAY || inten!=lastValue){
                delay=0;
                lastValue=inten;
                try {
                          if(inten>0 && inten<10)
                    serialPort.writeByte((byte)dimmer[inten]);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            System.out.println("DEV 3 inten "+inten + " curDev "+curDevice + " lastv "+lastValue + " delay "+delay);
            }
        }
    }
    
    
    public long getTime(){
        return new Date().getTime();
    }
    
    public void onFrame1(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();
        System.out.println("Frame id: " + frame.id()
                         + ", timestamp: " + frame.timestamp()
                         + ", hands: " + frame.hands().count()
                         + ", fingers: " + frame.fingers().count());

        //Get hands
        for(Hand hand : frame.hands()) {
            String handType = hand.isLeft() ? "Left hand" : "Right hand";
            System.out.println("  " + handType + ", id: " + hand.id()
                             + ", palm position: " + hand.palmPosition());

            // Get the hand's normal vector and direction
            Vector normal = hand.palmNormal();
            Vector direction = hand.direction();

            // Calculate the hand's pitch, roll, and yaw angles
            System.out.println("  pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
                             + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
                             + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees");

            // Get arm bone
            Arm arm = hand.arm();
            System.out.println("  Arm direction: " + arm.direction()
                             + ", wrist position: " + arm.wristPosition()
                             + ", elbow position: " + arm.elbowPosition());

            // Get fingers
            for (Finger finger : hand.fingers()) {
                System.out.println("    " + finger.type() + ", id: " + finger.id()
                                 + ", length: " + finger.length()
                                 + "mm, width: " + finger.width() + "mm");

                //Get Bones
                for(Bone.Type boneType : Bone.Type.values()) {
                    Bone bone = finger.bone(boneType);
                    System.out.println("      " + bone.type()
                                     + " bone, start: " + bone.prevJoint()
                                     + ", end: " + bone.nextJoint()
                                     + ", direction: " + bone.direction());
                }
            }
        }

        if (!frame.hands().isEmpty()) {
            System.out.println();
        }
    }
}

class Sample {
    public static void main(String[] args) {
        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}
