package com.airplane.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private SurfaceHolder holder;
    private boolean isPlaying;
    private Paint paint, glowPaint, textPaint, gradientPaint;
    private Random random;
    private int screenWidth, screenHeight;
    private int score = 0;
    private int health = 3;
    private int level = 1;
    private int killCount = 0;
    private boolean gameOver = false;
    private boolean showShop = false;
    private int weaponLevel = 1; // 1: single, 2: double, 3: triple
    private int fireRate = 10;
    private int bulletTimer = 0;
    private long lastTime = System.currentTimeMillis();

    // Player
    private Player player;
    private int playerX, playerY;

    // Enemies & Boss
    private List<Enemy> enemies;
    private List<Boss> bosses;
    private int enemySpawnTimer = 0;
    private static final int BASE_ENEMY_SPAWN_RATE = 40;

    // Bullets
    private List<Bullet> bullets;
    private List<Bullet> enemyBullets;

    // Effects
    private List<Explosion> explosions;
    private List<Star> stars;
    private Paint explosionPaint;

    // Sounds
    private SoundPool soundPool;
    private int shootSound, explodeSound, upgradeSound;
    private boolean soundLoaded = false;

    // Shop
    private String[] shopItems = {"Double Shot", "Triple Shot", "Rapid Fire"};
    private int[] shopPrices = {50, 150, 100};
    private RectF[] shopButtons = new RectF[3];
    private Paint shopPaint, shopTextPaint;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        holder = getHolder();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setShadowLayer(20, 0, 0, Color.CYAN);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setShadowLayer(10, 2, 2, Color.BLACK);
        try {
            textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        } catch (Exception e) {}

        random = new Random();

        player = new Player(Color.GREEN, 100, 100);
        enemies = new ArrayList<>();
        bosses = new ArrayList<>();
        bullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        explosions = new ArrayList<>();

        // Generate stars
        stars = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(random.nextInt(1000), random.nextInt(2000), random.nextInt(3) + 1));
        }

        // Explosion paint
        explosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        explosionPaint.setStyle(Paint.Style.FILL);

        // Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(attrs)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioAttributes.USAGE_GAME, 0);
        }
        // Load sounds (Anda perlu menyediakan file raw)
        // shootSound = soundPool.load(context, R.raw.shoot, 1);
        // explodeSound = soundPool.load(context, R.raw.explode, 1);
        // upgradeSound = soundPool.load(context, R.raw.upgrade, 1);
        // soundLoaded = true;

        // Shop paints
        shopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shopPaint.setColor(Color.argb(200, 0, 0, 0));
        shopTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shopTextPaint.setColor(Color.WHITE);
        shopTextPaint.setTextSize(40);
        shopTextPaint.setShadowLayer(10, 2, 2, Color.BLACK);
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;

            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            lastTime = now;

            if (!gameOver && !showShop) {
                updateGame(elapsed);
            }

            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame(long elapsed) {
        // Update stars
        for (Star star : stars) {
            star.update();
        }

        // Spawn enemies
        enemySpawnTimer += elapsed;
        int spawnRate = Math.max(150, BASE_ENEMY_SPAWN_RATE * 10 - level * 5);
        if (enemySpawnTimer > spawnRate) {
            if (level % 10 == 0 && bosses.isEmpty()) {
                // Spawn boss
                bosses.add(new Boss(screenWidth / 2 - 100, -150, 200, 150, level * 20));
                if (soundLoaded) soundPool.play(upgradeSound, 1, 1, 0, 0, 1);
            } else {
                // Spawn enemy
                int type = random.nextInt(3);
                int w = 50, h = 50;
                int speed = 3 + level / 5;
                int color = 0xFFFF4444;
                if (type == 1) {
                    w = 70; h = 70; speed = 2; color = 0xFFFFAA00;
                } else if (type == 2) {
                    w = 40; h = 40; speed = 5; color = 0xFFFF88FF;
                }
                enemies.add(new Enemy(random.nextInt(screenWidth - w), -h, w, h, speed, color));
            }
            enemySpawnTimer = 0;
        }

        // Player shooting
        bulletTimer += elapsed;
        int fireDelay = 200 - (6 - weaponLevel) * 30; // makin tinggi weapon level makin cepat
        if (weaponLevel == 3) fireDelay = 120;
        if (bulletTimer > fireDelay) {
            if (weaponLevel == 1) {
                bullets.add(new Bullet(playerX + player.width / 2 - 5, playerY - 10, 10, 20, 12, 0xFFFFFF00));
            } else if (weaponLevel == 2) {
                bullets.add(new Bullet(playerX + player.width / 2 - 15, playerY - 10, 10, 20, 12, 0xFFFFFF00));
                bullets.add(new Bullet(playerX + player.width / 2 + 5, playerY - 10, 10, 20, 12, 0xFFFFFF00));
            } else if (weaponLevel == 3) {
                bullets.add(new Bullet(playerX + player.width / 2 - 20, playerY - 10, 10, 20, 12, 0xFFFFFF00));
                bullets.add(new Bullet(playerX + player.width / 2, playerY - 10, 10, 20, 12, 0xFFFFFF00));
                bullets.add(new Bullet(playerX + player.width / 2 + 20, playerY - 10, 10, 20, 12, 0xFFFFFF00));
            }
            if (soundLoaded) soundPool.play(shootSound, 1, 1, 0, 0, 1);
            bulletTimer = 0;
        }

        // Update bullets
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();

            // Hit enemies
            Iterator<Enemy> enemyIt = enemies.iterator();
            while (enemyIt.hasNext()) {
                Enemy enemy = enemyIt.next();
                if (Rect.intersects(bullet.getRect(), enemy.getRect())) {
                    bulletIt.remove();
                    enemyIt.remove();
                    score += 10;
                    killCount++;
                    explosions.add(new Explosion(enemy.x + enemy.width/2, enemy.y + enemy.height/2, 50));
                    if (soundLoaded) soundPool.play(explodeSound, 1, 1, 0, 0, 1);
                    if (killCount % 10 == 0 && level < 100) level++;
                    break;
                }
            }

            // Hit bosses
            Iterator<Boss> bossIt = bosses.iterator();
            while (bossIt.hasNext()) {
                Boss boss = bossIt.next();
                if (Rect.intersects(bullet.getRect(), boss.getRect())) {
                    bulletIt.remove();
                    boss.health -= 10;
                    if (boss.health <= 0) {
                        bossIt.remove();
                        score += 100;
                        level++;
                        explosions.add(new Explosion(boss.x + boss.width/2, boss.y + boss.height/2, 100));
                        if (soundLoaded) soundPool.play(explodeSound, 1, 1, 0, 0, 1);
                    }
                    break;
                }
            }

            if (bullet.y < 0) bulletIt.remove();
        }

        // Update enemies
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update();

            if (Rect.intersects(enemy.getRect(), player.getRect())) {
                enemyIt.remove();
                health--;
                explosions.add(new Explosion(enemy.x + enemy.width/2, enemy.y + enemy.height/2, 30));
                if (soundLoaded) soundPool.play(explodeSound, 1, 1, 0, 0, 1);
                if (health <= 0) gameOver = true;
                continue;
            }

            if (enemy.y > screenHeight) enemyIt.remove();
        }

        // Update bosses
        Iterator<Boss> bossIt = bosses.iterator();
        while (bossIt.hasNext()) {
            Boss boss = bossIt.next();
            boss.update();

            if (Rect.intersects(boss.getRect(), player.getRect())) {
                health -= 5;
                if (health <= 0) gameOver = true;
            }

            // Boss shooting
            if (random.nextInt(100) < 2) {
                enemyBullets.add(new Bullet(boss.x + boss.width/2 - 5, boss.y + boss.height, 10, 20, 8, 0xFFFF0000));
                if (soundLoaded) soundPool.play(shootSound, 1, 1, 0, 0, 1);
            }

            if (boss.y > screenHeight + 200) bossIt.remove();
        }

        // Update enemy bullets
        Iterator<Bullet> eBulletIt = enemyBullets.iterator();
        while (eBulletIt.hasNext()) {
            Bullet bullet = eBulletIt.next();
            bullet.update();

            if (Rect.intersects(bullet.getRect(), player.getRect())) {
                eBulletIt.remove();
                health--;
                if (health <= 0) gameOver = true;
            }

            if (bullet.y > screenHeight) eBulletIt.remove();
        }

        // Update explosions
        Iterator<Explosion> expIt = explosions.iterator();
        while (expIt.hasNext()) {
            Explosion exp = expIt.next();
            exp.update();
            if (exp.isFinished()) expIt.remove();
        }
    }

    private void drawGame(Canvas canvas) {
        // Dark background
        canvas.drawColor(0xFF0A0A1A);

        // Draw stars
        for (Star star : stars) {
            paint.setColor(0xFFFFFFFF);
            paint.setAlpha(star.alpha);
            canvas.drawCircle(star.x, star.y, star.size, paint);
        }

        // Draw explosions
        for (Explosion exp : explosions) {
            exp.draw(canvas, explosionPaint);
        }

        // Draw player
        player.draw(canvas, glowPaint, paint);

        // Draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(canvas, paint);
        }

        // Draw bosses
        for (Boss boss : bosses) {
            boss.draw(canvas, paint);
        }

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(canvas, paint);
        }
        for (Bullet bullet : enemyBullets) {
            bullet.draw(canvas, paint);
        }

        // UI
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        canvas.drawText("Score: " + score, 20, 60, textPaint);
        canvas.drawText("Health: " + health, 20, 120, textPaint);
        canvas.drawText("Level: " + level, 20, 180, textPaint);
        canvas.drawText("Kills: " + killCount, 20, 240, textPaint);
        textPaint.setTextSize(30);
        canvas.drawText("Weapon Lv." + weaponLevel, screenWidth - 200, 60, textPaint);

        // Progress bar level
        int nextLevelKills = ((level / 10) + 1) * 10;
        int progress = (killCount % 10) * 10;
        paint.setColor(0xFF333333);
        canvas.drawRect(screenWidth - 220, 80, screenWidth - 20, 110, paint);
        paint.setColor(0xFF00FF00);
        canvas.drawRect(screenWidth - 220, 80, screenWidth - 220 + progress * 2, 110, paint);
        textPaint.setTextSize(20);
        canvas.drawText("Next level", screenWidth - 150, 70, textPaint);

        if (gameOver) {
            paint.setColor(0xAA000000);
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(80);
            canvas.drawText("GAME OVER", screenWidth / 2 - 200, screenHeight / 2, textPaint);
            textPaint.setTextSize(40);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("Tap to restart", screenWidth / 2 - 100, screenHeight / 2 + 80, textPaint);
        }

        // Shop button
        if (!gameOver && !showShop) {
            textPaint.setColor(Color.CYAN);
            textPaint.setTextSize(30);
            canvas.drawText("🛒 Shop", screenWidth - 120, 140, textPaint);
        }

        // Shop menu
        if (showShop) {
            canvas.drawRect(0, 0, screenWidth, screenHeight, shopPaint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(60);
            canvas.drawText("SHOP", screenWidth / 2 - 80, 200, textPaint);

            textPaint.setTextSize(40);
            for (int i = 0; i < shopItems.length; i++) {
                int x = screenWidth / 2 - 200;
                int y = 300 + i * 100;
                shopButtons[i] = new RectF(x, y - 40, x + 400, y + 40);
                paint.setColor(0xFF444444);
                paint.setShadowLayer(10, 2, 2, Color.BLACK);
                canvas.drawRoundRect(shopButtons[i], 20, 20, paint);
                paint.clearShadowLayer();
                textPaint.setColor(Color.WHITE);
                canvas.drawText(shopItems[i], x + 10, y, textPaint);
                textPaint.setColor(Color.YELLOW);
                canvas.drawText(shopPrices[i] + " pts", x + 250, y, textPaint);
            }

            textPaint.setColor(Color.RED);
            canvas.drawText("Tap outside to close", screenWidth / 2 - 150, screenHeight - 100, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            restart();
            return true;
        }

        if (showShop) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < shopButtons.length; i++) {
                    if (shopButtons[i] != null && shopButtons[i].contains(x, y)) {
                        buyItem(i);
                        return true;
                    }
                }
                showShop = false;
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (x > screenWidth - 150 && y < 150) {
                showShop = true;
                return true;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            playerX = x - player.width / 2;
            playerY = y - player.height / 2;
            playerX = Math.max(0, Math.min(playerX, screenWidth - player.width));
            playerY = Math.max(0, Math.min(playerY, screenHeight - player.height));
            player.setPosition(playerX, playerY);
        }
        return true;
    }

    private void buyItem(int index) {
        if (score >= shopPrices[index]) {
            score -= shopPrices[index];
            switch (index) {
                case 0: weaponLevel = 2; break;
                case 1: weaponLevel = 3; break;
                case 2: fireRate = 5; break;
            }
            if (soundLoaded) soundPool.play(upgradeSound, 1, 1, 0, 0, 1);
            showShop = false;
        }
    }

    private void restart() {
        gameOver = false;
        health = 3;
        score = 0;
        level = 1;
        killCount = 0;
        weaponLevel = 1;
        fireRate = 10;
        enemies.clear();
        bosses.clear();
        bullets.clear();
        enemyBullets.clear();
        explosions.clear();
        player.setPosition(playerX, playerY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        playerX = w / 2 - player.width / 2;
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
