package org.firstinspires.ftc.team4998;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import edu.berean.robotics.robots.team4998.HardwareMiniDoppleBot;

/**
 * Created by wdhoward on 12/2/16.
 */

@TeleOp (name = "Minibot: RecordTeleop - Encoder", group="MINI DOPPLE")
//@Disabled

public class MiniDoppleBotTeleopWithEncoderDrive extends MiniDoppleBotTeleop {

    private String LOG_TAG = "MiniDoppleBot ENCODER Teleop - ";

    /* Declare OpMode members. */


    @Override
    public void init(){
        robot.encoderDriveIsEnabled = true;
        super.init();
    }


}
