
package robot;


import java.util.ArrayList;
import java.util.List;

import robot.commands.ExampleCommand;
import robot.subsystems.ActuatorSubsystem;
import robot.subsystems.PowerSubsystem;
import robot.subsystems.RunnymedeSubsystem;
import robot.subsystems.SensorSubsystem;
import robot.subsystems.VisionSubsystem;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	private List<RunnymedeSubsystem> subsystemLs = new ArrayList<RunnymedeSubsystem>();
	
	public static final ActuatorSubsystem actuatorSubsystem = new ActuatorSubsystem();
	public static final SensorSubsystem   sensorSubsystem   = new SensorSubsystem();
	public static final VisionSubsystem   visionSubsystem   = new VisionSubsystem();
	public static final PowerSubsystem    powerSubsystem    = new PowerSubsystem();
	
	public static OI oi;

    Command autonomousCommand;

    public void autonomousInit() {
        // schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
    }
	
	/**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
        updateDashboard();
    }

    /**
     * This function is called when the disabled button is hit.
     * You can use it to reset subsystems before shutting down.
     */
    public void disabledInit() {}

    public void disabledPeriodic() {
		Scheduler.getInstance().run();
		updateDashboard();
	}

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
		oi = new OI();
		
        subsystemLs.add(actuatorSubsystem);
    	subsystemLs.add(sensorSubsystem);
    	subsystemLs.add(visionSubsystem);
    	subsystemLs.add(powerSubsystem);
    	
    	// Initialize all subsystems.
    	for (RunnymedeSubsystem subsystem: subsystemLs) {
    		subsystem.initSubsystem();
    	}

    	// instantiate the command used for the autonomous period
        autonomousCommand = new ExampleCommand();
    }

    public void teleopInit() {
		// This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        updateDashboard();
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
    }
    
    private void updateDashboard() {
    	for (RunnymedeSubsystem subsystem: subsystemLs) {
    		subsystem.updateDashboard();
    	}
    }
}
