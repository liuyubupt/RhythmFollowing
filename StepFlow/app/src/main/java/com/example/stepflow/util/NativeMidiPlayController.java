package com.example.stepflow.util;


import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;


import com.sun.swing.internal.plaf.synth.resources.synth;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;


import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.Synthesizer;

public class NativeMidiPlayController {

    private Sequence seq;
    private Sequencer midi;
    private SoftSynthesizer synth;
    private Receiver recv;
    //整个序列的时间长度
    private static long microSecondLength;
    //四分之一音符的长度（微秒）
    private float tempoMPQ;
    private static long tickLength;
    private static float beatLength;
    private SoundSwitchListener mSoundSwitchListener;
    public NativeMidiPlayController (InputStream sound, InputStream[]  sfStream, SoundSwitchListener soundSwitchListener) {
        try {
            synth = new SoftSynthesizer();
            synth.open();

            for (int i = 0; i < sfStream.length; i++) {
                synth.loadAllInstruments(new SF2Soundbank(sfStream[i]));
            }

            synth.getChannels()[0].programChange(0);
            synth.getChannels()[1].programChange(1);
            recv = synth.getReceiver();

            MidiSystem.addMidiDevice(synth);

            this.mSoundSwitchListener = soundSwitchListener;


            //下面每次更换音乐都需要更改
            seq = MidiSystem.getSequence(sound);
            midi= MidiSystem.getSequencer();
            midi.open();
            midi.setSequence(seq);



            tempoMPQ = midi.getTempoInMPQ();
            microSecondLength = midi.getMicrosecondLength();
            tickLength = midi.getTickLength();
            beatLength = microSecondLength / tempoMPQ;







        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setMusic (InputStream music) {
        try {
            seq = MidiSystem.getSequence(music);
            midi= MidiSystem.getSequencer();
            midi.open();
            midi.setSequence(seq);


            tempoMPQ = midi.getTempoInMPQ();
            microSecondLength = midi.getMicrosecondLength();
            tickLength = midi.getTickLength();
            beatLength = microSecondLength / tempoMPQ;
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

    }
    public float getBeat () {
        return beatLength * ((float)midi.getTickPosition() / (float) tickLength);
    }

    public float getTempo () {
        if (midi != null) return midi.getTempoInBPM();
        else return 0;
    }

    public void setTempoBPM (float bpm) {
        if (midi != null) midi.setTempoInBPM(bpm);
    }

    public void play() {
        if(!midi.isRunning()) {
            midi.start();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    while (midi.isRunning()) {
                        Thread.sleep(1000);
                    }
                    mSoundSwitchListener.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void stop () {
        if (midi.isRunning()) {
            midi.stop();
        }
    }


    public void sync1 (final float bpm) {
        //beat 当前拍
        float curBeat = (beatLength * ((float)midi.getTickPosition() / (float) tickLength));
        float adjustBMP = bpm;
        //终点偏右 多演奏一拍
        if (curBeat - (long)curBeat >= 0.5){
            adjustBMP = bpm  * (1 + (1 - (curBeat - (long)curBeat)));
        }else {
            adjustBMP = bpm * (1 - (curBeat - (long)curBeat));
        }
        midi.setTempoInBPM(adjustBMP);
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep((long) (60 * 1000 / bpm));
//                    midi.setTempoInBPM(bpm);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public interface SoundSwitchListener {
        void open();
        void close();
    }





}


