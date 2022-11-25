package com.android.reloop.utils

import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.DecimalFormat


class DecimalRemover(format: DecimalFormat?) : PercentFormatter() {
    protected lateinit var mFormat: DecimalFormat
    fun getFormattedValue(
        value: Float,
        entry: Map.Entry<*, *>?,
        dataSetIndex: Int,
        viewPortHandler: ViewPortHandler?
    ): String {
        return mFormat.format(value).toString() + " %"
    }

    init {
        this.mFormat = format
    }
}