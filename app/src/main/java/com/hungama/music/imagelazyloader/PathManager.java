package com.hungama.sdk.imagelazyloader;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

class PathManager
{
    private final String THUMBNAILS_FOLDER = "thumbnails";
    private final float MB = 1024.0f * 1024.0f;

    private final Context _context;
    private final Object _lockObject;
    private String __externalCachePath;

    PathManager(Context context)
    {
        _context = context;
        _lockObject = new Object();
        updateExternalStoragePath();
    }


    void recreateStoragePath()
    {
        synchronized (_lockObject)
        {
            refreshFolder(__externalCachePath);
        }
    }

    private void refreshFolder(String path)
    {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory())
        {
            folder.mkdirs();
        }
    }

    private void updateExternalStoragePath()
    {
        synchronized (_lockObject)
        {

            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state))
            {
                Log.w("ImageCaching", "ERROR: SDCard is not ready. State:"+state);
            }
            setCacheDirectory();
        }
    }

    private void setCacheDirectory()
    {
        try
        {
            File f = _context.getExternalCacheDir();
            float free = f.getFreeSpace() / MB;
            if (free < 100)
            {
                Log.w("ImageCaching", "WARNING: Less than 100 MB space left. Please free some space to continue using app successfully.");
            }
            __externalCachePath = checkAndCreateFolders(f.getAbsolutePath(), THUMBNAILS_FOLDER);
        }
        catch (Exception ex)
        {
            __externalCachePath = null;
            __externalCachePath = createDirectoryOnAlternatePath(THUMBNAILS_FOLDER);
            //HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
        }
    }

    private String checkAndCreateFolders(String path, String folderName)
    {
        if(path != null)
        {
            File folder = new File(path, folderName);
            if (folder.exists())
            {
                if(folder.isDirectory())
                {
                    return folder.getAbsolutePath();
                }
                else
                {
                    folder.delete();
                }
            }

            if (folder.mkdir())
            {
                return folder.getAbsolutePath();
            }
            else
            {
                Log.w("ImageCaching", "ERROR: Unable to create folder: "+folderName+" on alternate path. " + folder.getAbsolutePath());
            }
        }
        return createDirectoryOnAlternatePath(folderName);
    }

    private String createDirectoryOnAlternatePath(String folderName)
    {
        File file = _context.getFilesDir();
        if(file != null)
        {
            File folder = new File(file.getAbsolutePath(), folderName);
            if (folder.exists())
            {
                if(folder.isDirectory())
                {
                    return folder.getAbsolutePath();
                }
                else
                {
                    folder.delete();
                }
            }

            if (folder.mkdir())
            {
                return folder.getAbsolutePath();
            }
            else
            {
                Log.w("ImageCaching", "ERROR: Unable to create folder: "+folderName+" on alternate path. " + folder.getAbsolutePath());
            }

        }
        return "";
    }


    String getThumbnailsFolder() throws NullPointerException
    {
        String str = null;
        synchronized (_lockObject)
        {
            if (__externalCachePath == null)
            {
                recreateStoragePath();
                if (__externalCachePath == null)
                {
                    Log.w("ImageCaching", "ERROR: Thumbnails folder not available. Application will crash");
                }
            }
            str = __externalCachePath + "/";
        }
        return str;
    }


    File getThumbnailsFile(String fileName) throws NullPointerException
    {
        return new File(getThumbnailsFolder(), fileName);
    }

}
