/**************************************************************************************************
  Filename:       DeviceView.java
  Revised:        $Date: 2013-08-30 12:02:37 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27470 $

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

  All rights reserved not granted herein.
  Limited License. 

  Texas Instruments Incorporated grants a world-wide, royalty-free,
  non-exclusive license under copyrights and patents it now or hereafter
  owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
  this software subject to the terms herein.  With respect to the foregoing patent
  license, such license is granted  solely to the extent that any such patent is necessary
  to Utilize the software alone.  The patent license shall not apply to any combinations which
  include this software, other than combinations with devices manufactured by or for TI (�TI Devices�). 
  No hardware patent is licensed hereunder.

  Redistributions must preserve existing copyright notices and reproduce this license (including the
  above copyright notice and the disclaimer and (if applicable) source code license limitations below)
  in the documentation and/or other materials provided with the distribution

  Redistribution and use in binary form, without modification, are permitted provided that the following
  conditions are met:

    * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
      software provided in binary form.
    * any redistribution and use are licensed by TI for use only with TI Devices.
    * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

  If software source code is provided to you, modification and redistribution of the source code are permitted
  provided that the following conditions are met:

    * any redistribution and use of the source code, including any resulting derivative works, are licensed by
      TI for use only with TI Devices.
    * any redistribution and use of any object code compiled from the source code and any resulting derivative
      works, are licensed by TI for use only with TI Devices.

  Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
  promote products derived from this software without specific prior written permission.

  DISCLAIMER.

  THIS SOFTWARE IS PROVIDED BY TI AND TI�S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL TI AND TI�S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package com.example.ti.ble.sensortag;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import android.bluetooth.BluetoothDevice;


import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.ble.sensortag.data.SensortagDbHelper;

import com.example.ti.util.Point3D;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Fragment for Device View
public class DeviceView extends Fragment {

    private final String LOG_TAG = DeviceView.class.getSimpleName();

    private ArrayAdapter<String> mSensorAdapter;

    List<String> sensorReadings = new ArrayList<String>();

  // SensortagDbHelper myDb = new SensortagDbHelper(mActivity.getBaseContext());

    public DeviceView(){

    }
	// Sensor table; the iD corresponds to row number
	private static final int ID_OFFSET = 0;
	private static final int ID_KEY = 0;
	private static final int ID_ACC = 1;
	private static final int ID_MAG = 2;
	private static final int ID_OPT = 2;
	private static final int ID_GYR = 3;
	private static final int ID_OBJ = 4;
	private static final int ID_AMB = 5;
	private static final int ID_HUM = 6;
	private static final int ID_BAR = 7;

	public static DeviceView mInstance = null;

	// GUI
	private TableLayout table;
	public static TextView mAccValue;
	private TextView mMagValue;
	private TextView mLuxValue;
	private TextView mGyrValue;
	private TextView mObjValue;
	public static TextView mAmbValue;
	public static TextView mHumValue;
	public static TextView mBarValue;
	public static ImageView mButton;
    //public String tstmp;
	private ImageView mRelay;
	private TableRow mMagPanel;
	private TableRow mBarPanel;

	// House-keeping
	private DecimalFormat decimal = new DecimalFormat("+0.00;-0.00");
	private DeviceActivity mActivity;
	private static final double PA_PER_METER = 12.0;
	private boolean mIsSensorTag2;
	private boolean mBusy;

    //sensor values to be inserted from deviceview file

    String devid = "123456789";
    String temp = "25";
    String hum = "25";
    String bar = "25";
    String tstmp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
   // String tstmp = "25";



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mInstance = this;
		mActivity = (DeviceActivity) getActivity();
		mIsSensorTag2 = mActivity.isSensorTag2();
        SensortagDbHelper myDb = new SensortagDbHelper(mActivity.getBaseContext());


		// The last two arguments ensure LayoutParams are inflated properly.
		View view;

		if (mIsSensorTag2) {
			view = inflater.inflate(R.layout.services_browser2, container, false);
			table = (TableLayout) view.findViewById(R.id.services_browser_layout2);
			mLuxValue = (TextView) view.findViewById(R.id.luxometerTxt);
			mMagPanel = null;
			mRelay = (ImageView) view.findViewById(R.id.relay);
		} else {
			view = inflater.inflate(R.layout.services_browser, container, false);
			table = (TableLayout) view.findViewById(R.id.services_browser_layout);
			mMagValue = (TextView) view.findViewById(R.id.magnetometerTxt);
			mMagPanel = (TableRow) view.findViewById(R.id.magPanel);
			mRelay = null;
		}

		// UI widgets
		mAccValue = (TextView) view.findViewById(R.id.accelerometerTxt);
		mGyrValue = (TextView) view.findViewById(R.id.gyroscopeTxt);
		mObjValue = (TextView) view.findViewById(R.id.objTemperatureText);
		mAmbValue = (TextView) view.findViewById(R.id.ambientTemperatureTxt);
		mHumValue = (TextView) view.findViewById(R.id.humidityTxt);
		mBarValue = (TextView) view.findViewById(R.id.barometerTxt);
		mButton = (ImageView) view.findViewById(R.id.buttons);
         String tstmp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		// Support for calibration
		mBarPanel = (TableRow) view.findViewById(R.id.barPanel);
		OnClickListener cl = new OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.magPanel:
					mActivity.calibrateMagnetometer();
					break;
				case R.id.barPanel:
					mActivity.calibrateHeight();
					break;
				default:
				}
			}
		};

		if (mMagPanel != null)
			mMagPanel.setOnClickListener(cl);
		mBarPanel.setOnClickListener(cl);

		// Notify activity that UI has been inflated
		mActivity.onViewInflated(view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateVisibility();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * Handle changes in sensor values
	 * */

 	public void onCharacteristicChanged(String uuidStr, byte[] rawValue) throws IOException {


        BluetoothDevice mBtDevice = null;
        int mRssi = 56;

        BleDeviceInfo bld = new BleDeviceInfo(mBtDevice, mRssi );



        Point3D v;
        String msg = " ";
        String dmsg = "";
        String hmsg = " ";
        String bmsg = " ";
        String tmsg = "";


        //String tstmp = "";
        // String tstmp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());


        if (uuidStr.equals(SensorTagGatt.UUID_ACC_DATA.toString())) {
            v = Sensor.ACCELEROMETER.convert(rawValue);
            msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n"
                    + decimal.format(v.z) + "\n";
            mAccValue.setText(msg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_MAG_DATA.toString())) {
            v = Sensor.MAGNETOMETER.convert(rawValue);
            msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n"
                    + decimal.format(v.z) + "\n";
            mMagValue.setText(msg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_OPT_DATA.toString())) {
            v = Sensor.LUXOMETER.convert(rawValue);
            msg = decimal.format(v.x) + "\n";
            mLuxValue.setText(msg);
        }
        if (uuidStr.equals(SensorTagGatt.UUID_GYR_DATA.toString())) {
            v = Sensor.GYROSCOPE.convert(rawValue);
            msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n"
                    + decimal.format(v.z) + "\n";
            mGyrValue.setText(msg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_IRT_DATA.toString())) {
            v = Sensor.IR_TEMPERATURE.convert(rawValue);
            tmsg = decimal.format(v.x) + "\n";
            mAmbValue.setText(tmsg);
            msg = decimal.format(v.y) + "\n";
            mObjValue.setText(msg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_HUM_DATA.toString())) {
            v = Sensor.HUMIDITY.convert(rawValue);
            hmsg = decimal.format(v.x) + "\n";
            mHumValue.setText(hmsg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_BAR_DATA.toString())) {
            v = Sensor.BAROMETER.convert(rawValue);

            double h = (v.x - BarometerCalibrationCoefficients.INSTANCE.heightCalibration)
                    / PA_PER_METER;
            h = (double) Math.round(-h * 10.0) / 10.0;
            bmsg = decimal.format(v.x / 100.0f) + "\n" + h;
            mBarValue.setText(bmsg);
        }

        if (uuidStr.equals(SensorTagGatt.UUID_KEY_DATA.toString())) {
            int keys = rawValue[0];
            SimpleKeysStatus s;
            final int imgBtn;
            s = Sensor.SIMPLE_KEYS.convertKeys((byte) (keys & 3));

            switch (s) {
                case OFF_ON:
                    imgBtn = R.drawable.buttonsoffon;
                    setBusy(true);
                    break;
                case ON_OFF:
                    imgBtn = R.drawable.buttonsonoff;
                    setBusy(true);
                    break;
                case ON_ON:
                    imgBtn = R.drawable.buttonsonon;
                    break;
                default:
                    imgBtn = R.drawable.buttonsoffoff;
                    setBusy(false);
                    break;
            }

            mButton.setImageResource(imgBtn);

            if (mIsSensorTag2) {
                // Only applicable for SensorTag2
                final int imgRelay;

                if ((keys & 4) == 4) {
                    imgRelay = R.drawable.reed_open;
                } else {
                    imgRelay = R.drawable.reed_closed;
                }
                mRelay.setImageResource(imgRelay);
            }

        }
        addData( mActivity.getDeviceName(), tmsg, hmsg, bmsg);

         }

    String insval = "Local DB Inserted";
    String failval = "Failed to Insert in local DB";


  public void addData(String devid, String temp,String hum,String bar) throws IOException {

      String value = "tamil";
      tstmp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
      // Loop through elements.
      for (int i = 0; i < sensorReadings.size(); i++) {
          value = value + sensorReadings.get(i);
          System.out.println(" Element: " + value + " Timestamp: " + tstmp);

      }
      Log.e(LOG_TAG, value);

      SensortagDbHelper myDb = new SensortagDbHelper(mActivity.getBaseContext());
   /*  boolean isInserted = myDb.insertData(devid, temp, hum, bar, tstmp);
      if (isInserted) {
          System.out.println("Status of local DB insert: " + insval);
          Log.e(LOG_TAG, insval);
      } else {
          System.out.println("Status of local DB insert: " + failval);
          Log.e(LOG_TAG, failval);
      }*/


      InputStream is = null;
      //Setting the nameValuePairs
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
      //Adding the string variables inside the nameValuePairs
      nameValuePairs.add(new BasicNameValuePair("row_id", devid));
      nameValuePairs.add(new BasicNameValuePair("device_id", devid));
      nameValuePairs.add(new BasicNameValuePair("temperature", temp));
      nameValuePairs.add(new BasicNameValuePair("humidity", hum));
      nameValuePairs.add(new BasicNameValuePair("barometer", bar));
      nameValuePairs.add(new BasicNameValuePair("timestamp", tstmp));

      //Setting up the connection inside the try catch block
      try {
          //Setting up the default http client
          HttpClient httpClient = new DefaultHttpClient();

          //Setting up the http post method and passing the url in case
          //of online database and the ip address in case of the localhost database.
          //And the php file which serves as the link between the android app
          //and the database.
          HttpPost httpPost = new HttpPost("webportal url goes here");

          //Passing the nameValuePairs inside the httpPost
          httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

          //Getting the response
          HttpResponse response = httpClient.execute(httpPost);

          //Setting up the entity
          HttpEntity entity = response.getEntity();

          //Setting up the content inside an input stream reader
          //Lets define the input stream reader
          is = entity.getContent();

      } catch (ClientProtocolException e) {
          Log.e("ClientProtocol", "LOG_TAG");
          e.printStackTrace();
      } catch (IOException e) {
          Log.e("LOG_TAG", "IOException");
          e.printStackTrace();
      }
      finally {
          myDb.close();
          Log.d("Data entered into mysql", LOG_TAG);
      }
  }

    public void viewAll(){
        SensortagDbHelper myDb = new SensortagDbHelper(mActivity.getBaseContext());
        Cursor res = myDb.getAllData();
        if (res.getCount() ==0){
            // show message
            showMessage("Error", "No Data Found");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()){
            buffer.append("ID:"+ res.getString(0) +"\n");
            buffer.append("DEVID:"+ res.getString(1) +"\n");
            buffer.append("TEMP:"+ res.getString(2) +"\n");
            buffer.append("HUM:"+ res.getString(3) +"\n");
            buffer.append("BAR:"+ res.getString(4) +"\n");
            buffer.append("TIME:"+ res.getString(5) +"\n");

        }
        //show all data
        showMessage("Data", buffer.toString());
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity.getBaseContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }


    void updateVisibility() {
		showItem(ID_KEY, mActivity.isEnabledByPrefs(Sensor.SIMPLE_KEYS));
		showItem(ID_ACC, mActivity.isEnabledByPrefs(Sensor.ACCELEROMETER));
		if (mIsSensorTag2)
			showItem(ID_OPT, mActivity.isEnabledByPrefs(Sensor.LUXOMETER));
		else
			showItem(ID_MAG, mActivity.isEnabledByPrefs(Sensor.MAGNETOMETER));
		showItem(ID_GYR, mActivity.isEnabledByPrefs(Sensor.GYROSCOPE));
		showItem(ID_OBJ, mActivity.isEnabledByPrefs(Sensor.IR_TEMPERATURE));
		showItem(ID_AMB, mActivity.isEnabledByPrefs(Sensor.IR_TEMPERATURE));
		showItem(ID_HUM, mActivity.isEnabledByPrefs(Sensor.HUMIDITY));
		showItem(ID_BAR, mActivity.isEnabledByPrefs(Sensor.BAROMETER));
	}

	private void showItem(int id, boolean visible) {
		View hdr = table.getChildAt(id * 2 + ID_OFFSET);
		View txt = table.getChildAt(id * 2 + ID_OFFSET + 1);
		int vc = visible ? View.VISIBLE : View.GONE;
		hdr.setVisibility(vc);
		txt.setVisibility(vc);
	}


	void setBusy(boolean f) {
		if (f != mBusy)
		{
			mActivity.showBusyIndicator(f);
			mBusy = f;
		}
	}

}
