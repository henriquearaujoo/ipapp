/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android_serialport_api.SerialPort;

public class SerialPortActivity {

	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;

	public SerialPortActivity(){
		try {
			mSerialPort = new SerialPort(new File("/dev/ttySAC1"), 38400, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread
			mReadThread = new ReadThread();
			mReadThread.start();*/
		} catch (SecurityException e) {
		} catch (IOException e) {
		} catch (InvalidParameterException e) {
		}
	}

	public void print(String data){
		try {
			if (mOutputStream != null) {
				mOutputStream.write(data.getBytes());
				mOutputStream.write('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		mSerialPort = null;
	}
}
