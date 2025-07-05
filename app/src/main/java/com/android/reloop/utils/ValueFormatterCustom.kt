package com.reloop.reloop.utils

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ValueFormatterCustom  constructor() :
    ValueFormatter() {
    private val mFormat: DecimalFormat
    private var pieChart: PieChart? = null

    // Can be used to remove percent signs if the chart isn't in percent mode
    constructor(pieChart: PieChart?) : this() {
        this.pieChart = pieChart
    }

    override fun getFormattedValue(value: Float): String {
        //return mFormat.format(value.toDouble()) + "%"
        return ""
    }


    override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
        return if (pieChart != null && pieChart!!.isUsePercentValuesEnabled) {
            // Converted to percent
            getFormattedValue(value)
        } else {
            // raw value, skip percent sign
            mFormat.format(value.toDouble())
        }
    }

    init {
        mFormat = DecimalFormat("###,###,##0", DecimalFormatSymbols(Locale.US))
    }
}