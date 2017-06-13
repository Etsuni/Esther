package fr.nover.esther;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by nover on 02/11/16.
 */

public class FragmentHome extends Fragment {

    // View
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));

        Button button  = (Button) rootView.findViewById(R.id.btnCoffeeDouble);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendMessage("EXE_COFFEE_DOUBLE");
                display("Tu vas etre en forme...");
            }
        });

        Button button1  = (Button) rootView.findViewById(R.id.btnCoffeeLong);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendMessage("EXE_COFFEE_LONG");
                display("Let's go for Coffee !");
            }
        });

        Button button2 = (Button) rootView.findViewById(R.id.btnCoffeeCourt);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendMessage("EXE_COFFEE_COURT");
                display("Un café court, un ! !");
            }
        });

        Button button3 = (Button) rootView.findViewById(R.id.btnThe);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendMessage("EXE_THE");
                ((MainActivity) getActivity()).sendMessage("EXE_THE");
                display("Thé partant ?!");
            }
        });

        return rootView;
    }

    void display (String msg) {
        Snackbar mSnackbar = Snackbar.make(rootView.findViewById(R.id.content_home), msg, Snackbar.LENGTH_LONG);

        // On centre le texte
        View mView = mSnackbar.getView();
        TextView mTextView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        mSnackbar.show();
    }

}
