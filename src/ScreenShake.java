import java.util.Random;

public class ScreenShake {
    private boolean isShaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 0;
    private int shakeIntensity = 0;
    private int currentOffsetX = 0;
    private int currentOffsetY = 0;
    private Random random = new Random();
    
    public void start(long duration, int intensity) {
        this.isShaking = true;
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = duration;
        this.shakeIntensity = intensity;
    }
    
    public void update() {
        if (!isShaking) {
            currentOffsetX = 0;
            currentOffsetY = 0;
            return;
        }
        
        long now = System.currentTimeMillis();
        long elapsed = now - shakeStartTime;
        
        // Check if shake finished
        if (elapsed >= shakeDuration) {
            isShaking = false;
            currentOffsetX = 0;
            currentOffsetY = 0;
            return;
        }
        
        // Calculate decay (shake gets weaker over time)
        float progress = (float)elapsed / (float)shakeDuration;
        float decay = 1.0f - progress;
        
        // Generate random offset
        int maxOffset = (int)(shakeIntensity * decay);
        currentOffsetX = random.nextInt(maxOffset * 2 + 1) - maxOffset;
        currentOffsetY = random.nextInt(maxOffset * 2 + 1) - maxOffset;
    }
    
    public int getOffsetX() {
        return currentOffsetX;
    }

    public int getOffsetY() {
        return currentOffsetY;
    }
    
    public boolean isShaking() {
        return isShaking;
    }
    
    public void stop() {
        isShaking = false;
        currentOffsetX = 0;
        currentOffsetY = 0;
    }
}