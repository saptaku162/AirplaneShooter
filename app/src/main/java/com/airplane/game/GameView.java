package com.airplane.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private SurfaceHolder holder;
    private boolean isPlaying;
    private Paint paint;
    private Random random;
    private int screenWidth, screenHeight;

    // Player
    private MyPlane player;
    private int playerX, playerY;

    // Enemies
    private List<EnemyPlane> enemies;
    private int enemySpawnTimer = 0;
    private static final int ENEMY_SPAWN_RATE = 30;

    // Bullets
    private List<Bullet> bullets;
    private int bulletTimer = 0;
    private static final int BULLET_RATE = 10;

    // Score & Health
    private int score = 0;
    private int health = 3;
    private boolean gameOver = false;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        random = new Random();

        // Load bitmaps (pake rectangle dulu, nanti bisa ganti gambar)
        player = new MyPlane(Color.GREEN, 100, 100);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;

            // Update game state
            if (!gameOver) {
                updateGame();
            }

            // Draw
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);

            // Control FPS
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        // Spawn enemies
        enemySpawnTimer++;
        if (enemySpawnTimer > ENEMY_SPAWN_RATE) {
            enemies.add(new EnemyPlane(random.nextInt(getWidth() - 100) + 50, -50, Color.RED, 50, 50));
            enemySpawnTimer = 0;
        }

        // Shoot bullets
        bulletTimer++;
        if (bulletTimer > BULLET_RATE) {
            bullets.add(new Bullet(playerX + player.width/2 - 5, playerY - 10, Color.YELLOW, 10, 20));
            bulletTimer = 0;
        }

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            EnemyPlane enemy = enemies.get(i);
            enemy.moveDown(8);

            // Check collision with player
            if (Rect.intersects(enemy.getRect(), player.getRect())) {
                enemies.remove(i);
                health--;
                if (health <= 0) {
                    gameOver = true;
                }
                continue;
            }

            // Remove if off screen
            if (enemy.y > getHeight()) {
                enemies.remove(i);
            }
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.moveUp(12);

            // Check collision with enemies
            for (int j = enemies.size() - 1; j >= 0; j--) {
                EnemyPlane enemy = enemies.get(j);
                if (Rect.intersects(bullet.getRect(), enemy.getRect())) {
                    bullets.remove(i);
                    enemies.remove(j);
                    score += 10;
                    break;
                }
            }

            // Remove if off screen
            if (bullet.y < 0) {
                bullets.remove(i);
            }
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        // Draw player
        player.draw(canvas, paint);

        // Draw enemies
        for (EnemyPlane enemy : enemies) {
            enemy.draw(canvas, paint);
        }

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(canvas, paint);
        }

        // Draw UI
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("Score: " + score, 20, 60, paint);
        canvas.drawText("Health: " + health, 20, 120, paint);

        // Draw game over
        if (gameOver) {
            paint.setColor(Color.RED);
            paint.setTextSize(80);
            canvas.drawText("GAME OVER", getWidth()/2 - 200, getHeight()/2, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Restart game
            gameOver = false;
            health = 3;
            score = 0;
            enemies.clear();
            bullets.clear();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            playerX = (int) event.getX() - player.width/2;
            playerY = (int) event.getY() - player.height/2;

            // Keep player in screen
            playerX = Math.max(0, Math.min(playerX, getWidth() - player.width));
            playerY = Math.max(0, Math.min(playerY, getHeight() - player.height));

            player.setPosition(playerX, playerY);
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        // Init player position
        playerX = w/2 - player.width/2;
        playerY = h - 150;
        player.setPosition(playerX, playerY);
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
