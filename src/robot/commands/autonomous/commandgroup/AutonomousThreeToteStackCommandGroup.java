package robot.commands.autonomous.commandgroup;

import robot.commands.DriveDistanceCommand;
import robot.commands.ResetGyroCommand;
import robot.commands.autonomous.AutonomousDelayCommand;
import edu.wpi.first.wpilibj.command.CommandGroup;

public class AutonomousThreeToteStackCommandGroup extends CommandGroup{

	public AutonomousThreeToteStackCommandGroup() {
		addSequential(new ResetGyroCommand(270));
		
		addSequential(new DriveDistanceCommand(0.65, 0.0, 270, 21));
		addSequential(new AutonomousDelayCommand(500));
		addSequential(new DriveDistanceCommand(0.65, 270, 270, 48));
		addSequential(new AutonomousDelayCommand(500));
		addSequential(new DriveDistanceCommand(0.65, 180, 270, 21));
		addSequential(new AutonomousDelayCommand(500));
		
		addSequential(new DriveDistanceCommand(0.65, 0.0, 270, 21));
		addSequential(new AutonomousDelayCommand(500));
		addSequential(new DriveDistanceCommand(0.65, 270, 270, 48));
		addSequential(new AutonomousDelayCommand(500));
		addSequential(new DriveDistanceCommand(0.65, 180, 270, 21));
		addSequential(new AutonomousDelayCommand(500));
		
		addSequential(new DriveDistanceCommand(0.65, 0.0, 0.0, 42));
	}
	
}