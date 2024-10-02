package com.hungama.sdk.imagelazyloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

final class DiskCache
{
    private final int MAX_QUE_SIZE;
    private final Object _lockObject = new Object();
    private final HashMap<String, String> _localFiles;
    private AsyncTask<String, Void, Boolean> _loadFilesName;
    private AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> _loadFile1;
    private AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> _loadFile2;
    private AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> _loadFile3;
    private AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> _loadFile4;
    private AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> _loadFile5;

    private final ArrayList<DiskBitmapLoadInfo> _loadImagesQue;
    private final HashMap<Integer, DiskBitmapLoadInfo> _hashLoadImagesQue;
    private final OnImageReadyListener _listener;
    private final String _localFilesStorageFolder;

    DiskCache(OnImageReadyListener listener, PathServices systemServices, int maxQueSize)
    {
        MAX_QUE_SIZE = maxQueSize;
        _listener = listener;

        _localFilesStorageFolder = systemServices.getPathManager().getThumbnailsFolder();
        _localFiles = new HashMap<>();

        _loadImagesQue = new ArrayList<>();
        _hashLoadImagesQue = new HashMap<>();

        loadLocalCacheInfo();
    }

    private void loadLocalCacheInfo()
    {
        try
        {
            _loadFilesName = new LocalCacheInfo();
            _loadFilesName.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _localFilesStorageFolder);

        }
        catch (NullPointerException ex)
        {
            //ex.printStackTrace();
            HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
        }
    }

    boolean checkIfPathExits(String key)
    {
        boolean b = false;
        synchronized (_lockObject)
        {
            if (_localFiles.containsKey(key))
            {
                String str = _localFiles.get(key);
                File f = new File(str);
                if (f.exists() && f.isFile())
                {
                    b = true;
                }
                else
                {
                    _localFiles.remove(key);
                }
            }
        }
        return b;
    }

    void removeImage(String key)
    {
        synchronized (_lockObject)
        {
            try
            {
                String path = _localFiles.get(key);
                _localFiles.remove(key);
                File f = new File(path);
                f.delete();
            }
            catch (Exception ex)
            {

            }
        }
    }

    void removeItem(OnLazyImageLoaderListener listener)
    {
        synchronized (_lockObject)
        {
            try
            {
                int hashKey = listener.hashCode();
                if (_hashLoadImagesQue.containsKey(hashKey))
                {
                    DiskBitmapLoadInfo temp = _hashLoadImagesQue.remove(hashKey);
                    _loadImagesQue.remove(temp);
                }
            }
            catch (Exception ex)
            {

            }
        }
    }

    void loadLocalFile(String downloadPath, OnLazyImageLoaderListener lazyListener, int maxWidth, int maxHeight)
    {
        loadImage(null, downloadPath, lazyListener, maxWidth, maxHeight);
    }

    void loadImage(String key, String downloadUrl, OnLazyImageLoaderListener lazyListener, int maxWidth, int maxHeight)
    {
        if (lazyListener != null)
        {
            int hashKey = lazyListener.hashCode();
            DiskBitmapLoadInfo info = new DiskBitmapLoadInfo(lazyListener, key, downloadUrl, maxWidth, maxHeight, hashKey);
            synchronized (_lockObject)
            {
                if (_hashLoadImagesQue.containsKey(key))
                {
                    DiskBitmapLoadInfo temp = _hashLoadImagesQue.remove(hashKey);
                    _loadImagesQue.remove(temp);
                }
                _loadImagesQue.add(info);
                _hashLoadImagesQue.put(hashKey, info);
                int size = _loadImagesQue.size();
                if (MAX_QUE_SIZE > 0)
                {
                    while (size > MAX_QUE_SIZE)
                    {
                        try
                        {
                            DiskBitmapLoadInfo temp = _loadImagesQue.remove(0);
                            _hashLoadImagesQue.remove(temp.getHashKey());
                        }
                        catch (Exception ex)
                        {

                        }
                        size = _loadImagesQue.size();
                    }
                }
            }
        }
        checkAndLoad();
    }

    void saveImageToLocalCache(ImageDownloader.RemoteBitmapAssetInfo info)
    {
        String path = info._downloadPath;
        String key = info._key;
        synchronized (_lockObject)
        {
            _localFiles.put(key, path);
        }
    }

    private void checkAndLoad()
    {
        synchronized (_lockObject)
        {
            while (canLoadFile() && _loadImagesQue.size() > 0)
            {
                DiskBitmapLoadInfo temp = _loadImagesQue.remove(_loadImagesQue.size() - 1);
                _hashLoadImagesQue.remove(temp.getHashKey());

                if (temp.getLazyListener() != null)
                {
                    AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> loadFile = new DiskBitmapLoader();
                    setFileLoader(loadFile);
                    loadFile.executeOnExecutor(ImageLoaderTask.FILE_IO_EXECUTOR, temp);
                }
            }
        }
    }

    private boolean canLoadFile()
    {
        return (_loadFile1 == null || _loadFile2 == null || _loadFile3 == null || _loadFile4 == null || _loadFile5 == null);
    }

    private void setFileLoader(AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> loadFile)
    {
        if (_loadFile1 == null)
        {
            _loadFile1 = loadFile;
        }
        else if (_loadFile2 == null)
        {
            _loadFile2 = loadFile;
        }
        else if (_loadFile3 == null)
        {
            _loadFile3 = loadFile;
        }
        else if (_loadFile4 == null)
        {
            _loadFile4 = loadFile;
        }
        else if (_loadFile5 == null)
        {
            _loadFile5 = loadFile;
        }
    }

    private void clearFileLoader(AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo> loadFile)
    {
        if (_loadFile1 == loadFile)
        {
            _loadFile1 = null;
        }
        else if (_loadFile2 == loadFile)
        {
            _loadFile2 = null;
        }
        else if (_loadFile3 == loadFile)
        {
            _loadFile3 = null;
        }
        else if (_loadFile4 == loadFile)
        {
            _loadFile4 = null;
        }
        else if (_loadFile5 == loadFile)
        {
            _loadFile5 = null;
        }
    }

    private Bitmap getBitmap(String path, int maxWidth, int maxHeight) throws FileNotFoundException
    {
        if (maxWidth > 0 && maxWidth > 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        }
        else
        {
            return BitmapFactory.decodeFile(path);
        }
    }

    //FROM: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private class DiskBitmapLoader extends AsyncTask<DiskBitmapLoadInfo, Void, DiskBitmapLoadInfo>
    {

        @Override
        protected DiskBitmapLoadInfo doInBackground(DiskBitmapLoadInfo... params)
        {
            DiskBitmapLoadInfo info = params[0];
            synchronized (_lockObject)
            {
                String path = null;
                if (info.getKey() == null)
                {
                    path = info.getDownloadUrl();
                }
                else
                {
                    path = _localFiles.get(info.getKey());
                }
                try
                {
                    info._bitmap = getBitmap(path, info.getMaxWidth(), info.getMaxHeight()); //BitmapFactory.decodeFile(path);
                    File f = new File(path);
                    f.setLastModified(System.currentTimeMillis());
                }
                catch (FileNotFoundException e)
                {
                    info._bitmap = null;
                }
                catch (Exception ex)
                {
                    HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
                }
            }
            return info;
        }

        @Override
        protected void onPostExecute(DiskBitmapLoadInfo result)
        {
            super.onPostExecute(result);
            try
            {
                _listener.onImageLoadComplete(result);
            }
            catch (Exception ex)
            {
                HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
            }
            synchronized (_lockObject)
            {
                clearFileLoader(this);
            }
            checkAndLoad();
        }
    }

    class DiskBitmapLoadInfo
    {
        private String _key;
        private String _downloadUrl;
        private WeakReference<OnLazyImageLoaderListener> _lazyListener;
        private Bitmap _bitmap;
        private final int _maxWidth;
        private final int _maxHeight;
        private final int _hashKey;

        DiskBitmapLoadInfo(OnLazyImageLoaderListener lazyListener, String key, String downloadUrl, int maxWidth, int maxHeight, int hashKey)
        {
            _key = key;
            _lazyListener = new WeakReference<OnLazyImageLoaderListener>(lazyListener);
            _downloadUrl = downloadUrl;
            _maxWidth = maxWidth;
            _maxHeight = maxHeight;
            _hashKey = hashKey;
        }

        String getKey()
        {
            return _key;
        }

        Bitmap getBitmap()
        {
            return _bitmap;
        }

        OnLazyImageLoaderListener getLazyListener()
        {
            return _lazyListener.get();
        }

        String getDownloadUrl()
        {
            return _downloadUrl;
        }

        int getMaxWidth()
        {
            return _maxWidth;
        }

        int getMaxHeight()
        {
            return _maxHeight;
        }

        int getHashKey()
        {
            return _hashKey;
        }

        void destroy()
        {
            _key = null;
            _lazyListener = null;
            _bitmap = null;
            _downloadUrl = null;
        }
    }

    private class LocalCacheInfo extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... params)
        {
            String path = params[0];
            try
            {
                FilenameFilter thumbsFilter = new FilenameFilter()
                {
                    @Override
                    public boolean accept(File file, String filename)
                    {
                        return filename.endsWith(ImageLazyLoader.THUMBNAILS_EXTENTION);
                    }
                };

                File f = new File(path);
                String[] filesName = f.list(thumbsFilter);

                long current = System.currentTimeMillis();
                long max = 15 * 24 * 60 * 60 * 1000;
                synchronized (_lockObject)
                {
                    for (String fileName : filesName)
                    {
                        String temp = path + "/" + fileName;
                        File f1 = new File(temp);
                        long t = f1.lastModified();
                        long diff = current - t;
                        if (diff > max)
                        {
                            f1.delete();
                        }
                        else
                        {
                            _localFiles.put(fileName, temp);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            _loadFilesName = null;
        }
    }
}
