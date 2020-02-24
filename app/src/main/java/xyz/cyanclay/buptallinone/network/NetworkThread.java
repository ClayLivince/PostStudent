package xyz.cyanclay.buptallinone.network;

import android.util.Pair;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkThread extends Thread{

    private LinkedBlockingQueue<Integer> taskQueue;

    @Override
    public void run(){
        init();

        while(true){
            Integer taskID = taskQueue.poll();
            if (taskID != null){
                switch (taskID){
                    //case
                }
            }
        }
    }

    private void init(){

    }

}
