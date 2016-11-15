package com.cyberdynesystems.nxtremotecontrol_v0;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Stack;

/**
 * Created by mlindsa6 on 11/9/16.
 */

public class DriveIntent extends AppCompatActivity implements View.OnClickListener {

    ImageButton cv_btnUP, cv_btnDown, cv_btnLeft, cv_btnRight, cv_btnReset, cv_btnForwradC, cv_btnBackwardC;
    SeekBar cv_sbBMotor, cv_sbPowerC, cv_sbAMotor;
    TextView cv_tvBMotorPower, cv_tvPowerC, cv_tvAMotorPower;
    RobotController cv_robotController;
    BottomBar bottomBar;

    boolean[] buttonPressed = {false, false, false, false, false, false};
    /*
    cv_btnUP        = buttonPressed[0]
    cv_btnDown      = buttonPressed[1]
    cv_btnRight     = buttonPressed[2]
    cv_btnLeft      = buttonPressed[3]
    cv_btnForwradC  = buttonPressed[4]
    cv_btnBackwardC = buttonPressed[5]
     */
    ImageButton[] cv_allButtons;
    Stack<byte[]> moveList = new Stack<byte[]>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driveintent);

        bottomBar = (BottomBar) findViewById(R.id.driveBottomBar);
        BottomBarTab driveBar = bottomBar.getTabWithId(R.id.tab_drive);
        bottomBar.setDefaultTab(driveBar.getId());

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_connect) {
                    Intent lv_intent = new Intent(DriveIntent.this, MainActivity.class);
                    startActivity(lv_intent);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                }

            }
        });

        cv_robotController = RobotController.getRobotController(DriveIntent.this);

        cv_tvAMotorPower = (TextView) findViewById(R.id.vv_tvAMotorPower);

        cv_sbAMotor = (SeekBar) findViewById(R.id.vv_sbAMotor);

        cv_tvAMotorPower = (TextView) findViewById(R.id.vv_tvAMotorPower);
        cv_tvAMotorPower.setText(Integer.toString(cv_sbAMotor.getProgress()));

        cv_sbBMotor = (SeekBar) findViewById(R.id.vv_sbBMotor);

        cv_tvBMotorPower = (TextView) findViewById(R.id.vv_tvBMotorPower);
        cv_tvBMotorPower.setText("" + cv_sbBMotor.getProgress());

        cv_sbPowerC = (SeekBar) findViewById(R.id.vv_sbPowerC);

        cv_tvPowerC = (TextView) findViewById(R.id.vv_tvPowerC);
        cv_tvPowerC.setText("" + cv_sbPowerC.getProgress());
        cv_btnUP = (ImageButton) findViewById(R.id.vv_btnUP);
        cv_btnDown = (ImageButton) findViewById(R.id.vv_btnDown);
        cv_btnLeft = (ImageButton) findViewById(R.id.vv_btnLeft);
        cv_btnRight = (ImageButton) findViewById(R.id.vv_btnRight);
        cv_btnReset = (ImageButton) findViewById(R.id.vv_btnReset);
        cv_btnForwradC = (ImageButton) findViewById(R.id.vv_btnForwardC);
        cv_btnBackwardC = (ImageButton) findViewById(R.id.vv_btnBackwardC);

        cv_btnUP.setOnClickListener(this);
        cv_btnReset.setOnClickListener(this);
        cv_btnDown.setOnClickListener(this);
        cv_btnRight.setOnClickListener(this);
        cv_btnLeft.setOnClickListener(this);
        cv_btnBackwardC.setOnClickListener(this);
        cv_btnForwradC.setOnClickListener(this);

        cv_btnUP.setEnabled(true);
        cv_btnReset.setEnabled(true);
        cv_btnDown.setEnabled(true);
        cv_btnRight.setEnabled(true);
        cv_btnLeft.setEnabled(true);
        cv_btnBackwardC.setEnabled(true);
        cv_btnForwradC.setEnabled(true);
        cv_sbPowerC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cv_tvPowerC.setText(Integer.toString(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cv_sbAMotor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cv_tvAMotorPower.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cv_sbBMotor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cv_tvBMotorPower.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        cv_allButtons = new ImageButton[]{cv_btnUP, cv_btnDown, cv_btnLeft, cv_btnRight, cv_btnForwradC, cv_btnBackwardC};


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.vv_btnUP) {
            if (buttonPressed[0] == false) {


                moveList.push(cv_robotController.cf_moveMotor(0, cv_sbAMotor.getProgress(), 0x20));
                moveList.push(cv_robotController.cf_moveMotor(1, cv_sbBMotor.getProgress(), 0x20));
                cv_tvAMotorPower.setText("" + cv_sbBMotor.getProgress());
                buttonPressed[0] = true;
            } else {
                cv_robotController.cf_moveMotor(0, 0, 0x00);
                cv_robotController.cf_moveMotor(1, 0, 0x00);
                moveList.clear();
                buttonPressed[0] = false;

            }

            cv_btnUP.setPressed(buttonPressed[0]);


        }
        if (view.getId() == R.id.vv_btnReset) {
            cv_robotController.cf_moveMotor(0, 0, 0x00);
            cv_robotController.cf_moveMotor(1, 0, 0x00);
            moveList.clear();
            for(int i = 0 ; i < cv_allButtons.length; i++){
                buttonPressed[i] = false;
                //cv_allButtons[i].setPressed(buttonPressed[i]);
            }


        }
        if (view.getId() == R.id.vv_btnDown) {

            if (buttonPressed[1] == false) {


                moveList.push(cv_robotController.cf_moveMotor(0, -cv_sbAMotor.getProgress(), 0x20));
                moveList.push(cv_robotController.cf_moveMotor(1, -cv_sbBMotor.getProgress(), 0x20));
                buttonPressed[1] = true;
            } else {
                cv_robotController.cf_moveMotor(0, 0, 0x00);
                cv_robotController.cf_moveMotor(1, 0, 0x00);
                moveList.clear();
                buttonPressed[1] = false;

            }
            cv_btnDown.setPressed(buttonPressed[1]);



        }
        if (view.getId() == R.id.vv_btnLeft) {

            if (!buttonPressed[2]) {


                cv_robotController.cf_moveMotor(1, 0, 0x00);
                cv_robotController.cf_moveMotor(0, cv_sbAMotor.getProgress(), 0x20);

                buttonPressed[2] = true;
            } else {
                if (!moveList.isEmpty()) {
                    while (!moveList.isEmpty()) {
                        Log.e("---------->", "-------------->" + moveList.peek()[5]);
                        cv_robotController.cf_moveMotor(moveList.pop());

                    }
                } else {
                    cv_robotController.cf_moveMotor(0, 0, 0x00);
                    cv_robotController.cf_moveMotor(1, 0, 0x00);
                }
                buttonPressed[2] = false;

            }
            cv_btnLeft.setPressed(buttonPressed[2]);


        }
        if (view.getId() == R.id.vv_btnRight) {
            if (!buttonPressed[3]) {
                cv_robotController.cf_moveMotor(0, 0, 0x00);
                cv_robotController.cf_moveMotor(1, cv_sbAMotor.getProgress(), 0x20);

                buttonPressed[3] = true;


            } else {

                if (!moveList.isEmpty()) {
                    while (!moveList.isEmpty()) {
                        Log.e("---------->", "-------------->" + moveList.peek()[5]);
                        cv_robotController.cf_moveMotor(moveList.pop());

                    }
                } else {
                    cv_robotController.cf_moveMotor(0, 0, 0x00);
                    cv_robotController.cf_moveMotor(1, 0, 0x00);
                }
                buttonPressed[3] = false;


            }
            cv_btnRight.setPressed(buttonPressed[3]);


        }
        if (view.getId() == R.id.vv_btnForwardC) {
            if (!buttonPressed[4]) {
                cv_robotController.cf_moveMotor(2, cv_sbPowerC.getProgress(), 0x20);

                buttonPressed[4] = true;
            } else {
                cv_robotController.cf_moveMotor(2, 0, 0x00);

                buttonPressed[4] = false;
            }
            cv_btnForwradC.setPressed(buttonPressed[4]);


        }
        if (view.getId() == R.id.vv_btnBackwardC) {

            if (!buttonPressed[5]) {
                cv_robotController.cf_moveMotor(2, -cv_sbPowerC.getProgress(), 0x20);

                buttonPressed[5] = true;
            } else {
                cv_robotController.cf_moveMotor(2, 0, 0x00);

                buttonPressed[5] = false;
            }

            cv_btnBackwardC.setPressed(buttonPressed[5]);
        }
    }

}
