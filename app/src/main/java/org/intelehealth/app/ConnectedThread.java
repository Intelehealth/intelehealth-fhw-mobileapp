package org.intelehealth.app;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler; // Handler passed from UI activity

    // Constants for Handler message identification
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_ERROR = 2;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream (This is a blocking call)
                bytes = mmInStream.read(buffer);

                // Convert bytes to string format
                String readMessage = new String(buffer, 0, bytes);

                // Send the obtained bytes to the UI Activity via Handler
                Message message = mHandler.obtainMessage(MESSAGE_READ, readMessage);
                message.sendToTarget();

            } catch (IOException e) {
                // Send connection error message to UI
                Message message = mHandler.obtainMessage(MESSAGE_ERROR, "Connection Lost");
                message.sendToTarget();
                break;
            }
        }
    }
    // Call this from the main activity to send data to the remote device
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Call this from the main activity to shut down the connection
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}