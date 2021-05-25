package com.example.stepflow.util;

import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;

/**
 * tempo算法（阶段性平均值）
 * 维护跑步速度相关参量，并转化成tempo
 * 构造方法可指定frequency，单位ms
 * tempo将来自于frequency内的平均水平
 */
public class TempoAlgorithm{
    //平均间隔点
    private volatile double averageInterval = 0;
    //间隔点和
    private volatile double intervalSum = 0;
    //一拍有多少ms oneBeatTime = averageInterval * 20ms
    private volatile double oneBeatTime = 0;
    //tempo = 60 * 1000d / oneBeatTime
    private volatile double tempo = 0;
    //标识位，是否启动
    private volatile boolean start = false;
    //间隔点数队列，先入先出，
    private LinkedList<Integer> countQueue;
    //队列最大size,会通过最近的maxSize步来计算稳定tempo
    private long maxSize;
    //用于稳定判定的参量，maxSize步内，每一步的时间记为t,则t_max - t_min > point * 20ms则判定为不稳定
    private long point;

    private volatile boolean stable;

    public TempoAlgorithm () {
        this(10, 120, 3);
    }

    public boolean getStable () {
        return stable;
    }



    public TempoAlgorithm (long maxSize, double tempo, long point) {
        if (maxSize * tempo * point == 0) try {
            throw new Exception("参量不可为0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.maxSize = maxSize;
        this.tempo = tempo;
        this.point = point;
        countQueue = new LinkedList<>();
    }

    //获取此时的tempo
    public double getTempo () {
        return tempo;
    }
    public void setTempo (double tempo) {
        this.tempo = tempo;
    }
    //开始执行算法
    public void start() {
        start = true;
    }

    //结束执行算法
    public void stop () {
        start = false;
    }

    //核心算子
    public void count (int count) throws Exception{
        if (start) {
            if (countQueue.size() < maxSize) {
                countQueue.offer(count);
                intervalSum += count;
            }
            else if (countQueue.size() == maxSize){
                Integer poll = countQueue.poll();
                countQueue.offer(count);
                intervalSum = intervalSum - poll + count;
            }else {
                throw new IndexOutOfBoundsException("the count queue is full");
            }
            averageInterval = intervalSum / countQueue.size();
            oneBeatTime = averageInterval * 20;
            if (countQueue.size() == maxSize && stable()) {
                tempo = 60 * 1000d / oneBeatTime;
                Log.v("stable", tempo + "");
            }
        }else {
            throw new StateException("Check that the start method is executed");
        }
    }






    static class StateException extends Exception {
        public StateException (String msg) {
            super(msg);
        }
    }


    private boolean stable() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < countQueue.size(); i++) {
            int num = countQueue.get(i);
            min = Math.min(min, num);
            max = Math.max(max, num);
        }
        Log.v("min", min + "");
        Log.v("max", max + "");
        boolean res = max - min > point ? false : true;
//        Log.v("res", res + "");
        stable = res;
        return res;


    }
}
