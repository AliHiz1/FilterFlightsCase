package com.alihiz.filterflightscase.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.alihiz.filterflightscase.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createPlaneIcon(context: Context): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_plane)!!
    vectorDrawable.setBounds(
        0, 0,
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight
    )
    
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

