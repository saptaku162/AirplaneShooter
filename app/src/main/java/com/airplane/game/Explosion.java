package com.airplane.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class Explosion {
    public float x, y;
    public int radius;
    private int maxRadius;
    private int alpha = 255;

    public Explosion(float x, float y, int maxRadius) {
        this.x = x;
        this.y = y;
        this.radius = 0;
        this.maxRadius = maxRadius;
    }

    public void update() {
        radius += 5;
        alpha -= 10;
        if (alpha < 0) alpha = 0;
    }

    public boolean isFinished() {
        return radius >= maxRadius || alpha <= 0;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setShader(new RadialGradient(x, y, radius, 0xFFFFFF00, 0xFFFF0000, Shader.TileMode.CLAMP));
        paint.setAlpha(alpha);
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
        paint.setAlpha(255);
    }
}
