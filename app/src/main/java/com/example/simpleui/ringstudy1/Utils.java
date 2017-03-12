package com.example.simpleui.ringstudy1;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

/**
 * Created by wudeyan on 8/26/16.
 */
public class Utils {

    public static void writeFile(Context context, String fileName, String content)
    {
//        File dir = (Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS));



        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) +  "/SegTouch");

        try {
            Log.d("DEBUG", dir.getAbsolutePath());
            if(!dir.exists())
                dir.mkdirs();
//                file.

            File file = new File(dir,fileName);
            if(!file.exists())
                file.createNewFile();

            if(file.isFile())
            {
                FileOutputStream fos = new FileOutputStream(file,true);
                fos.write(content.getBytes());
                fos.close();
            }


//
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Drawable readImageFromAssets(AssetManager assetManager, String fileName)
    {
        InputStream ims = null;
        try {
            ims = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Drawable d = Drawable.createFromStream(ims, null);
        return d;
    }

    public static int majority(List<Integer> nums)
    {
        int candidate = -1;
        int count = 0;
        for (int num : nums) {
            if (count == 0)
                candidate = num;
            if (num == candidate)
                count++;
            else
                count--;
        }
        return candidate;
    }
}
