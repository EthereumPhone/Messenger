package org.ethereumphone.contacts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.database.model.ContactEntity

@Composable
fun CreateGroupSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: () -> Unit,
) {

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val buttonAlpha by animateFloatAsState(
        if (textState.text.isEmpty()) 0.35f else 1f,
        tween(300)
    )



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }

            Surface(
                color = dgenTurqoise,
                shape = CircleShape,
                modifier = Modifier
                    .height(25.dp)
                    .alpha(buttonAlpha)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onCreateGroup()
                        }
                    }
            ){
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp)
                    ,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text= "CREATE GROUP", color = dgenOcean , style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenWhite,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),modifier = Modifier.padding(horizontal = 8.dp))
                }

            }


        }
        Row(
            Modifier.padding(horizontal = 8.dp)
        ){
            OldSchoolThickCursorTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textState,
                onValueChange = { newTextFieldValue ->
                    textState = newTextFieldValue
                },
                placeholder = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Type group name",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenTurqoise.copy(alpha = 0.45f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            )
                        )
                    }

                },
                textStyle = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                cursorColor = dgenWhite,
                singleLine = true
            )
        }


        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MEMBERS ${members.size}",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(members) { contact ->
                        MemberItem(
                            header = contact.name,
                            subheader = contact.numbers.firstOrNull()?.address ?: "",
                            onDelete = {},
                        )
                    }
                }
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))))
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))

            }

        }

    }

}