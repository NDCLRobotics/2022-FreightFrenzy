package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="LogisNICS TeleOp", group="Interactive Opmode")

public class FreightFrenzyTeleOP extends OpMode
{

    // Control Hub
    private DcMotor frontRightMotor = null;
    private DcMotor frontLeftMotor = null;
    private DcMotor backLeftMotor = null;
    private DcMotor backRightMotor = null;
    private CRServo clawServo = null;
    private CRServo spinServo = null;
    private CRServo extendServo = null;

    // Expansion Hub
    private DcMotor intakeMotor = null;
    private DcMotor duckMotor = null;
    private DcMotor liftMotor = null;

    // Lift motor values
    private int liftMotorPos;
    private int liftMotorZero;
    private double liftPower;

    // Doubles for the power of our driving motors
    private double frontLeftPower, frontRightPower, backLeftPower, backRightPower;
    private double frontLeftPan, frontRightPan, backLeftPan, backRightPan;

    // Scale variable for all drive, turn, and pan functions
    private double powerScale = 0.8;

    double duckMax = 0.8;
    double duckPower = 0.0;
    double spinPos = -0.0777;
    double extendPos = -0.48;

    boolean startLift = true;
    boolean spinning = false;
    boolean extending = false;
    boolean powerSwitching = false;

    private boolean autoHome = false;

    // Frames
    private long currentFrame;
    private long startHomeFrame;


    @Override
    public void init ()
    {
        // Initalizations to connect our motor variables to the motor on the robot
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        duckMotor = hardwareMap.dcMotor.get("duckMotor");
        
        liftMotor = hardwareMap.dcMotor.get("liftMotor");
        liftMotorZero = liftMotor.getCurrentPosition()+ 200;

        clawServo = hardwareMap.crservo.get("clawServo");
        spinServo = hardwareMap.crservo.get("spinServo");
        extendServo = hardwareMap.crservo.get("extendServo");

        // Direction setting for the motors, depending on their physical orientation on the robot
        frontLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        duckMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        liftMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        telemetry.addData("", "Working");
        telemetry.addData("", "Last Update: 2021-11-08 16:57");

        currentFrame = 0;
        startHomeFrame = 0;
    }
    @Override
    public void init_loop()
    {
        // pls do
    }

    private void autoHoming()
    {
        long retractTime = startHomeFrame + 100; // How long it takes to retract arm
        long centerTime = retractTime + 200; // How long it takes to center and lower arm


        extendPos = -0.48;
        spinPos = -0.0777;

        // Lowers the lift motor until, it reaches the zero position
        if (liftMotorPos > liftMotorZero)
        {
            liftMotor.setPower(-0.6);
        }
        else
        {
            liftMotor.setPower(0.0);
        }

        if (currentFrame > startHomeFrame && currentFrame <= retractTime)
        {
            extendServo.setPower(extendPos);
            telemetry.addData("Retracting","");
        }
        if (currentFrame > retractTime && currentFrame <= centerTime) // raising
        {
            spinServo.setPower(spinPos);
            //liftMotor.setTargetPosition(liftMotorZero);
            telemetry.addData("Centering and Lowering","");
        }
        if (currentFrame > centerTime)
        {
            // stop
            autoHome = false;
        }
    }

    @Override
    public void loop()
    {
        // Frame incrementer
        currentFrame += 1;

        // Sets position of lift motor to the current position every loop
        liftMotorPos = liftMotor.getCurrentPosition();

        double drive = gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;
        double pan = gamepad1.left_stick_x;

        double lift = -gamepad2.left_stick_y;

        // Driving controls
        frontLeftPower = Range.clip(drive + turn, -1.0, 1.0);
        frontRightPower = Range.clip(drive - turn, -1.0, 1.0);
        backLeftPower = Range.clip(drive + turn, -1.0, 1.0);
        backRightPower = Range.clip(drive - turn, -1.0, 1.0);
        frontLeftMotor.setPower(powerScale * frontLeftPower);
        frontRightMotor.setPower(powerScale * frontRightPower);
        backLeftMotor.setPower(powerScale * backLeftPower);
        backRightMotor.setPower(powerScale * backRightPower);

        // Panning controls
        frontLeftPan = Range.clip(drive - pan, -1.0, 1.0);
        frontRightPan = Range.clip(drive - pan, -1.0, 1.0);
        backLeftPan = Range.clip(drive + pan, -1.0, 1.0);
        backRightPan = Range.clip(drive + pan, -1.0, 1.0);
        frontLeftMotor.setPower(powerScale * frontLeftPan);
        frontRightMotor.setPower(powerScale * frontRightPan);
        backLeftMotor.setPower(powerScale * backLeftPan);
        backRightMotor.setPower(powerScale * backRightPan);

        // Incrementing speed for driving motor, up speeds up motors, down slows down motors
        if (gamepad1.dpad_up && !powerSwitching)
        {
            powerSwitching = true;
            powerScale += 0.2;
        }
        if (gamepad1.dpad_down && !powerSwitching)
        {
            powerSwitching = true;
            powerScale -= 0.2;
        }
        if (!gamepad1.dpad_down && !gamepad1.dpad_up && powerSwitching)
        {
            powerSwitching = false;
        }

        // Clamp for driving power scale
        if (powerScale > 1.0)
        {
            powerScale = 1.0;
        }
        else if (powerScale < 0.2)
        {
            powerScale = 0.2;
        }

        // Set lift to zero position at start of TeleOP
        if (startLift && liftMotor.getCurrentPosition() < liftMotorZero)
        {
            liftPower = 0.6;
        }
        else // Normal function, rest of TeleOP
        {
            startLift = false;
            liftPower = Range.clip(lift, -0.8, 0.8);
        }
        liftMotor.setPower(liftPower);

        // Sets the lift motor zero to the current position
        if (gamepad2.triangle)
        {
            liftMotorZero = liftMotor.getCurrentPosition();
        }

        // Intake motor power: includes "boost" mode when right trigger is pressed, also includes reverse function
        if (gamepad1.right_trigger > 0)
        {
            intakeMotor.setPower(0.9);
        }
        else if (gamepad1.left_trigger > 0 || gamepad1.touchpad || gamepad1.y || gamepad1.x) // Reverse for intake motor
        {
            intakeMotor.setPower(-0.7);
        }
        else if (gamepad1.b) // Stops the intake motor completely
        {
            intakeMotor.setPower(0.0);
        }
        else // Runs the motor for the entirety of code
        {
            intakeMotor.setPower(0.7);
        }

        // Duck carousel spinner for blue side
        if (gamepad1.touchpad || gamepad1.y)
        {
            if (duckPower < duckMax)
            {
                duckPower += 0.005;
            }
            duckMotor.setPower(duckPower);
        }
        else if (gamepad1.x) // Duck carousel reverse spinner for red side
        {
            if (duckPower > -duckMax)
            {
                duckPower -= 0.005;
            }
            duckMotor.setPower(duckPower);
        }
        else
        {
            duckPower = 0.0;
            duckMotor.setPower(0.0);
        }

        // Controls the claw of the claw arm
        if (gamepad2.right_bumper) // Open position for claw
        {
            clawServo.setPower(-0.38);
        }
        else if (gamepad2.left_bumper) // Grab for cargo grip
        {
            clawServo.setPower(-0.17);
        }
        else if (gamepad2.start) // Grab for team element
        {
            clawServo.setPower(-0.075);
        }

        // Controls the spinning of the pick-and-place
        if (gamepad2.right_stick_x < 0 && !spinning)
        {
            spinning = true;
            spinPos += 0.02;
        }
        else if (gamepad2.right_stick_x > 0 && !spinning)
        {
            spinning = true;
            spinPos -= 0.02;
        }
        else if (gamepad2.right_stick_button && !spinning) // Center
        {
            spinning = true;
            spinPos = -0.0777;
        }

        if (gamepad2.right_stick_x == 0 && spinning) // Reset spinning flag
        {
            spinning = false;
        }

        if (gamepad2.square)
        {
            spinPos = 0.2;
        }

        // Clamp for spin motor
        if (spinPos < -0.11)
        {
            spinPos = -0.11;
        }
        if (spinPos > 0.3)
        {
            spinPos = 0.3;
        }

        // Sets the position of spin servo
        spinServo.setPower(spinPos);

        // Controls movement of the extending servo
        if (-gamepad2.right_stick_y > 0 && !extending)
        {
            extending = true;
            extendPos += 0.04;
        }
        else if (-gamepad2.right_stick_y < 0 && !extending)
        {
            extending = true;
            extendPos -= 0.04;
        }
        else if (gamepad2.circle)  //Home position for extend arm
        {
            extendPos = -0.6;
        }

        if (gamepad2.right_stick_y == 0 && extending) // Reset extending flag
        {
            extending = false;
        }

        // Clamp for extend motor
        if (extendPos > -0.12)
        {
            extendPos = -0.12;
        }
        if (extendPos < -0.56)
        {
            extendPos = -0.56;
        }

        // Sets the position of the extend servo
        extendServo.setPower(extendPos);

        // Calls the autoHome function to set the pick-and-place arm to the "home" position
        if (gamepad2.cross || autoHome)
        {
            if (!autoHome)
            {
                startHomeFrame = currentFrame;
            }

            autoHome = true;
            autoHoming();
        }

        // TELEMETRY, gives drivers valuable information
        telemetry.addData("Power Scale: ", powerScale);
        telemetry.addData("Extend Position: ", extendPos);
        telemetry.addData("Spin Position: ", spinPos);
        telemetry.addData("Lift Motor Position: ", liftMotorPos);
        telemetry.addData("Spinning Flag: ", spinning);
        telemetry.addData("Extending Flag: ", extending);

    }

    @Override
    public void stop()
    {
        // pls do, part two
        frontLeftMotor.setPower(0.0);
        frontRightMotor.setPower(0.0);
        backLeftMotor.setPower(0.0);
        backRightMotor.setPower(0.0);
        intakeMotor.setPower(0.0);
        duckMotor.setPower(0.0);
        liftMotor.setPower(0.0);
    }
}
