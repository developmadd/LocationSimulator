package com.madd.madd.locationsimulator;


import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class CommunicationHandler extends Handler {

    private OnReceiveMessage onReceiveMessage;

    CommunicationHandler(OnReceiveMessage onReceiveMessage){
        this.onReceiveMessage = onReceiveMessage;
    }


    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        onReceiveMessage.handleMessage(msg);
    }


    interface OnReceiveMessage{
        void handleMessage(Message message);
    }

}
