package com.example.lazyjogger.ColorUtils

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class ColorCalculator {

     fun calculateColor(strength: Float): String {
        val greenColor = Color.parseColor("#66d48f")
        val redColor = Color.parseColor("#B22222")
        val red = (greenColor.red + strength * (redColor.red - greenColor.red)).toInt()
        val green = (greenColor.green + strength * (redColor.green - greenColor.green)).toInt()
        val blue = (greenColor.blue + strength * (redColor.blue - greenColor.blue)).toInt()
        val hexRed = Integer.toHexString(red)
        val hexGreen = Integer.toHexString(green)
        val hexBlue = Integer.toHexString(blue)
        return "#$hexRed$hexGreen$hexBlue"

    }
}