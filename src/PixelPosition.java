import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PixelPosition extends MouseAdapter {

    private int mouseX;
    private int mouseY;
    private boolean clicked;

    private Camera gameCamera;

    public PixelPosition(Camera camera) {
        this.gameCamera = camera;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        mouseX = e.getX() + gameCamera.getCameraX();
        mouseY = e.getY() + gameCamera.getCameraY();
        clicked = true;

        System.out.println("Pixel coordinates: X=" + mouseX + " & Y=" + mouseY);
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void resetClick() {
        clicked = false;
    }
}