package cn.edu.fudan.ee.glasscameracontrol;

import cn.edu.fudan.ee.glasscamera.CameraParams;
import cn.edu.fudan.ee.glasscameracontrol.projectGlassScreentoPC.MainFrame;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class GlassCameraControl implements ChangeListener, ActionListener {
    private int PC_LOCAL_PORT = 22222;
    private int ANDROID_PORT = 22222;
    public static ADB mADB;
    private IDevice[] mDevices;
    public static IDevice mDevice;
    static GlassCameraControl glass;
    private Socket socket;
    private ObjectOutputStream transToGlass;// 用于socket通信
    static XMLUI UI;// 创建界面
    FileOperation fileOperation = FileOperation.getInstance();// 文件操作类
    private MainFrame mMainFrame;// 把Glass画面传送到PC
    public static boolean alreadyProjectToPC = false;// 判断是否已开启传送画面
    static String[] mArgs;

    public GlassCameraControl()
    {
        mADB = new ADB();
        mADB.initialize();
        mDevice = null;
        mDevices = mADB.getDevices();
        if(mDevices != null){
            mDevice = mDevices[0];
        }

        // 引入界面
        UI = new XMLUI();

        // slider和textField的事件监听
        for(int i=0; i<UI.slider.length; i++)
        {
            UI.slider[i].addChangeListener(this);
            UI.textField[i].addActionListener(this);
        }
        // 检测设备：google glass
        UI.detectDevice.addActionListener(this);
        // 建立socket通信
        UI.setUpSocket.addActionListener(this);
        // 更新参数
        UI.update.addActionListener(this);
        // 投影Glass画面到PC
        UI.projectGlassScreenToPC.addActionListener(this);
    }

    public static void main(String[] args) {
        mArgs = args;
        glass = new GlassCameraControl();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == UI.slider[0]) {
            UI.textField[0].setText("" + UI.slider[0].getValue());
        }

        if(e.getSource() == UI.slider[1]) {
            UI.textField[1].setText("" + UI.slider[1].getValue());
        }

        if(e.getSource() == UI.slider[2]) {
            UI.textField[2].setText("" + UI.slider[2].getValue());
        }

        if(e.getSource() == UI.slider[3]) {
            UI.textField[3].setText("" + UI.slider[3].getValue());
        }

        if(e.getSource() == UI.slider[4]) {
            UI.textField[4].setText("" + UI.slider[4].getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == UI.textField[0]) {
            UI.slider[0].setValue(new Integer(UI.textField[0].getText()));
        }

        if(e.getSource() == UI.textField[1]) {
            UI.slider[1].setValue(new Integer(UI.textField[1].getText()));
        }

        if(e.getSource() == UI.textField[2]) {
            UI.slider[2].setValue(new Integer(UI.textField[2].getText()));
        }

        if(e.getSource() == UI.textField[3]) {
            UI.slider[3].setValue(new Integer(UI.textField[3].getText()));
        }

        if(e.getSource() == UI.textField[4]) {
            UI.slider[4].setValue(new Integer(UI.textField[4].getText()));
        }

        // 检测设备：google glass
        if(e.getSource() == UI.detectDevice)
        {
            if(glass.mDevice!= null){
                UI.device.setText("Device:"+glass.mDevice.getSerialNumber());
                UI.detectDevice.setEnabled(false);
                UI.setUpSocket.setEnabled(true);
                UI.projectGlassScreenToPC.setEnabled(true);// 已经确保存在设备，所以使能按钮projectGlassScreenToPC
            }
        }

        // 建立socket通信
        if(e.getSource() == UI.setUpSocket)
        {
            try {
                glass.mDevice.createForward(PC_LOCAL_PORT, ANDROID_PORT);

            } catch (TimeoutException e1) {
                e1.printStackTrace();
            } catch (AdbCommandRejectedException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                socket = new Socket("localhost",PC_LOCAL_PORT);
                UI.socketStatus.setText("OK");
                UI.setUpSocket.setEnabled(false);
                UI.update.setEnabled(true);// 使能更新按钮
                transToGlass = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            // Glass相机参数变化，则服务端的界面相应控件显示值也要自动修改，才能保持服务端客户端参数一致
            // 所以这里开启线程进行读相机参数操作，然后修改界面控件显示值，并且记得把从相机获取的参数保存到本地文件
            new Thread(new ReadThread(socket)).start();
        }

        // 更新参数
        if(e.getSource() == UI.update)
        {
            fileOperation.myParams.params1 = UI.slider[0].getValue();
            // 测试参数
            fileOperation.myParams.params2 = UI.slider[1].getValue();
            fileOperation.myParams.params3 = UI.slider[2].getValue();
            try {
                System.out.println(socket.isClosed());
                transToGlass.writeObject(fileOperation.myParams);
                System.out.println("Update params1"+fileOperation.myParams.params1);
                System.out.println("Update params1"+fileOperation.myParams.params2);
                System.out.println("Update params1"+fileOperation.myParams.params3);
                transToGlass.reset();// writeObject后，一定要reset()

            } catch (IOException e1) {
                e1.printStackTrace();
            }

            // 更新完参数后立即保存更新后的参数到文件
            fileOperation.saveParams(fileOperation.myParams);
        }

        // 投影Glass画面到PC
        if(e.getSource() == UI.projectGlassScreenToPC)
        {
            if(alreadyProjectToPC == false)// 尚未存在投射，则点击后，开启投射
            {
                // 投影
                System.out.println("投影Glass画面到PC");
                UI.projectGlassScreenToPC.setText("Disable projecting");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mMainFrame = new MainFrame(mArgs);
                        mMainFrame.setLocation(0, 410);
                        mMainFrame.setVisible(true);
                        mMainFrame.selectDevice();
                    }
                });
                alreadyProjectToPC = true;
            }
            else
            {
                // 断开投影
                System.out.println("断开投影Glass画面到PC");
                UI.projectGlassScreenToPC.setText("Enable projecting");
                mMainFrame.dispose();// 关闭窗口
                mMainFrame = null;
                alreadyProjectToPC = false;
            }
        }
    }

    // 线程进行读
    class ReadThread implements Runnable
    {
        private Socket server;

        public ReadThread(Socket server)
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            try
            {
                ObjectInputStream receiveFromGlass = new ObjectInputStream(server.getInputStream());
                while(true)
                {
                    try
                    {
                        Object obj = receiveFromGlass.readObject();
                        fileOperation.myParams = (CameraParams)obj;
                        System.out.println("Receive params1 from glass:"+fileOperation.myParams.params1);
                        System.out.println("Receive params2 from glass:"+fileOperation.myParams.params2);
                        System.out.println("Receive params3 from glass:"+fileOperation.myParams.params3);
                    }
                    catch(ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }

                    // 修改界面控件显示值
                    UI.slider[0].setValue(fileOperation.myParams.params1);
                    UI.textField[0].setText(""+fileOperation.myParams.params1);
                    UI.slider[1].setValue(fileOperation.myParams.params2);
                    UI.textField[1].setText(""+fileOperation.myParams.params2);
                    UI.slider[2].setValue(fileOperation.myParams.params3);
                    UI.textField[2].setText(""+fileOperation.myParams.params3);

                    // 从glass获取参数后要保存到文件
                    fileOperation.saveParams(fileOperation.myParams);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
