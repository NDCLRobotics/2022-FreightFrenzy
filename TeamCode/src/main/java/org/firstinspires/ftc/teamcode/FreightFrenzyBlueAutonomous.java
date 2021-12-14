/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import java.util.ArrayList;
import java.util.List;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Blue Autonomous Default Route", group = "Concept")
public class FreightFrenzyBlueAutonomous extends LinearOpMode {
    private static final String FORWARD = "forward";
    private static final String BACKWARD = "backward";
    private static final String STOP = "stop";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final Boolean ON = true;
    private static final Boolean OFF = false;

    private double duckPower = 0.0;

    private long loopCount = 0;
    private boolean elementFound = false;
    private float elementCoordinate = -1;
    private int elementPosition = 0;
    private int resetTime = 0;
    private boolean timeReset = false;

    private DcMotor frontLeftMotor = null;
    private DcMotor frontRightMotor = null;
    private DcMotor backLeftMotor = null;
    private DcMotor backRightMotor = null;

    private DcMotor duckMotor = null;

    private int drive1 = 400;
    private int stop1 = drive1 + 100;
    private int pan1 = stop1 + 1950;
    private int duck1 = pan1 + 3200;
    private int pan2 = duck1 + 800;
    private int drive2 = pan2 + 750;

    public void drive (String fb)
    {
        if (fb.equals("forward"))
        {
            frontLeftMotor.setPower(0.3);
            frontRightMotor.setPower(0.3);
            backLeftMotor.setPower(0.3);
            backRightMotor.setPower(0.3);
        }
        if (fb.equals("backward"))
        {
            frontLeftMotor.setPower(-0.3);
            frontRightMotor.setPower(-0.3);
            backLeftMotor.setPower(-0.3);
            backRightMotor.setPower(-0.3);
        }
        if (fb.equals("stop"))
        {
            frontLeftMotor.setPower(0.0);
            frontRightMotor.setPower(0.0);
            backLeftMotor.setPower(0.0);
            backRightMotor.setPower(0.0);
        }
    }

    public void turn (String lr)
    {
        if (lr.equals("left"))
        {
            frontLeftMotor.setPower(-0.15);
            frontRightMotor.setPower(0.15);
            backLeftMotor.setPower(-0.15);
            backRightMotor.setPower(0.15);
        }
        if (lr.equals("right"))
        {
            frontLeftMotor.setPower(0.15);
            frontRightMotor.setPower(-0.15);
            backLeftMotor.setPower(0.15);
            backRightMotor.setPower(-0.15);
        }
        if (lr.equals("stop"))
        {
            frontLeftMotor.setPower(0.0);
            frontRightMotor.setPower(0.0);
            backLeftMotor.setPower(0.0);
            backRightMotor.setPower(0.0);
        }
    }

    public void pan (String lr)
    {
        if (lr.equals("left"))
        {
            frontLeftMotor.setPower(-0.45);
            frontRightMotor.setPower(-0.45);
            backLeftMotor.setPower(0.45);
            backRightMotor.setPower(0.45);
        }
        if (lr.equals("right"))
        {
            frontLeftMotor.setPower(0.45);
            frontRightMotor.setPower(0.45);
            backLeftMotor.setPower(-0.45);
            backRightMotor.setPower(-0.45);
        }
        if (lr.equals("stop"))
        {
            frontLeftMotor.setPower(0.0);
            frontRightMotor.setPower(0.0);
            backLeftMotor.setPower(0.0);
            backRightMotor.setPower(0.0);
        }
    }

    public void duck (Boolean quack)
    {
        if (quack)
        {
            if (duckPower < 0.8)
            {
                duckPower += 0.05;
            }
        }
        else
        {
            duckPower = 0.0;
        }
        duckMotor.setPower(duckPower);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String TFOD_MODEL_ASSET = "amogus.tflite";
    private static final String[] LABELS =
    {
      "element"
    };

    private static final String VUFORIA_KEY =
            "AX54Lyj/////AAABmSsIALipi0y4oiZBAoZS4o4Jppp+qbLTWgVQVVuyveVi7sLhVC8XAwvTGDzKpxm1tiMRMLgYEV3Y5YXvqKMiA7R7TUZQcZeyL9MMGoqcq7rIeFMX01KOuZUmfs754hgbnsINn38JjhLLAH3g2GuKF9QZBF/CJqw/UFKKzR8bDlv4TkkTP8AyxvF9Vyv9G9gQhK2HoOWuSCWQHzIWl+op5LEPLXU7RmdrWzxDm1zEY3DZoax5pYLMRR349NoNzpUFBzwNu+nmEzT3eXQqtppz/vE/gHA0LRys9MAktPmeXQfvaS2YUi4UdE4PcFxfCUPuWe6L9xOQmUBE7hB39jTRkYxGADmTxILyBZB6fD3qyFHv";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the TensorFlow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    @Override
    public void runOpMode() {
        // control hub 1
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
        duckMotor = hardwareMap.dcMotor.get("duckMotor");

        //setting the direction for each motor
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        duckMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia();
        initTfod();

        /**
         * Activate TensorFlow Object Detection before we wait for the start command.
         * Do it here so that the Camera Stream window will have the TensorFlow annotations visible.
         **/
        if (tfod != null) {
            tfod.activate();

            // The TensorFlow software will scale the input images from the camera to a lower resolution.
            // This can result in lower detection accuracy at longer distances (> 55cm or 22").
            // If your target is at distance greater than 50 cm (20") you can adjust the magnification value
            // to artificially zoom in to the center of image.  For best results, the "aspectRatio" argument
            // should be set to the value of the images used to create the TensorFlow Object Detection model
            // (typically 16/9).
            tfod.setZoom(1.0, 16.0/9.0);
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start op mode");
        telemetry.update();
        waitForStart();

        if (opModeIsActive()) {
            long initTime = System.currentTimeMillis();
            while (opModeIsActive()) {
                if (tfod != null) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    List<String> labels = new ArrayList<String>();
                    if (updatedRecognitions != null) {
                        telemetry.addData("# Object Detected", updatedRecognitions.size());

                        // step through the list of recognitions and display boundary info.
                        int i = 0;
                        for (Recognition recognition : updatedRecognitions) {
                            telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
                            telemetry.addData(String.format("  left,top (%d)", i), "%.03f , %.03f",
                                    recognition.getLeft(), recognition.getTop());
                            telemetry.addData(String.format("  right,bottom (%d)", i), "%.03f , %.03f",
                                    recognition.getRight(), recognition.getBottom());
                            i++;
                            labels.add(recognition.getLabel());
                            elementCoordinate = recognition.getLeft();
                        }
                        telemetry.update();

                        long finalTime = System.currentTimeMillis() - initTime;
                        long timeDifference = 0;
                        loopCount += 1;

                        telemetry.addData("Loop count:", loopCount);
                        if (resetTime == 0)
                        {
                            telemetry.addData("Time is:", finalTime);
                            telemetry.addData("Ms/loop:", finalTime / loopCount);
                        }
                        if (resetTime == 1)
                        {
                            telemetry.addData("Time is:", finalTime + timeDifference);
                            telemetry.addData("Ms/loop:", (finalTime + timeDifference) / loopCount);
                        }
                        telemetry.addData("Element position: ", elementPosition);

                        // initial step - detect what position duck/team element is in
                        if (finalTime > 1500 && !elementFound)
                        {
                            if (elementCoordinate >= 0 && elementCoordinate < 640)
                            {
                                elementPosition = 1;
                            }
                            else if (elementCoordinate >= 640)
                            {
                                elementPosition = 2;
                            }
                            else
                            {
                                elementPosition = 3;
                            }
                            elementFound = true;
                            resetTime = 1;
                        }
                        // spin duck wheel
                        if (resetTime == 1)
                        {
                            if (!timeReset)
                            {
                                timeDifference = finalTime;
                                initTime = System.currentTimeMillis();
                                timeReset = true;
                            }
                            if (finalTime < drive1)
                            {
                                drive(FORWARD);
                            }
                            if (finalTime < stop1 && finalTime > drive1)
                            {
                                drive(STOP);
                            }
                            if (finalTime < pan1 && finalTime > stop1)
                            {
                                pan(RIGHT);
                            }
                            if (finalTime < duck1 && finalTime > pan1)
                            {
                                pan(STOP);
                                duck(ON);
                            }
                            if (finalTime < pan2 && finalTime > duck1)
                            {
                                duck(OFF);
                                pan(LEFT);
                            }
                            if (finalTime < drive2 & finalTime > pan2)
                            {
                                drive(BACKWARD);
                            }
                            if (finalTime > drive2)
                            {
                                drive(STOP);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
            "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 320;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
    }
}