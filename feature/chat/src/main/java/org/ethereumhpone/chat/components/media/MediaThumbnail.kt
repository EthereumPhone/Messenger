package org.ethereumhpone.chat.components.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import org.ethereumhpone.chat.util.GalleryMedia
import org.ethereumhpone.chat.util.MediaType
import org.ethereumhpone.domain.model.Attachment

@Composable
fun MediaThumbnail(
    attachment: Attachment,
    onClick: ()->Unit,
    isInSelectionMode: Boolean,
    isSelected: Boolean
) {
    val uri = when(attachment){
        is Attachment.Image -> attachment.getUri()
        is Attachment.Video -> attachment.getUri()
        is Attachment.Contact -> TODO()
    }
    Box(modifier=Modifier.padding(4.dp).aspectRatio(1f).clickable(onClick=onClick)
        .then(if(isSelected) Modifier.border(3.dp, dgenTurqoise) else Modifier)){
        Image(
            painter=rememberAsyncImagePainter(uri),
            contentDescription=null,
            contentScale=ContentScale.Crop,
            modifier=Modifier.fillMaxSize()
        )
        if(attachment is Attachment.Video){
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
                    .background(
                        color = dgenTurqoise.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = dgenOcean,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        }
        if(isInSelectionMode){
            Box(modifier=Modifier.size(24.dp).align(Alignment.TopEnd).offset(-8.dp,8.dp).background(
                if(isSelected) dgenTurqoise else dgenOcean, CircleShape
            )){
                if(isSelected) Icon(Icons.Default.Check,null,modifier=Modifier.align(Alignment.Center))
            }
        }
    }
}