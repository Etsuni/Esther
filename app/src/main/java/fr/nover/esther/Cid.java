package fr.nover.esther;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by nover on 25/10/16.
 */

class Cid {

    // Debug
    private static final String TAG = "Cid";
    private static final boolean D = true;

    // Variables privées
    private final Context context;
    private final Handler mHandlerMain;
    private  Handler mHandler;
    private final String deviceName;

    // Variables référentes affichage
    final static int FLAG_SNACKBAR = 0;
    final static int FLAG_LOGS = 1;
    final static int FLAG_STATE_CHANGE = 2;
    final static int FLAG_PROG = 3;
    final static int FLAG_PROG_DAILY = 4;

    // Variables Bluetooth
    private Bluetooth bt;

    Cid(Context context, Handler mHandlerMain, String deviceName){
        this.context=context;
        this.mHandlerMain = mHandlerMain;
        this.deviceName = deviceName;

        initHandler();

        bt = new Bluetooth(context, mHandler);
        connectService();
    }

    void stop(){
        bt.stop();
    }

    void sendMessage(String message){ bt.sendMessage(message); }

    // Private
    private void connectService(){
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice(deviceName);
                if(D) Log.d(TAG, "Connexion à l'appareil "+deviceName);

            } else {
                if(D) Log.w(TAG, "Bluetooth non activé");
            }
        } catch(Exception e){
            Log.e(TAG, "Impossible de se connecter ",e);
            affiche(FLAG_SNACKBAR, "message", R.string.alert_bluetooth_failed);
        }
    }

    private void affiche(int flag, String flag2, int message) {
        String message2 = context.getResources().getString(message);
        affiche(flag,flag2,message2);
    }
    private void affiche(int flag, String flag2, String message) {
        Message msg = mHandlerMain.obtainMessage(flag);
        Bundle bundle = new Bundle();
        bundle.putString(flag2, message);
        msg.setData(bundle);
        mHandlerMain.sendMessage(msg);
    }


    private void initHandler(){

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message;
                switch (msg.what) {
                    case Bluetooth.MESSAGE_STATE_CHANGE:

                        affiche(FLAG_STATE_CHANGE, "state", "" + msg.arg1);

                        // On est passé d'une tentative de connexion à une connexion
                        if (msg.arg1 == 32) {
                            if (D) Log.d(TAG, "Bluetooth lancé : on écoute...");
                            affiche(FLAG_SNACKBAR, "message", R.string.alert_bluetooth_connected);

                            // On est passé d'une tentative de connexion à rien du tout (ECHEC) (02)
                        } else if (msg.arg1 == 2) {
                            affiche(FLAG_SNACKBAR, "message", R.string.alert_bluetooth_busy);
                        }

                        break;

                    case Bluetooth.MESSAGE_WRITE:
                        if (D) Log.d(TAG, "MESSAGE_WRITE");
                        break;

                    case Bluetooth.MESSAGE_READ:
                        message = msg.obj.toString();
                        if (D) Log.d(TAG, "MESSAGE_READ : " + message);
                        affiche(FLAG_LOGS, "message", "CID : " + message);
                        break;

                    case Bluetooth.MESSAGE_DEVICE_NAME:
                        //if(D) Log.d(TAG, "MESSAGE_DEVICE_NAME "+msg);
                        break;

                    case Bluetooth.MESSAGE_DISPLAY:
                        if (D) Log.d(TAG, "MESSAGE_DISPLAY : " + msg.obj.toString());
                        affiche(FLAG_SNACKBAR, "message", msg.obj.toString());
                        break;

                    case Bluetooth.MESSAGE_FLAG:
                        message = msg.obj.toString();
                        if (D) Log.d(TAG, "MESSAGE_FLAG : " + message);

                        String[] infos = message.split("_");
                        boolean success = false;
                        if (infos.length > 2) {
                            success = infos[2].equals("OK");
                        }

                        if (infos[0].equals("HOR")) {

                            if (infos[1].equals("DEF")) {
                                if (success) {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_update);
                                } else {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_update_failed);
                                }
                            } else if (infos[1].equals("SEL")) {
                                if (success) {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_sel);
                                } else {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_sel_failed);
                                }
                            }

                        } else if (infos[0].equals("PROG")) {

                            if (infos[1].equals("ADD")) {
                                if (success) {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_prog_add);
                                } else {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_prog_add_failed);
                                }
                            } else if (infos[1].equals("DEL")) {
                                if (success) {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_prog_del);
                                } else if (infos[1].equals("EX")) {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_prog_ex);
                                } else {
                                    affiche(FLAG_SNACKBAR, "message", R.string.action_bluetooth_prog_del_failed);
                                }
                            }

                        } else if (infos[0].equals("GET")) {

                            if (infos[1].equals("DATE")) {
                                affiche(FLAG_LOGS, "message", "CID : " + infos[2]);
                                affiche(FLAG_SNACKBAR, "message", infos[2]);

                            } else if (infos[1].equals("PROG")) {

                                if(infos[2].equals("DAILY")) {
                                    affiche(FLAG_PROG_DAILY, "message", infos[3]);
                                } else {
                                    affiche(FLAG_PROG, "message", infos[2]);
                                }
                            }
                        }

                        break;
                }
            }
        };
    }

}
