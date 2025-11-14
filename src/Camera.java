public class Camera {

    private double cameraX, cameraY;
    private int cameraScreenWidth;
    private int cameraScreenHeight;
    private int cameraWorldWidth;
    private int cameraWorldHeight;
    private double cameraSmoother = 0.05; // How smooth the camera will follow the MC (< 0.1 = slower & > 0.1 = faster & DangerZone = > 1.9)

    public Camera(int screenWidth, int screenHeight, int worldWidth, int worldHeight) {
        this.cameraScreenWidth = screenWidth;
        this.cameraScreenHeight = screenHeight;
        this.cameraWorldWidth = worldWidth;
        this.cameraWorldHeight = worldHeight;
    }

    // Centers camera on MC while keeping it within world boundary
    public void cameraOnCrane(int craneX, int craneY, int craneWidth, int craneHeight) {
        
        // Calculate desired camera position
        double targetX = craneX - cameraScreenWidth / 2.0 + craneWidth / 2.0;
        double targetY = craneY - cameraScreenHeight / 2.0 + craneHeight / 2.0;

        // Move camera gradually toward target
        cameraX += (targetX - cameraX) * cameraSmoother;
        cameraY += (targetY - cameraY) * cameraSmoother;

        cameraInBoundary();
    }

    // Center camera on MC when the game start while keeping it within world boundary
    public void instantlyCenterOnCrane(int craneX, int craneY, int craneWidth, int craneHeight) {
        cameraX = craneX - cameraScreenWidth / 2.0 + craneWidth / 2.0;
        cameraY = craneY - cameraScreenHeight / 2.0 + craneHeight / 2.0;
        
        cameraInBoundary();
    }

    // Ensures that camera stays within world boundary
    private void cameraInBoundary() {
        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (cameraX > cameraWorldWidth - cameraScreenWidth) cameraX = cameraWorldWidth - cameraScreenWidth;
        if (cameraY > cameraWorldHeight - cameraScreenHeight) cameraY = cameraWorldHeight - cameraScreenHeight;
    }

    // To access the camera
    public int getCameraX() { return (int) cameraX; }
    public int getCameraY() { return (int) cameraY; }
}