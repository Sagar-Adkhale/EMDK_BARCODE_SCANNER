

package com.workplacefabric.barcodescanning;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;

import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements EMDKListener, DataListener   {

    private EMDKManager emdkManager = null;

    // Declare a variable to store Barcode Manager object
    private BarcodeManager barcodeManager = null;

    // Declare a variable to hold scanner device to scan
    private Scanner scanner = null;
    int dataLength = 0;
    // Text view to display status of EMDK and Barcode Scanning Operations

    // Edit Text that is used to display scanned barcode data
    private EditText dataView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Reference to UI elements
        dataView = (EditText) findViewById(R.id.editText1);


// The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(
                getApplicationContext(), this);
// Check the return status of getEMDKManager and update the status Text
// View accordingly
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
           Toast.makeText(this,"EMDKManager Request Failed",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        new AsyncDataUpdate().execute(scanDataCollection);
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        try {

            // Call this method to enable Scanner and its listeners
            initializeScanner();

        } catch (ScannerException e) {
            e.printStackTrace();
        }

// Toast to indicate that the user can now start scanning
        Toast.makeText(MainActivity.this,
                "Press right yellow button to start scanning...",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClosed() {
        if (this.emdkManager != null) {

            this.emdkManager.release();
            this.emdkManager = null;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {

// Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager = null;

        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner.removeDataListener(this);

                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    private void initializeScanner() throws ScannerException {
        if (scanner == null) {
            // Get the Barcode Manager object
            barcodeManager = (BarcodeManager) this.emdkManager
                    .getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(com.symbol.emdk.barcode.BarcodeManager.DeviceIdentifier.DEFAULT);
            // Add data and status listeners
            scanner.addDataListener(this);
            // Hard trigger. When this mode is set, the user has to manually
            // press the trigger on the device after issuing the read call.
            scanner.triggerType = Scanner.TriggerType.SOFT_ONCE;
            // Enable the scanner

        }
        else{
            Log.i("saga", "hiiii");
            emdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);

        }
    }

    public class AsyncDataUpdate extends
            AsyncTask<ScanDataCollection, Void, String> {
        @Override
        protected String doInBackground(ScanDataCollection... params) {

            String statusStr = "";

            try {

                // Starts an asynchronous Scan. The method will not turn ON the
                // scanner. It will, however, put the scanner in a state in
                // which
                // the scanner can be turned ON either by pressing a hardware
                // trigger or can be turned ON automatically.
                scanner.read();

                ScanDataCollection scanDataCollection = params[0];

                // The ScanDataCollection object gives scanning result and the
                // collection of ScanData. So check the data and its status
                if (scanDataCollection != null
                        && scanDataCollection.getResult() == ScannerResults.SUCCESS) {

                    // ArrayList&lt;
                    ArrayList<ScanData> scanData = scanDataCollection
                            .getScanData();

                    // Iterate through scanned data and prepare the statusStr
                    for (ScanData data : scanData) {
                        // Get the scanned data
                        String barcodeData = data.getData();
                        // Get the type of label being scanned
                        ScanDataCollection.LabelType labelType = data.getLabelType();
                        // Concatenate barcode data and label type

                        statusStr = barcodeData + " " + labelType;
                    }
                }

            } catch (ScannerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Return result to populate on UI thread
            return statusStr;
        }
        @Override
        protected void onPostExecute(String result) {
            // Update the dataView EditText on UI thread with barcode data and
            // its label type
            if (dataLength++ > 50) {
                // Clear the cache after 50 scans
                dataView.getText().clear();
                dataLength = 0;
            }
            dataView.append(result + "\n");
        }


    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            scanner.disable();
        } catch (ScannerException e) {
            e.printStackTrace();
        }

        int action = event.getAction();
        int keyCode = event.getKeyCode();
        Log.i("skey", String.valueOf(keyCode));
        switch (keyCode) {
//            case KeyEvent.KEYCODE_VOLUME_UP:
//                if (action == KeyEvent.ACTION_DOWN) {
//                    //TODO
//                    Toast.makeText(getApplicationContext(),"Volume up press "+keyCode,Toast.LENGTH_LONG).show();
//                }
//                return true;
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                if (action == KeyEvent.ACTION_DOWN) {
//                    //TODO
//                    Toast.makeText(this,"volume down button press"+keyCode,Toast.LENGTH_LONG).show();
//                }
//                return true;
            case 285:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    try {
                        scanner.enable();
                        scanner.read();
                        scanner.triggerType = Scanner.TriggerType.SOFT_ONCE;
                    } catch (ScannerException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this,"bum press right",Toast.LENGTH_LONG).show();
                }
                return true;
//            case 286:
//                if (action == KeyEvent.ACTION_DOWN) {
//                    //TODO
//                    Toast.makeText(this,"bum press left 1",Toast.LENGTH_LONG).show();
//                }
//                return true;
//            case 284:
//                if (action == KeyEvent.ACTION_DOWN) {
//                    //TODO
//                    Toast.makeText(this,"bum press left 2",Toast.LENGTH_LONG).show();
//                }
//                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

}