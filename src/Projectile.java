import java.awt.*;

public class Projectile {
    public int x, y;
    public int width = 8;
    public int height = 8;
    private float vx, vy;
    private int originX, originY;
    private int maxRange;
    public boolean alive = true;
    private Rectangle hitbox;

    public Projectile(int startX, int startY, float dirX, float dirY, int speed, int maxRange) {
        this.maxRange = maxRange;

        // Put projectile origin centered on player
        this.x = startX - width/2;
        this.y = startY - height/2;

        // Normalize direction vector (dirX,dirY) and multiply by speed
        float len = (float) Math.sqrt(dirX*dirX + dirY*dirY);
        if (len == 0) { // fail-safe
            this.vx = 0;
            this.vy = speed;
        } else {
            this.vx = (dirX / len) * speed;
            this.vy = (dirY / len) * speed;
        }

        this.originX = this.x;
        this.originY = this.y;

        hitbox = new Rectangle(this.x, this.y, width, height);
    }

    public void update(Collision collision) {
        if (!alive) return;

        // Move projectile every frame
        x += Math.round(vx);
        y += Math.round(vy);
        hitbox.setLocation(x, y);

        // Check collision with map
        Rectangle hitObs = collision.getCollision(hitbox);
        if (hitObs != null) {
            alive = false;
            return;
        }

        // Check range exceeded
        int dx = x - originX;
        int dy = y - originY;
        double distSq = dx * dx + dy * dy;
        if (distSq >= (long) maxRange * maxRange) {
            alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    // Draw relative to camera
    public void draw(Graphics2D g, int cameraX, int cameraY) {
        if (!alive) return;
        g.setColor(Color.MAGENTA);
        g.fillRect(x - cameraX, y - cameraY, width, height);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
