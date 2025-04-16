package com.verve.emovision.data.source

import com.verve.emovision.R
import com.verve.emovision.data.model.Game

object ItemGame {
    val itemList =
        listOf(
            Game(
                1,
                R.string.text_game_face_match,
                R.drawable.img_face_match,
            ),
            Game(
                2,
                R.string.text_game_quiz,
                R.drawable.img_emo_quiz,
            ),
        )
}
