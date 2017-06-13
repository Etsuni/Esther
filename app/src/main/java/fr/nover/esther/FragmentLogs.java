package fr.nover.esther;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nover on 13/12/16.
 */

public class FragmentLogs extends Fragment {

    // View
    View rootView;
    ListView lstView;
    public static LinearLayout linearLayout;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    public static ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    public static ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_logs, container, false);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ));

        linearLayout = (LinearLayout) rootView.findViewById(R.id.content_logs);
        lstView = (ListView) rootView.findViewById(R.id.listLogs);

        TextView txtView = new TextView(getActivity());
        txtView.setText("Manual Add");
        txtView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(txtView);

        return rootView;
    }

    public static void addLog(int id, String log, Context context) {
        TextView txtView = new TextView(context);
        txtView.setText(log);
        txtView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(txtView);
    }
}