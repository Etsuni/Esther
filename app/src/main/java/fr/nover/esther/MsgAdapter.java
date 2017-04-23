package fr.nover.esther;

import android.app.Activity;
import android.content.Context;
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

    public MsgAdapter(Context context, LinearLayout mLayout) {
        this.context = context;
        this.mLayout = mLayout;
    }

    public MsgAdapter() {}

    Context getContext() { return context; }

    void setContext(Context context, LinearLayout mLayout) {
        this.context = context;
        this.mLayout = mLayout;
    }

    public void addEntry(int id, String msg) {

        if(mLayout != null) {

            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            View view;

            switch (id) {
                case (1):
                    view = inflater.inflate(R.layout.msg_cid, null);
                    break;

                case (2):
                    view = inflater.inflate(R.layout.msg_esther, null);
                    break;

                default:
                    view = inflater.inflate(R.layout.msg_cid, null);
                    break;
            }

            TextView txtView = (TextView) view.findViewById(R.id.txtview);
            txtView.setText(msg);
            mLayout.addView(view);

        }

    }
}