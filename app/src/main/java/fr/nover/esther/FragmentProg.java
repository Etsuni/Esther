package fr.nover.esther;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by nover on 02/11/16.
 */

public class FragmentProg extends Fragment {

    // Debug
    private static final boolean D = true;
    private static final String TAG = "FragmentProg";

    // View
    private View rootView;
    private TextView tvHeure;
    private TextView tvDate;
    private CheckBox checkRepeat;
    private RelativeLayout repeatLayout;

    // Pour les pickers
    int hour=0;
    int minute=0;
    int day=0;
    int month=0;
    int year=0;

    // Identifiant Dialog
    private static final int TIME_DIALOG_ID = 0;
    private static final int DATE_DIALOG_ID = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Variables
        ListView lstProg;

        // Applique le Fragment correspondant
        rootView = inflater.inflate(R.layout.fragment_prog, container, false);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ));

        repeatLayout = (RelativeLayout) rootView.findViewById(R.id.hiddenRepeat);

        lstProg = (ListView) rootView.findViewById(R.id.listeProg);
        ((MainActivity) getActivity()).displayProg(lstProg,"");

        tvHeure = (TextView) rootView.findViewById(R.id.tvHeure);
        tvHeure.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) { createDialog(TIME_DIALOG_ID).show();
            }
        });

        tvDate = (TextView) rootView.findViewById(R.id.tvDate);
        tvDate.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                createDialog(DATE_DIALOG_ID).show();
            }
        });

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        tvHeure.setText(formatHeure(hour) + " : " + formatHeure(minute));
        tvDate.setText(formatHeure(day)+"/"+formatHeure(month)+"/"+year);

        Button btn = (Button) rootView.findViewById(R.id.set_button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendProg();
            }
        });

        checkRepeat = (CheckBox) rootView.findViewById(R.id.checkProg);
        checkRepeat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // On change l'affichage au click
                if (((CheckBox) v).isChecked()) {
                    repeatLayout.setVisibility(View.VISIBLE);
                    tvDate.setVisibility(View.GONE);
                } else {
                    repeatLayout.setVisibility(View.GONE);
                    tvDate.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    public Dialog createDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(getActivity(), timePickerListener, hour, minute, true);

            case DATE_DIALOG_ID:
                return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
            hour   = hourOfDay;
            minute = minutes;
            tvHeure.setText(formatHeure(hour) + " : " + formatHeure(minute));
        }

    };

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int years, int months, int days) {
            day   = days;
            month = months;
            year = years;
            tvDate.setText(formatHeure(day)+"/"+formatHeure(month)+"/"+year);
        }

    };

    String formatHeure(int num) {
        if ( num<10 ) { return  "0"+num; }
        else { return ""+num; }
    }

    boolean[] getDaysChecked() {
        boolean[] daysChecked = new boolean[7];
        Arrays.fill(daysChecked, Boolean.FALSE);

        if( ((CheckBox) rootView.findViewById(R.id.lundi)).isChecked() ) daysChecked[0]=true;
        if( ((CheckBox) rootView.findViewById(R.id.mardi)).isChecked() ) daysChecked[1]=true;
        if( ((CheckBox) rootView.findViewById(R.id.mercredi)).isChecked() ) daysChecked[2]=true;
        if( ((CheckBox) rootView.findViewById(R.id.jeudi)).isChecked() ) daysChecked[3]=true;
        if( ((CheckBox) rootView.findViewById(R.id.vendredi)).isChecked() ) daysChecked[4]=true;
        if( ((CheckBox) rootView.findViewById(R.id.samedi)).isChecked() ) daysChecked[5]=true;
        if( ((CheckBox) rootView.findViewById(R.id.dimanche)).isChecked() ) daysChecked[6]=true;

        return daysChecked;
    }

    void sendProg() {

        // Programmation récursive
        if (checkRepeat.isChecked()) {
            boolean[] bool = getDaysChecked();
            for(int i=0; i<7; i++){
                if(bool[i]) {
                    if(D) Log.d(TAG,"PROG_ADD_DAILY_"+i+formatHeure(hour)+formatHeure(minute));
                    ((MainActivity) getActivity()).sendMessage("PROG_ADD_DAILY_"+i+formatHeure(hour)+formatHeure(minute));
                }
            }

        // Programmation ponctuelle
        } else {
            if(D) Log.d(TAG,"PROG_ADD_"+formatHeure(day)+formatHeure(month)+year+formatHeure(hour)+formatHeure(minute));
            ((MainActivity) getActivity()).sendMessage("PROG_ADD_"+formatHeure(day)+"_"+formatHeure(month)+"_"+year+"_"+formatHeure(hour)+"_"+formatHeure(minute));
        }

    }

}
