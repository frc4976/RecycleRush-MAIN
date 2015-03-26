package ca.team4976.sub;

import ca.team4976.io.Controller;
import ca.team4976.io.Input;
import edu.wpi.first.wpilibj.Gyro;

/**
* @author Marc Levesque
* @version 1.1.1
*/
public class DriveTrain extends CustomRobotDrive {

    Controller.Primary.Stick steeringAxis = Controller.Primary.Stick.LEFT; // makes things smaller later.
    Controller.Primary.Trigger leftTrigger = Controller.Primary.Trigger.LEFT; // makes things smaller later.
    Controller.Primary.Trigger RightTrigger = Controller.Primary.Trigger.RIGHT; // makes things smaller later.

    private double throttle = 0.3; // is used to limit the final output to the drive.
    private int gear = 1; // is used to determine the limit to the final drive.
    private int autoTurnFlag = 0; // is use to determine how many auto turns should be preformed.

    /**
     * Initializer use to set the dead band settings
     */
    public DriveTrain() {

        useDeadBand = true;
        DeadBand.setDeadBandType(DeadBand.LINEAR);
        DeadBand.setDeadBandZone(0.15);
    }

    /**
     * called periodically in teleop to determine the output to the drive train based on the controller input.
     */
    public void teleopArcadeDrive() {

        if (useDeadBand) {

            double[] drive = DeadBand.evaluateDeadBand(steeringAxis.horizontal(), Controller.Primary.Trigger.totalValue(RightTrigger, leftTrigger));

            arcadeDrive(drive[0] * throttle, drive[1] * throttle);

        } else
            arcadeDrive(steeringAxis.horizontal() * throttle, Controller.Primary.Trigger.totalValue(RightTrigger, leftTrigger) * throttle);
    }

    /**
     * called periodically in teleop to determine the gear based on the controller input.
     */
    public void updateGear() {

        if (Controller.Primary.DPad.NORTH.isDownOnce() && gear < 3) gear++;
        if (Controller.Primary.DPad.SOUTH.isDownOnce() && gear > 1) gear--;

        updateThrottle();
    }

    /**
     * called but updateGear to change the throttle limit based on the gear.
     */
    private void updateThrottle() {

        switch (gear) {

            case 1: throttle = 0.4;
            case 2: throttle = 0.7;
            case 3: throttle = 1.0;

            default: throttle = 0.3;
        }
    }

    /**
     * called periodically in teleop to determine the autoRotation of the robot in teleop.
     * requires controller input to determine how many times to turn.
     */
    public void updateAutoTurn() {

        if (Controller.Primary.DPad.EAST.isDownOnce() && gear < 3) autoTurnFlag++;
        if (Controller.Primary.DPad.WEST.isDownOnce() && gear > 1) autoTurnFlag--;

        if (autoTurnFlag < 0) {

            if (turnLeft(90, 0.15)) {
                Input.AnalogGyro.DRIVE.reset();
                autoTurnFlag++;
            }

        } else if (autoTurnFlag > 0) {

            if (turnRight(90, 0.15)) {
                Input.AnalogGyro.DRIVE.reset();
                autoTurnFlag--;
            }
        }
    }

    /**
     * @param angle is the angles want the robot to turn to.
     * @param speed is the speed at which we want the robot to turn at.
     *
     * @return a value from true or false to determine if we have completed the turn.
     */
    public boolean turnRight(double angle, double speed) {

        if (isGyroReset == false) { Input.AnalogGyro.DRIVE.reset(); isGyroReset = true; }

        switch (turnState) {

            case 0:

                arcadeDrive(speed, 0);

                if (Input.AnalogGyro.DRIVE.getAngle() >= angle - frictionRamp()) {

                    turnState++;

                } break;

            case 1:

                arcadeDrive(-0.2, 0);

                if (Input.AnalogGyro.DRIVE.getRate() < -0.1) {

                    turnState++;

                } break;

            case 2: arcadeDrive( 0, 0); turnState = 0; isGyroReset = false; return true;

            default: return true;
        }

        return false;
    }

    /**
     * @param angle is the angles want the robot to turn to.
     * @param speed is the speed at which we want the robot to turn at.
     *
     * @return a value from true or false to determine if we have completed the turn.
     */

    int turnState = 0;
    boolean isGyroReset = false;

    public boolean turnLeft(double angle, double speed) {

        if (isGyroReset == false) { Input.AnalogGyro.DRIVE.reset(); isGyroReset = true; }

        switch (turnState) {

            case 0:

                arcadeDrive(-speed, 0);

                if (Input.AnalogGyro.DRIVE.getAngle() <= -angle + frictionRamp() + 4) {

                    turnState++;

                } break;

            case 1:

                arcadeDrive(0.2, 0);

                if (Input.AnalogGyro.DRIVE.getRate() > 0.01) {

                    turnState++;

                } break;

            case 2: arcadeDrive(0, 0); turnState = 0; isGyroReset = false; return true;

            default: return true;
        }

        return false;
    }

    /**
     * @param distance is the distance we want the robot to drive to.
     * @param speed is the speed at which we want the robot to turn at.
     *
     * @return a value from true or false to determine if we have completed the turn.
     */
    public boolean forward(double distance, double speed) {

        if (Input.DigitalEncoder.DRIVE_LEFT.getDistance() > distance) {

            arcadeDrive(0, 0);
            return true;

        } else {

            speed = ramp(speed, distance - Input.DigitalEncoder.DRIVE_LEFT.getDistance());

            arcadeDrive(0, speed);
            return false;
        }
    }

    /**
     * @param distance is the distance we want the robot to drive to.
     * @param speed is the speed at which we want the robot to turn at.
     *
     * @return a value from true or false to determine if we have completed the turn.
     */
    public boolean back(double distance, double speed) {

        if (Input.DigitalEncoder.DRIVE_LEFT.getDistance() < -distance) {

            arcadeDrive(0, 0);
            return true;

        } else {

            speed = ramp(speed, distance + Input.DigitalEncoder.DRIVE_LEFT.getDistance());

            arcadeDrive(0, -speed);
            return false;
        }
    }
}
