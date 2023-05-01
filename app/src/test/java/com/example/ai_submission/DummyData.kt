package com.example.ai_submission

import com.example.ai_submission.data.retrofit.Story

object DummyData {
    fun generateDummyStoryResponse(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                i.toString(),
                "Cerite Ke-$i",
                "Entah Ini Ngapain $i?",
                "goo.gl/$i",
                "2023-01-01T00:00:00.000Z",
            )
            items.add(story)
        }
        return items
    }
}