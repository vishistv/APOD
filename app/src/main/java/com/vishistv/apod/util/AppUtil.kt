package com.vishistv.apod.util

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.animation.TranslateAnimation
import android.widget.TextView
import com.vishistv.apod.R
import java.util.*


object AppUtil {

    private var dialog: Dialog? = null

    fun showDatePickerDialog(context: Context, listener: DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(context, listener,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corner_datepicker)
        dialog.show()
    }

    // slide the view from below itself to the current position
    fun slideUp(view: View, isTop: Boolean) {
        val fromY = if (isTop) (-1)*view.height.toFloat() else view.height.toFloat()

        val animate = TranslateAnimation(
            0F,
            0F,
            fromY,
            0F
        )
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // slide the view from its current position to below itself
    fun slideDown(view: View, isTop: Boolean) {
        val fromY = if (isTop) (-1)*view.height.toFloat() else view.height.toFloat()

        val animate = TranslateAnimation(
            0F,
            0F,
            0F,
            fromY
        )
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun showProgressBar(context: Context) {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.setContentView(R.layout.custom_progress_bar)
        dialog?.show()
    }

    fun hideProgressBar() {
        dialog?.dismiss()
    }
}