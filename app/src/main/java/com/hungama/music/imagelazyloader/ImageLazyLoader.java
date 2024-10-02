package com.hungama.sdk.imagelazyloader;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

public class ImageLazyLoader implements OnImageReadyListener
{
    final static String THUMBNAILS_EXTENTION = ".thbn";

    private static ImageLazyLoader ourInstance;

    public static void initialize(Context context)
    {
        if (ourInstance == null)
        {
            ourInstance = new ImageLazyLoader(context);
        }
    }

    public static ImageLazyLoader getInstance()
    {
        return ourInstance;
    }

    private final MemoryCache _memoryCache;
    private final DiskCache _diskCache;
    private final ImageDownloader _imageDownloader;

    private ImageLazyLoader(Context context)
    {
        HandledErrorTracker.getHandledErrorTracker().init(context);
        PathServices pathServices = new PathServices(context);

        _memoryCache = new MemoryCache();
        _diskCache = new DiskCache(this, pathServices, -1);
        _imageDownloader = new ImageDownloader(this, pathServices, -1);
    }

    public void setOnlineLoadQueueSize(int size)
    {
        _imageDownloader.setMaxQueueSize(size);
    }

    public void loadLocalFile(OnLazyImageLoaderListener lazyListener, String downloadPath)
    {
        loadLocalFile(lazyListener, downloadPath, -1, -1);
    }

    public void loadLocalFile(OnLazyImageLoaderListener lazyListener, String downloadPath, int maxWidth, int maxHeight)
    {
        _diskCache.loadLocalFile(downloadPath, lazyListener, maxWidth, maxHeight);
    }


    public void loadImage(OnLazyImageLoaderListener listener, String downloadUrl, int maxWidth, int maxHeight)
    {
        if (downloadUrl != null && downloadUrl.length() > 0)
        {
            _imageDownloader.removeItem(listener);
            _diskCache.removeItem(listener);

            String key = Utils.getKeyFroUrl(downloadUrl, THUMBNAILS_EXTENTION);
            Bitmap image = _memoryCache.getBitmap(key);
            if (image == null)
            {
                if (_diskCache.checkIfPathExits(key))
                {
                    _diskCache.loadImage(key, downloadUrl, listener, maxWidth, maxHeight);
                }
                else
                {
                    _imageDownloader.download(key, downloadUrl, listener, maxWidth, maxHeight);
                }
            }
            else
            {
                listener.onImageLoadSuccess(downloadUrl, image);
            }
        }
    }

    public void loadImage(OnLazyImageLoaderListener listener, String downloadUrl)
    {
        this.loadImage(listener, downloadUrl, -1, -1);
    }

    public void removeImage(String downloadUrl)
    {
        String key = Utils.getKeyFroUrl(downloadUrl, THUMBNAILS_EXTENTION);
        Bitmap image = _memoryCache.getBitmap(key);
        if (image != null)
        {
            _memoryCache.removeImagesFromMemCache(key);
        }
        if (_diskCache.checkIfPathExits(key))
        {
            _diskCache.removeImage(key);
        }
    }

    @Override
    public void onImageLoadComplete(DiskCache.DiskBitmapLoadInfo info)
    {
        if (info.getBitmap() != null)
        {
            _memoryCache.addBitmap(info.getBitmap(), info.getKey());
            OnLazyImageLoaderListener lazyListener = info.getLazyListener();
            if (lazyListener != null)
            {
                lazyListener.onImageLoadSuccess(info.getDownloadUrl(), info.getBitmap());
            }
        }
        else
        {
            OnLazyImageLoaderListener lazyListener = info.getLazyListener();
            if (lazyListener != null)
            {
                _imageDownloader.download(info.getKey(), info.getDownloadUrl(), lazyListener, info.getMaxWidth(), info.getMaxHeight());
            }
        }
        info.destroy();
    }

    public void onImageLoadComplete(ImageDownloader.RemoteBitmapAssetInfo info)
    {
        if (info.isSuccess())
        {
            _memoryCache.addBitmap(info.getBitmap(), info.getKey());
            _diskCache.saveImageToLocalCache(info);
            OnLazyImageLoaderListener lazyListener = info.getLazyListener();
            if (lazyListener != null)
            {
                lazyListener.onImageLoadSuccess(info.getDownloadUrl(), info.getBitmap());
            }
        }
        else
        {
            OnLazyImageLoaderListener lazyListener = info.getLazyListener();
            if (lazyListener != null)
            {
                lazyListener.onImageLoadFailed(info.getDownloadUrl(), info.getErrorMesg());
            }
        }
        info.destroy();
    }

    @Override
    public void onImageLoadComplete(List<ImageDownloader.RemoteBitmapAssetInfo> list)
    {
        for (ImageDownloader.RemoteBitmapAssetInfo info : list)
        {
            if (info.isSuccess())
            {
                OnLazyImageLoaderListener lazyListener = info.getLazyListener();
                if (lazyListener != null)
                {
                    lazyListener.onImageLoadSuccess(info.getDownloadUrl(), info.getBitmap());
                }
            }
            else
            {
                OnLazyImageLoaderListener lazyListener = info.getLazyListener();
                if (lazyListener != null)
                {
                    lazyListener.onImageLoadFailed(info.getDownloadUrl(), info.getErrorMesg());
                }
            }
            info.destroy();
        }
    }
}
