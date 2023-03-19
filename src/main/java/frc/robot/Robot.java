// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import edu.wpi.first.wpilibj.Joystick;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.CANCoderFaults;
import com.ctre.phoenix.sensors.CANCoderStickyFaults;
import com.ctre.phoenix.sensors.MagnetFieldStrength;
import com.ctre.phoenix.sensors.WPI_CANCoder;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */

public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final VictorSP gearDrive = new VictorSP(0);
  private Joystick myJoystick = new Joystick(0);
  
  //CTRE CANcoder sample code from:
  //github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20General/CANCoder/src/main/java/frc/robot/Robot.java
  final int PRINTOUT_DELAY = 100; // in Milliseconds
  WPI_CANCoder _CANCoder = new WPI_CANCoder(1, "rio");
  CANCoderConfiguration _canCoderConfiguration = new CANCoderConfiguration();

    /**  
   * Doing lots of printing in Java creates a large overhead 
   * This Instrument class is designed to put that printing in a seperate thread
   * That way we can prevent loop overrun messages from occurring
   */
  class Instrument extends Thread {
    void printFaults(CANCoderFaults faults) {
      System.out.printf("Hardware fault: %s\t    Under Voltage fault: %s\t    Reset During Enable fault: %s\t    API Error fault: %s%n", 
        faults.HardwareFault ? "True " : "False",
        faults.UnderVoltage ? "True " : "False",
        faults.ResetDuringEn ? "True " : "False",
        faults.APIError ? "True " : "False");
    }
    void printFaults(CANCoderStickyFaults faults) {
      System.out.printf("Hardware fault: %s\t    Under Voltage fault: %s\t    Reset During Enable fault: %s\t     API Error fault: %s%n", 
        faults.HardwareFault ? "True " : "False",
        faults.UnderVoltage ? "True " : "False",
        faults.ResetDuringEn ? "True " : "False",
        faults.APIError ? "True " : "False");
    }
    void printValue(double val, String units, double timestamp) {
      System.out.printf("%20f %-20s @ %f%n", val, units, timestamp);
    }
    void printValue(MagnetFieldStrength val, String units, double timestamp) {
      System.out.printf("%20s %-20s @ %f%n", val.toString(), units, timestamp);
    }

    public void run() {
      /* Report position, absolute position, velocity, battery voltage */
      double posValue = _CANCoder.getPosition();
      String posUnits = _CANCoder.getLastUnitString();
      double posTstmp = _CANCoder.getLastTimestamp();
      
      double absValue = _CANCoder.getAbsolutePosition();
      String absUnits = _CANCoder.getLastUnitString();
      double absTstmp = _CANCoder.getLastTimestamp();
      
      double velValue = _CANCoder.getVelocity();
      String velUnits = _CANCoder.getLastUnitString();
      double velTstmp = _CANCoder.getLastTimestamp();
      
      double batValue = _CANCoder.getBusVoltage();
      String batUnits = _CANCoder.getLastUnitString();
      double batTstmp = _CANCoder.getLastTimestamp();

      /* Report miscellaneous attributes about the CANCoder */
      MagnetFieldStrength magnetStrength = _CANCoder.getMagnetFieldStrength();
      String magnetStrengthUnits = _CANCoder.getLastUnitString();
      double magnetStrengthTstmp = _CANCoder.getLastTimestamp();

      System.out.print("Position: ");
      printValue(posValue, posUnits, posTstmp);
      System.out.print("Abs Pos : ");
      printValue(absValue, absUnits, absTstmp);
      System.out.print("Velocity: ");
      printValue(velValue, velUnits, velTstmp);
      System.out.print("Battery : ");
      printValue(batValue, batUnits, batTstmp);
      System.out.print("Strength: ");
      printValue(magnetStrength, magnetStrengthUnits, magnetStrengthTstmp);

      /* Fault reporting */
      CANCoderFaults faults = new CANCoderFaults();
      _CANCoder.getFaults(faults);
      CANCoderStickyFaults stickyFaults = new CANCoderStickyFaults();
      _CANCoder.getStickyFaults(stickyFaults);

      System.out.println("Faults:");
      printFaults(faults);
      System.out.println("Sticky Faults:");
      printFaults(stickyFaults);

      System.out.println();
      System.out.println();
    }
  }
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
      /* Change the configuration configs as needed here */
      _canCoderConfiguration.unitString = "driveshaft-rotations";

      _CANCoder.configAllSettings(_canCoderConfiguration);
    }
  
    int count = 0;


  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */

  @Override
  public void robotPeriodic() {
    if(count++ >= PRINTOUT_DELAY / 10) {
      count = 0;

      /**  
       * Doing lots of printing in Java creates a large overhead 
       * This Instrument class is designed to put that printing in a seperate thread
       * That way we can prevent loop overrun messages from occurring
       */
      new Instrument().start();
    }
    // button 1 on myJoystick is A to clear sticky faults
    if(myJoystick.getRawButton(1)) {
      _CANCoder.clearStickyFaults();
    }
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

   //Pivot Arm
   if(myJoystick.getRawButton(5)) //this is l trigger
   {gearDrive.set(0.5);}
    else if (myJoystick.getRawButton(6))//this is r trigger
    {gearDrive.set(-.5);}
    else {gearDrive.set(0);}
  }
    //Cancoder
  //   if(_loopCount++ > 10)
  //   {
  //       _loopCount = 0;
  //       double degrees = _coder.getPosition();
  //       System.out.println("CANCoder position is: " + degrees);
  //   }
  // }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}}


