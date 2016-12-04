package edu.berean.robotics.dopple;

import android.os.Environment;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.util.RobotLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * The DoppleBotRecorder class is used as the control logic for recording a robot's state to a file.
 * Created by wdhoward on 3/5/16.
 */
public class DoppleBotRecorder {


    private static String ROBOT_HISTORY_FILE_BASE_NAME = "robotRec-";
    private static String ROBOT_HISTORY_FILE_NAME_PATTERN = "yyMMdd_HHmmss";
    private static String ROBOT_HISTORY_FILE_EXT = ".txt";
    private static String ROBOT_HISTORY_DIRECTORY = "/ROBO_DATA/";
    private static String LOG_TAG = "DoppleBotRecorder - ";

    private DoppleBotHistoryRecord doppleBotHistoryRecord;

    private HashMap<String, Double> previousRobotComponentValues;
    private HashMap<String, Double> currentRobotComponentValues;

    private HashMap<String, HardwareDevice> currentComponentSnapshot;

    private Set<String> robotComponentNames;

    long startTime;

    /**
     * Constructor
     * returns a DoppleBotRecorder that has been established with a set of expected components
     * These "robot components' are passed in the form of a HashMap of String, HardwareDevices.
     * The recorder must know about the components of the robot in order to be able to recognize
     * the state of the robot and its change over time.
     *
     * @param robotComponents
     */
    public DoppleBotRecorder(HashMap<String, HardwareDevice> robotComponents){

        if (robotComponents.isEmpty()){
            RobotLog.e(LOG_TAG + "could not initialize history.  Robot has no components.");
            throw new RuntimeException("Could not initialize history.  Robot has no components.");

        } else {
            RobotLog.i(String.format(LOG_TAG + "%d components found in the robot.", robotComponents.size()));
            currentComponentSnapshot = robotComponents;
            buildRobotHistoryTableHeader();
            startTimer();
            previousRobotComponentValues = getRobotValuesHashMap(robotComponents);
        }

    }

    /**
     * After instantiating the DoppleBotRecorder, the DoppleBotRecorder needs to be told to
     * make sure to update itself whenever the state is perceived to have changed.
     * The update() method will examine the currentComponentSnapshot that were provided during
     * construction.  If the state of these components has changed, the update method will
     * amend the new data to the history.
     *
     * Call the update() method when you suspect the robot state may have changed and the history
     * needs to be updated.  The update method will determine if the state has, indeed, changed
     * and write the history accordingly.
     *
     */
    public void update(){

        currentRobotComponentValues = getRobotValuesHashMap(currentComponentSnapshot);

        if (robotStateHasChanged()){
            RobotLog.i(LOG_TAG + "state has changed, adding history.");
            addPreviousStateToRobotHistory();
            previousRobotComponentValues = currentRobotComponentValues;
        } else {
            RobotLog.i(String.format(LOG_TAG + "Same robot state found, skipping. %d rows.", doppleBotHistoryRecord.getValueRows().size()));
        }
    }

    /**
     * The writeHistory() method will instruct the recorder to persist the current robot history
     * data to a file on the external storage of the RobotController device.
     */
    public void writeHistory(){



        int currentRow = 1;
        ArrayList valueRows = doppleBotHistoryRecord.getValueRows();

        RobotLog.i("**********************************************************");
        RobotLog.i(String.format("%s", doppleBotHistoryRecord.getHeaderRow().toString()));
        RobotLog.i("**********************************************************");

        Iterator rowIterator = valueRows.iterator();
        while (rowIterator.hasNext()){
            RobotLog.i(String.format("%d - %s", currentRow, (rowIterator.next()).toString()));
            currentRow++;
        }

        RobotLog.i("**********************************************************");

        try{
            if (isExternalStorageWritable()){

                RobotLog.i(LOG_TAG + "external storage is available");

                SimpleDateFormat fileNameDateFormatter = new SimpleDateFormat(ROBOT_HISTORY_FILE_NAME_PATTERN, new Locale("en"));
                String createDate = fileNameDateFormatter.format(new Date());

                File historyFile = getHistoryFile(ROBOT_HISTORY_FILE_BASE_NAME + createDate + ROBOT_HISTORY_FILE_EXT);
                FileWriter historyWriter = new FileWriter(historyFile);

                String currentRowData = doppleBotHistoryRecord.getHeaderRow().toString() + "\n";

                rowIterator = valueRows.iterator();
                while (rowIterator.hasNext()){
                    currentRowData = currentRowData + String.format("%s\n",rowIterator.next().toString());
                }

                historyWriter.write(currentRowData);
                historyWriter.close();

            } else RobotLog.i(LOG_TAG + "external storage is unavailable. no history");

        } catch (IOException e){
            RobotLog.e(String.format(LOG_TAG + "error writing history: %s",e.getMessage()));
        }


    }

    private void buildRobotHistoryTableHeader(){

        ArrayList<String> historyHeader = new ArrayList<String>();

        historyHeader.add("runtime(ms)");
        robotComponentNames = currentComponentSnapshot.keySet();
        historyHeader.addAll(robotComponentNames);
        doppleBotHistoryRecord = new DoppleBotHistoryRecord(historyHeader);

    }

    private boolean robotStateHasChanged(){

        for (String currentComponentName : robotComponentNames) {

            Double currentVal = currentRobotComponentValues.get(currentComponentName);
            Double prevVal = previousRobotComponentValues.get(currentComponentName);
            RobotLog.d(String.format("%s current val %f, previous val %f", currentComponentName, currentVal.doubleValue(), prevVal.doubleValue()));

            if (! currentVal.equals(prevVal)) {

                return true;

            }

        }

        return false;
    }

    private void addPreviousStateToRobotHistory(){
        ArrayList robotPreviousStateValuesList = new ArrayList();

        //insert millisecond change from timer
        long elapsedTime = System.currentTimeMillis() - startTime;
        robotPreviousStateValuesList.add(elapsedTime);
        startTime = System.currentTimeMillis();

        for (String currentComponentName : robotComponentNames)
        {

            robotPreviousStateValuesList.add(previousRobotComponentValues.get(currentComponentName));

        }

        doppleBotHistoryRecord.addHistoryValueRow(robotPreviousStateValuesList);
        RobotLog.i(String.format(LOG_TAG + "%d value rows added", doppleBotHistoryRecord.getValueRows().size()));

    }

    private void startTimer(){

        startTime=System.currentTimeMillis();
        RobotLog.i(String.format(LOG_TAG + "Start time in millis: %d",startTime));

    }

    private HashMap<String, Double> getRobotValuesHashMap(HashMap<String, HardwareDevice> robotComponents){

        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        RobotLog.i(LOG_TAG + "Getting Robot Values HashMap");

        for (String currentComponentName : robotComponentNames)
        {
            HardwareDevice currentComponent = robotComponents.get(currentComponentName);
            // class matters as some values are from a getPosition call (servos) and some values are from getPower call (DC motor)
            RobotLog.i(LOG_TAG + "currentComponent is " + currentComponentName + " a " + currentComponent.getClass().getName());
            if (currentComponent.getClass().equals(DcMotorImpl.class) ||
                    currentComponent.getClass().equals(ServoImpl.class)) {

                if (currentComponent.getClass().equals(DcMotorImpl.class)) {
                    returnMap.put(currentComponentName, ((DcMotor) currentComponent).getPower());
                }

                if (currentComponent.getClass().equals(ServoImpl.class)) {
                    returnMap.put(currentComponentName, ((Servo) currentComponent).getPosition());
                }

            } else returnMap.put(currentComponentName, -999.9);

        }

        return returnMap;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getHistoryFile(String filename){

        File path = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + ROBOT_HISTORY_DIRECTORY);
        File historyFile = new File(path, filename);

        if (!path.isDirectory()){
            if(!path.mkdirs());{
                RobotLog.w(String.format(LOG_TAG + "%s directory COULD NOT be created.", path.getAbsolutePath()));
            }
            RobotLog.i(String.format(LOG_TAG + "%s directory created", path.getAbsolutePath()));
        }

        return historyFile;
    }
}
