package com.example.calculator

import android.Manifest
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.delay
import java.io.File

class MediaPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme{
                MusicLibraryUI()
            }
        }
    }
}

@Composable
fun MusicLibraryUI() {
    var permissionGranted by remember { mutableStateOf(false) }
    val player = remember { MediaPlayer() }
    val tracks = remember { loadAudioFiles() }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = it
    }

    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
        permissionLauncher.launch(perm)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                player.release()
            } catch (e: Exception) {
                Log.e("MusicUI", "MediaPlayer cleanup failed", e)
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            position = player.currentPosition
            delay(1000)
        }
    }

    fun playTrack(index: Int) {
        try {
            if (index in tracks.indices) {
                player.reset()
                player.setDataSource(tracks[index].absolutePath)
                player.prepare()
                player.start()
                duration = player.duration
                currentIndex = index
                isPlaying = true
            }
        } catch (e: Exception) {
            Log.e("MusicUI", "Playback error", e)
        }
    }

    if (!permissionGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permission required to view music library")
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Music", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(tracks) { index, track ->
                ListItem(
                    headlineContent = { Text(track.nameWithoutExtension) },
                    leadingContent = {
                        Icon(Icons.Default.LibraryMusic, contentDescription = null)
                    },
                    modifier = Modifier.clickable { playTrack(index) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (currentIndex != -1) {
            Text("Now Playing: ${tracks[currentIndex].nameWithoutExtension}", style = MaterialTheme.typography.bodyLarge)
            Text("%02d:%02d / %02d:%02d".format(
                position / 1000 / 60,
                (position / 1000) % 60,
                duration / 1000 / 60,
                (duration / 1000) % 60
            ))
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    if (currentIndex > 0) playTrack(currentIndex - 1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                }
                IconButton(onClick = {
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = {
                    if (currentIndex < tracks.lastIndex) playTrack(currentIndex + 1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

private fun loadAudioFiles(): List<File> {
    val musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    return musicFolder.listFiles()?.filter { it.extension.lowercase() in listOf("mp3", "wav", "ogg") } ?: emptyList()
}
