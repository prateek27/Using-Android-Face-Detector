package com.example.facedetection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.facedetectionexample.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class FaceDetectionExample extends Activity {
    private static final int TAKE_PICTURE_CODE = 100;
    private static final int MAX_FACES = 5;
    
    private Bitmap cameraBitmap = null;
    
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    ((Button)findViewById(R.id.take_picture)).setOnClickListener(btnClick);
}

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
    
            if(TAKE_PICTURE_CODE == requestCode){
                    processCameraImage(data);
            }
    }

private void openCamera(){
    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    
    startActivityForResult(intent, TAKE_PICTURE_CODE);
}

private void processCameraImage(Intent intent){
    setContentView(R.layout.detectlayout);
    
    ((Button)findViewById(R.id.detect_face)).setOnClickListener(btnClick);
    
    ImageView imageView = (ImageView)findViewById(R.id.image_view);
    
    cameraBitmap = (Bitmap)intent.getExtras().get("data");
    
    imageView.setImageBitmap(cameraBitmap);
}

private void detectFaces(){
    if(null != cameraBitmap){
            int width = cameraBitmap.getWidth();
            int height = cameraBitmap.getHeight();
            
            //For Exception handling , odd width throws exception .
            if(width%2!=0)
            	width = width-1;
            
            FaceDetector detector = new FaceDetector(width, height,FaceDetectionExample.MAX_FACES);
            Face[] faces = new Face[FaceDetectionExample.MAX_FACES];
            
            Bitmap bitmap565 = Bitmap.createBitmap(width, height, Config.RGB_565);
            Paint ditherPaint = new Paint();
            Paint drawPaint = new Paint();
            
            Paint myPaint = new Paint();
            myPaint.setColor(Color.RED);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);
            

            Paint eyePaint = new Paint();
            eyePaint.setColor(Color.BLUE);
            eyePaint.setStyle(Paint.Style.STROKE);
            eyePaint.setStrokeWidth(1);
            
            ditherPaint.setDither(true);
            drawPaint.setColor(Color.argb(0,255,192,0));
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(2);
            
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap565);
            canvas.drawBitmap(cameraBitmap, 0, 0, ditherPaint);
            
            int facesFound = detector.findFaces(bitmap565, faces);
            PointF midPoint = new PointF();
            float eyeDistance = 0.0f;
            float confidence = 0.0f;
            
            Toast.makeText(getApplicationContext(), "Faces Found "+facesFound, Toast.LENGTH_LONG).show();
            Log.i("FaceDetector", "Number of faces found: " + facesFound);
            
            if(facesFound > 0)
            {
                    for(int index=0; index<facesFound; ++index){
                    		
                            faces[index].getMidPoint(midPoint);
                            eyeDistance = faces[index].eyesDistance();
                            confidence = faces[index].confidence();
                            
                            Log.i("FaceDetector", 
                                            "Confidence: " + confidence + 
                                            ", Eye distance: " + eyeDistance + 
                                            ", Mid Point: (" + midPoint.x + ", " + midPoint.y + ")");
                            
                            canvas.drawCircle(midPoint.x,midPoint.y, (float)1.5*eyeDistance, myPaint);
                           /* canvas.drawRect((int)midPoint.x - eyeDistance , 
                                                            (int)midPoint.y - eyeDistance , 
                                                            (int)midPoint.x + eyeDistance, 
                                                            (int)midPoint.y + eyeDistance, drawPaint);
                    */
                            canvas.drawCircle((float)(midPoint.x-eyeDistance/2),(float)(midPoint.y-eyeDistance/8),(float)eyeDistance/(float)2.5,eyePaint);
                            canvas.drawCircle(midPoint.x+eyeDistance/2,midPoint.y-eyeDistance/8,(float)eyeDistance/(float)2.5,eyePaint);
                            
                    }
            }
            
            String filepath = Environment.getExternalStorageDirectory() + "/facedetect" + System.currentTimeMillis() + ".jpg";
            
                    try {
                            FileOutputStream fos = new FileOutputStream(filepath);
                            
                            bitmap565.compress(CompressFormat.JPEG, 90, fos);
                            
                            fos.flush();
                            fos.close();
                    } catch (FileNotFoundException e) {
                            e.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                    
                    ImageView imageView = (ImageView)findViewById(R.id.image_view);
                    
                    imageView.setImageBitmap(bitmap565);
    }
}

    private View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    switch(v.getId()){
                            case R.id.take_picture:         openCamera();   break;
                            case R.id.detect_face:          detectFaces();  break;  
                    }
            }
    };
}