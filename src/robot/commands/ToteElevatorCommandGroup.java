package robot.commands;

import robot.Robot;
import robot.subsystems.ToteElevatorSubsystem.ToteElevatorLevel;
import edu.wpi.first.wpilibj.command.CommandGroup;

public class ToteElevatorCommandGroup extends CommandGroup {

	public ToteElevatorCommandGroup() {
		requires(Robot.sensorSubsystem);
		addSequential(new DriveToteElevatorCommand(ToteElevatorLevel.FLOOR));
		addSequential(new DriveToteElevatorCommand(ToteElevatorLevel.ONE));
	}
	
}
