package com.hungama.sdk.imagelazyloader;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class MemoryCache
{
    private final HashMap<String, SoftReference<Bitmap>> _loadedImages;
    private long _currentSize;
    private final long _cacheSize;

    MemoryCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        _cacheSize = maxMemory / 8;
        _loadedImages = new HashMap<>();
    }

    Bitmap getBitmap(String key)
    {
        synchronized (_loadedImages)
        {
            if (_loadedImages.containsKey(key))
            {
                try
                {
                    Bitmap bm = _loadedImages.get(key).get();
                    if (bm != null)
                    {
                        return bm;
                    }
                    else
                    {
                        _loadedImages.remove(key);
                    }
                }
                catch (Exception ex)
                {
                    //ex.printStackTrace();
                    HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
                }
            }
        }
        return null;
    }

    void addBitmap(Bitmap bm, String key)
    {
        synchronized (_loadedImages)
        {
            _loadedImages.put(key, new SoftReference<>(bm));
            _currentSize += getSizeInBytes(bm);
            removeImagesFromMemCache();
        }
    }

    void removeImagesFromMemCache(String key)
    {
        try
        {
            _loadedImages.remove(key);
        }
        catch (Exception ex)
        {

        }
    }

    private long getSizeInBytes(Bitmap bitmap)
    {
        if (bitmap == null)
        {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    private void removeImagesFromMemCache()
    {
        if (_currentSize > _cacheSize)
        {
            Iterator<Map.Entry<String, SoftReference<Bitmap>>> iterator = _loadedImages.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<String, SoftReference<Bitmap>> entry = iterator.next();
                _currentSize -= getSizeInBytes(entry.getValue().get());
                iterator.remove();
                if (_currentSize <= _cacheSize)
                    break;
            }
        }
    }
}
