package cn.edu.fudan.ee.glasscameracontrol;

import cn.edu.fudan.ee.glasscamera.CameraParams;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class XMLUI{

    public JFrame jFrame;
    public Container contentPane;
    public JLabel device;
    public JButton detectDevice;
    public JLabel socketStatus;
    public JButton setUpSocket;
    public JSlider[] slider = new JSlider[5];
    public JTextField[] textField = new JTextField[5];
    public JButton update;
    static CameraParams cameraParams;
    String folderPath;// 保存相机参数的文件夹路径(在本工程路径下)
    String fileName;// 保存相机参数的文件名
    FileOperation fileOperation = new FileOperation();// 文件操作类

    public static void main(String args[])
    {
        new XMLUI();
    }

    public XMLUI()
    {
        // 判断是否存在保存相机参数的序列化(.ser)文件
        // 如果用户是第一次使用此服务端，则是没有此文件的，要创建；
        // 如果已经存在此文件，则从此文件读取保存的相机参数
        try {
            folderPath = new File("").getCanonicalPath() + "\\savedInitialParams\\";
            fileName = "savedInitialParams.ser";
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        cameraParams = fileOperation.createOrLoadParamsFromFile(folderPath, fileName);


        // 创建Frame
        jFrame = createJFrame(false, true, false, true, true, 0, 0, 650, 400, "GlassCameraControl");
        contentPane = jFrame.getContentPane();
        // 添加控件
        device = createJLabel("Device:", 20, 22, 200, 30);
        detectDevice = createJButton(true, "Detect Device", 200, 20, 120, 30);
        socketStatus = createJLabel("No Socket", 340, 22, 60, 30);
        setUpSocket = createJButton(false, "Set Up Socket Connection", 420, 20, 200, 30);
        String[] str1 = new String[]{"Zoom", "WhiteBalance", "Exposure Compensation", "effect", "effect"};
        int left = 20, top = 80, interval = 10, width1 = 80, height1 = 27, width2 = 200, height2 = 30, width3 = 30, height3 = 30;
        int[] min = new int[]{0, 0, -30, 0, 0};
        int[] max = new int[]{60, 10, 30, 100, 100};
        int[] value = new int[]{cameraParams.params1, cameraParams.params2, cameraParams.params3, 50, 50};
        for(int i=0; i<slider.length; i++)
        {
            createJLabel(str1[i], left, top+i*height1+i*interval, width1, height1);
            slider[i] = createJSlider(min[i], max[i], value[i], left+width1+interval, top+i*height2+i*interval, width2, height2);
            textField[i] = createJTextField(Integer.toString(value[i]),left+width1+width2+2*interval, top+i*height3+i*interval, width3, height3);
        }
        update = createJButton(false, "Update", 1, 330, 648, 40);

        jFrame.repaint();
    }


    // createJFrame
    public JFrame createJFrame(boolean center, boolean visible, boolean resizable, boolean alwaysOnTop, boolean main, int left, int top, int width, int height, String name)
    {

        JFrame jFrame = new JFrame(name);
        JPanel jContentPane = new JPanel();
        jContentPane.setLayout(null);
        jFrame.setContentPane(jContentPane);

        if(center)
        {
            Toolkit theKit = jFrame.getToolkit();
            Dimension wndSize = theKit.getScreenSize();
            jFrame.setBounds(wndSize.width/2-width/2,wndSize.height/2-height/2,width,height);//窗口在屏幕中心显示
        }
        else
        {
            jFrame.setBounds(left,top,width,height);
        }
        jFrame.setVisible(visible);
        jFrame.setResizable(resizable);
        jFrame.setAlwaysOnTop(alwaysOnTop);

        if(main)
        {
            jFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent ev) {
                    System.exit(0);
                }
            });
        }

        return jFrame;
    }

    // createJPanel
//    public JPanel createJPanel(boolean visible, int left, int top, int width, int height) {
//        JPanel jPanel = new JPanel();
//        jPanel.setLayout(null);
//        jPanel.setVisible(visible);
//        jPanel.setBounds(left,top,width,height);
//        jPanel.setBackground(Color.LIGHT_GRAY);
//        contentPane.add(jPanel, null);
//        return jPanel;
//    }

    // createJLabel
    public JLabel createJLabel(String name, int left, int top, int width, int height)
    {
        JLabel jLabel = new JLabel(name);
        jLabel.setLayout(null);
        jLabel.setBounds(left,top,width,height);
        jLabel.setBackground(Color.BLUE);
        contentPane.add(jLabel, null);
        return jLabel;
    }

    // createJButton
    public JButton createJButton(boolean enable, String name, int left, int top, int width, int height)
    {
        JButton jButton = new JButton(name);
        jButton.setBounds(left,top,width,height);
        jButton.setEnabled(enable);
        contentPane.add(jButton, null);
        return jButton;
    }

    // createJSlider
    public JSlider createJSlider(int min, int max, int value, int left, int top, int width, int height)
    {
        JSlider jSlider = new JSlider();
        jSlider.setMinimum(min);
        jSlider.setMaximum(max);
        jSlider.setValue(value);
        jSlider.setMajorTickSpacing(max-min);
        jSlider.setPaintLabels(true);
        jSlider.setBounds(left,top,width,height);
        contentPane.add(jSlider, null);
        return jSlider;
    }

    // createJTextField
    public JTextField createJTextField(String text, int left, int top, int width, int height)
    {
        JTextField jTextField = new JTextField();
        jTextField.setText(text);
        jTextField.setBounds(left,top,width,height);
        contentPane.add(jTextField, null);
        return jTextField;
    }

}