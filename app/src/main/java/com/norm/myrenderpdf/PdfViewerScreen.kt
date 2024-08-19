package com.norm.myrenderpdf

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PdfViewerScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PdfBitmapConverter(
            context
        )
    }

    var pdfUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var renderPages by remember {
        mutableStateOf<List<Bitmap>>(emptyList())
    }

    val choosePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        pdfUri = it
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var searchResults by remember {
        mutableStateOf(emptyList<com.norm.myrenderpdf.SearchResults>())
    }

    LaunchedEffect(key1 = pdfUri) {
        pdfUri?.let { uri ->
            renderPages = pdfBitmapConverter.pdfToBitmaps(uri)
        }
    }

    val scope = rememberCoroutineScope()

    if (pdfUri == null) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    choosePdfLauncher.launch(
                        "application/pdf"
                    )
                }
            ) {
                Text(
                    text = "Choose PDF"
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                itemsIndexed(renderPages) { index, page ->
                    PdfPage(
                        page = page,
                        searchResults = searchResults.find { it.page == index },
                    )
                }
            }
            Button(
                onClick = {
                    choosePdfLauncher.launch("application/pdf")
                }
            ) {
                Text(
                    text = "Choose another PDF"
                )
            }
        }
        if (Build.VERSION.SDK_INT >= 35) {
            OutlinedTextField(
                value = searchText,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchText = ""
                                searchResults = emptyList()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = null,
                            )
                        }
                    }
                },
                onValueChange = { newSearchText ->
                    searchText = newSearchText

                    pdfBitmapConverter.renderer?.let { renderer ->
                        scope.launch(Dispatchers.Default) {
                            searchResults = (0 until renderer.pageCount).map { index ->
                                renderer.openPage(index).use { page ->
                                    val result = page.searchText(newSearchText)

                                    val matchRects = result.map {
                                        it.bounds.first()
                                    }

                                    SearchResults(
                                        page = index,
                                        results = matchRects,
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun PdfPage(
    page: Bitmap,
    searchResults: SearchResults? = null,
) {
    AsyncImage(
        model = page,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(page.width.toFloat() / page.height)
            .drawWithContent {
                drawContent()

                val scaleFactorX = size.width / page.width
                val scaleFactorY = size.height / page.height

                searchResults?.results?.forEach { rect ->
                    val adjustedRect = RectF(
                        rect.left * scaleFactorX,
                        rect.top * scaleFactorY,
                        rect.right * scaleFactorX,
                        rect.bottom * scaleFactorY,
                    )

                    drawRoundRect(
                        color = Color.Green.copy(alpha = 0.2f),
                        topLeft = Offset(
                            x = adjustedRect.left,
                            y = adjustedRect.top,
                        ),
                        size = Size(
                            width = adjustedRect.width(),
                            height = adjustedRect.height(),
                        ),
                        cornerRadius = CornerRadius(5.dp.toPx())
//                        colorFilter = ColorFilter.tint(
//                            color = Color.Black,
//                            blendMode = BlendMode.Softlight,
//                        ),
                    )
                }
            }
    )
}