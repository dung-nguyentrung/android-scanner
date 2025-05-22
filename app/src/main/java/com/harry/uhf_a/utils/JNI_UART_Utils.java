package com.harry.uhf_a.utils;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LinGump on 2018/11/28 0028.
 */

/**
 * JNI(调用java本地接口，实现串口的打开和关闭)
 * 串口有五个重要的参数：串口设备名，波特率，检验位，数据位，停止位
 * 其中检验位一般默认位NONE,数据位一般默认为8，停止位默认为1
 */

public class JNI_UART_Utils {

    /**
     * @param path       串口设备的据对路径
     * @param baudrate   波特率
     * @param flags      校验位
     */
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();

	private static final String TAG = "JNI_UART_Utils";
	private FileDescriptor fileDescriptor;
	private FileInputStream fileInputStream;
	private FileOutputStream fileOutputStream;

	public JNI_UART_Utils(File device, int baudrate, int flags) throws SecurityException,
            IOException {

		Log.i(TAG, "JNI_UART_Utils(): ");


//		这里是最早在UserDebug版本系统中做的代码，方便测试使用；
//		在User版本系统中无su程序，相关权限已经在系统启动时设置。

//		if (!device.canRead() || !device.canWrite()) {
//			try {
//				Log.i(TAG, "!device.canRead() || !device.canWrite()");
//				Process process = Runtime.getRuntime().exec("/system/xbin/su");
//				DataOutputStream os = new DataOutputStream(process.getOutputStream());
//				os.writeBytes("chmod 0777 " + device.getAbsolutePath() + "\n");
//				os.writeBytes("exit" + "\n");
//				没有下面这句话会导致接下来执行waitFor()的时候阻塞在里面出不来
//              os.close();
//
//				if ((process.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
//					throw new SecurityException();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SecurityException();
//			}
//		}

		fileDescriptor = open(device.getAbsolutePath(), baudrate, flags);

		if (fileDescriptor == null) {
			Log.e(TAG, "JNI_UART_Utils(): fileDescriptor is null");
			throw new IOException();
		}

		fileInputStream = new FileInputStream(fileDescriptor);
		fileOutputStream = new FileOutputStream(fileDescriptor);
	}

	public InputStream getInputStream() {
		return fileInputStream;
	}

	public OutputStream getOutputStream() {
		return fileOutputStream;
	}


	static {
		System.loadLibrary("uart_jni");
	}
}
