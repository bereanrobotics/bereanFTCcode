/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.team4998;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: PushbotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forwards, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backwards for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This methods assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="Qbot: Test Catapult", group="Qbot")
@Disabled

public class QbotAutonomousTest extends LinearOpMode {

    /* Declare OpMode members. */
    private HardwareQBot robot   = new HardwareQBot();   // Use a qbot's hardware
    private ElapsedTime     runtime = new ElapsedTime();

    static final int        COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final int        CATAPULT_LAUNCH_COUNT   = 935;
    static final double     CATAPULT_READY_POWER    = 0.5;
    static final double     CATAPULT_FIRE_POWER     =   1.0;

    @Override
    public void runOpMode() throws InterruptedException {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Waiting to start");    //
        telemetry.update();

        //idle();
        waitForStart();

        RobotLog.d("QbotAutonomousTest is now getting ready to encoderLaunch");

        encoderLaunch();

        telemetry.addData("Status", "Complete");
        telemetry.update();
    }

    private void encoderLaunch()
    {
        if (opModeIsActive())
        {
            encoderReadyCatapult();
            sleep(1000);
            encoderFire();
            telemetry.addData("Catapult", "End position %d", robot.catapultMotor.getCurrentPosition());
            telemetry.update();
            robot.catapultMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
    }

    private void encoderReadyCatapult()
    {
        RobotLog.d("QbotAutonomousTest: READY Catapult!");
        telemetry.addData("Catapult","Ready Position");
        telemetry.update();

        robot.catapultMotor.setTargetPosition(CATAPULT_LAUNCH_COUNT);
        robot.catapultMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.catapultMotor.setPower(CATAPULT_READY_POWER);

        while (opModeIsActive() && robot.catapultMotor.isBusy())
        {
            telemetry.addData("Catapult", "Position %d", robot.catapultMotor.getCurrentPosition());
            telemetry.update();
        }

        //robot.catapultMotor.setPower(0);

        telemetry.addData("Catapult","READIED!");
        telemetry.update();
        RobotLog.d("QbotAutonomousTest: Catapult READIED!");
    }

    private void encoderFire()
    {
        RobotLog.d("QbotAutonomousTest: Catapult loaded!");
        telemetry.addData("Catapult","Fire!");
        telemetry.update();

        robot.catapultMotor.setTargetPosition(COUNTS_PER_MOTOR_REV);
        robot.catapultMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.catapultMotor.setPower(CATAPULT_FIRE_POWER);

        while (opModeIsActive() && robot.catapultMotor.isBusy())
        {
            telemetry.addData("Catapult", "Position %d", robot.catapultMotor.getCurrentPosition());
            telemetry.update();
        }

        robot.catapultMotor.setPower(0);

        telemetry.addData("Catapult","FIRED!");
        telemetry.update();
        RobotLog.d("QbotAutonomousTest: Catapult FIRED!");
    }

}
