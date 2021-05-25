package com.example.stepflow;


import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;
import android.media.MediaPlayer;

import android.os.Bundle;


import com.example.stepflow.util.NativeMidiPlayController;
import com.example.stepflow.util.PermissionUtil;
import com.example.stepflow.util.TempoAlgorithm;


import androidx.appcompat.app.AppCompatActivity;


import android.os.Environment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements SensorEventListener, NativeMidiPlayController.SoundSwitchListener {
    private SensorManager sensorManager;
    private TextView speedX;
    private TextView speedY;
    private TextView speedZ;
    private TextView gyroscopeX;
    private TextView gyroscopeY;
    private TextView gyroscopeZ;
    private TextView tempoTxt;
    private EditText fileEdit;

    private Switch pointMonitor;
    private Switch pointSound;
    private Switch music;
    private Switch sensor;
    private Switch data;
    private Switch stepBeat;
    private Spinner musicSelect;
    private Spinner pointSoundSelect;


    private volatile boolean pointMonitorBln;
    private volatile boolean pointSoundBln;
    private volatile boolean sensorBln;
    private volatile boolean musicBln;
    private volatile boolean dataBln;
    private volatile boolean stepBeatBln;

    private volatile int num = 0;
    //用于控制传感器输出顺序
    private volatile int writeNum = 1;
    private volatile int offerNum = 1;

    private volatile int currentMusic = 0;

    //记录步数
    private volatile long stepDuringMonitor;






    //逻辑回归参数，历史5组数据（每组6维，包括当前预估维度）估算当前组数据是否为落地点0/1
    private double[] param = new double[]{
            -2.07381144e-02,
            -1.22071307e-02,
            6.29390719e-03,
            4.05514731e-03,
            -1.04845404e-02,
            7.08496630e-05,
            9.65668631e-03,
            2.28241785e-03,
            9.41933691e-03,
            9.19214801e-04,
            -4.78679054e-04,
            -9.71925443e-03,
            1.89886695e-02,
            7.55732303e-03,
            6.09323586e-04,
            -1.37686771e-03,
            3.09695663e-03,
            -9.37875976e-03,
            1.81772761e-02,
            4.59479680e-03,
            -4.58279655e-03,
            -2.92221186e-03,
            5.45357226e-04,
            -1.27057855e-03,
            2.00126348e-02,
            -3.69312644e-04,
            -1.45420161e-02,
            -7.79661728e-03,
            -1.22852272e-03,
            8.37864596e-03
    };
    //用于下面filter方法，用来将逻辑回归得到的数簇1，处理成离散的1
    volatile int count = 12;
    //用于存放传感器产生的数据，满30个数据，则进行计算。伴有传感器数据的出队和入队（虽然不能指定size，但使用时size_max = 30，即全部5组历史数据）
    static LinkedList<Double> queue = new LinkedList<Double>();


    //tempoAlgorithm
    private TempoAlgorithm mTempoAlgorithm = new TempoAlgorithm();
    //tempCount用于临时记录两个1之间的间隔点数量
    private volatile int tempCount = 0;
    private int bit = 1;
    private NativeMidiPlayController player;
    private TempoTask mTempoTask = new TempoTask();
    private ExecutorService mTempoExecutorService = Executors.newSingleThreadExecutor();
    private ExecutorService mSoundExecutorService = Executors.newSingleThreadExecutor();
    private ExecutorService mStepPeatExecutorService = Executors.newSingleThreadExecutor();

    //用于收集跑步打点数据的文件名txt，默认stepdata.txt
    private String pathname;

    private MediaPlayer mMediaPlayer = null;
    private MediaPlayer mp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        initView();
        initEvent();
        PermissionUtil.verifyStoragePermissions(this);
    }


    private void initEvent() {

        //传感器注册
        sensorManager.unregisterListener(MainActivity.this,sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        sensorManager.unregisterListener(MainActivity.this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.registerListener(MainActivity.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(MainActivity.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);






        pointMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pointMonitorBln = true;
                    //开始收集稳定速度
                    mTempoAlgorithm.start();
                    Toast.makeText(MainActivity.this, "开启监听", Toast.LENGTH_SHORT).show();
                }
                else {
                    pointMonitorBln = false;
                    pointSound.setChecked(false);
                    Toast.makeText(MainActivity.this, "停止监听", Toast.LENGTH_SHORT).show();
                    stepDuringMonitor = 0;
                }
            }
        });
        pointSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (! pointMonitor.isChecked()) pointMonitor.setChecked(true);
                    pointSoundBln = true;
                }
                else pointSoundBln = false;
            }
        });
        music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTempoAlgorithm.setTempo(120);
                    player.play();

                    musicBln = true;
                }
                else {
                    musicBln = false;
                    player.stop();
                }
            }
        });
        //数据是否展示
        sensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorBln = true;
                }
                else {
                    sensorBln = false;
                }
            }
        });

        data.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pathname = fileEdit.getText().toString();
                    music.setChecked(false);
                    if (pointMonitor.isChecked()) pointMonitor.setChecked(false);

                    dataBln = true;
                }
                else {
                    dataBln = false;
                }
            }
        });

        stepBeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (! pointMonitor.isChecked()) pointMonitor.setChecked(true);
                    if (! music.isChecked()) music.setChecked(true);
                    stepBeatBln = true;
                }else {
                    stepBeatBln = false;
                }
            }
        });


        musicSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (! musicBln) {
                    switch (position) {
                        case 0:
                            try {
                                player.setMusic(getAssets().open("doudizhu.mid"));
                                currentMusic = 0;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            try {
                                player.setMusic(getAssets().open("AUD_HTX0551.mid"));
                                currentMusic = 1;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            try {
                                player.setMusic(getAssets().open("王妃.mid"));
                                currentMusic = 2;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            try {
                                player.setMusic(getAssets().open("活着.mid"));
                                currentMusic = 3;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }else {
                    musicSelect.setSelection(currentMusic);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        pointSoundSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mp = MediaPlayer.create(MainActivity.this, R.raw.step);
                        break;
                    case 1:
                        mp = MediaPlayer.create(MainActivity.this, R.raw.drum);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initView() {
        speedX = findViewById(R.id.speed_x_data);
        speedY = findViewById(R.id.speed_y_data);
        speedZ = findViewById(R.id.speed_z_data);

        gyroscopeX = findViewById(R.id.gyroscope_x_data);
        gyroscopeY = findViewById(R.id.gyroscope_y_data);
        gyroscopeZ = findViewById(R.id.gyroscope_z_data);;

        fileEdit = findViewById(R.id.fileEdit);

        pointMonitor = findViewById(R.id.point_monitor);
        pointSound = findViewById(R.id.point_sound);
        music = findViewById(R.id.music);
        sensor = findViewById(R.id.sensor);
        data = findViewById(R.id.data);
        stepBeat = findViewById(R.id.step_beat);

        pointSoundSelect = findViewById(R.id.point_sound_select);
        pointSoundSelect.setSelection(0);
        musicSelect = findViewById(R.id.music_select);
        musicSelect.setSelection(0);


        tempoTxt = findViewById(R.id.tempoTxt);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        mPointSoundTask = new PointSoundTask(this);
        mMediaPlayer = MediaPlayer.create(this, R.raw.step);
        mp = MediaPlayer.create(this, R.raw.step);

        try {
            player = new NativeMidiPlayController(getAssets().open("doudizhu.mid"), new InputStream[]{getAssets().open("SmallTimGM6mb.sf2")}, this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = 0;
        double y = 0;
        double z = 0;
        double x1 = 0;
        double y1 = 0;
        double z1 = 0;
        if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION)
        {
            float[] values=event.values;

            if (sensorBln) {
                speedX.setText(Float.toString(values[0]));
                speedY.setText(Float.toString(values[1]));
                speedZ.setText(Float.toString(values[2]));
            }
            x = values[0];
            y = values[1];
            z = values[2];

            if (dataBln && writeNum == 1) {
                writeData(x + " " + y + " " + z + " ", pathname);
                writeNum = 2;
            }
            if (queue.size() == 30) {
                for (int i = 0; i < 6; i++) {
                    queue.poll();
                }
            }
            if (offerNum == 1) {
                queue.offer(x);
                queue.offer(y);
                queue.offer(z);
                offerNum = 2;
            }

        }
        else if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            float[] values=event.values;

            if (sensorBln) {
                gyroscopeX.setText(Float.toString(values[0]));
                gyroscopeY.setText(Float.toString(values[1]));
                gyroscopeZ.setText(Float.toString(values[2]));
            }
            x1 = values[0];
            y1 = values[1];
            z1 = values[2];
            if (dataBln && writeNum == 2) {
                writeData(x1 + " " + y1 + " " + z1 + " " + num + "\n", pathname);
                writeNum = 1;
            }
            if (offerNum == 2) {
                queue.offer(x1);
                queue.offer(y1);
                queue.offer(z1);
                offerNum = 1;
            }
            num = 0;
        }

        //落地点检测核心代码
        if (pointMonitorBln) {

            if (queue.size() == 30 && filter() == 1) {
                if (pointSoundBln && mp != null) {
                    mp.start();
                }
                mTempoExecutorService.submit(mTempoTask);
                tempoTxt.setText("tempo: " + mTempoAlgorithm.getTempo());

                if (bit % 4 == 0) {
                    player.sync1((float) mTempoAlgorithm.getTempo());
                    bit = 0;
                }else if (bit % 4 == 1) {
                    player.setTempoBPM((float) mTempoAlgorithm.getTempo());
                }
                bit++;

                //打印落地点与拍点对应情况
                stepDuringMonitor++;
                if (pointMonitorBln && musicBln && stepBeatBln && player != null) mStepPeatExecutorService.submit(new StepAndPeatTask(stepDuringMonitor, player.getBeat(), player.getTempo(), tempCount));


            }
        }

    }





    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        destroy();
        sensorDestroy();
    }
    public void destroy () {
        mTempoAlgorithm.stop();
        pointSound.setChecked(false);
        pointMonitor.setChecked(false);
        music.setChecked(false);
        data.setChecked(false);

    }
    public void sensorDestroy () {
        sensor.setChecked(false);
        sensorManager.unregisterListener(this);
    }


    //落地点判定逻辑
    public int filter () {
        double b = 0d;
        for (int i = 0; i < 30; i++) {
            b += (queue.get(i) * param[i]);
        }
        Log.v("res", sigmoid(b) + "");
        if (sigmoid(b) >= 0.53 && count >= 12) {
            tempCount = count;
            count = 0;
            return 1;
        }
        else {
            count++;
            return 0;
        }

    }
    //sigmoid函数
    public double sigmoid (double x) {
        return 1d / (1 + Math.pow(Math.E, -x));
    }


    //MusicSwitchListener接口
    @Override
    public void open() {

    }

    @Override
    public void close() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                music.setChecked(false);
            }
        });
    }

    //用于跑步过程中生成稳定tempo
    class TempoTask implements Runnable {

        @Override
        public void run() {
            if (mTempoAlgorithm != null) {
                try {
                    mTempoAlgorithm.count(tempCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //向文件中写入步数与拍点的对应关系
    class StepAndPeatTask implements Runnable {
        long step;
        float beat;
        float bpm;
        int time;
        public StepAndPeatTask (long step, float beat, float bpm, int tempCount) {
            this.step = step;
            this.beat = beat;
            this.bpm = bpm;
            this.time = tempCount * 20;
        }
        @Override
        public void run() {
            writeData(step + "步-----" + beat + "拍-----" + bpm + "bpm-----距上一步的时间：" + time + "ms\n", "stepandbeat.txt");
        }
    }

    class PointSoundTask implements Runnable {
        private MediaPlayer mp = null;
        public PointSoundTask (Context context) {
            mp = MediaPlayer.create(context, R.raw.step);
        }
        @Override
        public void run() {
            if (mp != null) mp.start();
        }
    }




    //文件内部写入数据
    public void writeData(String msg, String pathname) {
        try {
            String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/stepdata/";
            File dec = new File(path);
            //tvx.setText(path);
            if (!dec.exists())
            {
                dec.mkdirs();
            }
            File file = new File(path, (pathname == null || pathname == "") ? "stepdata.txt" : pathname);
            if(!file.exists())
            {
                file.createNewFile();
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            bufferedWriter.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //用于收集打点数据
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
//            // 音量减小
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                return true;
            // 音量增大
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (dataBln) {
                    return true;
                }
        }
        return super.onKeyDown (keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // 音量减小
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                Toast.makeText(this, "down", Toast.LENGTH_SHORT).show();
//                num = 1;
//                return true;
            // 音量增大
            case KeyEvent.KEYCODE_VOLUME_UP:
//                if (mMediaPlayer != null) mMediaPlayer.start();
                if (dataBln) {
                    num = 1;
                    return true;
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    //数据记录频率计算（条/秒）
//    public double dataRate () {
//        double second = (stopTime - startTime) / (double)1000;
//        double rate = num / second;
//        return rate;
//    }
//    /**
//     * 合加速度计算，三向平方和开方
//     * @param x
//     * @param y
//     * @param z
//     * @return 返回线性加速度，保留两位小数
//     */
//    public String cube (double x, double y, double z) {
//        double f =  Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
//        return String.format("%.2f", f);
//    }

//    /**
//     * 返回三向加速度，保留两位小数
//     * @param x
//     * @param y
//     * @param z
//     * @return
//     */
//    public String three (double x, double y, double z) {
//        return String.format("%.2f", x) + " " + String.format("%.2f", y) + " " + String.format("%.2f", z);
//        return x + " " + y + " " + z;
//    }


//    public void play()
//    {
//        try
//        {
//            sound = new File("");
//            Log.v("sound", sound.toString());
//            seq = MidiSystem.getSequence(sound);
//            Log.v("seq", seq.toString());
//            midi= MidiSystem.getSequencer();
//            Log.v("midi", midi.toString());
//            midi.open();
//            midi.setSequence(seq);
//
//            if(!midi.isRunning())
//                midi.start();
//
//        } catch (Exception ex) {
//        }
//    }
//
//
//    public void stop()
//    {
//        if(midi.isRunning())
//            midi.stop();
//
//        if(midi.isOpen())
//            midi.close();
//    }


}
