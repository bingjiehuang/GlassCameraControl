package cn.edu.fudan.ee.glasscameracontrol;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import javax.swing.*;

import android.hardware.Camera;
import com.android.ddmlib.AdbCommandRejectedException;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;

public class GlassCameraControl {
    public static final int panelNum = 5;
    public static final String UPDATE_CAMERA_SETTINGS = "Update";
    public static int PC_LOCAL_PORT = 22222;
    public static int ANDROID_PORT = 22222;
    private ADB mADB;
    private IDevice[] mDevices;
    private IDevice mDevice;
    static GlassCameraControl glass;
    static Socket socket;
    public GlassCameraControl()
    {
        mADB = new ADB();
        mADB.initialize();
        mDevice = null;
        mDevices = mADB.getDevices();
        if(mDevices != null){
            mDevice = mDevices[0];
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        JPanel topPanel = new JPanel();
        final JLabel device = new JLabel("Device:                                                             ");
        final JButton detectDevice = new JButton("Detect Device");
        final JLabel socketStatus = new JLabel("No Socket");
        final JButton setUpSocket = new JButton("Set Up Socket Connection");
        JPanel[] panel = new JPanel[panelNum];
        JLabel[] label = new JLabel[panelNum];
        JSlider[] slider = new JSlider[panelNum];
        JTextField[] val = new JTextField[panelNum];
        final JButton button = new JButton(UPDATE_CAMERA_SETTINGS);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new GridLayout(panelNum+2,1));

        topPanel.add(device);
        topPanel.add(detectDevice);
        topPanel.add(socketStatus);
        topPanel.add(setUpSocket);
        contentPane.add(topPanel);
        for(int i=0;i<panel.length;i++){
            panel[i] = new JPanel();
            label[i] = new JLabel("text"+i);
            slider[i] = new JSlider();
            val[i] = new JTextField("0");
            panel[i].add(label[i]);
            panel[i].add(slider[i]);
            panel[i].add(val[i]);
            contentPane.add(panel[i]);
        }

        contentPane.add(button);
        frame.pack();
        frame.setVisible(true);
        setUpSocket.setEnabled(false);
        button.setEnabled(false);
        detectDevice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                glass = new GlassCameraControl();
                if(glass.mDevice!= null){
                    setUpSocket.setEnabled(true);

                    detectDevice.setEnabled(false);
                    device.setText("Device:"+glass.mDevice.getSerialNumber());
                }
            }
        });
        setUpSocket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    glass.mDevice.createForward(PC_LOCAL_PORT, ANDROID_PORT );

                } catch (TimeoutException e1) {
                    e1.printStackTrace();
                } catch (AdbCommandRejectedException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket = new Socket("localhost",PC_LOCAL_PORT);
                    button.setEnabled(true);
                    socketStatus.setText("OK");
                    setUpSocket.setEnabled(false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

}