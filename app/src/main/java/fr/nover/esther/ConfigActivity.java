package fr.nover.esther;

/**
 * Created by nover on 29/10/16.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigActivity extends PreferenceActivity {

    private ListPreference prefAppareil;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> devices;

    private List<CharSequence> listeAppareilsArray = new ArrayList<>();
    private CharSequence[] listeAppareils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config_layout);

        prefAppareil = (ListPreference) findPreference("pref_Appareil");

        // Listage des appareils bluetooth Appairés
        devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice blueDevice : devices) { listeAppareilsArray.add( new String (blueDevice.getName()) ); }

        listeAppareils = listeAppareilsArray.toArray(new
                CharSequence[listeAppareilsArray.size()]);

        // Entrée de la liste dans les options
        prefAppareil.setEntries(listeAppareils);
        prefAppareil.setEntryValues(listeAppareils);
    }
}