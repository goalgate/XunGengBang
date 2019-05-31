package com.xungengbang.Tool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.xungengbang.R;


public class Alarm {

    private TextView alarmText;

    private AlertView alert;

    private ViewGroup alarmView;

    private Context context;

    private boolean networkIsKnown = false;

    private static Alarm instance = null;

    public static Alarm getInstance(Context context) {
        if (instance == null) {
            instance = new Alarm(context);
        }
        return instance;
    }

    private Alarm(Context context) {
        this.context = context;
        alarmView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.alarm_text, null);
        alarmText = (TextView) alarmView.findViewById(R.id.alarmText);
        alert = new AlertView("", null, null, new String[]{"确定"}, null, context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {

            }
        });
        alert.addExtView(alarmView);
    }




    public void messageAlarm(String msg) {
        alarmText.setText(msg);
        alert.show();
    }
    public void setKnown( boolean known) {
        networkIsKnown = known;
    }

    public interface doorCallback {
        void onTextBack(String msg);

        void onSucc();
    }

    public interface networkCallback {
        void onIsKnown();
        void onTextBack(String msg);
    }


    public void release(){
        instance = null;
    }
}
