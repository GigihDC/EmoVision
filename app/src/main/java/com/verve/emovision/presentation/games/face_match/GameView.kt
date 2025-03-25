package com.verve.emovision.presentation.games.face_match

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private val thread: GameThread
    private var playerExpression: String = "Neutral"

    init {
        holder.addCallback(this)
        thread = GameThread(this)
    }

    fun updatePlayerExpression(expression: String) {
        playerExpression = expression
        invalidate()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread.running = false
    }
}
