package edu.berean.robotics.dopple;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


/**
 * The abstract class DoppleBot is used to define a robot that can record its own state history
 * over time and play back it's history from an opmode.
 *
 * To enable robot record and playback, it is necessary to consistently initialize a robot across opmodes.
 * This is done to ensure each robot opmode has the same robot components
 * mapped to the same hardware and initialized consistently.
 *
 * To use, extend this class and define you own robot.  Implement all abstract methods as needed.
 *
 * This class also sets up the ability of any subclassed robot to record it's movement history.
 *
 * Created by wdhoward on 3/3/16.
 */
public abstract class DoppleBot {

    private static String LOG_TAG = "RECORDING ROBOT - ";
    private HashMap<String,HardwareDevice> robotComponents = new HashMap<String,HardwareDevice>();

    private boolean robotRecordingIsOn = false;
    private DoppleBotRecorder historyRecorder;
    private ArrayList initialStateValues = new ArrayList(Arrays.asList(0));



    /**
     * Run the DoppleBot initialization process setting the record mode upon init.
     * The boolean recordMode is used to tell the robot to record it's history.
     *
     * It is expected that the opmode init() method will call this method.  As a result,
     * be sure to override this with any robot specific initialization.
     *
     */
    public abstract void initializeRobot(HardwareMap hardwareMap);

    /**
     * It is expected that all subclasses will implement this robot specific abstract method
     * and connect this to the opmode start() method.
     */
    public abstract void startRobot();

    /**
     * Stop the robot and cleanup whatever needs to be cleaned up
     *
     * It is expected that this is called by the opmode stop() method.
     */
    public abstract void stopRobot();

    /**
     * Get a directory in the form of a HashMap of all available robot components
     * @return a HashMap of com.qualomm.robotcore.hardware.hardwareDevice where the key
     * is the name as a string. This map should be built in the subclass and will contain only
     * hardware devices that are supported by the recording functionality.
     */
    public HashMap<String, HardwareDevice> getRobotComponents(){
        return robotComponents;
    }

    /**
     * Instruct the robot to set things up for recording.  This doesn't actually record any
     * data, but ensures that the infrastructure is readied.  use the robotRecordingIsOn() method
     * to verify that the robot is ready to record.
     */
    public void startRecording(){

        RobotLog.i(LOG_TAG + "start recording");
        robotRecordingIsOn = true;
        historyRecorder = new DoppleBotRecorder(robotComponents);

    }
    /**
     * Instruct the robot to check its internal state and, if the state has changed since
     * the last update, it will update it's history in the DoppleBotHistoryRecord.
     */
    public void updateRecording(){

        if (robotRecordingIsOn) {
            RobotLog.i(LOG_TAG + "update recording");
            historyRecorder.update();
        } else {
            RobotLog.w(LOG_TAG + "can't update recording: recording is OFF!");
        }
    }

    /**
     * Instruct the robot to stop recording.  This will result in the robot writing the output
     * of the robot's history to a file.  A new file is created each time the recording is
     * started and stopped.
     */
    public void stopRecording(){

        RobotLog.i(LOG_TAG + "stop recording");

        if (robotRecordingIsOn){
            historyRecorder.writeHistory();
        } else {
            RobotLog.w(LOG_TAG + "can't stop recording: recording is OFF!");
        }
    }

    /**
     * Returns a boolean of true if the robot is currently recording history and false if it is not
     * recording history.
     * @return
     */
    public boolean robotRecordingIsOn(){
        return robotRecordingIsOn;
    }

    /**
     * Instruct the robot to replay a recording of it's history by sending an instance of the
     * DoppleBotHistoryRecord (as robotHistory) to this method.  The robot will examine the states
     * contained in the table and set the components state to match each row.  it will then allow the
     * robot to execute with those state value for the period of time indicated in the data.
     *
     * It is expected that this will always be called from an LinerOpMode class (or subclass) as that is the only
     * way to execute operations within the FTC infrastructure.  In order to facilitate the timing
     * between state changes, the calling OpMode itself must be passed in (as it contains the "sleep" function)
     * needed to allow the robot to operate in the state requested for the time requested.
     *
     * @param robotHistory
     * @param opMode
     */
    public void startPlayback(DoppleBotHistoryRecord robotHistory, LinearOpMode opMode){

        ArrayList<String> componentNames = robotHistory.getHeaderRow();
        ArrayList<ArrayList> valueRows = robotHistory.getValueRows();

        int rowsPlayedBack = 1;

        if (robotHistoryIsValid(robotHistory) && opMode.opModeIsActive()){
            RobotLog.d(LOG_TAG + String.format("component names: %s", componentNames.toString()));
            for (ArrayList valueRow: valueRows) {
                long timeToRun = 0;
                timeToRun = play(componentNames, valueRow);
                RobotLog.d(LOG_TAG + String.format("playing row %d of %d: %s for %d", rowsPlayedBack, valueRows.size(), valueRow.toString(), timeToRun));
                opMode.sleep(timeToRun);
                rowsPlayedBack++;
            }
            RobotLog.d(LOG_TAG + "Playback completed; making robot still.");
            stopPlayback(componentNames);
        } else
            RobotLog.w(LOG_TAG + "Cannot playback robot.  Table contains invalid data for robot.");

    }

    /**
     * This protected method is used by subclasses to add the components of the robot that will be recorded.
     * The doppleBot class will then be able to know which to pay attention to.  As part of adding a component
     * the initial state of the component can be recorded and used later when turning off.
     *
     * @param name a String that contains the name of the device for access. it's key value
     * @param currentComponent a HardwareDevice that is mapped to the name. As of now it must be a DCmotor or Servo
     */
    protected void addRobotComponent(String name, HardwareDevice currentComponent)
    {
        if (currentComponent.getClass().equals(DcMotorImpl.class) ||
                currentComponent.getClass().equals(ServoImpl.class))
        {
            robotComponents.put(name,currentComponent);

            if (currentComponent.getClass().equals(DcMotorImpl.class)) {
                double value = ((DcMotorImpl) currentComponent).getPower();
                initialStateValues.add(value);
            }

            if (currentComponent.getClass().equals(ServoImpl.class)) {
                double value = ((ServoImpl) currentComponent).getPosition();
                initialStateValues.add(value);
            }

            RobotLog.d(LOG_TAG + "Initial state is currently: " + initialStateValues.toString());
        }
    }

    /**
     * This method sets the robot to the initialized state
     * Pass in an arrayList of all the robot component names and it uses the initialStateValues
     * variable to set the state of all components back to that initial state.
     *
     * @param componentNames
     */
    private void stopPlayback(ArrayList<String> componentNames){
        long time = play(componentNames, initialStateValues);
    }

    /**
     * private method to play a single row from a robot history table.  Actually, it sets the state
     * of the robot for that row's data and returns the amount of time that it should execute
     * to the caller.  The caller will then be responsible for the delay until the next state
     * is sent to this method.
     *
     *
     * @param componentNames
     * @param values
     * @return
     */
    private long play(ArrayList<String> componentNames, ArrayList values){

        if (componentNames.size() != values.size())
        {
            RobotLog.e(LOG_TAG + "ERROR playing back.  The number of component values doesn't match the number of component names in the playback request!");
            throw new RuntimeException("ERROR playing back.  The number of component values doesn't match the number of component names in the playback request!");
        }

        int timeToRun = (Integer) values.get(0); //the first value should be the time in milliseconds
        for (int i = 1; i < componentNames.size() ; i++) {

            HardwareDevice currentComponent = robotComponents.get(componentNames.get(i));

            if (currentComponent.getClass().equals(DcMotorImpl.class) ||
                    currentComponent.getClass().equals(ServoImpl.class)) {

                if (currentComponent.getClass().equals(DcMotorImpl.class)) {
                    double currentPower = (Double) values.get(i);
                    ((DcMotor) currentComponent).setPower(currentPower);
                    RobotLog.d(LOG_TAG + String.format("playing: %s at power %f", componentNames.get(i), currentPower));
                }

                if (currentComponent.getClass().equals(ServoImpl.class)) {
                    double currentPosition = (Double) values.get(i);
                    ((Servo) currentComponent).setPosition(currentPosition);
                    RobotLog.d(LOG_TAG + String.format("playing: %s at position value %f", componentNames.get(i),currentPosition));
                }



            }

        }

        return timeToRun;

    }

    private boolean robotHistoryIsValid(DoppleBotHistoryRecord recordToValidate){
        return true;  //@// TODO: 3/15/16 implement a validator
    }


}
