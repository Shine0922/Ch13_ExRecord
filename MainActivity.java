package com.example.win7.exrecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private TextView txtRec;
    private ImageView imgRecord,imgPlay,imgStop,imgEnd;
    private ListView listRec;

    private MediaPlayer mediaplayer;
    private MediaRecorder mediarecorder;

    private String temFile; //  以日期時間做暫存檔名
    private File recFile,recPATH;
    private List<String> listArray = new ArrayList<String>();   //  檔名陣列
    private int cListItem = 0;  //  目前播放錄音

        @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRec = (TextView)findViewById(R.id.txtRec);
        imgRecord = (ImageView)findViewById(R.id.imgRecord);
        imgPlay = (ImageView)findViewById(R.id.imgPlay);
        imgStop = (ImageView)findViewById(R.id.imgStop);
        imgEnd = (ImageView)findViewById(R.id.imgEnd);
        listRec = (ListView)findViewById(R.id.listRec);

        imgRecord.setOnClickListener(listener);
        imgPlay.setOnClickListener(listener);
        imgStop.setOnClickListener(listener);
        imgEnd.setOnClickListener(listener);

        listRec.setOnItemClickListener(listListener);

        //  SD卡路徑
        recPATH = Environment.getExternalStorageDirectory();

        mediaplayer = new MediaPlayer();

        //  一開始都沒播放東西，所以停止鈕不能按
        imgDisable(imgStop);

        requestStoragePermission();

    }

    private void requestStoragePermission()
    {
        if(Build.VERSION.SDK_INT >= 23) //  安卓6.0以上
        {   //  判斷是否取得憑證
            int haspermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if( haspermission != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,
                                                  Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                return;
            }
        }
        recList();  //  已取得授權
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //按 允許 鈕
                recList();
            } else {
                Toast.makeText(this, "未取得權限！", Toast.LENGTH_SHORT).show();
                finish();  //結束應用程式
            }
        } else  {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //  取得錄音檔案列表
    private void recList()
    {
        listArray.clear();  //  清除陣列
        for(File file:recPATH.listFiles())
        {
            if(file.getName().toLowerCase().endsWith(".amr"))
            {
                listArray.add(file.getName());
            }
        }
        if(listArray.size() > 0)
        {
            ArrayAdapter<String> adaRec = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listArray);
            listRec.setAdapter(adaRec);
        }
    }

    private ImageView.OnClickListener listener = new ImageView.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.imgRecord:
                    try
                    {
                        Calendar calendar = new GregorianCalendar();
                        Date nowtime = new Date();  //  取得現在日期與時間
                        calendar.setTime(nowtime);
                        temFile = "R" + add0(calendar.get(Calendar.YEAR)) + add0(calendar.get(Calendar.MONTH)) + add0(calendar.get(Calendar.DATE))
                                + add0(calendar.get(Calendar.HOUR)) + add0(calendar.get(Calendar.MINUTE)) + add0(calendar.get(Calendar.SECOND));
                        recFile = new File(recPATH + "/" + temFile + ".amr");
                        mediarecorder = new MediaRecorder();
                        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        mediarecorder.setOutputFile(recFile.getAbsolutePath());
                        mediarecorder.prepare();
                        mediarecorder.start();
                        txtRec.setText("    正在錄音......");

                        imgDisable(imgRecord);  //  處理按鈕是否可按
                        imgDisable(imgPlay);
                        imgEnable(imgStop);
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;

                case R.id.imgPlay:
                    playSong(recPATH + "/" + listArray.get(cListItem).toString());
                    break;

                case R.id.imgStop:
                    if(mediaplayer.isPlaying()) //  停止撥放
                        mediaplayer.reset();
                    else if(recFile != null)
                    {
                        mediarecorder.stop();
                        mediarecorder.release();
                        mediarecorder = null;
                        txtRec.setText(" 停止 " + recFile.getName() + " 錄音 ! ");
                        recList();
                    }
                    //  更新哪些按鈕可按 哪些不能按
                    imgEnable(imgRecord);
                    imgEnable(imgPlay);
                    imgDisable(imgStop);
                    break;

                case R.id.imgEnd:   //  結束
                    finish();
                    break;
            }
        }
    };

    //  ListView 監聽事件
    private ListView.OnItemClickListener listListener = new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            cListItem = position;   // 取得點選位置
            playSong(recPATH + "/" + listArray.get(cListItem).toString());   //  播放錄音
        }
    };

    //  playSong 方法
    private void playSong(String path)
    {
        try
        {
            mediaplayer.reset();
            mediaplayer.setDataSource(path);    //  播放路徑
            mediaplayer.prepare();
            mediaplayer.start();    //  開始播放
            txtRec.setText(" 播放 " + listArray.get(cListItem).toString()); // 更新顯示檔名

            //  更新哪些按鈕可按 哪些不能按
            imgDisable(imgRecord);
            imgDisable(imgPlay);
            imgEnable(imgStop);

                //  檢查檔案是否撥放完畢事件
            mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                public void onCompletion(MediaPlayer arg0)
                {
                    txtRec.setText(listArray.get(cListItem).toString() + "播完！");

                    //  更新哪些按鈕可按 哪些不能按
                    imgEnable(imgRecord);
                    imgEnable(imgPlay);
                    imgDisable(imgStop);
                }
            });
        }
        catch (IOException e)
        {

        }
    }
        //  使按鈕有效
    private void imgEnable(ImageView image)
    {
        image.setEnabled(true);
        image.setAlpha(255);
    }

        //  使按鈕無效
    private void imgDisable(ImageView image)
    {
        image.setEnabled(false);
        //  按鈕變變透明
        image.setAlpha(50);
    }
        //  add0 方法
    protected String add0 (int n)
    {   //  各位數前面補0
        if(n<10) return("0"+ n);
        else return("" + n);
    }
}

