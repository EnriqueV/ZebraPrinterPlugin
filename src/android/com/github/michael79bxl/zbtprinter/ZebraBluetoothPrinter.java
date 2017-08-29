package com.github.michael79bxl.zbtprinter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.zebra.android.discovery.BluetoothDiscoverer;
import com.zebra.android.discovery.DiscoveredPrinter;
import com.zebra.android.discovery.DiscoveredPrinterBluetooth;
import com.zebra.android.discovery.DiscoveryHandler;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.graphics.ZebraImageFactory;
import com.zebra.sdk.graphics.ZebraImageI;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.io.File.*;




public class ZebraBluetoothPrinter extends CordovaPlugin {

    private static final String LOG_TAG = "ZebraBluetoothPrinter";
    //String mac = "AC:3F:A4:1D:7A:5C";

    public ZebraBluetoothPrinter() {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("print")) {
            try {
                String mac = args.getString(0);
                String msg = args.getString(1);
                printPdf(callbackContext, mac);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        }
        if (action.equals("find")) {
            try {
                findPrinter(callbackContext);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        }
	    
        return false;
    }

    
    public void findPrinter(final CallbackContext callbackContext) {
      try {
          BluetoothDiscoverer.findPrinters(this.cordova.getActivity().getApplicationContext(), new DiscoveryHandler() {

              public void foundPrinter(DiscoveredPrinter printer) {
                  if(printer instanceof DiscoveredPrinterBluetooth) {
                     JSONObject printerObj = new JSONObject();
                     try {
                       printerObj.put("address", printer.address);
                       printerObj.put("friendlyName", ((DiscoveredPrinterBluetooth) printer).friendlyName);
                       callbackContext.success(printerObj);
                     } catch (JSONException e) {
                     }
                  } else {              
                    String macAddress = printer.address;
                    //I found a printer! I can use the properties of a Discovered printer (address) to make a Bluetooth Connection
                    callbackContext.success(macAddress);
                  }
              }

              public void discoveryFinished() {
                  //Discovery is done
				   callbackContext.error("discoveryDone");
              }

              public void discoveryError(String message) {
                  //Error during discovery
                  callbackContext.error(message);
              }
          });
      } catch (Exception e) {
          e.printStackTrace();
      }      
    }

	
	/*
     * This will send an image to the bluetooth printer
     */

    void printPdf(final CallbackContext callbackContext, final String mac) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Instantiate insecure connection for given Bluetooth MAC Address.
                    //Connection thePrinterConn = new BluetoothConnectionInsecure(mac);
                    Log.e("IR", "error: ");
                    Connection thePrinterConn = new BluetoothConnection(mac);
                    final ZebraPrinter printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL, thePrinterConn);
                    // Initialize
                    Looper.prepare();

                    // Verify the printer is ready to print
                    if (isPrinterReady(thePrinterConn, PrinterLanguage.CPCL)) {





                        // Open the connection - physical connection is established here.
                        thePrinterConn.open();

                       
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = true;
                        int x =0;
                        int y = 0;
                         Bitmap bm = BitmapFactory.decodeFile("Remplace with image path file", options);
                        int width= bm.getWidth(); // set width
                        int height= bm.getHeight(); // set height
                        Bitmap newBitmap= Bitmap.createScaledBitmap(bm,625,height,true);

                        ZebraImageI image = ZebraImageFactory.getImage(newBitmap);

                       // printer.printImage(image, x, y, -1, -1, false);
                       printer.printImage(new ZebraImageAndroid(newBitmap), x, y, -1, -1, false);

                        // Make sure the data got to the printer before closing the connection
                        Thread.sleep(500);

                        // Close the insecure cnnection to release resources.
                        thePrinterConn.close();

                        Looper.myLooper().quit();
                        callbackContext.success("Done");


                    } else {
                        callbackContext.error("Printer is not ready");
                    }
                } catch (Exception e) {
                    // Handle communications error here.
                    callbackContext.error(e.getMessage());
                }
            }
        }).start();
    }

	
	








    private Boolean isPrinterReady(Connection connection, PrinterLanguage printerLanguage) throws ConnectionException, ZebraPrinterLanguageUnknownException {
        Boolean isOK = false;
        connection.open();
        // Creates a ZebraPrinter object to use Zebra specific functionality like getCurrentStatus()
        ZebraPrinter printer = ZebraPrinterFactory.getInstance(printerLanguage,connection);
        
        // Creates a LinkOsPrinter object to use with newer printer like ZQ520 
        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
            
        //PrinterStatus printerStatus = printer.getCurrentStatus();
        PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
             
        if (printerStatus.isReadyToPrint) {
            isOK = true;
        } else if (printerStatus.isPaused) {
            throw new ConnectionException("Cannot print because the printer is paused");
        } else if (printerStatus.isHeadOpen) {
            throw new ConnectionException("Cannot print because the printer media door is open");
        } else if (printerStatus.isPaperOut) {
            throw new ConnectionException("Cannot print because the paper is out");
        } else {
            throw new ConnectionException("Cannot print");
        }
        
        connection.close();
        return isOK;
        
    }
}

