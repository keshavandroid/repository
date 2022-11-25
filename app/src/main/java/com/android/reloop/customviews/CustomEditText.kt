package com.reloop.reloop.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import androidx.appcompat.widget.AppCompatEditText
import com.reloop.reloop.R

class CustomEditText(
    context: Context,
    attrs: AttributeSet?
) : AppCompatEditText(context, attrs) {
    var focusedBackground: Drawable?
    var unFocusedBackground: Drawable?
    var iconFocused: Drawable?
    var iconUnfocused: Drawable?
//    var focusedPaddingTop: Float?
//    var focusedPaddingStart: Float?
//    var focusedPaddingEnd: Float?
//    var focusedPaddingBottom: Float?


    init {
        @SuppressLint("CustomViewStyleable") val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.editTextStyleable)
        focusedBackground = typedArray.getDrawable(R.styleable.editTextStyleable_focusedBackground)
        unFocusedBackground =
            typedArray.getDrawable(R.styleable.editTextStyleable_unFocusedBackground)
        iconFocused = typedArray.getDrawable(R.styleable.editTextStyleable_focusedDrawable)
        iconUnfocused = typedArray.getDrawable(R.styleable.editTextStyleable_unFocusedDrawable)
       /* focusedPaddingTop =
            typedArray.getDimension(R.styleable.editTextStyleable_focusedPaddingTop, 0f)
        focusedPaddingStart =
            typedArray.getDimension(R.styleable.editTextStyleable_focusedPaddingStart, 0f)
        focusedPaddingBottom =
            typedArray.getDimension(R.styleable.editTextStyleable_focusedPaddingBottom, 0f)
        focusedPaddingEnd =
            typedArray.getDimension(R.styleable.editTextStyleable_focusedPaddingEnd, 0f)*/
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                background = focusedBackground
                if (iconFocused != null)
                    setCompoundDrawablesWithIntrinsicBounds(iconFocused, null, null, null)
            } else {
                background = unFocusedBackground
                if (iconFocused != null)
                    setCompoundDrawablesWithIntrinsicBounds(iconUnfocused, null, null, null)
            }
        }
        typedArray.recycle()
    }

/*    private fun setPaddingIfAvailable() {
        if (focusedPaddingTop != null) {
            setPadding(0, focusedPaddingTop!!.toInt(), 0, 0)
        }
        if (focusedPaddingTop != null && focusedPaddingStart != null) {
            setPadding(focusedPaddingStart!!.toInt(), focusedPaddingTop!!.toInt(), 0, 0)
        }
        if (focusedPaddingStart != null && focusedPaddingEnd != null) {
            setPadding(focusedPaddingStart!!.toInt(), 0, focusedPaddingEnd!!.toInt(), 0)
        }

        if (focusedPaddingTop != null && focusedPaddingStart != null && focusedPaddingEnd != null) {
            setPadding(
                focusedPaddingStart!!.toInt(),
                focusedPaddingTop!!.toInt(),
                focusedPaddingEnd!!.toInt(),
                0
            )
        }
        if (focusedPaddingTop != null && focusedPaddingStart != null && focusedPaddingBottom != null && focusedPaddingEnd != null) {
            setPadding(
                focusedPaddingStart!!.toInt(),
                focusedPaddingTop!!.toInt(),
                focusedPaddingEnd!!.toInt(),
                focusedPaddingBottom!!.toInt()
            )
        }
    }*/

    fun markRequired() {
        hint = "$hint*"
        //Asterisk Feild Is Also Included For Custom Spinner in Utils Object
    }
    fun markOptional() {
        hint = "$hint(Optional)"
    }
}