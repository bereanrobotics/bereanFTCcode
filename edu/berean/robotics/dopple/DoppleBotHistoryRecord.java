package edu.berean.robotics.dopple;

import com.qualcomm.robotcore.util.RobotLog;

import java.util.ArrayList;


/**
 * The DoppleBotHistoryRecord is an expression of a robot's state over time
 * It is composed of a header row, which has a set of strings which match
 * the hardware device setting names that are used in configuring the robot.
 *
 * This is followed by n number of value rows, which contain a number
 * for each name in the header row, corresponding to the setting of the component.
 *
 * The first [0] element of each is the millisecond value.  This value corresponds to
 * how long the robot maintained the hardware settings of the remaining value row elements.
 *
 * so, imagine a robot with two motors to move the wheels and one servo to move a guard up and down
 *
 * header row: [runtime(m/s), left_motor, right_motor, guard]
 * value rows: [1000, 1.0, -1.0, 0.0]
 *             [500, -1.0, 1,0, 0.75]
 *
 * This indicates that the robot should, for one second run with the left motor at full power forward
 * the right motor at full power reverse and the guard down (assuming 0 is down).  Then, after 1 sec
 * it should reverse course and raise the guard 75% up for 1/2 second.
 *
 * Ultimately, the interpretation of this data is up to the client, the DoppleBotRecorder class, but
 * this example is the typical usage.
 *
 * Created by wdhoward on 3/5/16.
 */
public class DoppleBotHistoryRecord {

    public int columns;
    public int rows;

    private ArrayList<String> historyTableHeaderRow = new ArrayList<String>();
    private ArrayList<ArrayList> historyTableValueRows = new ArrayList<ArrayList>();

    public DoppleBotHistoryRecord(){

    }

    /**
     * Creates a DoppleBotHistoryRecord with a defined header row using the headerRow param
     * @param headerRow
     */
    public DoppleBotHistoryRecord(ArrayList<String> headerRow)
    {
        setHistoryTableHeaderRow(headerRow);
    }

    /**
     * The header row of the robot's history is expected to contain an ArrayList of column names
     * @param headerRow is the ArrayList of names to use.  This should be the label for the time element
     *                  as well as each robot component name used to identify the hardware device when
     *                  accessing it programmatically
     */
    private void setHistoryTableHeaderRow (ArrayList<String> headerRow){
        if (!headerRow.isEmpty()){
            historyTableHeaderRow = headerRow;
            columns = historyTableHeaderRow.size();
        }

    }

    /**
     * This method will add the data from the ArrayList param, valuesRow, to the end of the history table.
     * This is an "append" operation.
     *
     * The number of elements in the ArrayList valuesRow MUST match the number of elements in the header row.
     *
     * @param valuesRow
     */

    public void addHistoryValueRow (ArrayList valuesRow){
        if (valuesRow.size() == historyTableHeaderRow.size()){
            historyTableValueRows.add(valuesRow);
            rows = historyTableValueRows.size();
        } else {
            RobotLog.e("ROBOT HISTORY TABLE - ERROR - History value row to add has a different number of columns than the header row.");
            throw new RuntimeException("Mismatch! History value row has a different number of columns than the header row.");
        }
    }

    /**
     * Returns an ArrayList of Strings which is the header row created with the class instance.
     * @return
     */
    public ArrayList<String> getHeaderRow(){
        return historyTableHeaderRow;
    }

    /**
     * Returns an ArrayList of ArrayLists (expected to be rows of columnar data) containing
     * all of the movement data over time.
     *
     * @return
     */

    public ArrayList<ArrayList> getValueRows(){
        return historyTableValueRows;
    }

}
