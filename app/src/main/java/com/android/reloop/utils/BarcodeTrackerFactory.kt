package com.android.reloop.utils

import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode

object BarcodeTrackerFactory : MultiProcessor.Factory<Barcode> {
    override fun create(barcode: Barcode): MyBarcodeTracker {
        return MyBarcodeTracker()
    }
}


class MyBarcodeTracker : Tracker<Barcode?>() {
    override fun onUpdate(detectionResults: Detections<Barcode?>, barcode: Barcode?) {
        // Access detected barcode values
    }
}