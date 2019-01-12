package com.serenegiant.ZX333;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.provider.DocumentFile;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.serenegiant.ZX333.UvcDebug.DEBUG;

/**
 * Created by Administrator on 2016/9/22.
 */
public class AvaliableStorage {
    private static final String TAG = "StorageInfo";
    public static String Spath = "none";
    public static String TEST = "none";
    private WeakReference<MainActivity> mWeakParent;
    private final boolean debugME = true;

    public static ArrayList testL;

    private String[] pathList={
            "/mnt/usb_storage/USB_DISK0/udisk0",
            "/mnt/usb_storage/USB_DISK1/udisk0",
            "/mnt/usb_storage/USB_DISK2/udisk0",
            "/mnt/usb_storage/USB_DISK3/udisk0",
            "/mnt/usb_storage/USB_DISK4/udisk0",
            "/mnt/usb_storage/USB_DISK5/udisk0",
            "/mnt/usb_storage/USB_DISK6/udisk0",
            "/mnt/usb_storage/USB_DISK7/udisk0",
            "/mnt/usb_storage/USB_DISK8/udisk0",
            "/mnt/usb_storage/USB_DISK9/udisk0",
            "/mnt/usb_storage/USB_DISK10/udisk0",
            "/mnt/usb_storage/USB_DISK11/udisk0",
            "/mnt/usb_storage/USB_DISK12/udisk0",
            "/mnt/usb_storage/USB_DISK13/udisk0",
            "/mnt/usb_storage/USB_DISK14/udisk0",
            "/mnt/usb_storage/USB_DISK15/udisk0",
            "/mnt/usb_storage/USB_DISK16/udisk0",
            "/mnt/usb_storage/USB_DISK17/udisk0",
            "/mnt/usb_storage/USB_DISK18/udisk0",
            "/mnt/usb_storage/USB_DISK19/udisk0",
            "/mnt/usb_storage/USB_DISK1",
            "/mnt/usb_storage/USB_DISK2",
            "/mnt/usb_storage/USB_DISK3",
            "/mnt/usb_storage/USB_DISK4",
            "/mnt/usb_storage/USB_DISK5",
            "/mnt/usb_storage/USB_DISK6",
            "/mnt/usb_storage/USB_DISK7",
            "/mnt/usb_storage/USB_DISK8",
            "/mnt/usb_storage/USB_DISK9",
            "/mnt/usb_storage/USB_DISK10",
            "/mnt/usb_storage/USB_DISK11",
            "/mnt/usb_storage/USB_DISK12",
            "/mnt/usb_storage/USB_DISK13",
            "/mnt/usb_storage/USB_DISK14",
            "/mnt/usb_storage/USB_DISK15",
            "/mnt/usb_storage/USB_DISK16",
            "/mnt/usb_storage/USB_DISK17",
            "/mnt/usb_storage/USB_DISK18",
            "/mnt/usb_storage/USB_DISK19",
            "/mnt/usb_storage/USB_DISK20",
            "/mnt/usb_storage1",
            "/storage/uhost1",
            "/storage/usb1-1",
            "/mnt/storage/usb1"};
    private static final int CNT = 100;
    //  private String setPh = "none";
    private class StorageInfo {
        public String path;
        public String state;
        public boolean isRemoveable;

        public StorageInfo(String path) {
            this.path = path;
        }

        public boolean isMounted() {
            return "mounted".equals(state);
        }
    }

    public void AvaliableStorage (final MainActivity parent){
        //mWeakParent = new WeakReference<MainActivity>(parent);
        //final MainActivity parent = mWeakParent.get();
    }

    public HashMap<String,Integer> getAvaliableStorage(Context context) {
        File fpicture,frecord;
        String path_t;
        ArrayList storagges = new ArrayList();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        HashMap<String,Integer> extPath = new HashMap<String,Integer>();
        int sizeP;

        testL = new ArrayList();

        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
            if (invokes != null) {
                StorageInfo info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath", new Class[0]);
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    if (UvcDebug.DEBUG && debugME) Log.v(TAG, "paht[" + i + "]" + " " + path);
                    info = new StorageInfo(path);
                    File file = new File(info.path);
                    if ((file.exists()) && (file.isDirectory()) && (file.canWrite())) {
                        Method isRemovable = obj.getClass().getMethod("isRemovable", new Class[0]);
                        String state = null;
                        try {
                            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                            state = (String) getVolumeState.invoke(storageManager, info.path);
                            info.state = state;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (info.isMounted()) {
                            info.isRemoveable = ((Boolean) isRemovable.invoke(obj, new Object[0])).booleanValue();

                            if (UvcDebug.DEBUG && debugME) Log.v(TAG, "...path " + info.path + " state " + info.state + " remove = " + info.isRemoveable + " remove2 " + isRemovable.invoke(obj));
                            storagges.add(info);
                        }
                    }
                }
            }
            for(int ti = 0; ti<storagges.size() ; ti++){
                StorageInfo tinfo = (StorageInfo)storagges.get(ti);
/*                if(tinfo.path.contains("sdcard1")) {
                    //UVCService.Spath = tinfo.path;//"/storage/sdcard1";
                    Spath = tinfo.path;
                    if(UvcDebug.DEBUG) Log.d(TAG, "Spath= " + Spath);
                    return storagges;
                }*/
                TEST = tinfo.path + " " + tinfo.state + " " + tinfo.isRemoveable;
                testL.add(TEST);
                if(tinfo.isRemoveable == true) {
                    fpicture = new File(tinfo.path+"/ZXCam","picture");
                    frecord = new File(tinfo.path+"/ZXCam","record");
                    if (UvcDebug.DEBUG && debugME) Log.d(TAG, "Spath= " + tinfo.path+"/ZXCam");

                    sizeP = dfPath(tinfo.path);
                    extPath.put(tinfo.path, sizeP);

                  /*  if (fpicture.exists() && frecord.exists()) {
                       // Spath = tinfo.path+"/ZXCam";
                        Spath = tinfo.path;
                        if (UvcDebug.DEBUG && debugME) Log.d(TAG, "Spath= " + Spath);
                        return storagges;
                    }*/
                }
            }
            //check fixed path
            // File fpicture = new File("/mnt/usb_storage/USB_DISK0/udisk0/ZXCam","picture");
            // File frecord = new File("/mnt/usb_storage/USB_DISK0/udisk0/ZXCam","record");
            for(String pth:pathList){
                fpicture = new File(pth+"/ZXCam","picture");
                frecord = new File(pth+"/ZXCam","record");

                if(new File(pth).exists()) {
                    sizeP = dfPath(pth);
                    extPath.put(pth, sizeP);
                }
               /* if (fpicture.exists() && frecord.exists()) {
                    Spath = pth;
                    if (UvcDebug.DEBUG && debugME) Log.d(TAG, "Spath= " + Spath);
                    return storagges;
                }*/
            }
            for(int i=0;i<20;i++){
                path_t = "/mnt/usb_storage/USB_DISK"+ Integer.toString(i) +"/udisk0";
                fpicture = new File(path_t+"/ZXCam","picture");
                frecord = new File(path_t+"/ZXCam","record");

                if(new File(path_t).exists()) {
                    sizeP = dfPath(path_t);
                    extPath.put(path_t, sizeP);
                }

            /*    if (fpicture.exists() && frecord.exists()) {
                    Spath = path_t;
                    if (UvcDebug.DEBUG && debugME) Log.d(TAG, "Spath= " + Spath);
                    return storagges;
                }*/

                path_t = "/mnt/usb_storage/USB_DISK" + Integer.toString(i);
                fpicture = new File(path_t+"/ZXCam","picture");
                frecord = new File(path_t+"/ZXCam","record");
                if(new File(path_t).exists()) {
                    sizeP = dfPath(path_t);
                    extPath.put(path_t, sizeP);
                }
              /*  if (fpicture.exists() && frecord.exists()) {
                    Spath = path_t;
                    if (UvcDebug.DEBUG && debugME) Log.d(TAG, "Spath= " + Spath);
                    return storagges;      //wzz 屏蔽2016-12-29 10:03:33
                }*/
            }
            //
            //Spath = "none";     //wzz 2016年12月29日10:04:33
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        storagges.trimToSize();

        return extPath;
        //return storagges;
    }

    private  StringBuffer fileInfo;
    // dir info
    private StringBuffer dirInfo;
    // child file info
    private File[] fm;

    public void searchFolder(){
        // 查找目录
        File dir = new File("/mnt");
        // 要查找的关键字
        String key = "ZXCam";
        // 打印文件夹信息
        printAllInfo(dir);
        System.out.println("/nsearch key : " + key);
        System.out.println("search results : ");
        for (File file : fm) {
            if (file.getName().indexOf(key) >= 0) {
                if (file.isFile()) {
                    System.out.println("file : " + file.getName() + "   ");
                } else if (file.isDirectory()) {
                    System.out.println("dir : " + file.getName() + "   ");
                }
            }
        }
    }
    /**
     * print info of this directory
     *
     * @param dir
     */
    public void printAllInfo(File dir) {
        fileInfo = new StringBuffer();
        dirInfo = new StringBuffer();
        fm = dir.listFiles();
        for (File file : fm) {
            if (file.isFile()) {
                fileInfo.append(file.getName() + "    ");
            } else if (file.isDirectory()) {
                dirInfo.append(file.getName() + "    ");
            }
        }
        System.out.println(dir.getAbsolutePath());
        System.out.println("contains : ");
        System.out.println("file ---> " + fileInfo);
        System.out.println("dir  ---> " + dirInfo);
    }

    public void delOldfile(Context context, String pathVid){
        pathVid = pathVid.substring(0,pathVid.length() - 1);//"/sdcard/Movies/ZXcam";
        if (UvcDebug.DEBUG && debugME) Log.v(TAG, " delOldfile " + pathVid);
        File fileold = new File(pathVid);
        long tmpStr = 990000000000L;
        int delY = 0;
        try{
            File[] files=fileold.listFiles();
            if (UvcDebug.DEBUG && debugME) Log.v(TAG, "files.length " + files.length + " size " + getAvailableSize(Environment.getExternalStorageDirectory().toString()) );
            while (dfPath(pathVid) < 300) //left smaller than 300M, delete one, 3min video need 200M ROM
            //while(files.length>20)
            {
                for(int j=0;j<files.length;j++)
                {
                    if(!files[j].isDirectory())
                    {
                        String str = files[j].getName();
                        str = str.substring(2,str.indexOf("_")) + str.substring(str.indexOf("_")+1,str.indexOf("."));
                        if (UvcDebug.DEBUG && debugME) Log.v(TAG, " " + files[j].getName() + " " + str);
                        long lstr = Long.parseLong(str);
                        if(lstr < tmpStr){
                            tmpStr = lstr;
                            delY = j;
                        }
                    }
                    else{
                        //this.search(files[j]);
                    }
                }

                if (UvcDebug.DEBUG && debugME) Log.v(TAG, "delete " + files[delY].getPath() + " len " + files.length);
                files[delY].delete();

                files[delY] = files[files.length-1];
                files = Arrays.copyOf(files, files.length-1);
            }
         /*   while(files.length>20){ // actually , should delete according to the space left
                    if (UvcDebug.DEBUG && debugME) Log.v(TAG, "delete " + files[delY].getPath() + " len " + files.length);
                    files[delY].delete();
            }*/
        }
        catch(Exception e) {}
    }

    public void delOldfile_SD(DocumentFile zxDir){ //do it with arraylist
        long tmpStr = 990000000000L;
        DocumentFile file_tmp = null;
        if (UvcDebug.DEBUG && debugME) Log.v(TAG, "delOldfile_SD  length = " + zxDir.length() + " name " + zxDir.getName());
        try{
            for (DocumentFile file : zxDir.listFiles()) {
                if (UvcDebug.DEBUG && debugME) Log.v(TAG, "Found file " + file.getName() + " with size " + file.length());
                if(file.isFile())
                {
                    String str = file.getName();
                    if(str.length() < 16) continue; //skip file with short name
                   // str = str.substring(2,str.indexOf("_")) + str.substring(str.indexOf("_")+1,str.indexOf("."));
                    str = str.substring(2,8) + str.substring(9,15);
                    if (UvcDebug.DEBUG && debugME) Log.v(TAG, " " + file.getName() + " " + str);
                    long lstr = Long.parseLong(str);
                    if(lstr < tmpStr){
                        tmpStr = lstr;
                        file_tmp = file;
                    }
                }
                else{
                    //this.search(files[j]);
                }
            }
            file_tmp.delete();
        }
        catch(Exception e) {}

        List list = new ArrayList();
        Collections.sort(list);
    }

    public String findOldfile(){
        String pathVid = "/sdcard/Movies/ZXcam";
        String result = null;
        File fileold = new File(pathVid);
        long tmpStr = 990000000000L;
        int delY = 0;
        try{
            File[] files=fileold.listFiles();
            if(files.length > 1) {
                 for (int j = 0; j < files.length; j++) {
                     if (!files[j].isDirectory()) {
                         String str = files[j].getName();
                         str = str.substring(2, str.indexOf("_")) + str.substring(str.indexOf("_") + 1, str.indexOf("."));
                         if (UvcDebug.DEBUG && debugME) Log.v(TAG, " " + files[j].getName() + " " + str);
                         long lstr = Long.parseLong(str);
                         if (lstr < tmpStr) {
                             tmpStr = lstr;
                             delY = j;
                         }
                     } else {
                         //this.search(files[j]);
                     }
                 }
                 result = files[delY].getName();
             }else if(files.length == 1){
                 if (!files[0].isDirectory())
                     result = files[0].getName();
             }
        }
        catch(Exception e) {}
        return result;
    }

    public static String getAvailableSize(String path)
    {
        StatFs fileStats = new StatFs(path);
        fileStats.restat(path);
        return String.valueOf(fileStats.getAvailableBlocks() * fileStats.getBlockSize()); // 注意与fileStats.getFreeBlocks()的区别
//	return getPrintSize((long) fileStats.getFreeBlocks() * fileStats.getFreeBlocks()); // 注意与fileStats.getFreeBlocks()的区别
    }
    public long readSystem() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        if (UvcDebug.DEBUG && debugME)Log.d(TAG, " root-path " + root.getPath());
        long blockSize = sf.getBlockSizeLong();
        long blockCount = sf.getBlockCountLong();
        long availCount = sf.getAvailableBlocksLong();
        if (UvcDebug.DEBUG && debugME)Log.d(TAG, "blocksize:"+ blockSize+" ,blockcount:"+ blockCount+" , :"+blockSize*blockCount/1024/1024+"M");
        if (UvcDebug.DEBUG && debugME)Log.d(TAG, "availCount:"+ availCount+" ,:"+ availCount*blockSize/1024/1024+"M");

        //String totalText = formatSize(blockSize * blockCount);
        //String availableText = formatSize(blockSize * availCount);
        //if (UvcDebug.DEBUG && debugME) Log.d(TAG_THREAD, "total: " + totalText + " ,avai " + availableText);

        return availCount*blockSize/1024/1024;
    }
    public void readSDCard() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, " sd-path " + sdcardDir.getPath());
            long blockSize = sf.getBlockSizeLong();
            long blockCount = sf.getBlockCountLong();
            long availCount = sf.getAvailableBlocksLong();
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, "blockSize:"+ blockSize+",blockCount:"+ blockCount+" ,:"+blockSize*blockCount/1024/1024+"M");
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, "availCount:"+ availCount+",:"+ availCount*blockSize/1024/1024+"M");
        }
    }
    public int dfPath(String path) { //列出可插拔存储的路径和空间大小
            StatFs sf = new StatFs(path);
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, "path " + path);
            long blockSize = sf.getBlockSizeLong();
            long blockCount = sf.getBlockCountLong();
            long availCount = sf.getAvailableBlocksLong();
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, "    blockSize:"+ blockSize+",blockCount:"+ blockCount+" ,:"+blockSize*blockCount/1024/1024+"M");
            if (UvcDebug.DEBUG && debugME)Log.d(TAG, "    availCount:"+ availCount+",:"+ availCount*blockSize/1024/1024+"M");
            return (int)(availCount*blockSize/1024/1024);
    }
    private String formatSize(Context context, long size)
    {
        return Formatter.formatFileSize(context, size);
    }

}
