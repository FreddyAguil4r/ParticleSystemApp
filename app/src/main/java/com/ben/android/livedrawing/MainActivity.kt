package com.ben.android.livedrawing

import android.app.Activity
import android.graphics.Point

import android.os.Bundle

class MainActivity : Activity() {

    private lateinit var liveDrawingView: LiveDrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = windowManager.defaultDisplay

        //resolución de pantalla en el x y las y propiedades escondidas en el size objeto.
        val size = Point()
        display.getSize(size)
        liveDrawingView = LiveDrawingView(this, size.x)
        setContentView(liveDrawingView)
    }

    override fun onResume() {
        super.onResume()
        liveDrawingView.resume()
    }

    override fun onPause() {
        super.onPause()
        liveDrawingView.pause()
    }

}