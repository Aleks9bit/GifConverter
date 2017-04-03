package com.bargin.alexey.converter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends ActionBarActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    RelativeLayout selectContainer, printLogoContainer, fpsAndLogoContainer, selectFpsContainer, selectPositionContainer, selectColorContainer;
    VideoView capturedImageView;
    Button btnOpen, btnSave, btnOpenGif, btnRecord;
    TextView textInfo, textMaxDur, textCurDur;
    SeekBar timeFrameBar;
    Spinner chooseFps, chooseColor, choosePosition;
    String file_name = "";
    long j = 0;
    byte[] bytes;
    String newPath = "";
    long maxDur;
    CheckBox prinLogo;
    String videoPath, DURATION;
    int fps = 10;
    String position = "Center"; // Center, Bottom right, Bottom left, Top right, Top left
    String color = "Black and White"; // Color, Black and White
    boolean hasLogo = false;
    boolean isAviFormat = false;
    FFmpegMediaMetadataRetriever mediaMetadataRetriever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean perm = false;
        if (checkForPermission())
            perm = true;
        else
            finish();

        selectContainer = (RelativeLayout) findViewById(R.id.selectContainer);
        selectContainer.setVisibility(View.GONE);
        printLogoContainer = (RelativeLayout) findViewById(R.id.printLogoContainer);
        fpsAndLogoContainer = (RelativeLayout) findViewById(R.id.fpsAndLogoContainer);
        fpsAndLogoContainer.setVisibility(View.GONE);
        prinLogo = (CheckBox) findViewById(R.id.printLogo);
        prinLogo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                hasLogo = b;
                int visibility;
                if (b) visibility = View.VISIBLE;
                else visibility = View.GONE;
                selectContainer.setVisibility(visibility);
            }
        });

        btnOpenGif = (Button) findViewById(R.id.openGifInBrowser);
        btnOpenGif.setVisibility(View.GONE);

        chooseFps = (Spinner) findViewById(R.id.chooseFps);
        chooseColor = (Spinner) findViewById(R.id.chooseColor);
        choosePosition = (Spinner) findViewById(R.id.choosePosition);

        setSpinnerAdapter();

        btnOpen = (Button) findViewById(R.id.open);
        btnSave = (Button) findViewById(R.id.save);
        btnSave.setVisibility(View.GONE);
        textInfo = (TextView) findViewById(R.id.info);
        textInfo.setVisibility(View.GONE);
        textMaxDur = (TextView) findViewById(R.id.maxdur);
        textMaxDur.setVisibility(View.GONE);
        textCurDur = (TextView) findViewById(R.id.curdur);
        textCurDur.setVisibility(View.GONE);
        timeFrameBar = (SeekBar) findViewById(R.id.timeframe);

        capturedImageView = (VideoView) findViewById(R.id.capturedimage);

        mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();

        btnOpen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean permission = checkForPermission();
                if (permission) {
                    textCurDur.setVisibility(View.GONE);
                    btnOpenGif.setVisibility(View.GONE);

                    if (Build.VERSION.SDK_INT >= 20) { //TODO for customer!!!
//                    if (Build.VERSION.SDK_INT >= 23) {   //TODO for me!!!
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("video/*");
                        startActivityForResult(intent, 101);
                    } else {
                        Intent videoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        videoIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        videoIntent.setType("video/*");
                        videoIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
                        startActivityForResult(videoIntent, 101);
                    }
                }
            }
        });

        btnRecord = (Button) findViewById(R.id.btnRecord);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        btnOpen.setWidth(width / 2 - 50);
        btnRecord.setWidth(width / 2 - 50);
        selectColorContainer = (RelativeLayout) findViewById(R.id.selectColorContainer);
        selectFpsContainer = (RelativeLayout) findViewById(R.id.selectFpsContainer);
        selectPositionContainer = (RelativeLayout) findViewById(R.id.selectPositionContainer);
        selectColorContainer.getLayoutParams().width = width / 2 - 50;
        selectFpsContainer.getLayoutParams().width = width / 2 - 50;
        selectPositionContainer.getLayoutParams().width = width / 2 - 50;
        printLogoContainer.getLayoutParams().width = width / 2 - 50;
        chooseFps.setDropDownWidth(selectFpsContainer.getLayoutParams().width);

        btnRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean permission = checkForPermission();
                if (permission) {
                    textCurDur.setVisibility(View.GONE);
//                    btnSave.setVisibility(View.VISIBLE);
                    btnOpenGif.setVisibility(View.GONE);
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(intent, 101);
                }
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mediaMetadataRetriever.getMetadata() == null) {
                    Toast.makeText(MainActivity.this, "Please select video", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("ENTER GIF NAME");

                final EditText input = new EditText(MainActivity.this);
                input.setGravity(View.TEXT_ALIGNMENT_CENTER);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file_name = input.getText() + ".gif";
                                if (mediaMetadataRetriever.getMetadata() != null && !String.valueOf(textMaxDur.getText()).isEmpty()
                                        && !input.getText().toString().isEmpty()) {
                                    TaskSaveGIF myTaskSaveGIF = new TaskSaveGIF(timeFrameBar);
                                    myTaskSaveGIF.execute();
                                    fpsAndLogoContainer.setVisibility(View.GONE);
                                    textMaxDur.setVisibility(View.GONE);
                                    textInfo.setVisibility(View.GONE);
                                    chooseFps.setVisibility(View.GONE);
                                    btnSave.setVisibility(View.GONE);
                                    btnRecord.setVisibility(View.GONE);
                                    selectContainer.setVisibility(View.GONE);
                                    btnOpen.setVisibility(View.GONE);
                                } else if (input.getText().toString().isEmpty())
                                    Toast.makeText(MainActivity.this, "Enter file name", Toast.LENGTH_LONG).show();
                                else if (videoPath == null || videoPath.isEmpty())
                                    Toast.makeText(MainActivity.this, "Please select video", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        });

                alertDialog.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();

            }

        });


        timeFrameBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateFrame();

            }
        });


    }

    private void setSpinnerAdapter() {

        ArrayAdapter<?> fpsAdapter =
                ArrayAdapter.createFromResource(this, R.array.fps, android.R.layout.simple_spinner_item);
        fpsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseFps.setAdapter(fpsAdapter);
        chooseFps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fps = Integer.parseInt(String.valueOf(adapterView.getItemAtPosition(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fps = 10;
            }
        });

        ArrayAdapter<?> colorAdapter =
                ArrayAdapter.createFromResource(this, R.array.color, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseColor.setAdapter(colorAdapter);
        chooseColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color = String.valueOf(adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fps = 10;
            }
        });

        ArrayAdapter<?> positionAdapter =
                ArrayAdapter.createFromResource(this, R.array.position, android.R.layout.simple_spinner_item);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choosePosition.setAdapter(positionAdapter);
        choosePosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                position = String.valueOf(adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fps = 10;
            }
        });
    }


    private void updateFrame() {

    }

    private boolean checkForPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                return true;
            } else {
                requestPermission(); // Code for permission
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        newPath = "";
        if (resultCode == RESULT_OK) {
            prinLogo.setChecked(false);
            fpsAndLogoContainer.setVisibility(View.VISIBLE);
            btnOpen.setVisibility(View.VISIBLE);
            btnRecord.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            textMaxDur.setVisibility(View.VISIBLE);
//            textInfo.setVisibility(View.VISIBLE);
            chooseFps.setVisibility(View.VISIBLE);
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            videoPath = cursor.getString(column_index);
            if (videoPath == null)
                videoPath = uri.getPath();
            if (cursor != null) {
                cursor.close();
                if (videoPath != null) {
                    String[] arr = videoPath.split("/");
                    for (int i = 0; i < arr.length - 1; i++) {
                        newPath += arr[i] + "/";
                    }
                }
                String[] arr = videoPath.split("\\.");
                if (arr[arr.length - 1].equals("avi"))
                    isAviFormat = true;
                capturedImageView.setVisibility(View.VISIBLE);
                capturedImageView.setVideoPath(String.valueOf(uri));
                capturedImageView.start();
                capturedImageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (capturedImageView.isPlaying())
                            capturedImageView.pause();
                        else
                            capturedImageView.start();
                        return false;
                    }
                });

//                textInfo.setText(videoPath);

                FFmpegMediaMetadataRetriever tRetriever = new FFmpegMediaMetadataRetriever();

                try {
                    tRetriever.setDataSource(videoPath);

                    mediaMetadataRetriever = tRetriever;
                    mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
                    mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                    //extract duration in millisecond, as String
                    DURATION = mediaMetadataRetriever.extractMetadata(
                            FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                    textMaxDur.setText(((double) Double.parseDouble(DURATION) / 1000) + " sec");
                    //convert to us, as int
                    maxDur = (long) (1000 * Double.parseDouble(DURATION));

                    timeFrameBar.setProgress(0);
                    updateFrame();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,
                            "Something Wrong!",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                btnSave.setVisibility(View.GONE);
            }
        }
        if (resultCode == RESULT_CANCELED) {
            fpsAndLogoContainer.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            selectContainer.setVisibility(View.GONE);
            textInfo.setVisibility(View.GONE);
            textMaxDur.setVisibility(View.GONE);
        }
    }

    public class TaskSaveGIF extends AsyncTask<Void, Integer, String> {

        SeekBar bar;

        public TaskSaveGIF(SeekBar sb) {
            bar = sb;
            Toast.makeText(MainActivity.this,
                    "Generate GIF animation",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            final String thisNewPath = Environment.getExternalStorageDirectory() + "/DCIM/";
            final File outFile = new File(thisNewPath, file_name);
            try {
                FileOutputStream bos = new FileOutputStream(outFile);

                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bytes = genGIF();
                    }
                });
                th.start();
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bos.write(bytes);
                bos.flush();
                bos.close();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textCurDur.setText("Saved in: " + thisNewPath);
                        capturedImageView.setVisibility(View.GONE);
                        btnOpenGif.setVisibility(View.VISIBLE);

                        btnRecord.setVisibility(View.VISIBLE);
                        btnOpen.setVisibility(View.VISIBLE);
                        btnOpenGif.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(outFile));
                                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(outFile).toString());
                                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                intent.setDataAndType(Uri.fromFile(outFile), mimetype);
                                startActivity(intent);
//                                GifActivity.gif_path = newPath;                                 //TODO open gif in app
//                                GifActivity.gif_name = file_name;                               //TODO open gif in app
//                                startActivity(new Intent(MainActivity.this, GifActivity.class));//TODO open gif in app
                            }
                        });
                    }
                });
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));
                return (outFile.getAbsolutePath() + " Saved");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this,
                    result,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            bar.setProgress(values[0]);
            updateFrame();
        }

        private byte[] genGIF() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textCurDur.setText("");
                    textCurDur.setVisibility(View.VISIBLE);
                }
            });
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
            animatedGifEncoder.setRepeat(0);
            animatedGifEncoder.start(bos);
            animatedGifEncoder.setTransparent(5);
            animatedGifEncoder.setFrameRate(Float.parseFloat(String.valueOf(fps)));
            long diff = 1000000 / fps;
            int width, height;
            width = 320;
            height = 240;
            long duration = (Integer.parseInt(DURATION)) * 1000L;
            if (duration > 5000000) duration = 5000000;
            final double k = 100D / (((double) duration / 1000000) * fps);
            for (long i = 0; i < duration; i += diff) {
                animatedGifEncoder.addFrame(addWaterMark(mediaMetadataRetriever
                        .getScaledFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST, width, height)));
                publishProgress((int) (j * k));
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textCurDur.setText("Creating .GIF " + (int) (k * j) + " %");
                    }
                });
                j += 1;
            }
            j = 0;
            publishProgress(100);
            animatedGifEncoder.finish();
            return bos.toByteArray();
        }
    }

    private boolean checkForOrientation() {
        String rotationKey = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        int rotationValue = Integer.parseInt(rotationKey);
        return rotationValue == 0 || rotationValue == 180;
    }

    private Bitmap addWaterMark(Bitmap src) {
        if (!isAviFormat)
            if (!checkForOrientation())
                src = rotateBitmap(src, Integer.parseInt(mediaMetadataRetriever
                        .extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)));
        int backgroundWidth = src.getWidth();
        int backgroundHeight = src.getHeight();
        Bitmap result = Bitmap.createBitmap(backgroundWidth, backgroundHeight, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Bitmap logo = null;
        int logoResource = R.drawable.customlogo;
        if (color.equals("Color"))
            logoResource = R.drawable.customlogo;
        else if (color.equals("Black and White"))
            logoResource = R.drawable.bawlogo;
        Bitmap logoDrawable = BitmapFactory.decodeResource(getResources(), R.drawable.bawlogo);
        Bitmap smallLogoDrawable = BitmapFactory.decodeResource(getResources(), R.drawable.smallbawlogo);
        int smallLogoWidth = smallLogoDrawable.getWidth();
        int smallLogoHeight = smallLogoDrawable.getHeight();
        int logoWidth = logoDrawable.getWidth();
        int logoHeight = logoDrawable.getHeight();
        int x = 0, y = 0;
        switch (position) {
            case "Center":
                x = backgroundWidth / 2 - logoWidth / 2;
                y = backgroundHeight / 2 - logoHeight / 2;
                break;
            case "Bottom right":
                x = backgroundWidth - smallLogoWidth;
                y = backgroundHeight - smallLogoHeight;
                if (logoResource == R.drawable.customlogo)
                    logoResource = R.drawable.smallcustomlogo;
                else if (logoResource == R.drawable.bawlogo)
                    logoResource = R.drawable.smallbawlogo;
                break;
            case "Bottom left":
                x = 0;
                y = backgroundHeight - smallLogoHeight;
                if (logoResource == R.drawable.customlogo)
                    logoResource = R.drawable.smallcustomlogo;
                else if (logoResource == R.drawable.bawlogo)
                    logoResource = R.drawable.smallbawlogo;
                break;
            case "Top left":
                x = 0;
                y = 0;
                if (logoResource == R.drawable.customlogo)
                    logoResource = R.drawable.smallcustomlogo;
                else if (logoResource == R.drawable.bawlogo)
                    logoResource = R.drawable.smallbawlogo;
                break;
            case "Top right":
                x = backgroundWidth - smallLogoWidth;
                y = 0;
                if (logoResource == R.drawable.customlogo)
                    logoResource = R.drawable.smallcustomlogo;
                else if (logoResource == R.drawable.bawlogo)
                    logoResource = R.drawable.smallbawlogo;
                break;
        }
        logo = BitmapFactory.decodeResource(getResources(), logoResource);
        Paint paint = new Paint();
        paint.setAlpha(70);
        canvas.drawBitmap(logo, x, y, paint);
        if (hasLogo)
            return result;
        else
            return src;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}