/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Cette classe a été optimisée par Nicolas Guilloux
 * dans le cadre de son projet de 2ème année.
 * De nombreux bugs ont été corrigés.
 */

package fr.nover.esther;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class Bluetooth {

    // Debugging
    private static final String TAG = "CustomBluetoothService";
    private static final boolean D = false;
    private static final boolean E = false;
    private static final boolean I = false;
    private static final boolean W = false;

    // Nom pour qualifier la connexion avec le dispositif
    private static final String NAME = "cidChat";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

    // INSECURE "8ce255c0-200a-11e0-ac64-0800200c9a66"
    // SECURE "fa87c0d0-afac-11de-8a39-0800200c9a66"
    // SPP "0001101-0000-1000-8000-00805F9B34FB"

    // Constantes qui serviront de repères à envoyer pour le Handler
    static final int MESSAGE_STATE_CHANGE = 1;
    static final int MESSAGE_READ = 2;
    static final int MESSAGE_WRITE = 3;
    static final int MESSAGE_DEVICE_NAME = 4;
    static final int MESSAGE_DISPLAY = 5;
    static final int MESSAGE_FLAG = 6;

    private static final String INPUT_SEPERATOR="\n";

    // Variables
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private final Context context;

    // Constantes d'état du système de connexion
    static final int STATE_NONE = 0;
    static final int STATE_LISTEN = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;

    /**
     * Constructeur. Permet de préparer la connexion
     *
     * @param context
     *            Le contexte de l'Activité appelant la classe
     * @param handler
     *            Un Handler qui permet d'effectuer des actions asynchronisées
     */
    public Bluetooth(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (D) for (BluetoothDevice bd: mAdapter.getBondedDevices()) Log.d(TAG, "Appareil appairé : "+bd);
        mState = STATE_NONE;
        mHandler = handler;
        this.context = context;
    }

    /**
     * Définit l'état du système
     *
     * @param state
     *            Un Integer définit les états (cf variables STATE_)
     */
    private synchronized void setState(int state) {
        String states = state + "" + mState;
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);

        // Envoie ça au Handler
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, Integer.parseInt(states), -1)
                .sendToTarget();

        mState = state;
    }

    /**
     * Retourne l'état actuel
     */
    synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a                           //Todo: Traduire
     * session in listening (server) mode. Called by the Activity onResume()
     *
     * Démarre le chat avec le périphérique.
     */
    synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device
     *            The BluetoothDevice to connect
     */
    private synchronized void connect(BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * On lance la procédure en cas de succès de la connexion
     *
     * @param socket
     *            Le BluetoothSocket avec lequel on a commencé la connexion
     * @param device
     *            Le BluetoothDevice sur lequel on est connecté
     */
    private synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device, final String socketType) {
        if (D)
            Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // On arrête le AcceptThread car on est enfin connecté
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Ce Thread est pour le dialogue entre les deux dispositifs
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("Connected", device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Arrête tous les Threads
     */
    synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Envoie les bytes à écrire au périphérique (asynchronisé)
     *
     * @param out
     *            Les bytes à écrire
     */
    private void write(byte[] out) {
        // Créé un objet temporaire afin de rendre l'écriture asynchronisée
        ConnectedThread r;

        // On met une copie du vrai ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // On écrit par le ConnectedThread temporaire
        r.write(out);
    }

    /**
     * Si la connexion échoue, on envoie un Toast
     */
    private void connectionFailed() {
        // On envoie au Handler un message Toast pour informer de l'échec

        mHandler.obtainMessage(MESSAGE_DISPLAY, "Impossible de se connecter :(").sendToTarget();     //Todo: Changer le Unable to connect device avec un R.string.alert_bluetooth_failed

        // On relance le service à l'écoute
        Bluetooth.this.start();
    }

    /**
     * Si la connexion est perdue, on envoie un Toast
     */
    private void connectionLost() {
        // Envoie un message au Handler

        mHandler.obtainMessage(MESSAGE_DISPLAY, "Connexion avec l'appareil perdue !").sendToTarget(); //Todo: Changer Device connection was lost pour R.string

        // On recommence du début
        Bluetooth.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves                        //Todo: Traduire et comprendre AcceptThread
     * like a server-side client. It runs until a connection is accepted (or
     * until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType="";

        private AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter
                        .listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D)
                Log.d(TAG, "Socket Type: " + mSocketType
                        + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType
                            + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (Bluetooth.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D)
                Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D)
                Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType
                        + "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType="";

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                if(E) Log.e(TAG,"Unable to connect socket ",e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    if(E) Log.e(TAG, "unable to close() " + mSocketType
                            + " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Bluetooth.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if(E) Log.e(TAG, "close() of connect " + mSocketType
                        + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private ConnectedThread(BluetoothSocket socket, String socketType) {
            if(D) Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if(E) Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            if(I) Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes, len;
            StringBuilder readMessage = new StringBuilder();
            String[] messages;
            String read;

            // Boucle infinie pour rester écouter
            while (true) {
                try {
                    // Lit le buffer et le traduit en String
                    bytes = mmInStream.read(buffer);
                    read = new String(buffer, 0, bytes, "UTF-8");

                    // Associe tous les morceaux de chaines de caractères qu'il reçoit
                    readMessage.append(read);

                    // Coupe en String à pour chaque retour à la ligne
                    messages = readMessage.toString().split(INPUT_SEPERATOR);
                    len = messages.length;

                    for(int i=0; i<(len-1); i++){
                        if (messages[i] != null && (messages[i].length() != 0)) {
                            // On renvoie tout sauf le dernier élément qui ne contient pas \n
                            // Et on regarde si c'est un FLAG ou non
                            // On retire aussi le dernier caractère qui est parasite (null)
                            if(messages[i].length() > 4 && messages[i].substring(0,4).equals("FLAG")) { mHandler.obtainMessage(MESSAGE_FLAG, bytes, -1, messages[i].substring(5,messages[i].length()-1)).sendToTarget(); }
                            else { mHandler.obtainMessage(MESSAGE_READ, bytes, -1, messages[i]).sendToTarget(); }

                            // readMessage n'est plus que le dernier élément
                            readMessage.setLength(0);
                            readMessage.append(messages[len-1]);
                        }
                    }

                } catch (IOException e) {
                    if(E) Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    Bluetooth.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */
        private void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                if(E) Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if(E) Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (this.getState() != Bluetooth.STATE_CONNECTED) {
            if(W) Log.w(TAG, "Le Bluetooth n'est pas connecté !");
            mHandler.obtainMessage(MESSAGE_DISPLAY, "Le Bluetooth n'est pas connecté !").sendToTarget();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            char EOT = (char)3 ;
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = (message + EOT).getBytes();
            this.write(send);
        }
    }

    void connectDevice(String deviceName) {
        // Get the device MAC address
        String address = null;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        for(BluetoothDevice d: adapter.getBondedDevices()){
            if (d.getName().equals(deviceName)) address = d.getAddress();
        }

        try {
            BluetoothDevice device = adapter.getRemoteDevice(address); // Get the BluetoothDevice object
            this.connect(device); // Attempt to connect to the device
        } catch (Exception e){
            if(E) Log.e("Unable to connect "+ address,e.getMessage());
        }
    }

}
