package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

public class Boss {
    public int x, y, width, height, health, maxHealth;
    private int speedX = 3;
    private int speedY = 1;

    public Boss(int x, int y, int width, int height, int health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = health;
    }

    public void update() {
        // Zigzag move
        x += speedX;
        if (x < 0 || x > 1000 - width) speedX = -speedX;
        y += speedY;
        if (y < 50) speedY = 1;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        // Body gradient
        paint.setShader(new LinearGradient(x, y, x, y + height, 0xFFFF4444, 0xFFAA0000, Shader.TileMode.CLAMP));
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setShader(null);
        // Health bar
        paint.setColor(0xFFFF0000);
        canvas.drawRect(x, y - 20, x + width, y - 10, paint);
        paint.setColor(0xFF00FF00);
        int healthWidth = (int) ((float) health / maxHealth * width);
        canvas.drawRect(x, y - 20, x + healthWidth, y - 10, paint);
    }
}
