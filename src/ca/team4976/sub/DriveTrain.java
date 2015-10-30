package ca.team4976.sub;

import ca.team4976.io.Controller;
import ca.team4976.io.Input;

import java.util.ArrayList;


public class DriveTrain extends CustomRobotDrive implements Runnable {

    public Thread thread = new Thread(this);
    public Safety safety = new Safety(this);

    private boolean teleopEnabled = false;
    private boolean isEnabled = false;
    private int defaultTickTiming = 1000 / 50;
    private int currentTickTiming = defaultTickTiming;
    private long waitTime = 20;

    private ArrayList<double[]> turnCount = new ArrayList<>();
    private ArrayList<double[]> moveCount = new ArrayList<>();

    private ArrayList<Boolean> isTurnComplete;
    private ArrayList<Boolean> isMoveComplete;

    private final int gearDefault = 1;
    private int gear = gearDefault;

    private double[] gears = new double[] {0.3, 0.5, 1};

    private boolean continuous = false;

    private void userControl() {

        Controller.Primary.Stick stick = Controller.Primary.Stick.LEFT;
        Controller.Primary.Trigger left = Controller.Primary.Trigger.LEFT;
        Controller.Primary.Trigger right = Controller.Primary.Trigger.RIGHT;

        if (Controller.Primary.DPad.WEST.isDownOnce())

            addTurnCount(-90, gears[gear]);

        else if (Controller.Primary.DPad.EAST.isDownOnce())

            addTurnCount(90, gears[gear]);

        else if (Controller.Primary.DPad.NORTH.isDownOnce()) incrementGear(1);

        else if (Controller.Primary.DPad.WEST.isDownOnce()) incrementGear(-1);

        arcadeDrive(stick.horizontal() * gears[gear],
                (right.value() - left.value() * gears[gear]));
    }

    private void checkTurn() {

        if (turnCount.get(0)[0] != 0 && moveCount.get(0)[0] != 0)

            System.out.println("ERROR: DRIVE_443");

        else {

            if (turnCount.get(0)[0] < 0) {

                if (turnLeft(-turnCount.get(0)[0], turnCount.get(0)[1])) {

                    turnCount.remove(0); isTurnComplete.add(true);
                }

            } else if (turnCount.get(0)[0] > 0) {

                if (turnRight(turnCount.get(0)[0], turnCount.get(0)[1])) {

                    turnCount.remove(0); isTurnComplete.add(true);
                }

            } else Input.AnalogGyro.DRIVE.reset();
        }
    }

    public void checkMove() {

        if (turnCount.get(0)[0] != 0 && moveCount.get(0)[0] != 0)

            System.out.println("ERROR: DRIVE_443");

        else {

            if (moveCount.get(0)[0] < 0) {

                if (moveBackwards(-moveCount.get(0)[0], moveCount.get(0)[1])) {

                    moveCount.remove(0); isMoveComplete.add(true);
                }

            } else if (moveCount.get(0)[0] > 0) {

                if (moveForward(moveCount.get(0)[0], moveCount.get(0)[1])) {

                    moveCount.remove(0); isMoveComplete.add(true);
                }

            } else Input.DigitalEncoder.DRIVE_LEFT.reset();
        }
    }

    private boolean moveForward(double distance, double speed) {

        currentTickTiming = 1000 / 200;

        if (Input.DigitalEncoder.DRIVE_LEFT.getDistance() >= distance) {

            directDrive(-0.2, -0.2);
            waitTime--;

        } else {

            directDrive(speed, speed);
            waitTime = 20;
        }

        if (waitTime == 0) {

           if (!continuous) directDrive(0, 0);
            currentTickTiming = defaultTickTiming;
            return true;
        }

        return true;
    }

    private boolean moveBackwards(double distance, double speed) {

        currentTickTiming = 1000 / 200;

        if (Input.DigitalEncoder.DRIVE_LEFT.getDistance() <= -distance) {

            directDrive(0.2, 0.2);
            waitTime--;

        } else {

            directDrive(-speed, -speed);
            waitTime = 20;
        }

        if (waitTime == 0) {

            if (!continuous) directDrive(0, 0);
            currentTickTiming = defaultTickTiming;
            return true;
        }

        return true;
    }

    private boolean turnLeft(double angle, double speed) {

        currentTickTiming = 1000 / 200;

        if (Input.AnalogGyro.DRIVE.getAngle() <= -angle) {

            directDrive(0.2, -0.2);
            waitTime--;

        } else {

            directDrive(-speed, speed);
            waitTime = 20;
        }

        if (waitTime == 0) {

            if (!continuous) directDrive(0, 0);
            currentTickTiming = defaultTickTiming;
            return true;
        }

        return true;
    }

    private boolean turnRight(double angle, double speed) {

        currentTickTiming = 1000 / 200;

        if (Input.AnalogGyro.DRIVE.getAngle() >= angle) {

            directDrive(0.2, -0.2);
            waitTime--;

        } else {

            directDrive(-speed, speed);
            waitTime = 100;
        }

        if (waitTime == 0) {

            if (!continuous) directDrive(0, 0);
            currentTickTiming = defaultTickTiming;
            return true;
        }

        return true;
    }

    @Override
    public void run() {

        long lastTick = System.currentTimeMillis();

        while (isEnabled) {

            if (lastTick - System.currentTimeMillis() >= currentTickTiming) {

                if (teleopEnabled) {

                    if (turnCount.get(0)[0] == 0) userControl();

                    else checkTurn();

                } else if (isEnabled) {

                    checkTurn();
                    checkMove();
                }
            }
        }
    }

    public void incrementGear(int direction) {

        if (gear < 2 && direction > 0) gear++;

        else if (gear > 0 && direction < 0) gear--;

        else gear = gearDefault;
    }

    public void teleopInit() {

        teleopEnabled = true;
        isEnabled = true;
        thread.start();
        isTurnComplete = new ArrayList<>();
        isMoveComplete = new ArrayList<>();
    }

    public void autonomousInit() {

        teleopEnabled = false;
        isEnabled = true;
        thread.start();
        isTurnComplete = new ArrayList<>();
        isMoveComplete = new ArrayList<>();
    }

    public void disableInit() { teleopEnabled = false; isEnabled = false; }

    public int addTurnCount(double count, double speed) {

        turnCount.add(new double[] {count, speed});
        return turnCount.size() - 1;
    }

    public int addMoveCount(double count, double speed) {

        moveCount.add(new double[] {count, speed});
        return moveCount.size() - 1;
    }

    public boolean isLastTurnComplete() {

        boolean returnVal = isTurnComplete.get(0);

        isTurnComplete.remove(0);

        return returnVal;
    }

    public boolean isLastMoveComplete() {

        boolean returnVal = isMoveComplete.get(0);

        isMoveComplete.remove(0);

        return returnVal;
    }

    public void setContinuous(boolean continuous) { this.continuous = continuous; }

    public class Safety {

        DriveTrain drive;

        public Safety(DriveTrain drive) { this.drive = drive; }

        public void stopTurn(int id) { turnCount.remove(id); stopDrive(); }

        public void stopMove(int id) { moveCount.remove(id); stopDrive(); }

        private void stopDrive() { drive.directDrive(0, 0); }
    }
}