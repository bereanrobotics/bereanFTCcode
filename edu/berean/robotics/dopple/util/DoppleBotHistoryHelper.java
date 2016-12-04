package edu.berean.robotics.dopple.util;

import com.qualcomm.robotcore.util.RobotLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.berean.robotics.dopple.DoppleBotHistoryRecord;

/**
 * This class provides static helper methods to allow the caller
 * to work with the robot history
 * Created by wdhoward on 3/15/16.
 */
public class DoppleBotHistoryHelper {

    /**
     * Use this method to get a completed DoppleBotHistoryRecord instance from the contents
     * of a file.  File should have the format of
     * one header row: [time(ms), Stringname1, Stringname2, ... Stringname n]
     * multiple value rows: [timeint, value1, value2, ... value n]
     * [timeint, value1, value2, ... value n]
     * [timeint, value1, value2, ... value n]
     *
     * @param historyFile
     * @return
     */
    public static DoppleBotHistoryRecord getHistoryFromFile(File historyFile) {

        DoppleBotHistoryRecord historyTable = new DoppleBotHistoryRecord();

        if (fileIsValid(historyFile)){
            try{
                BufferedReader reader = new BufferedReader(new FileReader(historyFile));

                //read and set the headerLine data
                String headerLine = reader.readLine();
                headerLine = headerLine.substring(1);
                headerLine = headerLine.replaceFirst("]", "");
                List headerList = Arrays.asList(headerLine.split("\\s*,\\s*"));
                RobotLog.i(String.format("HELPER: - %s", headerList.toString()));
                ArrayList<String> arrayListHeader = new ArrayList(headerList);
                historyTable = new DoppleBotHistoryRecord(arrayListHeader);

                //read and set the values data
                String valueLine;

                while ((valueLine = reader.readLine()) != null) {
                    valueLine = valueLine.substring(1);
                    valueLine = valueLine.replaceFirst("]","");
                    String valueLineString[] = valueLine.split("\\s*,\\s*");
                    //handle the first value - it's an int
                    ArrayList valueArrayList = new ArrayList(valueLineString.length);
                    valueArrayList.add(Integer.parseInt(valueLineString[0]));
                    for (int i = 1; i < valueLineString.length; i++) {
                        valueArrayList.add(Double.parseDouble(valueLineString[i]));
                    }
                    RobotLog.i(String.format("HELPER: - value row - %s", valueArrayList.toString()));
                    historyTable.addHistoryValueRow(valueArrayList);
                }

                reader.close();


            }catch (IOException e){
                RobotLog.e(e.getMessage());
            }
        }

        return historyTable;
    }

    /**
     * Found this online!;)
     *
     * lastFileModified will return the most recent file modified in a directory specified by the
     * pathString parameter.
     *
     * Do we want to make this always check the dopplebot history file directory or leave it generic?
     *
     * @// TODO: 3/17/16 null checks on listFiles as well as pathString
     * @param pathString
     * @return
     */
    public static File lastFileModified(String pathString) {
        File path = new File(pathString);
        File[] filesInPath = path.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastModifiedTime = Long.MIN_VALUE;
        File lastModifiedFile = null;
        for (File file : filesInPath) {
            if (file.lastModified() > lastModifiedTime) {
                lastModifiedFile = file;
                lastModifiedTime = file.lastModified();
            }
        }
        return lastModifiedFile;
    }

    /**
     * This private method validates that the historyFile passed into the method
     * is actually well formed and can be processed.
     * @// TODO: 3/16/16 build historyFile validation procedure
     * @param historyFile
     * @return
     */
    private static boolean fileIsValid(File historyFile){
        return true;
    }
}
