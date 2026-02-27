package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Bullet {
    public int x, y, color, width, height;

    public Bullet(int x, int y, int color, int width, int height) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public void moveUp(int speed) {
        y -= speed;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
