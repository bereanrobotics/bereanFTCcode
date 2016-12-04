package org.firstinspires.ftc.team6818;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by wdhoward on 12/3/16.
 */

@TeleOp(name="AimBot: Teleop Record", group="Aimbot")
public class AimbotTeleopRecord extends AimbotTeleop {

    @Override
    public void start(){
        telemetry.addData("Status", "starting");
        telemetry.update();

        robot.startRobot();
        robot.startRecording();
    }

    @Override
    public void stop(){

        telemetry.addData("Status", "stopping");
        telemetry.update();

        robot.stopRecording();
        robot.stopRobot();

    }

    @Override
    public void loop()
    {
        super.loop();
        robot.updateRecording();
    }
}
