import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shiftPressed;
    public boolean meleeAttackPressed = false;
    public boolean rangeAttackPressed = false;
    public boolean respawnPressed;

    @Override
    public void keyTyped(KeyEvent e) {
        // Nothing goes here but this is essential
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyUsed = e.getKeyCode();

        // MC movement pressed (WASD & L_SHIFT)
        if (keyUsed == KeyEvent.VK_W) upPressed = true;
        if (keyUsed == KeyEvent.VK_S) downPressed = true;
        if (keyUsed == KeyEvent.VK_A) leftPressed = true;
        if (keyUsed == KeyEvent.VK_D) rightPressed = true;
        if (keyUsed == KeyEvent.VK_SHIFT) shiftPressed = true;

        // MC attack pressed (U & I)
        if (keyUsed == KeyEvent.VK_U) meleeAttackPressed = true;
        if (keyUsed == KeyEvent.VK_I) rangeAttackPressed = true;

        // Respawn key
        if (keyUsed == KeyEvent.VK_R) respawnPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyUsed = e.getKeyCode();

        // MC movement released (WASD & L_SHIFT)
        if (keyUsed == KeyEvent.VK_W) upPressed = false;
        if (keyUsed == KeyEvent.VK_S) downPressed = false;
        if (keyUsed == KeyEvent.VK_A) leftPressed = false;
        if (keyUsed == KeyEvent.VK_D) rightPressed = false;
        if (keyUsed == KeyEvent.VK_SHIFT) shiftPressed = false;

        // MC attack released (U & I)
        if (keyUsed == KeyEvent.VK_U) meleeAttackPressed = false;
        if (keyUsed == KeyEvent.VK_I) rangeAttackPressed = false;
        // Respawn key
        if (keyUsed == KeyEvent.VK_R) respawnPressed = false;
    }
}