package cn.edu.fudan.ee.glasscameracontrol;

import cn.edu.fudan.ee.glasscamera.CameraParams;

import java.io.*;

/**
 * Created by hbj on 2014/11/4.
 */
public class FileOperation {
    FileInputStream fi;
    ObjectInputStream oi;
    FileOutputStream fo;
    ObjectOutputStream os;

    // 判断是否存在保存相机参数的序列化(.ser)文件
    // 如果用户是第一次使用此服务端，则是没有此文件的，要创建；
    // 如果已经存在此文件，则从此文件读取保存的相机参数
    public CameraParams createOrLoadParamsFromFile(String folderPath, String fileName)
    {
        CameraParams cameraParams = null;
        String filePath = folderPath + fileName;
        File folder = new File(folderPath);
        if(!folder.exists()&&!folder.isDirectory())
        {
            folder.mkdir();// 若不存在保存相机参数的文件所在的文件夹，则创建（同时也说明了不存在保存相机参数的文件）
            System.out.println("文件夹不存在，已创建");
        }
        else
        {
            System.out.println("文件夹已存在");
        }
        File file = new File(filePath);
        if(!file.exists())
        {
            cameraParams = new CameraParams();// 第一次使用此服务端，相机要初次实例化
            try {
                file.createNewFile();// 创建保存相机参数的文件
                System.out.println("第一次运行此服务端，创建保存相机参数的.ser文件");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            saveParamsToFile(filePath, cameraParams);// 保存相机参数到文件
        }
        else// 打开服务端时自动加载参数，用于在之后初始化控件后设置控件的参数值
        {
            cameraParams = loadParamsFromFile(filePath);
            System.out.println("存在保存相机参数的.ser文件");
        }
        return cameraParams;
    }

    // 从文件加载参数
    public CameraParams loadParamsFromFile(String filePath)
    {
        CameraParams cameraParams = null;
        try {
            fi = new FileInputStream(filePath);
            oi = new ObjectInputStream(fi);
            cameraParams = (CameraParams)oi.readObject();
            oi.close();
            fi.close();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return cameraParams;
    }

    // 保存参数到文件
    public void saveParamsToFile(String filePath, CameraParams cameraParams)
    {
        try {
            fo = new FileOutputStream(filePath);
            os = new ObjectOutputStream(fo);
            os.writeObject(cameraParams);
            os.close();
            fo.close();
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
    }
}
