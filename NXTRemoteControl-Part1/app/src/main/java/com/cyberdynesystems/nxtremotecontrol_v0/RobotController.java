package com.cyberdynesystems.nxtremotecontrol_v0;

import android.app.Application;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by geoffduong on 11/9/16.
 */

public class RobotController extends Application {
    private static RobotController singleton;
    final String CV_ROBOTNAME = "NXT06";

    // BT Variables
    private BluetoothAdapter cv_btInterface;
    private Set<BluetoothDevice> cv_pairedDevices;
    private BluetoothSocket cv_socket;
    private InputStream cv_is;
    private OutputStream cv_os;

    public void setCv_bConnected(boolean cv_bConnected) {
        this.cv_bConnected = cv_bConnected;
    }

    private boolean cv_bConnected;

    private RobotController() {
        cv_btInterface = BluetoothAdapter.getDefaultAdapter();
        cv_pairedDevices = cv_btInterface.getBondedDevices();
        cv_bConnected = false;
    }

    public synchronized static RobotController getRobotController(Context context) {
        if(null == singleton) {
            singleton = new RobotController();
        }
        return singleton;
    }

    // page 390
    public synchronized boolean cf_findRobot(final Context context) {
        try {
            final ArrayList<BluetoothDevice> lv_arr = new ArrayList<BluetoothDevice>();
            lv_arr.addAll(cv_pairedDevices);

            MyListAdapter lv_adapter = new MyListAdapter(context, lv_arr, "main");

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.list);

            ListView lv_listView = (ListView) dialog.findViewById(R.id.vv_listView);
            lv_listView.setAdapter(lv_adapter);

            lv_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    cf_connectToRobot(lv_arr.get(position));
                    dialog.cancel();
                }
            });
            dialog.setCancelable(true);
            dialog.setTitle("");
            dialog.show();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public synchronized String cf_connectToRobot(BluetoothDevice bd) {
        try {
            cv_socket = bd.createRfcommSocketToServiceRecord(bd.getUuids()[0].getUuid());
            cv_socket.connect();
            cv_bConnected = true;
            return "Connect to " + bd.getName() + " at " + bd.getAddress();
        }
        catch (Exception e) {
            return "Error interacting with remote device [" + e.getMessage() + "]";
        }
    }

    //monitors the bluetooth connection
    public synchronized BroadcastReceiver cf_getBTMonitor() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                    try {
                        cf_handleConnected();
                        Log.d("cf_getBTMonitor", "android.bluetooth.device.action.ACL_CONNECTED");
                    }
                    catch (Exception e) {
                        cf_handleDisconnected();
                        cv_is = null;
                        cv_os = null;
                        Log.d("cf_getBTMonitor", e.getStackTrace().toString());
                    }
                }
                if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                    cf_handleDisconnected();
                    Log.d("cf_getBTMonitor", "android.bluetooth.device.action.ACL_DISCONNECTED");
                }
            }
        };
    }

    public synchronized void cf_handleConnected() {
        try {
            cv_is = cv_socket.getInputStream();
            cv_os = cv_socket.getOutputStream();
            cv_bConnected = true;
        }
        catch (Exception e) {
            cf_handleDisconnected();
            cv_is = null;
            cv_os = null;
        }
    }

    public synchronized void cf_handleDisconnected() {
        try {
            cv_socket.close();
            cv_is.close();
            cv_os.close();
            cv_bConnected = false;
        }
        catch(Exception e) {

        }
    }

    public byte[] cf_moveMotor(int motor,int speed, int state) {
        try {
            byte[] buffer = new byte[15];

            buffer[0] = (byte) (15-2);			//length lsb
            buffer[1] = 0;						// length msb
            buffer[2] =  0;						// direct command (with response)
            buffer[3] = 0x04;					// set output state
            buffer[4] = (byte) motor;			// output 1 (motor B)
            buffer[5] = (byte) speed;			// power
            buffer[6] = 1 + 2;					// motor on + brake between PWM
            buffer[7] = 0;						// regulation
            buffer[8] = 0;						// turn ration??
            buffer[9] = (byte) state; //0x20;					// run state
            buffer[10] = 0;
            buffer[11] = 0;
            buffer[12] = 0;
            buffer[13] = 0;
            buffer[14] = 0;

            cv_os.write(buffer);
            cv_os.flush();
            return buffer;
        }
        catch (Exception e) {
            Log.d("cf_moveMotor1", e.getStackTrace().toString());
        }
        return null;
    }

    public byte[] cf_moveMotor(byte[] move) {
        try {
            cv_os.write(move);
            cv_os.flush();
            return move;
        }
        catch (Exception e) {
            Log.d("cf_moveMotor2", e.getStackTrace().toString());
        }
        return null;
    }

    public double cf_battery() {
        double tempBattery;
        double battery = 9000.0;
        try {
            byte[] buffer = new byte[4];
            buffer[0] = 0x02;			//length lsb
            buffer[1] = 0x00;						// length msb
            buffer[2] =  0x00;						// direct command (with response)
            buffer[3] = 0x0b;					// set output state

            cv_os.write(buffer);
            cv_os.flush();

            boolean flag1 = false;
            boolean flag2 = false;

            while (flag1 == false || flag2 == false) {
                cv_is = cv_socket.getInputStream();
                int test = cv_is.read();
                if (test == 5) {
                    flag1 = true;
                }
                if (flag1 == true && test == 0) {
                    flag2 = true;
                    break;
                }
            }

            int[] inBuffer = new int[5];
            for (int i = 0; i < inBuffer.length; i++) {
                inBuffer[i] = cv_is.read();
                //System.out.println(inBuffer[i]);
            }

            byte array[] = new byte[2];
            array[0] = (byte) inBuffer[3];
            array[1] = (byte) inBuffer[4];
            int nValue = array[0] + (array[1] << 8);

            //double newBattery = ((double)inBuffer[3] * (double)inBuffer[4]) / 9000.0;
            //if (nValue <= battery) {
                battery =  nValue;
            //}

            //SeekBar cv_skbBattery = (SeekBar) findViewById(R.id.BatteryseekBar);
            //cv_skbBattery.setProgress((int) tempBatt);
        }
        catch (Exception e) {
            Log.d("cf_battery", e.getStackTrace().toString());
        }

        tempBattery = Math.floor((battery / 9000.0) * 100);
        return tempBattery;
    }

    //sets the sensor to be polled
    //int sensorType: see below for values for valid sensor types
    //int inputPort: 0x00-0x03 depending on which port the sensor is connected to
    public void cf_setInputMode(int sensorType, int inputPort) {
        try {
            byte[] buffer = new byte[7];
            buffer[0] = 0x05;	            // length lsb
            buffer[1] = 0x00;			    // length msb
            buffer[2] = (byte) 0x80;	    // direct command (without response)
            buffer[3] = 0x05;		        // set input mode
            buffer[4] = (byte)inputPort;    // input port 0x00-0x03
            buffer[5] = (byte)sensorType;   // sensor type(enumerated)
            if(sensorType == 0x01)
                buffer[6] = (byte) 0x40;    // touch sensor mode(enumerated) TRANSITIONCNTMODE
            else
                buffer[6] = (byte) 0x80;    // sensor mode(enumerated) PCTFULLSCALEMODE

            cv_os.write(buffer);
            cv_os.flush();

            /*
            sensor type
            NO_SENSOR: 0x00
            SWITCH: 0x01 <--- TOUCH
            TEMPERATURE: 0x02
            REFLECTION: 0x03
            ANGLE: 0x04
            LIGHT_ACTIVE: 0x05
            LIGHT_INACTIVE: 0x06
            SOUND_DB: 0x07
            SOUND_DBA: 0x08
            CUSTOM: 0x09
            LOWSPEED: 0x0A
            LOWSPEED_9V: 0x0B
            NO_OF_SENSOR_TYPES: 0x0C
             */

            /*
            sensor mode
            RAWMODE: 0x00
            BOOLEANMODE: 0x20
            TRANSITIONCNTMODE: 0x40
            PERIODCOUNTERMODE: 0x60
            PCTFULLSCALEMODE: 0x80
            CELSIUSMODE: 0xA0
            FAHRENHEITMODE: 0xC0
            ANGLESTEPMODE: 0xE0
            SLOPEMASK: 0x1F
            MODEMASK: 0xE0
             */

            /*
            return package
            byte 0: 0x02
            byte 1: 0x05
            byte 2: status byte
             */
        }
        catch(Exception e) {
            Log.d("cf_setSensor", e.getStackTrace().toString());
        }
    }

    //polls a currently active sensor and returns an integer value
    //int inputPort: 0x00-0x03 depending on which port the sensor is connected to
    public int cf_getInputValues(int inputPort) {
        try {
            byte[] buffer = new byte[5];

            buffer[0] = 0x03;	// length lsb
            buffer[1] = 0x00;			// length msb
            buffer[2] =  0x00;			// direct command (with response)
            buffer[3] = 0x07;			// get output state
            buffer[4] = (byte)inputPort;   // input port

            cv_os.write(buffer);
            cv_os.flush();

            int[] inBuffer = new int[18];
            for (int i = 0; i < inBuffer.length; i++) {
                inBuffer[i] = cv_is.read();
            }

            //index 14 should contain the value we want
            return inBuffer[14];
            /*
            return package
            byte 0: 0x02
            byte 1: 0x07
            byte 2: status byte
            byte 3: input port
            byte 4: valid? boolean true if valid data
            byte 5: calibrated?
            byte 6: sensor type
            byte 7: sensor mode
            byte 8-9: raw a/d value
            byte 10-11: normalized a/d value
            byte 12-13: scaled value <-- values we want for display
            byte 14-15: calibrated value
             */
        }
        catch(Exception e) {
            Log.d("cf_getSensorState", e.getStackTrace().toString());
        }
        return 0;
    }

    public int cf_getOutputValues(int outputPort) {
        try {
            byte[] buffer = new byte[5];

            buffer[0] = 0x03;	// length lsb
            buffer[1] = 0x00;			// length msb
            buffer[2] =  0x00;			// direct command (with response)
            buffer[3] = 0x06;			// get output state
            buffer[4] = (byte)outputPort;   // input port

            cv_os.write(buffer);
            cv_os.flush();

            int[] inBuffer = new int[27];
            for (int i = 0; i < inBuffer.length; i++) {
                inBuffer[i] = cv_is.read();
                System.out.println(inBuffer[i]);
            }

            return (inBuffer[19] + inBuffer[20] + inBuffer[21] + inBuffer[22]);
        }
        catch(Exception e) {

        }
        return 0;
    }

    public boolean getConnectionStatus()
    {
        return cv_bConnected;
    }
    public String getRobotName() { return CV_ROBOTNAME; }

}
