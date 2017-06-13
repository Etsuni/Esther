package fr.nover.esther;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nover on 13/12/16.
 */

public class MsgAdapter {

    private LinearLayout mLayout;
    private Context context;
    private ViewGroup container;

    // DEBUG
    public final String TAG = "CustomMsgAdapter";
    public final boolean D = true;

    public MsgAdapter(Context context, LinearLayout mLayout) {
        this.context = context;
        this.mLayout = mLayout;
    }

    Context getContext() { return context; }

    public void setContainer(ViewGroup container) { this.container = container; }

    public void addEntry(int id, String msg) {

        if(mLayout != null) {

            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            View view;

            switch (id) {
                case (1):
                    view = inflater.inflate(R.layout.msg_cid, container, false);
                    break;

                case (2):
                    view = inflater.inflate(R.layout.msg_esther, container, false);
                    break;

                default:
                    view = inflater.inflate(R.layout.msg_cid, container, false);
                    break;
            }

            if(D) Log.d(TAG, "view : "+view);

            TextView txtView = (TextView) view.findViewById(R.id.txtview);
            if(D) Log.d(TAG, "TextView : "+ txtView);
            txtView.setText(msg);
            mLayout.addView(view);
        }
    }
}