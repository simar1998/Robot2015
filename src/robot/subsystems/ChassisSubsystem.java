package robot.subsystems;

import robot.EncoderCorrection;
import robot.MockSpeedController;
import robot.OffsetableGyro;
import robot.PolarCoordinate;
import robot.RobotMap;
import robot.RunnymedeMecanumDrive;
import robot.commands.TeleopDriveCommand;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The chassis subsystem contains the drive motors and uses the gyro sensor.
 */
public class ChassisSubsystem extends RunnymedeSubsystem {

	public enum DriveMode { FIELD_RELATIVE, ROBOT_RELATIVE }
	public enum PIDEnable { ENABLED, DISABLED }
	
	private boolean subsystemEnabled = false;

	// MOTORS 
	
	// Motor definitions for arrays
	private static final int FRONT_LEFT  = 0;
	private static final int REAR_LEFT   = 1;
	private static final int FRONT_RIGHT = 2;
	private static final int REAR_RIGHT  = 3;
	private static final int MOTOR_COUNT = 4;
	
	private boolean MOTOR_INVERTED     = false;
	private boolean MOTOR_NOT_INVERTED = true;

	// Motors
	
	Talon [] talonArr = {
			new Talon(RobotMap.FRONT_LEFT_MOTOR), 
	        new Talon(RobotMap.REAR_LEFT_MOTOR),
	        new Talon(RobotMap.FRONT_RIGHT_MOTOR),
	        new Talon(RobotMap.REAR_RIGHT_MOTOR)   };
	
	// Mecanum Drive for the robot configuration.
	
	RunnymedeMecanumDrive mecanumDrive = 
			new RunnymedeMecanumDrive(MOTOR_NOT_INVERTED,
									  MOTOR_NOT_INVERTED,
					                  MOTOR_INVERTED,
					                  MOTOR_INVERTED);
	
	/*
	RunnymedeMecanumDrive mecanumDrive = 
			new RunnymedeMecanumDrive(MOTOR_INVERTED,
					                  MOTOR_INVERTED,
					                  MOTOR_NOT_INVERTED,
					                  MOTOR_NOT_INVERTED);
	*/
	
	// SENSORS
	
	// Encoders
	// PID Input and Output are between -1 and 1, so we divide the rate by the max rate
	// to normalize the encoder output so it can be compared with the motor drive input and output range
	Encoder [] encoderArr = {
			new Encoder(RobotMap.FRONT_LEFT_ENCODER_ONE, RobotMap.FRONT_LEFT_ENCODER_TWO, true){
				public double pidGet() {
					return this.getRate() / RobotMap.MAX_ENCODER_RATE;
				}
			},
			new EncoderCorrection(RobotMap.REAR_LEFT_ENCODER_ONE,  RobotMap.REAR_LEFT_ENCODER_TWO,  true){
				public double pidGet() {
					return this.getRate() / RobotMap.MAX_ENCODER_RATE;
				}
			},
			new Encoder(RobotMap.FRONT_RIGHT_ENCODER_ONE,RobotMap.FRONT_RIGHT_ENCODER_TWO,true){
				public double pidGet() {
					return this.getRate() / RobotMap.MAX_ENCODER_RATE;
				}
			},
			new Encoder(RobotMap.REAR_RIGHT_ENCODER_ONE, RobotMap.REAR_RIGHT_ENCODER_TWO, true){
				public double pidGet() {
					return this.getRate() / RobotMap.MAX_ENCODER_RATE;
				}
			}};

	// Gyro
	OffsetableGyro gyro = new OffsetableGyro(RobotMap.GYRO_PORT);
	
	// PIDS and PID outputs

	// AnglePID
	
	MockSpeedController anglePIDOutput    = new MockSpeedController();

	PIDController anglePID = new PIDController(0.02, 0.0, -0.001, 0.0,
			new PIDSource() {
				public double pidGet() {
					return gyro.getAngle();
				}
			}, anglePIDOutput);
	
	double angleRelativeSetpoint = 0.0;
	double angleSetpointPrev     = 0.0;
	
	public static final double ANGLE_PID_ABSOLUTE_TOLERANCE = 2.7d;

	public static final double ANGLE_PID_PRODUCTION_P = 0.02d;
	public static final double ANGLE_PID_PRODUCTION_I = 0.0d;
	public static final double ANGLE_PID_PRODUCTION_D = -0.001d;
	
	public static final double ANGLE_PID_PRACTICE_P = 0.02d;
	public static final double ANGLE_PID_PRACTICE_I = 0.0d;
	public static final double ANGLE_PID_PRACTICE_D = -0.001d;
	
	public static final double ANGLE_PID_TEST_P = 0.02d;
	public static final double ANGLE_PID_TEST_I = 0.0d;
	public static final double ANGLE_PID_TEST_D = -0.001d;
	
	// DriveAnglePID
	
	MockSpeedController driveHoldAnglePIDOutput  = new MockSpeedController();

	PIDController driveHoldAnglePID = new PIDController(0.04, 0.004, 0.0, 0.0,
			new PIDSource() {
				public double pidGet() {
					return gyro.getAngle();
				}
			}, driveHoldAnglePIDOutput);
	
	double driveHoldAngle = -1.0d;
	long driveHoldEnableTimerStart = -1;
	
	public static final double DRIVE_HOLD_ANGLE_PID_PRODUCTION_P = 0.02d;
	public static final double DRIVE_HOLD_ANGLE_PID_PRODUCTION_I = 0.001d;
	public static final double DRIVE_HOLD_ANGLE_PID_PRODUCTION_D = 0.0d;
	
	public static final double DRIVE_HOLD_ANGLE_PID_PRACTICE_P = 0.02d;
	public static final double DRIVE_HOLD_ANGLE_PID_PRACTICE_I = 0.001d;
	public static final double DRIVE_HOLD_ANGLE_PID_PRACTICE_D = 0.0d;
	
	public static final double DRIVE_HOLD_ANGLE_PID_TEST_P = 0.02d;
	public static final double DRIVE_HOLD_ANGLE_PID_TEST_I = 0.001d;
	public static final double DRIVE_HOLD_ANGLE_PID_TEST_D = 0.0d;

	// RotationPID - angular velocity
	
	MockSpeedController rotationPIDOutput    = new MockSpeedController();

	
	// p 0.4, i 0.0, d 0.0, f RobotMap.MAX_ANGULAR_VELOCITY / 360.0
	PIDController rotationPID = new PIDController(0.025, 0, 0, RobotMap.MAX_ANUGLAR_VELOCITY / 190.0, 
			new PIDSource() {
				public double pidGet() {
					return getGyroRotation() / RobotMap.MAX_ANUGLAR_VELOCITY;
				}
			},	rotationPIDOutput);
	
	public static final double ROTATION_PID_PRODUCTION_P = 0.02d;
	public static final double ROTATION_PID_PRODUCTION_I = 0.001d;
	public static final double ROTATION_PID_PRODUCTION_D = 0.0d;
	
	public static final double ROTATION_PID_PRACTICE_P = 0.02d;
	public static final double ROTATION_PID_PRACTICE_I = 0.001d;
	public static final double ROTATION_PID_PRACTICE_D = 0.0d;
	
	public static final double ROTATION_PID_TEST_P = 0.02d;
	public static final double ROTATION_PID_TEST_I = 0.001d;
	public static final double ROTATION_PID_TEST_D = 0.0d;

	// Distance PID
	
	MockSpeedController distancePIDOutput = new MockSpeedController();

	PIDController distancePID = new PIDController(0.007, 0.0, -0.0005, 0.0, 
			new PIDSource() {
				public double pidGet() {
					return getDistance();
				}
			}, distancePIDOutput);

	public static final double DISTANCE_PID_PRODUCTION_P = 0.007d;
	public static final double DISTANCE_PID_PRODUCTION_I = 0.0d;
	public static final double DISTANCE_PID_PRODUCTION_D = -0.0005d;
	
	public static final double DISTANCE_PID_PRACTICE_P = 0.007d;
	public static final double DISTANCE_PID_PRACTICE_I = 0.0d;
	public static final double DISTANCE_PID_PRACTICE_D = -0.0005d;
	
	public static final double DISTANCE_PID_TEST_P = 0.007d;
	public static final double DISTANCE_PID_TEST_I = 0.0d;
	public static final double DISTANCE_PID_TEST_D = -0.0005d;

	// Wheel Speed PID

	MockSpeedController [] wheelSpeedPIDOutputArr = {
			new MockSpeedController(),
			new MockSpeedController(),
			new MockSpeedController(),
			new MockSpeedController()  };

	PIDController [] wheelSpeedPIDArr = {
			new PIDController(0.4, 0.0, -0.0,	1.0, 
					encoderArr[FRONT_LEFT], wheelSpeedPIDOutputArr[FRONT_LEFT]),
			new PIDController(0.4, 0.0, -0.0,	1.0, 
					encoderArr[REAR_LEFT],  wheelSpeedPIDOutputArr[REAR_LEFT]),
			new PIDController(0.4, 0.0, -0.0,	1.0, 
					encoderArr[FRONT_RIGHT],wheelSpeedPIDOutputArr[FRONT_RIGHT]),
			new PIDController(0.4, 0.0, -0.0,	1.0, 
					encoderArr[REAR_RIGHT], wheelSpeedPIDOutputArr[REAR_RIGHT])	};

	public static final double WHEEL_SPEED_PID_PRODUCTION_P = 1.3d;
	public static final double WHEEL_SPEED_PID_PRODUCTION_I = 0.0d;
	public static final double WHEEL_SPEED_PID_PRODUCTION_D = -0.65d;
	
	public static final double WHEEL_SPEED_PID_PRACTICE_P = 1.3d;
	public static final double WHEEL_SPEED_PID_PRACTICE_I = 0.0d;
	public static final double WHEEL_SPEED_PID_PRACTICE_D = -0.65d;
	
	public static final double WHEEL_SPEED_PID_TEST_P = 1.3d;
	public static final double WHEEL_SPEED_PID_TEST_I = 0.0d;
	public static final double WHEEL_SPEED_PID_TEST_D = -0.65d;

	/**
	 * Is the angle on target for the specified drive angle driveToAngle which enables the anglePID.
	 * @return true if on target, false otherwise
	 */
	public boolean angleOnTarget() {
		return anglePID.onTarget();
	}

	@Override
	public void disableSubsystem() {

		subsystemEnabled = false;
		
		disablePIDs();

		// Stop all the motors
		stopMotors();
	}

	/**
	 * Is the distance on target for the specified drive distance after calling driveToDistance
	 * which enables the distancePID.
	 * @return true if on target, false otherwise
	 */
	public boolean distanceOnTarget() {
		return distancePID.onTarget();
	}

	/**
	 * Set the motors to drive in the direction and magnitude of the PolarCoordinate and 
	 * with the specified rotation.
	 * 
	 * @param p - PolarCoordinate (r, theta) used to determine speed and direction
	 * @param rotation - the requested rotational speed
	 * @param driveMode - FIELD_RELATIVE, or ROBOT_RELATIVE.
	 * @param rotationPIDEnable - ENABLE to use rotation PIDS for driving the motor speeds
	 * @param motorPIDEnable - ENABLE to use motor PIDS for driving the motor speeds
	 */
	public void driveJoystick(PolarCoordinate p, double rotation, DriveMode driveMode, 
			PIDEnable rotationPIDEnable, PIDEnable motorPIDEnable) {

		// Disable unused PIDs
		disableAnglePID();
		disableDistancePID();
		
		// Calculate the angle of travel relative to the robot heading.
		PolarCoordinate drivePolarCoordinate = getDrivePolarCoordinate(p, driveMode);

		drivePolar(drivePolarCoordinate, rotation, rotationPIDEnable, motorPIDEnable);

	}

	/**
	 * Drive while rotating to the target angle.
	 * <p>
	 * The robot direction and speed drive are still active.
	 * 
	 * @param p - PolarCoordinate (r, theta) used to determine speed and direction
	 * @param targetAngle
	 * @param motorPIDEnable
	 */
	public void driveToAngle(PolarCoordinate p, double angleSetpoint, DriveMode driveMode,
			PIDEnable rotationPIDEnable, PIDEnable motorPIDEnable) {

		// Disable unused PIDs
		disableDistancePID();
		
		// Update the angle setpoint based on the target angle specified.  If the target angle is 
		// not specified (-1) then do not update the setpoint angle.
		
		if (angleSetpoint >= 0) {

			// In FIELD_RELATIVE mode, this command will try to line the robot up with the angle
			// specified.
			// In ROBOT_RELATIVE mode, this command will rotate the robot to face in the new
			// direction indicated relative to the current direction.  
			if (driveMode == DriveMode.ROBOT_RELATIVE) {
				
				// If the PID is not yet enabled, or the angle changes, then set the target
				// based on the current direction in ROBOT_RELATIVE.
				if (!anglePID.isEnable() || angleSetpoint != angleSetpointPrev) {
				
					angleRelativeSetpoint = gyro.getAngle() + angleSetpoint;
					while (angleRelativeSetpoint > 360.0) {
						angleRelativeSetpoint -= 360.0d;
					}
				}
				
			} else {
				
				// Drive mode is field relative.
				angleRelativeSetpoint = angleSetpoint;
			}
			
			// Keep track of the input setpoint to track when it changes which is important for 
			// ROBOT_RELATIVE mode.
			angleSetpointPrev = angleSetpoint;
		}

		enableAnglePID();
		
		SmartDashboard.putNumber("Angle difference", angleRelativeSetpoint - gyro.getAngle());
		
		// Use the output of the gyro angle PID to set the rotational velocity of the robot.
		anglePID.setSetpoint(angleRelativeSetpoint);
		
		PolarCoordinate drivePolarCoordinate = getDrivePolarCoordinate(p, driveMode);
		
		// Drive the robot using the input p and use the anglePID to set the rotation.
		drivePolar(drivePolarCoordinate, anglePIDOutput.get(), rotationPIDEnable, motorPIDEnable);
		
	}
	
	@Override
	public void enableSubsystem() {

		subsystemEnabled = true;
		
	}
	
	/**
	 *  Get the Gyro rotational speed
	 *  
	 *  @return double - Gyro rotational speed.
	 */
	public double getGyroRotation() {
		return gyro.getRate();
	}

	/**
	 *  Get the Gyro angle
	 *  
	 *  @return double - Gyro angle.
	 */
	public double getGyroAngle() {
		return gyro.getAngle();
	}

	/**
	 * Gyro Angle on target
	 * 
	 * @return - true if the gyro angle is at the gyro setpoint after calling
	 * driveToAngle.
	 */
	public boolean gyroAngleOnTarget() {
		return anglePID.onTarget();
	}


	@Override
	public void initDefaultCommand() {
		setDefaultCommand(new TeleopDriveCommand());
	}
	
	public void initSubsystem() {
		
		// Initialize Sensors
		gyro.setSensitivity(0.0125);
		//gyro.initGyro();

		// Initialize PID parameters
		// Angle tolerance to determine if the PID is on target in degrees.
		// Set the PID Constants based on the robot that is executing.  The default is the 
		// test robot
		anglePID.setInputRange(0.0d, 360.0d);
		anglePID.setContinuous(true);
		anglePID.setOutputRange(-1.0d, 1.0d);
		switch (RobotMap.currentRobot) {
		case RobotMap.ROBOT_PRODUCTION:anglePID.setPID(ANGLE_PID_PRODUCTION_P, ANGLE_PID_PRODUCTION_I, ANGLE_PID_PRODUCTION_D); break;
		case RobotMap.ROBOT_PRACTICE:  anglePID.setPID(ANGLE_PID_PRACTICE_P,   ANGLE_PID_PRACTICE_I,   ANGLE_PID_PRACTICE_D);   break;
		case RobotMap.ROBOT_TEST:      anglePID.setPID(ANGLE_PID_TEST_P,       ANGLE_PID_TEST_I,       ANGLE_PID_TEST_D);       break;
		default: break;	}

		// Angle tolerance to determine if the PID is on target in degrees.
		driveHoldAnglePID.setInputRange(0.0d, 360.0d);
		driveHoldAnglePID.setContinuous(true);
		driveHoldAnglePID.setOutputRange(-1.0d, 1.0d);
		switch (RobotMap.currentRobot) {
		case RobotMap.ROBOT_PRODUCTION:driveHoldAnglePID.setPID(DRIVE_HOLD_ANGLE_PID_PRODUCTION_P, DRIVE_HOLD_ANGLE_PID_PRODUCTION_I, DRIVE_HOLD_ANGLE_PID_PRODUCTION_D); break;
		case RobotMap.ROBOT_PRACTICE:  driveHoldAnglePID.setPID(DRIVE_HOLD_ANGLE_PID_PRACTICE_P,   DRIVE_HOLD_ANGLE_PID_PRACTICE_I,   DRIVE_HOLD_ANGLE_PID_PRACTICE_D);   break;
		case RobotMap.ROBOT_TEST:      driveHoldAnglePID.setPID(DRIVE_HOLD_ANGLE_PID_TEST_P,       DRIVE_HOLD_ANGLE_PID_TEST_I,       DRIVE_HOLD_ANGLE_PID_TEST_D);       break;
		default: break;	}

		// Rotation PID
		rotationPID.setInputRange(-1.0, 1.0);
		rotationPID.setOutputRange(-1.0d, 1.0d);
		switch (RobotMap.currentRobot) {
		case RobotMap.ROBOT_PRODUCTION:rotationPID.setPID(ROTATION_PID_PRODUCTION_P, ROTATION_PID_PRODUCTION_I, ROTATION_PID_PRODUCTION_D); break;
		case RobotMap.ROBOT_PRACTICE:  rotationPID.setPID(ROTATION_PID_PRACTICE_P,   ROTATION_PID_PRACTICE_I,   ROTATION_PID_PRACTICE_D);   break;
		case RobotMap.ROBOT_TEST:      rotationPID.setPID(ROTATION_PID_TEST_P,       ROTATION_PID_TEST_I,       ROTATION_PID_TEST_D);       break;
		default: break;	}
		
		// Distance tolerance to determine if the PID is on target in encoder counts.
		distancePID.setAbsoluteTolerance(8d);
		distancePID.setOutputRange(-1.0, 1.0);
		switch (RobotMap.currentRobot) {
		case RobotMap.ROBOT_PRODUCTION:distancePID.setPID(DISTANCE_PID_PRODUCTION_P, DISTANCE_PID_PRODUCTION_I, DISTANCE_PID_PRODUCTION_D); break;
		case RobotMap.ROBOT_PRACTICE:  distancePID.setPID(DISTANCE_PID_PRACTICE_P,   DISTANCE_PID_PRACTICE_I,   DISTANCE_PID_PRACTICE_D);   break;
		case RobotMap.ROBOT_TEST:      distancePID.setPID(DISTANCE_PID_TEST_P,       DISTANCE_PID_TEST_I,       DISTANCE_PID_TEST_D);       break;
		default: break;	}

		// WheelSpeedPID
		for (int i=0; i<MOTOR_COUNT; i++) {
			wheelSpeedPIDArr[i].setInputRange(-1.0d, 1.0d);
			wheelSpeedPIDArr[i].setOutputRange(-1.0d, 1.0d);
			switch (RobotMap.currentRobot) {
			case RobotMap.ROBOT_PRODUCTION:wheelSpeedPIDArr[i].setPID(WHEEL_SPEED_PID_PRODUCTION_P, WHEEL_SPEED_PID_PRODUCTION_I, WHEEL_SPEED_PID_PRODUCTION_D, 1.0d); break;
			case RobotMap.ROBOT_PRACTICE:  wheelSpeedPIDArr[i].setPID(WHEEL_SPEED_PID_PRACTICE_P,   WHEEL_SPEED_PID_PRACTICE_I,   WHEEL_SPEED_PID_PRACTICE_D,   1.0d); break;
			case RobotMap.ROBOT_TEST:      wheelSpeedPIDArr[i].setPID(WHEEL_SPEED_PID_TEST_P,       WHEEL_SPEED_PID_TEST_I,       WHEEL_SPEED_PID_TEST_D,       1.0d); break;
			default: break;	}
		}

		// Initialize SmartDashboard objects

		// SmartDashboard.putData("Accel", accel);

		SmartDashboard.putData("Gyro", gyro);

		SmartDashboard.putData("FrontLeftTalon", talonArr[FRONT_LEFT]);
		SmartDashboard.putData("RearLeftTalon",  talonArr[REAR_LEFT]);
		SmartDashboard.putData("FrontRightTalon",talonArr[FRONT_RIGHT]);
		SmartDashboard.putData("RearRightTalon", talonArr[REAR_RIGHT]);
		
		SmartDashboard.putData("FrontLeftEncoder", encoderArr[FRONT_LEFT]);
		SmartDashboard.putData("RearLeftEncoder",  encoderArr[REAR_LEFT]);
		SmartDashboard.putData("FrontRightEncoder",encoderArr[FRONT_RIGHT]);
		SmartDashboard.putData("RearRightEncoder", encoderArr[REAR_RIGHT]);

		SmartDashboard.putData("FrontLeftPID", wheelSpeedPIDArr[FRONT_LEFT]);
		SmartDashboard.putData("RearLeftPID",  wheelSpeedPIDArr[REAR_LEFT]);
		SmartDashboard.putData("FrontRightPID",wheelSpeedPIDArr[FRONT_RIGHT]);
		SmartDashboard.putData("RearRightPID", wheelSpeedPIDArr[REAR_RIGHT]);
		
		SmartDashboard.putData("DistancePID", distancePID);
		SmartDashboard.putData("GyroAnglePID", anglePID);
		SmartDashboard.putData("DriveAnglePID", driveHoldAnglePID);
		SmartDashboard.putData("GyroRotationPID", rotationPID);

	}
	
	public void print() {
		/*
		 * if (System.currentTimeMillis() - lastPrintTime > 1000) {
		 * System.out.println("Gyro Rate:" + gyro.getRate());
		 * 
		 * System.out.println("PID: " + (frontLeftPID.isEnable() &&
		 * rearLeftPID.isEnable() && frontRightPID.isEnable() &&
		 * rearRightPID.isEnable()));
		 * 
		 * System.out.println("Left Encoders:"); System.out.println("Front: " +
		 * frontLeftEncoder.getDistance() + " Rear: " +
		 * rearLeftEncoderCorrected.getDistance() *
		 * RobotMap.ENCODER_RESOLUTION_CORRECTION);
		 * System.out.println("RightEncoders:"); System.out.println("Front: " +
		 * frontRightEncoder.getDistance() + " Rear: " +
		 * rearRightEncoder.getDistance());
		 * 
		 * lastPrintTime = System.currentTimeMillis();
		 * 
		 * }
		 */
	}

	/**
	 * Reset the Gyro to the supplied angle.  
	 * <p>
	 * This will allow the driver to reset the gyro to a given angle relative to the field to 
	 * compensate for gyro drift.  The robot should not be moving when the gyro is reset. 
	 *
	 * @param fieldAngle
	 */
	public void resetGyro(int fieldAngle) {

		gyro.setOffset(fieldAngle);
		
	}
	
	@Override
	public void updateDashboard() {
		// accel.updateTable();

		gyro.updateTable();
		
		for (Encoder encoder: encoderArr) {
			encoder.updateTable();
		}

		for (PIDController wheelSpeedPID: wheelSpeedPIDArr) {
			wheelSpeedPID.updateTable();
		}
		
		SmartDashboard.putNumber("Front Left Output", talonArr[FRONT_LEFT].get());
		SmartDashboard.putNumber("Front Right Output", talonArr[FRONT_RIGHT].get());
		SmartDashboard.putNumber("Rear Left Output", talonArr[REAR_LEFT].get());
		SmartDashboard.putNumber("Rear Right Output", talonArr[REAR_RIGHT].get());

		distancePID.updateTable();
		SmartDashboard.putNumber("Distance PID Output", distancePIDOutput.get());
		
		anglePID.updateTable();
		SmartDashboard.putNumber("Angle PID Output", anglePIDOutput.get());

		driveHoldAnglePID.updateTable();
		SmartDashboard.putNumber("Drive Angle PID Output", driveHoldAnglePIDOutput.get());
		
		rotationPID.updateTable();
		SmartDashboard.putNumber("Rotation PID Output", rotationPIDOutput.get());

		SmartDashboard.putNumber("Gyro angle",     gyro.getAngle());
		SmartDashboard.putNumber("Gyro rate",      getGyroRotation());

		SmartDashboard.putNumber("EncoderAverager", getDistance());

		SmartDashboard.putBoolean("Chassis Subsystem Enabled" , subsystemEnabled);

		// SmartDashboard.putNumber("X pos", xpos);
	}
	
	/**
	 * Disable the AnglePID
	 * 
	 * If already disabled, this routine does nothing.
	 */
	private void disableAnglePID() {

		if (anglePID.isEnable()) {
			anglePID.disable();
		}
		
	}
	
	/**
	 * Disable the HoldAnglePID
	 * 
	 * If already disabled, this routine does nothing.
	 */
	private void disableHoldAnglePID() {

		if (driveHoldAnglePID.isEnable()) {
			driveHoldAnglePID.disable();
		}
		
	}
	
	/**
	 * Disable the DistancePID
	 * 
	 * If already disabled, this routine does nothing.
	 */
	private void disableDistancePID() {

		if (distancePID.isEnable()) {
			distancePID.disable();
		}
	}

	private void disablePIDs() {

		disableAnglePID();
		disableRotationPID();
		disableDistancePID();
		disableWheelSpeedPIDs();
		disableHoldAnglePID();
		
	}
	
	
	/**
	 * Disable the RotationID
	 * 
	 * If already disabled, this routine does nothing.
	 */
	private void disableRotationPID() {

		if (rotationPID.isEnable()) {
			rotationPID.disable();
		}
	}

	/**
	 * Disable the wheelSpeedPIDs
	 * 
	 * If they are already disabled, this routine does nothing.
	 */
	private void disableWheelSpeedPIDs() {

		for (PIDController wheelSpeedPID: wheelSpeedPIDArr) {

			if (wheelSpeedPID.isEnable()) {
				wheelSpeedPID.disable();
			}
		}
		
	}
	
	/**
	 * Set the motors to drive in the direction and magnitude of the PolarCoordinate and 
	 * with the specified rotation.
	 * 
	 * @param p - PolarCoordinate (r, theta) used to determine speed and direction
	 * @param rotation - speed of rotation of the robot
	 * @param motorPIDEnable - ENABLE to use motor PIDS for driving the motor speeds
	 */
	private void drivePolar(PolarCoordinate p, double rotation, 
			PIDEnable rotationPIDEnable, PIDEnable motorPIDEnable) {

		// Determine if the angle should be held constant during this move sequence.
		// if there is no rotational input then try to hold the rotation constant.
		// Override the rotation PID to disabled.
		double angleRotation = rotation;
		if (Math.abs(rotation) < .02d) {
			
			// Wait 2 seconds after there is zero rotation input before enabling the
			// driveHoldAnglePID.
			if (driveHoldEnableTimerStart < 0) {
				driveHoldEnableTimerStart = System.currentTimeMillis();
			}
			
			// Enable the driveHoldPID after 2 seconds
			if (System.currentTimeMillis() - driveHoldEnableTimerStart > 2000) {
				disableRotationPID();
				rotationPIDEnable = PIDEnable.DISABLED;
				if (driveHoldAngle < 0) { 
					driveHoldAngle = gyro.getAngle(); 
					driveHoldAnglePID.reset();
					driveHoldAnglePID.enable();
					driveHoldAnglePID.setSetpoint(driveHoldAngle);
				}
				angleRotation = driveHoldAnglePIDOutput.get();
			} else {
				driveHoldAnglePID.disable();
				driveHoldAngle = -1.0d;
			}
		} else {
			driveHoldAnglePID.disable();
			driveHoldAngle = -1.0d;
			driveHoldEnableTimerStart = -1;
		}

		// Use a rotation PID if required.
		double mecanumRotation = angleRotation;
		
		// If the rotationPID is enabled, then put the rotation through the PID.
		if (rotationPIDEnable == PIDEnable.ENABLED) {
			enableRotationPID();
			rotationPID.setSetpoint(angleRotation);
			mecanumRotation = rotationPIDOutput.get();
		} else {
			disableRotationPID();
		}
		
		/* 
		 * The mecannum drive uses the angle theta of the polar coordinate p(r,theta) 
		 * to set the relative speeds of all of the drive wheels.
		 */
		double [] motorSpeeds = mecanumDrive.drivePolar(p, mecanumRotation);
		
		// If the motor PIDs are enabled, then use the motor speeds as the setpoint values
		// for the motor PIDs.
		if (motorPIDEnable == PIDEnable.ENABLED) {
			enableWheelSpeedPIDs();
			
			for (int i=0; i<MOTOR_COUNT; i++) {
				wheelSpeedPIDArr[i].setSetpoint(motorSpeeds[i]);
			}
		} else {
			disableWheelSpeedPIDs();
		}

		// Set the motor speeds.
		if (motorPIDEnable == PIDEnable.ENABLED) {
			for (int i=0; i<MOTOR_COUNT; i++) {
				talonArr[i].set(wheelSpeedPIDOutputArr[i].get());
			}
		} else {
			for (int i=0; i<MOTOR_COUNT; i++) {
				talonArr[i].set(motorSpeeds[i]);
			}
		}
	}
	
	/**
	 * Enable and reset the AnglePID
	 * 
	 * If already enabled, this routine does nothing.
	 */
	private void enableAnglePID() {

		if (!anglePID.isEnable()) {
			anglePID.reset();
			anglePID.enable();
		}
		
	}
	
	/**
	 * Enable and reset the RotationPID
	 * 
	 * This pid controls the angular velocity of the robot.  The integral portion
	 * of this control helps the robot to move in a straight line by driving the 
	 * angle error (integral of rotation) to zero.
	 * 
	 * If already enabled, this routine does nothing.
	 */
	private void enableRotationPID() {

		if (!rotationPID.isEnable()) {
			rotationPID.reset();
			rotationPID.enable();
		}
	}
	
	/**
	 * Enable and reset the DistancePID
	 * 
	 * If already enabled, this routine does nothing.
	 */
	private void enableDistancePID() {

		if (!distancePID.isEnable()) {
			
			distancePID.reset();
			distancePID.enable();

			resetEncoders();
		}
	}

	/**
	 * Enable and reset the wheel speed PIDs.
	 * 
	 * If they are already enabled, this routine does nothing.
	 */
	private void enableWheelSpeedPIDs() {

		for (PIDController wheelSpeedPID: wheelSpeedPIDArr) {

			if (!wheelSpeedPID.isEnable()) {
				wheelSpeedPID.reset();
				wheelSpeedPID.enable();
				
			}
		}
		
	}
	
	// The distance travelled is roughly the average distance of all the encoders
	private double getDistance() {
		double distanceTotal = 0;
		for (Encoder encoder: encoderArr) {
			distanceTotal += Math.abs(encoder.getDistance());
		}
		return distanceTotal / encoderArr.length;
	}

	private PolarCoordinate getDrivePolarCoordinate(PolarCoordinate p, DriveMode driveMode) {

		PolarCoordinate drivePolarCoordinate = new PolarCoordinate();
		drivePolarCoordinate.set(p);
		
		if (driveMode == DriveMode.FIELD_RELATIVE) {
			drivePolarCoordinate.setTheta(p.getTheta() - gyro.getAngle());
		}
		
		return drivePolarCoordinate;
	}

	private void resetEncoders() {

		for (Encoder encoder: encoderArr) {
			encoder.reset();
		}
		
		for (PIDController wheelSpeedPID: wheelSpeedPIDArr) {
			wheelSpeedPID.setSetpoint(0.0);
		}

	}
	
	private void stopMotors() {
		for (Talon talon: talonArr) {
			talon.stopMotor();
		}
	}
	
}
