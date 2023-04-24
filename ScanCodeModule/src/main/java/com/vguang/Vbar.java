package com.vguang;


import android.util.Log;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public class Vbar {

	public interface Vdll extends Library {

		Vdll INSTANCE = (Vdll) Native.loadLibrary("vbar", Vdll.class);

		//打开信道
		public IntByReference vbar_channel_open(int type, long arg);

		//发送数据
		public int vbar_channel_send(IntByReference vbar_channel, byte[] buffer, int length);

		//接收数据
		public int vbar_channel_recv(IntByReference vbar_channel, byte[] buffer, int size, int milliseconds);

		//关闭信道
		public void vbar_channel_close(IntByReference vbar_channel);


	}


	//初始化设备变量
	IntByReference vbar_channel = null;


	//打开设备
	public boolean vbarOpen() {
		if (vbar_channel == null) {
			vbar_channel = Vbar.Vdll.INSTANCE.vbar_channel_open(1, 1);
		}
		if (vbar_channel != null) {
			System.out.println("open device success");
			return true;
		} else {
			System.out.println("open device fail");
			return false;
		}
	}

	//背光控制
	public int vbarLight(boolean lightstate) {

		byte[] buffer = new byte[1024];
		byte[] buffer1 = new byte[1024];
		int i = 0;
		if (lightstate) {
			buffer[i] = 0x55;
			buffer[++i] = (byte)0xAA;
			buffer[++i] = 0x24;
			buffer[++i] = 0x01;
			buffer[++i] = 0x00;
			buffer[++i] = 0x01;
			buffer[++i] = (byte)0xDB;
			for(int j=7;j<64;j++)
			{
				buffer[j] = 0x00;
			}

			int send = Vbar.Vdll.INSTANCE.vbar_channel_send(vbar_channel, buffer, 64);
			Vbar.Vdll.INSTANCE.vbar_channel_recv(vbar_channel, buffer1, 64, 100);
			return send;
		} else {
			buffer[i] = 0x55;
			buffer[++i] = (byte) 0xAA;
			buffer[++i] = 0x24;
			buffer[++i] = 0x01;
			buffer[++i] = 0x00;
			buffer[++i] = 0x00;
			buffer[++i] = (byte) 0xDA;

			int ss = Vbar.Vdll.INSTANCE.vbar_channel_send(vbar_channel, buffer, 64);
			Vbar.Vdll.INSTANCE.vbar_channel_recv(vbar_channel, buffer1, 64, 200);
			return ss;

		}

	}

	//关闭设备
	public void closeDev() {
		if (vbar_channel != null) {
			Vbar.Vdll.INSTANCE.vbar_channel_close(vbar_channel);
			vbar_channel = null;
			System.out.println("close success");
		}
	}


	//接收结果
	public String getResultsingle() {
		byte[] bufferrecv = new byte[1024];

		Vbar.Vdll.INSTANCE.vbar_channel_recv(vbar_channel, bufferrecv, 1024, 200);
//		System.out.println(bufferrecv[0]);
//		System.out.println(bufferrecv[1]);
//		System.out.println(bufferrecv[4]);
		if (bufferrecv[0] == 85 && bufferrecv[1] == -86 && bufferrecv[3] == 0x00) {
			int datalen = (bufferrecv[4] & 0xff) + ((bufferrecv[5] << 8) & 0xff);  //高位左移位8位 按协议低位在前 高位在后 扫码数据总长度
			if (datalen > 0) {
				byte[] readBuffers = new byte[datalen];
				for (int s1 = 0; s1 < datalen; s1++)
				{
					readBuffers[s1] = bufferrecv[6 + s1];
				}
				String str = new String(readBuffers);
				Log.d("TAG", "getResultsingle: "+str);
				return str;
			}
			else
			{
				return null;
			}
		} else {
			return null;
		}
	}
}



	
	

