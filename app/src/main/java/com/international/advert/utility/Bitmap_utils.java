package com.international.advert.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alex on 1/24/2016.
 */
public class Bitmap_utils {





    ///////////////////////////////////////////
    ////                                   ////
    ////       get bitmap from path        ////
    ////                                   ////
    ///////////////////////////////////////////


    //Given the bitmap size and View size calculate a subsampling size (powers of 2)
    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;	//Default subsampling size
        // See if image raw height and width is bigger than that of required view
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            //bigger
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }

    public static String getRealPathFromURI(Activity activity, Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }

    public static Bitmap cropBitmap(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int x = 0, y = 0;

        if (width > height) {
            x = (width - height) / 2;
        } else if (width < height) {
            y = (height - width) / 2;
        }

        return Bitmap.createBitmap(src, x, y, Math.min(width, height), Math.min(width, height));
    }

    public static Bitmap scaleBitmap(Bitmap src, int width, int height) {
        return Bitmap.createScaledBitmap(src, width, height, false);

    }

    public static Bitmap cropAndScale(Bitmap src, int width, int height) {
        return scaleBitmap(cropBitmap(src), width, height);
    }

    public static Bitmap getImageFromOrientation(String path, int width){
        Bitmap bitmap = null;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int orientaion = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientaion){
            case ExifInterface.ORIENTATION_ROTATE_90:
                bitmap = rotateImage(decodeSampledBitmapFromResource(path, width, width), 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                bitmap = rotateImage(decodeSampledBitmapFromResource(path, width, width), 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                bitmap = rotateImage(decodeSampledBitmapFromResource(path, width, width), 270);
                break;
            default:
                bitmap = decodeSampledBitmapFromResource(path, width, width);
                break;
        }

        return bitmap;
    }

    public static boolean saveBitmapToImageInSDCARD(Bitmap bitmap, String fileName){
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        String sdCardDirectoryPath = sdCardDirectory.getPath() + App.FOLDER_NAME;
        File image = new File(sdCardDirectoryPath, fileName);
        boolean success = false;

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {
//            bitmap = rotateImage(bitmap, 90);
            outStream = new FileOutputStream(image);
            if (bitmap.getHeight() < 2400) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } else if (bitmap.getHeight() > 2400 && bitmap.getHeight() < 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            } else if (bitmap.getHeight() > 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
            }


            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (success) {
            return true;
        } else {
            return false;
        }
    }

    public static Bitmap squareBitmap(Bitmap bitmap){
        int height, width;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        if (height > width){
            bitmap = Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width);
        } else {
            bitmap = Bitmap.createBitmap(bitmap, (width - height) / 2, 0, height, height);
        }

        return bitmap;
    }

    public static Bitmap cropBitmapAnySize(Bitmap bitmap, int width, int height, int imgWidth, int imgHeigh){

        int temp;
        try {

            if (width >= height && imgWidth >= imgHeigh) {

                if ((float) imgWidth / (float) imgHeigh > (float) width / (float) height) {
                    temp = imgWidth;
                    imgWidth = (int) ((float) imgHeigh * (float) width / (float) height);
                    return Bitmap.createBitmap(bitmap, (temp - imgWidth) / 2, 0, imgWidth, imgHeigh);
                } else {
                    temp = imgHeigh;
                    imgHeigh = (int) ((float) imgWidth / ((float) width / (float) height));
                    return Bitmap.createBitmap(bitmap, 0, (temp - imgHeigh) / 2, imgWidth, imgHeigh);
                }
            }

            if (width >= height && imgWidth < imgHeigh) {
                temp = imgHeigh;
                imgHeigh = (int) ((float) imgWidth / ((float) width / (float) height));
                return Bitmap.createBitmap(bitmap, 0, (temp - imgHeigh) / 2, imgWidth, imgHeigh);
            }

            if (width < height && imgWidth >= imgHeigh) {
                temp = imgWidth;
                imgWidth = (int) ((float) imgHeigh * (float) width / (float) height);
                return Bitmap.createBitmap(bitmap, (temp - imgWidth) / 2, 0, imgWidth, imgHeigh);
            }

            if (width < height && imgWidth < imgHeigh) {

                if ((float) imgHeigh / (float) imgWidth > (float) height / (float) width) {
                    temp = imgHeigh;
                    imgHeigh = (int) ((float) imgWidth / ((float) width / (float) height));
                    return Bitmap.createBitmap(bitmap, 0, (temp - imgHeigh) / 2, imgWidth, imgHeigh);
                } else {
                    temp = imgWidth;
                    imgWidth = (int) ((float) imgHeigh * (float) width / (float) height);
                    return Bitmap.createBitmap(bitmap, (temp - imgWidth) / 2, 0, imgWidth, imgHeigh);
                }
            }


            return bitmap;
        }catch (OutOfMemoryError e){
            e.printStackTrace();
            Log.d("OOMDetailImgAdapter", "");
            return null;
        }catch (Throwable e){
            if (!(e instanceof ThreadDeath)){
                e.printStackTrace(System.err);
                Log.d("OOMDetailImgAdapter", "");
            }
            return null;
        }
    }

    public static String saveImageAndGetPath(String fileName, Bitmap bitmap){

        String fullSavedPath = "";
        saveBitmapToImageInSDCARD(bitmap, fileName);
        fullSavedPath = Environment.getExternalStorageDirectory().getPath() +  App.FOLDER_NAME + fileName + ".jpg";

        return fullSavedPath;
    }

    public static String getPathFromGallery(Context context, Intent data)
    {
        Uri selectedImage = data.getData();
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        String picturePath = c.getString(columnIndex);
        Log.d("gallery image path", picturePath);
        String fileName = picturePath.substring(picturePath.lastIndexOf("/") + 1);
        c.close();

        return picturePath;
    }


    // Returns the URI path to the Bitmap displayed in specified ImageView
    public static Uri getLocalBitmapUri(Bitmap imageView) {
        // Extract Bitmap from ImageView drawable

        Bitmap bmp = imageView;

        if (bmp == null)
            return null;

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
