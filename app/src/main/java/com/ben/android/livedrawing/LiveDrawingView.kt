package com.ben.android.livedrawing

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView

//SurfaceView es decendiente de View
class LiveDrawingView(context: Context, screenX: Int): SurfaceView(context), Runnable
{
    // Are we debugging?
    private val debugging = true

    // These objects are needed to do the drawing
    private lateinit var canvas: Canvas
    private val paint: Paint = Paint()

    // How many frames per second did we get?
    private var fps: Long = 0
    // The number of milliseconds in a second
    private val millisInSecond: Long = 1000

    // How big will the text be?
    // Font is 5% (1/20th) of screen width
    // Margin is 1.5% (1/75th) of screen width
    private val fontSize: Int = screenX / 20
    private val fontMargin: Int = screenX / 75

    // The particle systems will be declared here later
    // Draw the particle systems and the HUD

    // Here is the Thread and two control variables
    private lateinit var thread: Thread
    // This volatile variable can be accessed
    // from inside and outside the thread
    @Volatile
    private var drawing: Boolean = false
    private var paused = true

    // These will be used to make simple buttons
    private var resetButton: RectF
    private var togglePauseButton: RectF

    // The particle systems will be declared here later
    private val particleSystems = ArrayList<ParticleSystem>()

    private var nextSystem = 0
    private val maxSystems = 1000
    private val particlesPerSystem = 100

    init {
        // Initialize the two buttons
        resetButton = RectF(0f, 0f, 100f, 100f)
        togglePauseButton = RectF(0f, 150f, 100f, 250f)

        // Initialize the particles and their systems
        for (i in 0 until maxSystems) {
            particleSystems.add(ParticleSystem())
            particleSystems[i]
                .initParticles(particlesPerSystem)
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            // Lock the canvas (graphics memory) ready to draw
            canvas = holder.lockCanvas()

            // Fill the screen with a solid color
            canvas.drawColor(Color.argb(255, 0, 0, 0))

            // Choose a color to paint with
            paint.color = Color.argb(255, 255, 255, 255)

            // Choose the font size
            paint.textSize = fontSize.toFloat()

            // Draw the particle systems
            for (i in 0 until nextSystem) {
                particleSystems[i].draw(canvas, paint)
            }

            // Draw the buttons
            canvas.drawRect(resetButton, paint)
            canvas.drawRect(togglePauseButton, paint)

            // Draw the particle systems
            // Draw the HUD
            if (debugging) {
                printDebuggingText()
            }

            // Display the drawing on screen
            // unlockCanvasAndPost is a
            // function of SurfaceHolder
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun printDebuggingText() {
        val debugSize = fontSize / 2
        val debugStart = 150
        paint.textSize = debugSize.toFloat()
        canvas.drawText("fps: $fps",
            10f, (debugStart + debugSize).toFloat(), paint)

        canvas.drawText("Systems: $nextSystem",
            10f, (fontMargin + debugStart +
                    debugSize * 2).toFloat(), paint)

        canvas.drawText("Particles: ${nextSystem *
                particlesPerSystem}",
            10f, (fontMargin + debugStart
                    + debugSize * 3).toFloat(), paint)

    }

    override fun run() {
        // The drawing Boolean gives us finer control
        // rather than just relying on the calls to run
        // drawing must be true AND
        // the thread running for the main
        // loop to execute
        while (drawing) {

            // What time is it now at the
            // start of the loop?
            val frameStartTime = System.currentTimeMillis()

            // Provided the app isn't paused
            // call the update function
            if (!paused) {
                update()
            }

            // The movement has been handled
            // we can draw the scene.
            draw()

            // How long did this frame/loop take?
            // Store the answer in timeThisFrame
            val timeThisFrame = System.currentTimeMillis() - frameStartTime

            // Make sure timeThisFrame is
            // at least 1 millisecond
            // because accidentally dividing
            // by zero crashes the app
            if (timeThisFrame > 0) {
                // Store the current frame rate in fps
                // ready to pass to the update functions of
                // of our particles in the next frame/loop
                fps = millisInSecond / timeThisFrame
            }
        }
    }

    private fun update() {
        for (i in 0 until particleSystems.size) {
            if (particleSystems[i].isRunning) {
                particleSystems[i].update(fps)
            }
        }
    }

    // This function is called by MainActivity
    // when the user quits the app
    fun pause() {
        // Set drawing to false
        // Stopping the thread isn't
        // always instant
        drawing = false
        try {
            // Stop the thread
            thread.join()
        }  catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

    }

    // This function is called by MainActivity
    // when the player starts the app
    fun resume() {
        drawing = true
        // Initialize the instance of Thread
        thread = Thread(this)

        // Start the thread
        thread.start()
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean
    {
        // User moved a finger while touching screen
        if (motionEvent.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_MOVE)
        {
            particleSystems[nextSystem].emitParticles(PointF(motionEvent.x,motionEvent.y))

            nextSystem++
            if (nextSystem == maxSystems) {
                nextSystem = 0
            }
        }

        // Did the user touch the screen
        if (motionEvent.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN)
        {
            // User pressed the screen so let's
            // see if it was in the reset button
            if (resetButton.contains(motionEvent.x,
                    motionEvent.y)) {

                // Clear the screen of all particles
                nextSystem = 0
            }

            // User pressed the screen so let's
            // see if it was in the toggle button
            if (togglePauseButton.contains(motionEvent.x,motionEvent.y)) {
                paused = !paused
            }
        }
        return true
    }


}