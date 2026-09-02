package com.example.uniqo

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Shows the listing's real uploaded photo when it has one, otherwise falls
 * back to the deterministic pastel placeholder used before real uploads existed.
 */
@Composable
fun ListingImage(
    imageUrl: String?,
    seed: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp)
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        PlaceholderImage(seed = seed, modifier = modifier, shape = shape)
    }
}