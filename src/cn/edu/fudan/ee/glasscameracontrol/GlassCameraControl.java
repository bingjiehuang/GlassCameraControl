package cn.edu.fudan.ee.glasscameracontrol;
import android.hardware.Camera;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
class CameraParams{
    public int params1;
    public int params2;
}
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
    static CameraParams cameraParams = new CameraParams();
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
        final JSlider[] slider = new JSlider[panelNum];
        final JTextField[] val = new JTextField[panelNum];
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
            val[i] = new JTextField(3);
            panel[i].add(label[i]);
            panel[i].add(slider[i]);
            panel[i].add(val[i]);
            contentPane.add(panel[i]);
        }

        contentPane.add(button);
        frame.pack();
        frame.setVisible(true);

        slider[0].addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                val[0].setText("" + slider[0].getValue());
            }
        });
        val[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slider[0].setValue(new Integer(val[0].getText()));
            }
        });

        detectDevice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                glass = new GlassCameraControl();
                if(glass.mDevice!= null){
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
                    socketStatus.setText("OK");

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cameraParams.params1 = new Integer(val[0].getText());
                try {
                    ObjectOutputStream transToGlass = new ObjectOutputStream(socket.getOutputStream());
                    transToGlass.writeObject(cameraParams);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

    }


}
