package com.example.photoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button pickBt;
    Button selfieBt;
    ImageView imageView;
    String dir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ "/Folder/";
    File newdir = new File(dir);
    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickBt = findViewById(R.id.pick_image);
        selfieBt = findViewById(R.id.selfieButton);
        imageView= findViewById(R.id.imageView2);
        newdir.mkdirs();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },200);
        }

        selfieBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
                   dispatchTakePictureIntent();
                   galleryAddPic();
//                } catch (IOException e) {
//                    Log.d("Error",e+"\tFile Creation Error");
//                }
//                Uri outputUri = Uri.fromFile(newfile);
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,outputUri);
                //set type
//                startActivityForResult(intent,200);
            }
        });

        pickBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
    }

    private  File createImageFile() throws IOException{
//        String fileName = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString()+".jpg";
//        File image = File.createTempFile(fileName,
//                ".jpg",
//                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.photoeditor",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 200);
            }
        }

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }


    private void checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pickImage();
        }else{
            //device vers below androi 10
            //check condition
            if (permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }else{
                pickImage();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set type
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    private Uri getImageUri(Bitmap src,Bitmap.CompressFormat format, int quality){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format,quality,os);

        String path =MediaStore.Images.Media.insertImage(getContentResolver(),src,"picImage",null);
        return Uri.parse(path);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length>0 && grantResults[0]
        == PackageManager.PERMISSION_GRANTED){
            pickImage();
        }else{
            Toast.makeText(getApplicationContext(),
                    "Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
          Uri uri;
            switch (requestCode){
                case 100:
                    uri = data.getData();
                    Intent intent = new Intent(MainActivity.this,
                            DsPhotoEditorActivity.class);

                    intent.setData(uri);
                    intent.putExtra(
                            DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,"Images");
                    intent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
                            Color.parseColor("#FF6200EE"));
                    intent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR,
                            Color.parseColor("#FFFFFF"));
                    intent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                            new int[]{DsPhotoEditorActivity.TOOL_WARMTH,
                            DsPhotoEditorActivity.TOOL_PIXELATE} );
                    startActivityForResult(intent,101);
                    break;
                case 101:
                    uri = data.getData();
                    imageView.setImageURI(uri);
                    Toast.makeText(getApplicationContext(),
                            "Photo Saved",Toast.LENGTH_SHORT).show();
                    break;
                case 200:
////                    Uri captureImage = (Uri) data.getExtras().get("data");
//                    Bitmap captureImage = (Bitmap) data.getExtras().get("data");
////                    imageView.setImageBitmap(captureImage);
//                    Intent intent1 = new Intent(MainActivity.this,
//                            DsPhotoEditorActivity.class);
//
//                    intent1.setData(getImageUri(captureImage, Bitmap.CompressFormat.PNG,100));
//                    intent1.putExtra(
//                            DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,"Images");
//                    intent1.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
//                            Color.parseColor("#FF6200EE"));
//                    intent1.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR,
//                            Color.parseColor("#FFFFFF"));
//                    intent1.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
//                            new int[]{DsPhotoEditorActivity.TOOL_WARMTH,
//                                    DsPhotoEditorActivity.TOOL_PIXELATE} );
//                    startActivityForResult(intent1,101);
//                    setPic();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    imageView.setImageBitmap(bitmap);
                    break;

            }
        }
    }
}