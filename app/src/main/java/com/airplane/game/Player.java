package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

public class Player {
    public int x, y, width, height;
    private int color;

    public Player(int color, int width, int height) {
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

    public void draw(Canvas canvas, Paint glowPaint, Paint paint) {
        // Glow effect
        glowPaint.setColor(color);
        canvas.drawRect(x - 5, y - 5, x + width + 5, y + height + 5, glowPaint);
        // Gradient body
        paint.setShader(new LinearGradient(x, y, x, y + height, 0xFF00FF00, 0xFF00AA00, Shader.TileMode.CLAMP));
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setShader(null);
        // Wings
        paint.setColor(color);
        canvas.drawRect(x - 15, y + height/2 - 10, x, y + height/2 + 10, paint);
        canvas.drawRect(x + width, y + height/2 - 10, x + width + 15, y + height/2 + 10, paint);
        // Cockpit
        paint.setColor(0xFF88FF88);
        canvas.drawCircle(x + width/2, y + height/4, width/5, paint);
    }
}
