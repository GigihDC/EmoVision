package com.verve.emovision.presentation.games.face_match

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val gameView: GameView) : Thread() {
    private val holder: SurfaceHolder = gameView.holder
    var running = false

    override fun run() {
        while (running) {
            val canvas: Canvas? = holder.lockCanvas()
            if (canvas != null) {
                synchronized(holder) {
                    gameView.draw(canvas)
                }
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
