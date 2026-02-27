package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

public class Enemy {
    public int x, y, width, height, speed, color;

    public Enemy(int x, int y, int width, int height, int speed, int color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.color = color;
    }

    public void update() {
        y += speed;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        // Gradient
        paint.setShader(new LinearGradient(x, y, x, y + height, color, 0xFF000000, Shader.TileMode.CLAMP));
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setShader(null);
    }
}
