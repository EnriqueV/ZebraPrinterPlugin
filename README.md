# zbtprinter
A Cordova/Phonegap driver for Zebra bluetooth printers

- Zebra SDK Updated to v2.11.2800
- Tested with Zebra IMZ320

only includes a function for image files printing.


##Usage
You can find Zebra printer using:

```
cordova.plugins.zbtprinter.find(function(result) { 
        alert(result.address + ', ' + result.friendlyName);
    }, function(fail) { 
        alert(fail); 
    }
);
```
Printer Image file:

```
cordova.plugins.zbtprinter.print("device mac addrees","",
			function (success) {
				
			alert(success);
	      
			},
			function (fail) {
				alert(fail);
	        
				
			}
			
);
```

About java method

```
void printPdf(final CallbackContext callbackContext, final String mac) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Instantiate insecure connection for given Bluetooth MAC Address.
                    
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
                    Bitmap bm = BitmapFactory.decodeFile("Remplace with image file path", options);
                        int width= bm.getWidth();
                        int height= bm.getHeight();
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
```


##Install
###Cordova

```
cordova plugin add https://github.com/EnriqueV/ZebraPrinterPlugin.git

```


##ZPL - Zebra Programming Language
For more information about ZPL please see the  [PDF Official Manual](https://support.zebra.com/cpws/docs/zpl/zpl_manual.pdf)

This is a fork of https://github.com/bstmedia/zbtprinter .
