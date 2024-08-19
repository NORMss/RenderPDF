package com.norm.myrenderpdf

import android.graphics.RectF

data class SearchResults(
    val page: Int,
    val results: List<RectF>
)
