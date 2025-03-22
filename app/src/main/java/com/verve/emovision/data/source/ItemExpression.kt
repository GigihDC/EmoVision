package com.verve.emovision.data.source

import com.verve.emovision.R
import com.verve.emovision.data.model.Expression

object ItemExpression {
    val itemList =
        listOf(
            Expression(
                1,
                R.string.text_expression_angry,
                R.drawable.img_angry,
                R.string.text_desc_angry,
            ),
            Expression(
                2,
                R.string.text_expression_disgust,
                R.drawable.img_disgust,
                R.string.text_desc_disgust,
            ),
            Expression(
                3,
                R.string.text_expression_fear,
                R.drawable.img_fear,
                R.string.text_desc_fear,
            ),
            Expression(
                4,
                R.string.text_expression_happy,
                R.drawable.img_happy,
                R.string.text_desc_happy,
            ),
            Expression(
                5,
                R.string.text_expression_neutral,
                R.drawable.img_neutral,
                R.string.text_desc_neutral,
            ),
            Expression(
                6,
                R.string.text_expression_sad,
                R.drawable.img_sad,
                R.string.text_desc_sad,
            ),
            Expression(
                7,
                R.string.text_expression_surprise,
                R.drawable.img_surprise,
                R.string.text_desc_surprise,
            ),
        )
}
