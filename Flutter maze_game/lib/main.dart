import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';

void main() {
  runApp(const MazeGameApp());
}

class MazeGameApp extends StatelessWidget {
  const MazeGameApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      //showPerformanceOverlay: true, // ✅ Add this line
      home: const MazeGame(),
    );
  }
}

class MazeGame extends StatefulWidget {
  const MazeGame({super.key});

  @override
  _MazeGameState createState() => _MazeGameState();
}

class _MazeGameState extends State<MazeGame> {
  int size = 9;
  late List<List<int>> maze;
  int playerX = 1, playerY = 1;
  int level = 1;
  int timeLeft = 30;
  bool isPaused = false;
  bool gameOver = false;
  bool isMuted = false;
  Timer? _timer;

  final AudioPlayer _bgMusicPlayer = AudioPlayer();
  final AudioPlayer _tickSoundPlayer = AudioPlayer();

  @override
  void initState() {
    super.initState();
    generateNewMaze();
    startTimer();
    startBackgroundMusic();
  }

  Future<void> startBackgroundMusic() async {
    if (!isMuted) {
      await _bgMusicPlayer.setReleaseMode(ReleaseMode.loop);
      await _bgMusicPlayer.play(
        AssetSource('suits-you-69233.mp3'),
        mode: PlayerMode.mediaPlayer,
      );
    }
  }

  void startTimer() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted && !isPaused && !gameOver) {
        if (timeLeft > 0) {
          setState(() {
            timeLeft--;
          });

          if (timeLeft == 10) {
            _tickSoundPlayer.play(
              AssetSource('clock-ticking-sound-effect-240503.mp3'),
              mode: PlayerMode.lowLatency,
            );
          }
        } else {
          _bgMusicPlayer.stop();
          setState(() {
            gameOver = true;
          });
          _timer?.cancel();
          showGameOverDialog();
        }
      }
    });
  }

  void resetGame() {
    setState(() {
      level = 1;
      size = 9;
      timeLeft = 30;
      gameOver = false;
      isPaused = false;
      generateNewMaze();
    });
    startBackgroundMusic();
    startTimer();
  }

  void togglePause() {
    setState(() {
      isPaused = !isPaused;
    });
  }

  void showGameOverDialog() {
    showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text("Game Over"),
            content: const Text("Time's up! Try again."),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                  resetGame();
                },
                child: const Text("Restart"),
              ),
            ],
          ),
    );
  }

  void generateNewMaze() {
    maze = generateMaze(size, size);
    playerX = 1;
    playerY = 1;
  }

  List<List<int>> generateMaze(int rows, int cols) {
    List<List<int>> newMaze = List.generate(rows, (_) => List.filled(cols, 1));

    void carvePath(int x, int y) {
      newMaze[x][y] = 0;
      List<List<int>> directions = [
        [1, 0],
        [-1, 0],
        [0, 1],
        [0, -1],
      ];
      directions.shuffle();

      for (var dir in directions) {
        int nx = x + dir[0] * 2, ny = y + dir[1] * 2;
        if (nx > 0 &&
            ny > 0 &&
            nx < rows - 1 &&
            ny < cols - 1 &&
            newMaze[nx][ny] == 1) {
          newMaze[x + dir[0]][y + dir[1]] = 0;
          carvePath(nx, ny);
        }
      }
    }

    carvePath(1, 1);
    newMaze[rows - 2][cols - 2] = 2;
    return newMaze;
  }

  void movePlayer(int dx, int dy) {
    if (gameOver || isPaused) return;

    int newX = playerX + dx;
    int newY = playerY + dy;

    if (maze[newX][newY] != 1) {
      setState(() {
        playerX = newX;
        playerY = newY;
      });

      if (maze[newX][newY] == 2) {
        showDialog(
          context: context,
          builder:
              (context) => AlertDialog(
                title: Text("🎉 Level $level Complete!"),
                content: const Text("Proceeding to the next level..."),
                actions: [
                  TextButton(
                    onPressed: () {
                      Navigator.pop(context);
                      nextLevel();
                    },
                    child: const Text("Continue"),
                  ),
                ],
              ),
        );
      }
    }
  }

  void nextLevel() {
    int newSize = min(size + 2, 19);
    setState(() {
      level++;
      size = newSize;
      timeLeft = max(30 - level * 2, 10);
      generateNewMaze();
    });
    startBackgroundMusic();
    startTimer();
  }

  @override
  Widget build(BuildContext context) {
    double cellSize = MediaQuery.of(context).size.width / (size + 2);
    return Scaffold(
      appBar: AppBar(title: Text("Maze Game - Level $level")),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: togglePause,
                child: Text(isPaused ? "Resume ⏯️" : "Pause ⏸️"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: resetGame,
                child: const Text("Restart 🔄"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: () {
                  setState(() {
                    isMuted = !isMuted;
                    if (isMuted) {
                      _bgMusicPlayer.pause();
                    } else {
                      startBackgroundMusic();
                    }
                  });
                },
                child: Text(isMuted ? "Unmute 🔊" : "Mute 🔇"),
              ),
            ],
          ),
          const SizedBox(height: 10),

          Stack(
            alignment: Alignment.center,
            children: [
              SizedBox(
                height: 100,
                width: 100,
                child: CircularProgressIndicator(
                  value: timeLeft / 30,
                  strokeWidth: 8,
                  backgroundColor: Colors.grey[300],
                  valueColor: AlwaysStoppedAnimation(
                    timeLeft > 15
                        ? Colors.blue
                        : timeLeft > 10
                        ? Colors.yellow
                        : timeLeft > 5
                        ? Colors.orange
                        : Colors.red,
                  ),
                ),
              ),
              Text(
                "$timeLeft sec",
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),

          Expanded(
            child: GridView.builder(
              itemCount: size * size,
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: size,
              ),
              itemBuilder: (context, index) {
                int row = index ~/ size;
                int col = index % size;
                Color color =
                    maze[row][col] == 1
                        ? Colors.black
                        : maze[row][col] == 2
                        ? Colors.green
                        : Colors.white;
                if (playerX == row && playerY == col) color = Colors.red;
                return Container(
                  width: cellSize,
                  height: cellSize,
                  color: color,
                  margin: const EdgeInsets.all(1),
                );
              },
            ),
          ),

          Column(
            children: [
              IconButton(
                onPressed: () => movePlayer(-1, 0),
                icon: const Icon(Icons.arrow_upward, size: 40),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  IconButton(
                    onPressed: () => movePlayer(0, -1),
                    icon: const Icon(Icons.arrow_back, size: 40),
                  ),
                  const SizedBox(width: 20),
                  IconButton(
                    onPressed: () => movePlayer(0, 1),
                    icon: const Icon(Icons.arrow_forward, size: 40),
                  ),
                ],
              ),
              IconButton(
                onPressed: () => movePlayer(1, 0),
                icon: const Icon(Icons.arrow_downward, size: 40),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
