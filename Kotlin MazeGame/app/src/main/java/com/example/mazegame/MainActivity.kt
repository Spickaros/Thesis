package com.example.mazegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Initialize and Play Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.suits_you_69233)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.5f, 0.5f)
        mediaPlayer.start()

        setContent {
            MazeGameScreen(
                stopMusic = { mediaPlayer.stop() },
                startMusic = {
                    mediaPlayer = MediaPlayer.create(this, R.raw.suits_you_69233)
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVolume(0.5f, 0.5f)
                    mediaPlayer.start()
                }
            )
        }




        fun onDestroy() {
            super.onDestroy()
            mediaPlayer.release()  // 🛑 Stop Music when Game Closes
        }

    }

    @Composable
    fun MazeGameScreen(stopMusic: () -> Unit, startMusic: () -> Unit) {
        val size = remember { mutableStateOf(9) }
        val level = remember { mutableStateOf(1) }
        val timeLeft = remember { mutableStateOf(30) }
        val gameOver = remember { mutableStateOf(false) }
        val isPaused = remember { mutableStateOf(false) }
        val showDialog = remember { mutableStateOf(false) }

        val context = LocalContext.current
        val tickSound =
            remember { MediaPlayer.create(context, R.raw.clock_ticking_sound_effect_240503) }

        val maze = remember { mutableStateOf(generateMaze(size.value, size.value)) }
        val playerX = remember { mutableStateOf(1) }
        val playerY = remember { mutableStateOf(1) }
        val isMuted = remember { mutableStateOf(false) }


        // Timer logic
        LaunchedEffect(timeLeft.value, isPaused.value) {
            while (timeLeft.value > 0 && !gameOver.value && !isPaused.value) {
                delay(1000)
                timeLeft.value--

                if (timeLeft.value in 1..9) tickSound.start()
            }

            if (timeLeft.value == 0) {
                gameOver.value = true
                stopMusic()  // 🔹 Stop the background music
                showDialog.value = true  // 🔹 Show the "Time's Up" dialog
            }
        }

        // "Time's Up" Dialog
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Time's Up!") },
                text = { Text("Your time has run out. Would you like to restart?") },
                confirmButton = {
                    Button(onClick = {
                        showDialog.value = false
                        resetGame(playerX, playerY, maze, size, level, {
                            gameOver.value = false
                            timeLeft.value = 30
                        }, startMusic)  // 🔹 Fix: Pass startMusic
                    }) {
                        Text("Restart ")
                    }
                }
            )
        }


        // Dynamic Timer Color
        val timerColor = when {
            timeLeft.value > 20 -> Color.Green
            timeLeft.value in 10..20 -> Color.Yellow
            timeLeft.value in 5..9 -> Color(0xFFFFA500)
            else -> Color.Red
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Level ${level.value}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))




            // Circular Timer
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = timeLeft.value / 30f,
                    modifier = Modifier.size(100.dp),
                    color = timerColor,
                    strokeWidth = 8.dp
                )
                Text(
                    text = "${timeLeft.value}s",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Maze Grid
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .border(2.dp, Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val maxCellSize = 30.dp
                val cellSize = min(maxCellSize, maxWidth / size.value)

                Column {
                    for (row in 0 until size.value) {
                        Row {
                            for (col in 0 until size.value) {
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .background(
                                            when {
                                                playerX.value == row && playerY.value == col -> Color.Red
                                                maze.value[row][col] == 1 -> Color.Black
                                                maze.value[row][col] == 2 -> Color.Green
                                                else -> Color.White
                                            }
                                        )
                                        .border(1.dp, Color.Gray)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    movePlayer(
                        -1,
                        0,
                        playerX,
                        playerY,
                        maze,
                        size,
                        level,
                    { timeLeft.value = 30 },isPaused, tickSound)
                }) { Text("⬆️") }
                Row {
                    Button(onClick = {
                        movePlayer(
                            0,
                            -1,
                            playerX,
                            playerY,
                            maze,
                            size,
                            level,
                        { timeLeft.value = 30 },isPaused, tickSound)
                    }) { Text("⬅️") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        movePlayer(
                            0,
                            1,
                            playerX,
                            playerY,
                            maze,
                            size,
                            level,
                         { timeLeft.value = 30 },isPaused, tickSound)
                    }) { Text("➡️") }
                }
                Button(onClick = {
                    movePlayer(
                        1,
                        0,
                        playerX,
                        playerY,
                        maze,
                        size,
                        level,
                    { timeLeft.value = 30 }, isPaused, tickSound)
                }) { Text("⬇️") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // ⏯️ Pause/Resume Button
                Button(onClick = { isPaused.value = !isPaused.value }) {
                    Text(if (isPaused.value) "Resume ⏯️" else "Pause ⏸️")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 🔄 Restart Button
                Button(onClick = {
                    showDialog.value = false
                    resetGame(
                        playerX, playerY, maze, size, level,
                        {
                            gameOver.value = false
                            timeLeft.value = 30
                        },
                        startMusic // 🔹 Restart the music after reset
                    )
                }) {
                    Text("Restart 🔄")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 🔇 Mute/Unmute Button
                Button(onClick = {
                    isMuted.value = !isMuted.value
                    if (isMuted.value) {
                        mediaPlayer.setVolume(0f, 0f) // 🔇 Mute
                    } else {
                        mediaPlayer.setVolume(0.5f, 0.5f) // 🔊 Unmute
                    }
                }) {
                    Text(if (isMuted.value) "Unmute 🔊" else "Mute 🔇")
                }
            }

        }
    }


    fun generateMaze(rows: Int, cols: Int): Array<IntArray> {
        val maze = Array(rows) { IntArray(cols) { 1 } }

        fun carvePath(x: Int, y: Int) {
            maze[x][y] = 0
            val directions = listOf(-2 to 0, 2 to 0, 0 to -2, 0 to 2).shuffled()
            for ((dx, dy) in directions) {
                val nx = x + dx
                val ny = y + dy
                if (nx in 1 until rows - 1 && ny in 1 until cols - 1 && maze[nx][ny] == 1) {
                    maze[x + dx / 2][y + dy / 2] = 0
                    carvePath(nx, ny)
                }
            }
        }

        carvePath(1, 1)
        maze[rows - 2][cols - 2] = 2
        return maze
    }

    fun movePlayer(
        dx: Int,
        dy: Int,
        playerX: MutableState<Int>,
        playerY: MutableState<Int>,
        maze: MutableState<Array<IntArray>>,
        size: MutableState<Int>,
        level: MutableState<Int>,
        resetTimer: () -> Unit,
        isPaused: MutableState<Boolean>,
        tickSound: MediaPlayer // 🔹 Add tickSound parameter
    ) {
        if (isPaused.value) return // ⛔ Block movement if paused

        val newX = playerX.value + dx
        val newY = playerY.value + dy
        if (newX in 0 until size.value && newY in 0 until size.value && maze.value[newX][newY] != 1) {
            playerX.value = newX
            playerY.value = newY

            if (maze.value[newX][newY] == 2) {
                levelUp(playerX, playerY, maze, size, level, resetTimer, tickSound) // ✅ Pass tickSound
            }
        }
    }




    fun levelUp(
        playerX: MutableState<Int>, playerY: MutableState<Int>,
        maze: MutableState<Array<IntArray>>, size: MutableState<Int>,
        level: MutableState<Int>, resetTimer: () -> Unit, tickSound: MediaPlayer
    ) {
        tickSound.pause()  // 🔹 Stop ticking sound immediately
        tickSound.seekTo(0)  // 🔹 Reset sound position

        size.value = min(size.value + 2, 19)  // Increase maze size
        level.value++  // Increase level
        playerX.value = 1
        playerY.value = 1
        maze.value = generateMaze(size.value, size.value)
        resetTimer()
    }


    fun resetGame(
        playerX: MutableState<Int>, playerY: MutableState<Int>,
        maze: MutableState<Array<IntArray>>, size: MutableState<Int>,
        level: MutableState<Int>, resetTimer: () -> Unit, startMusic: () -> Unit
    ) {
        level.value = 1  // 🔹 Reset to Level 1
        size.value = 9  // 🔹 Reset maze size
        playerX.value = 1
        playerY.value = 1
        maze.value = generateMaze(size.value, size.value)
        resetTimer()  // 🔹 Restart the timer
        startMusic()  // 🔹 Restart the music after resetting the game
    }

}




