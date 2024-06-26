package com.example.heartsteel.presentation.notifications

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.heartsteel.R
import com.example.heartsteel.components.*
import com.example.heartsteel.components.core.TopBar
import com.example.heartsteel.domain.model.Music
import com.example.heartsteel.domain.model.Tabs
import com.example.heartsteel.navigation.Router
import com.example.heartsteel.navigation.Screen
import com.example.heartsteel.tools.Ext.offsetY
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("RememberReturnType")
@Composable
fun NotificationsScreen(
    router: Router? = null,
    paddingValues: PaddingValues = PaddingValues(),
    navController: NavHostController?
) {
    val contentHeight = 150.dp
    val scrollState = rememberLazyListState()
    val offsetY = scrollState.offsetY(contentHeight)
    val alpha = 1f - offsetY.value / contentHeight.value

    val tabsList = remember { mutableListOf<Tabs>() }
    val musicsNew = remember { mutableListOf<Music>() }
    val listMusic = remember { mutableListOf<Music>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val snapshot = FirebaseDatabase.getInstance().getReference("tabs").get().await()

                snapshot.children.forEach { dataSnap ->
                    val tab = dataSnap.getValue(Tabs::class.java)
                    tab?.let { tabsList.add(it) }
                }
            } catch (e: Exception) {
                Log.e("NotificationsScreen", "Error fetching tabs", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val snapshot =
                    FirebaseDatabase.getInstance().getReference("musics").limitToFirst(4).get()
                        .await()

                snapshot.children.forEach { dataSnap ->
                    val music = Music().apply {
                        id = dataSnap.child("key").value.toString()
                        title = dataSnap.child("title").value.toString()
                        image = dataSnap.child("image").value.toString()
                        tag = dataSnap.child("tag").value.toString()
                        author = dataSnap.child("author").value.toString()
                        genre = dataSnap.child("genre").value.toString()
                    }
                    musicsNew.add(music)
                }
            } catch (e: Exception) {
                Log.e("NotificationsScreen", "Error fetching tabs", e)
            }
        }
    }
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val snapshot =
                    FirebaseDatabase.getInstance().getReference("musics").limitToLast(10).get()
                        .await()

                snapshot.children.forEach { dataSnap ->
                    val music = Music().apply {
                        id = dataSnap.key!!
                        title = dataSnap.child("title").value.toString()
                        image = dataSnap.child("image").value.toString()
                        author = dataSnap.child("author").value.toString()
                    }
                    listMusic.add(music)
                }
            } catch (e: Exception) {
                Log.e("NotificationsScreen", "Error fetching tabs", e)
            }
        }
    }
    val goBack: () -> Unit = {
        router?.goHome()
    }
    val goPlayer: (Music?) -> Unit = {
        navController?.navigate("${Screen.PlayerFull.route}/${it?.id}")
    }
    val goSearchTag: (Tabs?) -> Unit = {
        navController?.navigate("${Screen.SearchTag.route}/${it?.title}")
    }

    TopBar(
        modifier = Modifier,
        navigationIcon = {
            IconBtn(
                resIcon = R.drawable.ic_left,
                onClick = { goBack() }
            )
        },
        title = {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .alpha(1 - alpha),
                text = "Whats new",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                onTextLayout = {}
            )
        },
    )

    LazyColumn(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 50.dp),
        state = scrollState,
        contentPadding = PaddingValues(
            bottom = paddingValues.calculateBottomPadding(),
        )
    ) {
        item {
            Box(modifier = Modifier.height(contentHeight)) {
                TextTitle(
                    "Whats new", modifier = Modifier
                        .alpha(alpha)
                        .padding(vertical = 20.dp, horizontal = 8.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.BottomStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(tabsList) { tab ->
                        tab.title?.let {
                            ChipTag(
                                modifier = Modifier.padding(end = 8.dp),
                                text = it,
                                onChipSelected = { goSearchTag(tab) })
                        }
                    }
                }
            }
        }
        item {
            TextTitle("New", modifier = Modifier.padding(8.dp))
        }
        itemsIndexed(musicsNew) { index, track ->
            track.title?.let {
                val round: Dp? = if (index % 4 == 0) null else 10.dp
                CardRow(
                    60.dp,
                    round = round,
                    roundPercent = 100,
                    item = track,
                    onClick = { goPlayer(track) })
            }
        }
        item {
            TextTitle("Early", modifier = Modifier.padding(8.dp))
        }
        items(listMusic) { track ->
            track.title?.let {
                CardRow(60.dp, round = 10.dp, item = track, onClick = { goPlayer(track) })
            }
        }

    }
}