package com.airplane.game;

import java.util.Random;

public class Star {
    public float x, y, size;
    public int alpha;
    private float speed;
    private Random random = new Random();

    public Star(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.alpha = random.nextInt(155) + 100;
        this.speed = size / 2;
    }

    public void update() {
        y += speed;
        if (y > 2000) {
            y = -10;
            x = random.nextInt(1000);
            alpha = random.nextInt(155) + 100;
        }
        // Twinkle
        alpha += random.nextInt(11) - 5;
        if (alpha < 100) alpha = 100;
        if (alpha > 255) alpha = 255;
    }
}
