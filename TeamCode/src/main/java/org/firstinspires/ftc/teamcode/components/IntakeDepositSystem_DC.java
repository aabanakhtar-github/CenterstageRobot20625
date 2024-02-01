package org.firstinspires.ftc.teamcode.components;

import static com.qualcomm.robotcore.hardware.Servo.Direction.REVERSE;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Config
public class IntakeDepositSystem_DC {

    public enum SpintakeModes {
        INTAKE, STOP, SPIT
    }

    // Quickness over precision; we can use 1.2s max extension time
    public static int LOW_POSITION = 30;

    public static int ROTATABLE_POSITION = 200;

    public static  int SET_LINE_1 = 1000;

    public static  int SET_LINE_2 = 2000;

    public static  int SET_LINE_3 = 3000;

    public static  double ARM_POWER = 0.8;

    public static double ROTATOR_MAX = 0.8;

    public static double ROTATOR_MIN = 0.0;

    public SpintakeModes SpintakeMode = SpintakeModes.STOP;

    public int CurrentLocationState = LOW_POSITION;

    private DcMotorEx m_LeftSlide, m_RightSlide, m_Spintake;

    private Servo m_LeftRot, m_RightRot, m_ReleaseLatch;

    private int LastPosition = LOW_POSITION;

    public IntakeDepositSystem_DC(HardwareMap hwmap) {
        m_LeftSlide = hwmap.get(DcMotorEx.class, "left slide");
        m_RightSlide = hwmap.get(DcMotorEx.class, "right slide");
        m_RightSlide.setDirection(DcMotorSimple.Direction.REVERSE);

        m_LeftSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m_RightSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m_Spintake = hwmap.get(DcMotorEx.class, "spintake");

        //TODO: Setup servos
        m_RightRot = hwmap.get(Servo.class, "right rot");
        m_ReleaseLatch = hwmap.get(Servo.class, "release");
    }


    /**
     * @param: autonomous, use in autonomous to enable blocking behavior
     */
    public void UpdateStateMachine(boolean autonomous) {
        if (!autonomous) {
           if (SpintakeMode == SpintakeModes.INTAKE) {
               CurrentLocationState = LOW_POSITION;
           }
           if (CurrentLocationState != LastPosition) {
                if (CurrentLocationState == LOW_POSITION || SpintakeMode == SpintakeModes.INTAKE) {
                    PrepareToIntake();
                    RunSlidesToPosition(LOW_POSITION);
                } else {
                    RunSlidesToPosition(CurrentLocationState);
                    PrepareToDeposit();
                }
            }
        } else {
            if (SpintakeMode == SpintakeModes.INTAKE) {
                CurrentLocationState = LOW_POSITION;
            }
            if (CurrentLocationState != LastPosition) {
                if (CurrentLocationState == LOW_POSITION || SpintakeMode == SpintakeModes.INTAKE) {
                    PrepareToIntake();
                    RunSlidesToPosition(LOW_POSITION);
                } else {
                    RunSlidesToPosition(CurrentLocationState);
                    PrepareToDeposit();
                }
            }
            while (IsBusy()) {}
        }
        LastPosition = CurrentLocationState;

        // spintake state machine
        if (SpintakeMode == SpintakeModes.INTAKE) {
            m_Spintake.setPower(1.0);
        }
        else if (SpintakeMode == SpintakeModes.SPIT) {
            m_Spintake.setPower(-1.0);
        }
        else {
            m_Spintake.setPower(0.0);
        }

    }

    private void PrepareToIntake() {
        //m_LeftRot.setPosition(ROTATOR_MAX);
        m_RightRot.setPosition(ROTATOR_MIN);
        ReleaseDeposit();
    }

    private void PrepareToDeposit() {
        //m_LeftRot.setPosition(ROTATOR_MAX);
        m_RightRot.setPosition(ROTATOR_MAX);
        CloseDeposit();
    }

    private void RunSlidesToPosition(int pos) {
        m_LeftSlide.setTargetPosition(pos);
        m_RightSlide.setTargetPosition(pos);
        m_LeftSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        m_RightSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        m_LeftSlide.setPower(ARM_POWER);
        m_RightSlide.setPower(ARM_POWER);
    }

    public void ReleaseDeposit() {
        m_ReleaseLatch.setPosition(0.0);
    }

    public void CloseDeposit() {
        m_ReleaseLatch.setPosition(0.6);
    }

    public boolean IsBusy() {
        return m_LeftSlide.isBusy() && m_RightSlide.isBusy();
    }
}
