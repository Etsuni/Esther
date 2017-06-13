package fr.nover.esther;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // DEBUG
    public final String TAG = "CustomMainActivity";
    private static final boolean D = true;
    private static final boolean E = false;
    private static final boolean I = true;
    private static final boolean W = false;

    // Variables Layout
    private DrawerLayout drawer;
    private int nbFragment = 0;
    ListView listeProg;

    // Variables Bluetooth
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public boolean bluetoothOK = false;

    public Cid cid;

    // Configuration
    private SharedPreferences settings;

    // Dialog pour les Logs
    static List<String> listLogsString = new ArrayList<String>();
    static ArrayAdapter adapterLogs;
    static MsgAdapter msgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Définition du Drawer (Ouverture par la gauche du menu)
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        Log.wtf("Test", "test");

        // Déclare la première vue (FragmentHome)
        Fragment fragment = null;
        Class fragmentClass = FragmentHome.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        setTitle(R.string.navigation_drawer_home);

        // Réaction à la sélection d'items du menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Chargement des paramètres
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener myPrefListner = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (D) Log.d(TAG, "onSharedPrefChanged : " + key);
                if (key.equals("pref_Appareil") && cid != null) {
                    cid.stop();
                    cid = null;
                }
                // ToDo: Mettre à jour les paramètres du café
            }
        };
        settings.registerOnSharedPreferenceChangeListener(myPrefListner);

        // Vérification de l'activation du Bluetooth
        if (bluetoothAdapter == null)
            // Pas de bluetooth
            Toast.makeText(this, R.string.alert_bluetooth_none,
                    Toast.LENGTH_SHORT).show();

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth non activé
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            // Bluetooth activé
            bluetoothOK = true;
            display(this.getResources().getString(R.string.alert_bluetooth_connect));
            cid = new Cid(this, mHandlerMain, settings.getString("pref_Appareil", ""));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothOK && cid == null) {
            display(this.getResources().getString(R.string.alert_bluetooth_reconnect) + " " + settings.getString("pref_Appareil", ""));
            cid = new Cid(this, mHandlerMain, settings.getString("pref_Appareil", ""));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cid != null) {
            cid.stop();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        Class fragmentClass = null;
        FragmentManager fragmentManager;

        switch (item.getItemId()) {
            case R.id.nav_home:
                fragmentClass = FragmentHome.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Change de Fragment
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

                // Surligne l'élément sélectionné
                item.setChecked(true);
                // Change le titre
                setTitle(item.getTitle());
                nbFragment = 0;
                break;

            case R.id.nav_prog:
                fragmentClass = FragmentProg.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Change de Fragment
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

                // Surligne l'élément sélectionné
                item.setChecked(true);
                // Change le titre
                setTitle(item.getTitle());
                nbFragment = 0;
                break;

            /*case R.id.nav_update:
                // On récupère la date, on l'envoie et on l'affiche
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df1 = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
                int day = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                String date = df1.format(c.getTime());
                msgAdapter.addEntry(1, "EHHHHHHh");

                sendMessage("HOR_DEF_" + day + date);
                if (D) Log.d(TAG, "HOR_DEF_" + day + date);                                               // ToDo: Erreur NullPointer
                break;*/

            case R.id.nav_get_date:
                // On lui demande la date de l'appareil
                sendMessage("GET_DATE");
                listLogsString.add("Esther : Quelle est la date de CID ?");
                //adapterLogs.notifyDataSetChanged();
                break;

            case R.id.nav_settings:
                // Lance settings activity
                Intent i = new Intent(this, ConfigActivity.class);
                startActivity(i);
                break;

            case R.id.nav_logs:

                if (D) Log.d(TAG, "Log View");
                fragmentClass = FragmentLogs.class;

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Change de Fragment
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

                // Surligne l'élément sélectionné
                item.setChecked(true);
                // Change le titre
                setTitle(R.string.title_logs);
                nbFragment = 0;
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, R.string.alert_bluetooth_enabled,
                    Toast.LENGTH_SHORT).show();
            bluetoothOK = true;
            display(this.getResources().getString(R.string.alert_bluetooth_connect));
            cid = new Cid(this, mHandlerMain, settings.getString("pref_Appareil", ""));
        } else {
            Toast.makeText(MainActivity.this, R.string.alert_bluetooth_disabled,
                    Toast.LENGTH_SHORT).show();
        }
    }

    void display(String msg) {
        Snackbar mSnackbar = Snackbar.make(findViewById(R.id.flContent), msg, Snackbar.LENGTH_LONG);

        // On centre le texte
        View mView = mSnackbar.getView();
        TextView mTextView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        mSnackbar.show();
    }

    public void sendMessage(String msg) {
        if (cid != null) {
            cid.sendMessage(msg+"\n");
        } else {
            display("Vous n'êtes pas connecté");
        }
    }

    public void displayProg(ListView lstView, String msg) {
        if (lstView != null) {
            listeProg = lstView;
        } else {

        }
    }

    private final Handler mHandlerMain = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case Cid.FLAG_SNACKBAR:
                    display(msg.getData().getString("message"));
                    break;

                case Cid.FLAG_LOGS:
                    if(I) Log.i(TAG, msg.getData().getString("message"));

                    break;

                case Cid.FLAG_STATE_CHANGE:
                    @SuppressWarnings("ConstantConditions") int state = (int) msg.getData().getString("state").charAt(0);
                    switch (state) {

                        case (Bluetooth.STATE_NONE):
                            // Déconnecté
                            break;

                        case (Bluetooth.STATE_CONNECTING):
                            // En connexion
                            break;

                        case (Bluetooth.STATE_LISTEN):
                            // En écoute
                            break;

                        case (Bluetooth.STATE_CONNECTED):
                            // Connecté
                            break;
                    }
                    break;

                case Cid.FLAG_PROG:
                    if (nbFragment == 1) {
                        displayProg(null, msg.getData().getString("message"));
                    }
                    break;

                case Cid.FLAG_PROG_DAILY:
                    if (nbFragment == 1) {
                        displayProg(null, msg.getData().getString("message"));
                    }
                    break;
            }
        }
    };
}
