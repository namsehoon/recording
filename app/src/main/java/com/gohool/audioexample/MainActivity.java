package com.gohool.audioexample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //녹음
    private FloatingActionButton add;
    private EditText edit;
    private MediaRecorder mediaRecorder;
    private SimpleDateFormat simpleDateFormat;
    private boolean isRecording = false;

    //재생
    private boolean isPlaying = false;
    private Button play;
    private SeekBar seekBar;

    private MediaPlayer mediaPlayer;


    private FrameLayout frameLayout;





    private int count;//버튼 클릭횟수에 따른 메소드를 받기 위한 인자


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit = (EditText) findViewById(R.id.edit);
        add = (FloatingActionButton) findViewById(R.id.add);
        String state = Environment.getExternalStorageState();//읽기, 쓰기 가능 체크
        add.setOnClickListener(this);

        frameLayout = (FrameLayout) findViewById(R.id.frame);




        count = 0;
        permission();

        if(state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "읽기,쓰기가능", Toast.LENGTH_SHORT).show();
        }


    }






    @RequiresApi(api = Build.VERSION_CODES.O)
    private void readyAndGo(){
        //파일 이름에 날짜, 이름 추가
        simpleDateFormat = new SimpleDateFormat("MM-dd");
        String date = simpleDateFormat.format(new Date());
        String Filename = "Memo : "+ date + ".3gp";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Filename;
        
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //녹화 스트림 설정
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//아웃풋 포맷 설정
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//인코더 설정

        //Operator '+' cannot be applied to 'java.io.File', 'long'
        // start, stop에서 Illegalstatement 에러가 순차적으로 나서 3일 동안 고생한 결과, mediaRecorder에는 문제가 없었고, 저장경로를 설정하는데 오류가 있었다.
        mediaRecorder.setOutputFile(filePath);
        try {
            mediaRecorder.prepare();
        }catch (Exception e){
            e.printStackTrace();

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        if(count == 0 && !isRecording){
            readyAndGo();
            mediaRecorder.start(); //녹음시작
            Toast.makeText(this, "녹음", Toast.LENGTH_SHORT).show();
            isRecording = true;
            count++;
        }else if(count == 1 && isRecording) {
            if(mediaRecorder != null){
                mediaRecorder.stop(); //녹음 종료
                mediaRecorder.release();
                mediaRecorder = null;
                Button playBtn = new Button(this);
                playBtn.setText("Play");
                int cursorPosition =  edit.getSelectionStart();
                //TODO 위치값 가져와서 한칸 띄운다음에 버튼은 사이에 집어넣고 ,text도 그만큼 띄어 주고
                System.getProperty("line.separator");
                frameLayout.addView(playBtn, cursorPosition, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            isRecording = false;
            count = 0;
            Toast.makeText(this, "저장", Toast.LENGTH_SHORT).show();
        }
    }//getResources().getDrawable(R.drawable.ic_baseline_fiber_manual_record_24








    public void permission(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport
                                                             multiplePermissionsReport) {
                        // 권한 여부를 다 묻고 실행되는 메소드
                        if(multiplePermissionsReport.areAllPermissionsGranted()){
                            Toast.makeText(MainActivity.this, "모든 권한 허용", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                        showRefused();
                    }



                })
                .check();

    }


    private void showRefused(){ // 권한 거부시 Alert 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("녹음 권한 필요");
        builder.setMessage("음성녹음 사용을 위해 권한이 필요합니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSetting(); //권한 설정 페이지
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();


            }
        });
        builder.show();
    }

    private void openSetting(){ // 권한 설정 페이지 이동
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
        startActivity(intent);
    }


}