package com.hungama.sdk.imagelazyloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;


import androidx.appcompat.widget.AppCompatImageView;

import java.net.MalformedURLException;
import java.net.URL;

public class LazyImageHolder extends AppCompatImageView implements OnLazyImageLoaderListener
{
    private String _imageURL;

    public LazyImageHolder(Context context)
    {
        super(context);
    }

    public LazyImageHolder(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LazyImageHolder(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void resetImageWith(int resID)
    {
        this.setImageResource(resID);
        _imageURL = null;
    }

    public void setImageURL(String imageURL)
    {
        _imageURL = imageURL;
        ImageDimensions d = getDimentions();
        setImageURL(imageURL, d._width, d._height);
    }

    public void setImageURL(String imageURL, int placeHolderResourceID)
    {
        resetImageWith(placeHolderResourceID);
        setImageURL(imageURL);
    }

    public void setImageURL(String imageURL, int width, int height)
    {
        _imageURL = imageURL;
        if (_imageURL != null)
        {
            if (isLocalFile(imageURL))
            {
                ImageLazyLoader.getInstance().loadLocalFile(this, _imageURL, width, height);
            }
            else
            {
                ImageLazyLoader.getInstance().loadImage(this, _imageURL, width, height);
            }
        }
    }

    public void setImageURL(String imageURL, int width, int height, int placeHolderResourceID)
    {
        resetImageWith(placeHolderResourceID);
        setImageURL(imageURL, width, height);
    }

    public void setImageURLWithoutCalculatingSize(String imageURL)
    {
        _imageURL = imageURL;
        if (isLocalFile(imageURL))
        {
            ImageLazyLoader.getInstance().loadLocalFile(this, _imageURL);
        }
        else
        {
            ImageLazyLoader.getInstance().loadImage(this, _imageURL);
        }
    }

    public void setImageURLWithoutCalculatingSize(String imageURL, int placeHolderResourceID)
    {
        resetImageWith(placeHolderResourceID);
        setImageURLWithoutCalculatingSize(imageURL);
    }

    public void setImagePath(String imagePath)
    {
        _imageURL = imagePath;
        if (_imageURL != null)
        {
            ImageDimensions d = getDimentions();
            ImageLazyLoader.getInstance().loadLocalFile(this, _imageURL, d._width, d._height);
        }
    }

    public void setImagePath(String imagePath, int placeHolderResourceID)
    {
        resetImageWith(placeHolderResourceID);
        setImagePath(imagePath);
    }

    public void setImagePathWithoutCalculatingSize(String imagePath)
    {
        _imageURL = imagePath;
        ImageLazyLoader.getInstance().loadLocalFile(this, _imageURL);
    }

    public void setImagePathWithoutCalculatingSize(String imagePath, int placeHolderResourceID)
    {
        resetImageWith(placeHolderResourceID);
        setImagePathWithoutCalculatingSize(imagePath);
    }

    private boolean isLocalFile(String imageURL)
    {
        boolean isLocalFile = false;
        try
        {
            URL url = new URL(imageURL);
        }
        catch (MalformedURLException e)
        {
            //e.printStackTrace();
            isLocalFile = true;
        }

        if (!isLocalFile)
        {
            isLocalFile = imageURL.startsWith("file:///");
        }
        return isLocalFile;
    }

    private ImageDimensions getDimentions()
    {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
        {
            w = getMeasuredWidth();
            h = getMeasuredHeight();
        }
        return new ImageDimensions(w, h);
    }

    @Override
    public void onImageLoadSuccess(String downloadUrl, Bitmap bm)
    {
        if (downloadUrl.equalsIgnoreCase(_imageURL) && bm != null)
        {
            try
            {
                if (bm.getWidth() > 0 && bm.getHeight() > 0)
                {
                    this.setImageBitmap(bm);
                }
            }
            catch (Exception ex)
            {

            }
        }
    }

    @Override
    public void onImageLoadFailed(String downloadUrl, String errorMesg)
    {

    }

    private class ImageDimensions
    {
        private final int _height;
        private final int _width;

        private ImageDimensions(int width, int height)
        {
            _height = height;
            _width = width;
        }
    }
}