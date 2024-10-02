package com.hungama.sdk.imagelazyloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

class ImageDownloader
{
    private int MAX_QUE_SIZE;
    private final ArrayList<RemoteBitmapAssetInfo> _que;
    private final HashMap<Integer, RemoteBitmapAssetInfo> _hashQue;
    private final OnImageReadyListener _listener;
    private final PathServices _pathServices;
    private AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> _loadImageFromNet1;
    private AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> _loadImageFromNet2;
    private AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> _loadImageFromNet3;
    private AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> _loadImageFromNet4;
    private AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> _loadImageFromNet5;

    ImageDownloader(OnImageReadyListener listener, PathServices pathServices, int maxQueSize)
    {
        _que = new ArrayList<>();
        _hashQue = new HashMap<>();
        _listener = listener;
        _pathServices = pathServices;
        MAX_QUE_SIZE = maxQueSize;
    }

    void setMaxQueueSize(int maxQueueSize)
    {
        MAX_QUE_SIZE = maxQueueSize;
    }

    void download(String key, String dowloadUrl, OnLazyImageLoaderListener listener, int maxWidth, int maxHeight)
    {
        this.download(key, dowloadUrl, listener, maxWidth, maxHeight, true);
    }

    void removeItem(OnLazyImageLoaderListener listener)
    {
        synchronized (_que)
        {
            try
            {
                int hashKey = listener.hashCode();
                if (_hashQue.containsKey(hashKey))
                {
                    RemoteBitmapAssetInfo temp = _hashQue.remove(hashKey);
                    _que.remove(temp);
                }
            }
            catch (Exception ex)
            {
                HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
            }
        }
    }

    void download(String key, String dowloadUrl, OnLazyImageLoaderListener listener, int maxWidth, int maxHeight, boolean decodeImageAfterDownload)
    {
        if (listener != null)
        {
            synchronized (_que)
            {
                int hashKey = listener.hashCode();
                RemoteBitmapAssetInfo info = new RemoteBitmapAssetInfo(listener, dowloadUrl, key, maxWidth, maxHeight, decodeImageAfterDownload, hashKey);

                if (_hashQue.containsKey(hashKey))
                {
                    RemoteBitmapAssetInfo temp = _hashQue.remove(hashKey);
                    _que.remove(temp);
                }

                _que.add(info);
                _hashQue.put(hashKey, info);

                if (MAX_QUE_SIZE > 0)
                {
                    while (_que.size() > MAX_QUE_SIZE)
                    {
                        try
                        {
                            RemoteBitmapAssetInfo temp = _que.remove(0);
                            _hashQue.remove(temp.getHashKey());
                        }
                        catch (Exception ex)
                        {
                            HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
                        }
                    }
                }
            }
        }
        checkAndLoad();
    }

    private void checkAndLoad()
    {
        synchronized (_que)
        {
            while (_que.size() > 0 && canDownloadFile())
            {
                RemoteBitmapAssetInfo temp = _que.remove(0);
                _hashQue.remove(temp.getHashKey());
                if (temp.getLazyListener() != null)
                {
                    AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> loadImageFromNet = new LoadImageFromNet();
                    setDownloader(loadImageFromNet);
                    loadImageFromNet.executeOnExecutor(ImageLoaderTask.DOWNLOAD_EXECUTOR, temp);
                }
            }
        }
    }

    private boolean canDownloadFile()
    {
        return (_loadImageFromNet1 == null || _loadImageFromNet2 == null || _loadImageFromNet3 == null || _loadImageFromNet4 == null || _loadImageFromNet5 == null);
    }

    private void setDownloader(AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> loadImageFromNet)
    {
        if (_loadImageFromNet1 == null)
        {
            _loadImageFromNet1 = loadImageFromNet;
        }
        else if (_loadImageFromNet2 == null)
        {
            _loadImageFromNet2 = loadImageFromNet;
        }
        else if (_loadImageFromNet3 == null)
        {
            _loadImageFromNet3 = loadImageFromNet;
        }
        else if (_loadImageFromNet4 == null)
        {
            _loadImageFromNet4 = loadImageFromNet;
        }
        else if (_loadImageFromNet5 == null)
        {
            _loadImageFromNet5 = loadImageFromNet;
        }
    }

    private void clearDownloader(AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo> loadImageFromNet)
    {
        if (_loadImageFromNet1 == loadImageFromNet)
        {
            _loadImageFromNet1 = null;
        }
        else if (_loadImageFromNet2 == loadImageFromNet)
        {
            _loadImageFromNet2 = null;
        }
        else if (_loadImageFromNet3 == loadImageFromNet)
        {
            _loadImageFromNet3 = null;
        }
        else if (_loadImageFromNet4 == loadImageFromNet)
        {
            _loadImageFromNet4 = null;
        }
        else if (_loadImageFromNet5 == loadImageFromNet)
        {
            _loadImageFromNet5 = null;
        }
    }

    private File getFile(RemoteBitmapAssetInfo info)
    {
        return _pathServices.getPathManager().getThumbnailsFile(info.getKey());
    }

    private File getTempFile(String key)
    {
        return _pathServices.getPathManager().getThumbnailsFile(key);
    }

    private void updateInfo(File f, RemoteBitmapAssetInfo info) throws FileNotFoundException
    {
        info._downloadPath = f.getAbsolutePath();
        if (info._maxWidth > 0 && info._maxHeight > 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(info._downloadPath, options);

            options.inSampleSize = calculateInSampleSize(options, info._maxWidth, info._maxHeight);
            options.inJustDecodeBounds = false;
            info._bitmap = BitmapFactory.decodeFile(info._downloadPath, options);
        }
        else
        {
            info._bitmap = BitmapFactory.decodeFile(info._downloadPath);
        }
        info._isSuccess = true;
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

    private class LoadImageFromNet extends AsyncTask<RemoteBitmapAssetInfo, Void, RemoteBitmapAssetInfo>
    {

        @Override
        protected RemoteBitmapAssetInfo doInBackground(RemoteBitmapAssetInfo... params)
        {
            RemoteBitmapAssetInfo info = params[0];
            HttpURLConnection conn = null;
            OutputStream outPutStream = null;
            try
            {
                info._start = Calendar.getInstance().getTimeInMillis();
                info._date = getDate(info._start);
                File f = getFileForImage(info);
                URL imageUrl = new URL(info._downloadUrl);
                conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                //conn.setRequestMethod("HEAD");
                //conn.setRequestProperty("Connection","keep-alive");

                InputStream inputStream = conn.getInputStream();

                outPutStream = new FileOutputStream(f);
                copyStream(inputStream, outPutStream);
                //AssetsLoaderUtils.copyStream(inputStream, outPutStream);

                outPutStream.close();
                conn.disconnect();
                if (info._decodeImageAfterDownload)
                {
                    updateInfo(f, info);
                    File f1 = new File(f.getAbsolutePath());
                    f1.setLastModified(System.currentTimeMillis());
                }
                info._end = Calendar.getInstance().getTimeInMillis();
            }
            catch (Exception ex)
            {
                info._end = Calendar.getInstance().getTimeInMillis();
                info._isSuccess = false;
                info._errorMesg = ex.getMessage();
                try
                {
                    info._errorLog = Log.getStackTraceString(ex);
                }
                catch (Exception e)
                {

                }
                //ex.printStackTrace();
                //////Log.w(ApplicationController.TAG,ex.getMessage());
                HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
            }
            return info;
        }

        @Override
        protected void onPostExecute(RemoteBitmapAssetInfo info)
        {
            super.onPostExecute(info);
            ArrayList<RemoteBitmapAssetInfo> list = new ArrayList<RemoteBitmapAssetInfo>();
            synchronized (_que)
            {
                if (info._isSuccess)
                {
                    for (RemoteBitmapAssetInfo item : _que)
                    {
                        if (item.getKey() != null && item.getKey().equalsIgnoreCase(info.getKey()))
                        {
                            item._bitmap = info._bitmap;
                            item._isSuccess = info._isSuccess;
                            list.add(item);
                        }
                    }
                }
                _que.removeAll(list);
                clearDownloader(this);
            }
            _listener.onImageLoadComplete(list);
            _listener.onImageLoadComplete(info);
            checkAndLoad();
        }

        private File getFileForImage(RemoteBitmapAssetInfo info) throws Exception
        {
            if (info.getKey() == null)
            {
                throw new NullKeyException("Key generated for image is null");
            }
            File f = null;
            try
            {
                f = getFile(info);
                if (f.exists())
                {
                    f.delete();
                }
            }
            catch (Exception ex)
            {
                throw ex;
            }
            return f;
        }
    }

    private void copyStream(InputStream is, OutputStream os) throws Exception
    {
        final int buffer_size = 1024;
        try
        {

            byte[] bytes = new byte[buffer_size];
            for (; ; )
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                {
                    break;
                }
                os.write(bytes, 0, count);
            }
        }
        catch (Exception ex)
        {
            HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
            throw ex;
        }
    }

    private String getDate(long milliSeconds)
    {
        String dateFormat = "dd/MM/yyyy hh:mm:ss.SSS";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    final class RemoteBitmapAssetInfo
    {
        private WeakReference<OnLazyImageLoaderListener> _lazyImageLoaderListener;
        private String _downloadUrl;
        private Bitmap _bitmap;
        private boolean _isSuccess;
        private String _errorMesg;
        String _key;
        String _downloadPath;
        private final int _maxWidth;
        private final int _maxHeight;
        private final boolean _decodeImageAfterDownload;
        private String _errorLog;
        private long _start;
        private long _end;
        private String _date;
        private final int _hashKey;

        RemoteBitmapAssetInfo(OnLazyImageLoaderListener listener, String downloadUrl, String key, int maxWidth, int maxHeight, boolean decodeImageAfterDownload, int hashKey)
        {
            _lazyImageLoaderListener = new WeakReference<>(listener);
            _downloadUrl = downloadUrl;
            _key = key;
            _maxWidth = maxWidth;
            _maxHeight = maxHeight;
            _errorLog = "";
            _decodeImageAfterDownload = decodeImageAfterDownload;
            _hashKey = hashKey;
        }

        OnLazyImageLoaderListener getLazyListener()
        {
            return _lazyImageLoaderListener.get();
        }

        String getDownloadUrl()
        {
            return _downloadUrl;
        }

        String getErrorMesg()
        {
            return _errorMesg;
        }

        boolean isSuccess()
        {
            return _isSuccess;
        }

        Bitmap getBitmap()
        {
            return _bitmap;
        }

        String getKey()
        {
            return _key;
        }

        String getDownloadPath()
        {
            return _downloadPath;
        }

        String getErrorLog()
        {
            return _errorLog;
        }

        long getStart()
        {
            return _start;
        }

        long getEnd()
        {
            return _end;
        }

        String getDate()
        {
            return _date;
        }

        int getHashKey()
        {
            return _hashKey;
        }

        void destroy()
        {
            _lazyImageLoaderListener = null;
            _downloadUrl = null;
            _bitmap = null;
            _errorMesg = null;
        }
    }
}