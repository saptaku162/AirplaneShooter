package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class MyPlane {
    public int x, y, width, height, color;

    public MyPlane(int color, int width, int height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        canvas.drawRect(x, y, x + width, y + height, paint);

        // Draw wings/kepak
        paint.setColor(color);
        canvas.drawRect(x - 20, y + height/2 - 5, x, y + height/2 + 5, paint); // left wing
        canvas.drawRect(x + width, y + height/2 - 5, x + width + 20, y + height/2 + 5, paint); // right wing
    }
}
