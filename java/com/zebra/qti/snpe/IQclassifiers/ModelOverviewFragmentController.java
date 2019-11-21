/*
 * Copyright (c) 2016-2018 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.zebra.qti.snpe.IQclassifiers;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;
import com.zebra.qti.snpe.IQclassifiers.tasks.AbstractClassifyImageTask;
import com.zebra.qti.snpe.IQclassifiers.tasks.ClassifyImageWithFloatTensorTask;
import com.zebra.qti.snpe.IQclassifiers.tasks.ClassifyImageWithUserBufferTf8Task;
import com.zebra.qti.snpe.IQclassifiers.tasks.LoadImageTask;
import com.zebra.qti.snpe.IQclassifiers.tasks.LoadNetworkTask;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;


import static android.os.Environment.getExternalStorageState;


public class ModelOverviewFragmentController extends AbstractViewController<ModelOverviewFragment> {

    String appTag="IQclassifier";

    public enum SupportedTensorFormat {
        FLOAT,
        UB_TF8
    }

    //added SC 040419
    private static final int MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int IMAGE_DIMENSION=299;

    private final Map<String, SoftReference<Bitmap>> mBitmapCache;

    private final Model mModel;

    private final Application mApplication;

    private NeuralNetwork mNeuralNetwork;

    private LoadNetworkTask mLoadTask;

    private NeuralNetwork.Runtime mRuntime;

    private SupportedTensorFormat mCurrentSelectedTensorFormat;

    private SupportedTensorFormat mNetworkTensorFormat;

    public ModelOverviewFragmentController(final Application application, Model model) {
        mBitmapCache = new HashMap<>();
        mApplication = application;
        mModel = model;
    }

    @Override
    protected void onViewAttached(ModelOverviewFragment view) {
        view.setModelName(mModel.name);
        view.setSupportedRuntimes(getSupportedRuntimes());
        view.setSupportedTensorFormats(Arrays.asList(SupportedTensorFormat.values()));
       // loadImageSamples(view);
}

  //  private void loadImageSamples(ModelOverviewFragment view) {
    //changed SC 4/9/19
    public void loadImageSamples(ModelOverviewFragment view) {

        //added SC 040519:  added size of images check
        Log.i(appTag, "In loadImageSamples.");
        if(mModel.jpgImages==null){ //without this, app will stop when fileNames.length is null
            Log.i(appTag,"Null Sample file directory");
        }else {
            for (int i = 0; i < mModel.jpgImages.length; i++) {
                final File jpeg = mModel.jpgImages[i];
                final Bitmap cached = getCachedBitmap(jpeg);
                if (cached != null) {
                    view.addSampleBitmap(cached);
                } else {
                    final LoadImageTask task = new LoadImageTask(this, jpeg);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }

    //added SC 04/03/19
    public void loadImageDir() {
        // added SC 04/03/19 load sample images when load network
        ModelOverviewFragment view0 = getView();
        loadImageDir0(view0);
    }
    //load images from a given directory, preprocess, clearout current display, if clicked, classify
    public void loadImageDir0(ModelOverviewFragment view) {
        // File representing the folder (can replace by using a FileChooser)
        final File dir = new File("sdcard/DCIM","Camera");

        File[] fileNames;
        if(dir.exists()){
            fileNames = dir.listFiles(); // get all the files in the dir (not just file names)
            Log.i(appTag,"file directory: "+getExternalStorageState());

            if(fileNames==null){ //without this, app will stop when fileNames.length is null
                Log.i(appTag,"Null dir file directory");
            }else{
                for (int i = 0; i < fileNames.length; i++) {
                    final File jpeg1 = fileNames[i];
                    //final Bitmap cached1 = BitmapFactory.decodeFile(dir.getPath()+"/"+ fileNames[i]);
                    final Bitmap cached1 = getCachedBitmap0(jpeg1,IMAGE_DIMENSION);
                    if (cached1 != null) {
                        Log.i(appTag,"cached bitmap is not null");
                        view.addSampleBitmap(cached1);
                    } else {// SC: this branch may not be needed
                        final LoadImageTask task = new LoadImageTask(this, jpeg1);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    ///Now set this bitmap on imageview
                }
            }
        }else{
            Log.i(appTag,dir.getPath()+"Cannot find images directory ");
        }
    }

    private Bitmap getCachedBitmap(File jpeg) {
        final SoftReference<Bitmap> reference = mBitmapCache.get(jpeg.getAbsolutePath());
        if (reference != null) {
            final Bitmap bitmap = reference.get();
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    //added SC 040519, with dimension check and scale to dimension Dim*Dim
    private Bitmap getCachedBitmap0(File jpeg, int dim) {
//        Log.i(appTag,"In getCachedBitmap0");
        //final SoftReference<Bitmap> reference = mBitmapCache.get(jpeg.getAbsolutePath());
        String jpegPath = jpeg.getAbsolutePath();
        //final SoftReference<Bitmap> reference = BitmapFactory.decodeFile(jpegPath);
        final Bitmap bitmap = BitmapFactory.decodeFile(jpegPath);

        if (bitmap == null) {
            Log.i(appTag,"Bitmap0 reference is null");
        }else{
                   //scale the input bitmap
                    Log.i(appTag,"Scale bitmap started.");
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    float scaleWidth = ((float) dim) / width;
                    float scaleHeight = ((float) dim) / height;
                    // CREATE A MATRIX FOR THE MANIPULATION
                    Matrix matrix = new Matrix();
                    // RESIZE THE BIT MAP
                    matrix.postScale(scaleWidth, scaleHeight);

                    // "RECREATE" THE NEW BITMAP
                    Bitmap resizedBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, width, height, matrix, false);
                    bitmap.recycle();
                    //end of scale
                    Log.i(appTag,"resized size "+resizedBitmap.getWidth()+"*"+resizedBitmap.getHeight());

                    return resizedBitmap;
        }
        return null;
    }


    private List<NeuralNetwork.Runtime> getSupportedRuntimes() {
        final List<NeuralNetwork.Runtime> result = new LinkedList<>();
        final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder(mApplication);
        for (NeuralNetwork.Runtime runtime : NeuralNetwork.Runtime.values()) {
            if (builder.isRuntimeSupported(runtime)) {
                result.add(runtime);
            }
        }
        return result;
    }

    @Override
    protected void onViewDetached(ModelOverviewFragment view) {
        if (mNeuralNetwork != null) {
            mNeuralNetwork.release();
            mNeuralNetwork = null;
        }
    }

    public void onBitmapLoaded(File imageFile, Bitmap bitmap) {
        mBitmapCache.put(imageFile.getAbsolutePath(), new SoftReference<>(bitmap));
        if (isAttached()) {
            getView().addSampleBitmap(bitmap);
        }
    }

    public void onNetworkLoaded(NeuralNetwork neuralNetwork, final long loadTime) {
        if (isAttached()) {
            mNeuralNetwork = neuralNetwork;
            ModelOverviewFragment view = getView();
            view.setNetworkDimensions(getInputDimensions());
            view.setOutputLayersNames(neuralNetwork.getOutputLayers());
            view.setModelVersion(neuralNetwork.getModelVersion());
            view.setLoadingNetwork(false);
            view.setModelLoadTime(loadTime);
        } else {
            neuralNetwork.release();
        }
        mLoadTask = null;
    }

    public void onNetworkLoadFailed() {
        if (isAttached()) {
            ModelOverviewFragment view = getView();
            view.displayModelLoadFailed();
            view.setLoadingNetwork(false);
        }
        mLoadTask = null;
        mNetworkTensorFormat = null;
    }

    public void classify(final Bitmap bitmap) {
        if (mNeuralNetwork != null) {
            AbstractClassifyImageTask task;
            switch (mNetworkTensorFormat) {
                case UB_TF8:
                    task = new ClassifyImageWithUserBufferTf8Task(this, mNeuralNetwork, bitmap, mModel);
                    break;
                case FLOAT:
                default:
                    task = new ClassifyImageWithFloatTensorTask(this, mNeuralNetwork, bitmap, mModel);
                    break;
            }
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        } else {
            getView().displayModelNotLoaded();
        }
    }

    public void onClassificationResult(String[] labels, long javaExecuteTime) {
        if (isAttached()) {
            ModelOverviewFragment view = getView();
            view.setClassificationResult(labels);
            view.setJavaExecuteStatistics(javaExecuteTime);
        }
    }

    public void onClassificationFailed() {
        if (isAttached()) {
            getView().displayClassificationFailed();
            getView().setJavaExecuteStatistics(-1);
        }
    }

    public void setTargetRuntime(NeuralNetwork.Runtime runtime) {
        mRuntime = runtime;
    }

    public void setTensorFormat(SupportedTensorFormat format) {
        mCurrentSelectedTensorFormat = format;
    }

    public void loadNetwork() {
        // added SC 04/03/19 load sample images when load network
        ModelOverviewFragment view0 = getView();
        loadImageSamples(view0);
        Log.i(appTag, "Done load image samples.");


        if (isAttached()) {
            ModelOverviewFragment view = getView();
            view.setLoadingNetwork(true);
            view.setNetworkDimensions(null);
            view.setOutputLayersNames(new HashSet<String>());
            view.setModelVersion("");
            view.setModelLoadTime(-1);
            view.setJavaExecuteStatistics(-1);
            view.setClassificationHint();

            final NeuralNetwork neuralNetwork = mNeuralNetwork;
            if (neuralNetwork != null) {
                neuralNetwork.release();
                mNeuralNetwork = null;
            }

            if (mLoadTask != null) {
                mLoadTask.cancel(false);
            }

            mNetworkTensorFormat = mCurrentSelectedTensorFormat;
            mLoadTask = new LoadNetworkTask(mApplication, this, mModel, mRuntime, mCurrentSelectedTensorFormat);
            mLoadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    private int[] getInputDimensions() {
        Set<String> inputNames = mNeuralNetwork.getInputTensorsNames();
        Iterator<String> iterator = inputNames.iterator();
        return iterator.hasNext() ? mNeuralNetwork.getInputTensorsShapes().get(iterator.next()) : null;
    }

}
