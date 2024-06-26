package com.example.heartsteel.presentation.addMusic

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heartsteel.R
import com.example.heartsteel.components.IconBtn
import com.example.heartsteel.components.SearchBar
import com.example.heartsteel.components.TextSubtitle
import com.example.heartsteel.components.TextTitle
import com.example.heartsteel.components.core.BaseRow
import com.example.heartsteel.domain.model.Music
import com.example.heartsteel.navigation.Router
import com.example.heartsteel.presentation.search.SearchViewModel
import com.example.heartsteel.tools.Ext.offsetY
import com.example.heartsteel.ui.theme.Sizes
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@ExperimentalFoundationApi
@Composable
fun AddPersonsScreen(router: Router? = null) {

    val viewModel: SearchViewModel = viewModel()
    val searchText by viewModel.searchText.collectAsState()
    val musics by viewModel.movies.collectAsState()

    val context = LocalContext.current
    val userId = Firebase.auth.currentUser?.uid
    val addMusic: (Music?) -> Unit = { music ->
        if (userId != null && music != null) {
            val albumRef =
                FirebaseDatabase.getInstance().getReference("users").child(userId.toString())
                    .child("album")
            val musicQuery = albumRef.orderByChild("id").equalTo(music.id)

            musicQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        albumRef.push().setValue(music)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT)
                                    .show()
                                router?.goLib()
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "AddPersonsScreen",
                                    "Error adding music to database",
                                    exception
                                )
                            }
                    } else {
                        Toast.makeText(context, "Nhạc đã thêm vào thư viện", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "AddPersonsScreen",
                        "Error checking music existence",
                        databaseError.toException()
                    )
                }
            })
        }
    }

    val scrollState = rememberLazyListState()
    val contentHeight = 100.dp
    val offsetY = scrollState.offsetY(contentHeight)
    Box {
        LazyVerticalGrid(
            state = LazyGridState(0),
            modifier = Modifier.padding(top = 70.dp,start = 10.dp),
            contentPadding = PaddingValues(top = 130.dp, bottom = 80.dp),
            columns = GridCells.Fixed(1)
        ) {
            items(musics) {
                BaseRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Sizes.MEDIUM),
                    imageSize = 50.dp,
                    imageRes = it.image,
                    roundPercent = 100,
                    contentEnd = {
                        IconBtn(
                            resIcon = R.drawable.ic_h_outline,
                            tint = Color.Gray,
                            onClick = { addMusic(it) }
                        )
                    }
                ) {
                    TextTitle(
                        text = it.title ?: "title",
                        fontSize = 22.sp
                    )
                    TextSubtitle(
                        text = it.author ?: "author",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Gray
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .offset(y = -offsetY)
                .alpha(if (offsetY > 0.dp) 0.5f else 1f)
        ) {
            Box(
                modifier = Modifier
                    .height(contentHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomStart
            ) {

                TextTitle(
                    modifier = Modifier.padding(Sizes.DEFAULT),
                    text = "Search Musics",
                )
            }
            SearchBar(
                modifier = Modifier
                    .padding(Sizes.MEDIUM)
                    .height(60.dp),
                onValueChange = { viewModel.onSearchTextChange(it) },
                value = searchText,
                placeholder = "Tìm Kiếm"
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
@Preview
fun AddPersonsScreenPreview() {
    AddPersonsScreen()
}