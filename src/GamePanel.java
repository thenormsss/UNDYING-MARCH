import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    // ========================================
    // SCREEN & WORLD SETTINGS
    // ========================================

    final int screenWidth = 1080;
    final int screenHeight = 720;
    final int worldWidth = 14720;
    final int worldHeight = 15280;
    
    // ========================================
    // GAME SYSTEMS
    // ========================================

    private Player player;
    private Camera camera;
    private KeyHandler keyH = new KeyHandler();
    private PixelPosition pixelPosition;
    private Collision collision;
    private ScreenShake screenShake = new ScreenShake();

    // ========================================
    // WORLD RENDERING
    // ========================================

    private BufferedImage map;
    private BufferedImage house;
    private BufferedImage tree;
    
    // ========================================
    // ENEMY SYSTEM
    // ========================================
    
    private final List<Enemy> enemies = new ArrayList<>();
    
    // ========================================
    // GAME LOOP SETTINGS
    // ========================================

    private final int FPS = 60;
    private Thread gameThread;
    
    // ========================================
    // UI SETTINGS
    // ========================================
    
    private final int healthBarX = 20;
    private final int healthBarY = 20;
    private final int barWidth = 200;
    private final int barHeight = 20;
    private final int manaBarY = healthBarY + barHeight + 10;

    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    public GamePanel() {
        initPanelSettings();
        try {
            loadSprites();
            initPlayer();
            initCamera();
            initCollisions();
            initEnemies();
        } catch (IOException e) {
            System.err.println("Failed to initialize GamePanel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // INITIALIZATION METHODS
    // ========================================
    
    private void initPanelSettings() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.DARK_GRAY);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }
    
    private void loadSprites() throws IOException {
        loadWorldSprites();
    }
    
    private void loadWorldSprites() throws IOException {
        try {
            map = ImageIO.read(new File("resources\\Map\\map_sprite.png"));
            house = ImageIO.read(new File("resources\\Map\\House1.png"));
            tree = ImageIO.read(new File("resources\\Map\\Tree1.png"));
        } catch (IOException e) {
            System.err.println("Failed to load map or obstacle sprites.");
            throw e;
        }
    }
    
    private void initPlayer() {
        player = new Player(220, 12980);
        player.setSpawnPoint(220, 12980);
    }
    
    private void initCamera() {
        camera = new Camera(screenWidth, screenHeight, worldWidth, worldHeight);
        pixelPosition = new PixelPosition(camera);
        this.addMouseListener(pixelPosition);
        
        // Center camera on player at start
        camera.instantlyCenterOnCrane(
            player.getcraneX(), 
            player.getcraneY(), 
            player.getcraneWidth(),
            player.getcraneHeight()
        );
    }
    
    private void initCollisions() {
        collision = new Collision();
        // Optional: Load collision map from external file
        // collision.loadFromFile("resources\\Map\\collision_map.txt");
        
        // Manually defined collision obstacles
        collision.addObstacle(675, 13680, 232, 60); // House collision box
        collision.addObstacle(473, 13757, 25, 35);  // Tree collision box
    }
    
    private void initEnemies() {
        try {
            // Dummy (stationary practice target)
            Enemy dummy = EnemyFactory.createDummy(410, 13025);
            enemies.add(dummy);

            // Bandit (melee attacks only, patrols an area)
            Enemy bandit = EnemyFactory.createBandit(300, 14550);
            bandit.setPatrolArea(250, 14225, 300, 200);
            enemies.add(bandit);
            
            // High Mage (ranged attacks only, patrols an area)
            Enemy high_mage = EnemyFactory.createHighMage(430, 12230);
            high_mage.setPatrolArea(430, 12230, 300, 200);
            enemies.add(high_mage);
            
            // Musketeer (both melee and ranged attacks, patrols an area)
            Enemy musketeer = EnemyFactory.createMusketeer(1353, 12864);
            musketeer.setPatrolArea(1353, 12864, 300, 200);
            enemies.add(musketeer);
            
        } catch (IOException e) {
            System.err.println("Failed to create enemies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // GAME LOOP
    // ========================================
    
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS; // Nanoseconds per frame
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            // Update game state
            update();
            
            // Render frame
            repaint();
            
            // Frame rate control
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1000000; // Convert to milliseconds
                
                if (remainingTime < 0) {
                    remainingTime = 0; // Prevent negative sleep
                }
                
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ========================================
    // UPDATE LOGIC
    // ========================================
    
    public void update() {
        // Update player (movement, attacks, state, death handling)
        player.update(keyH, collision, worldWidth, worldHeight);
        
        // Only update game logic if player is alive
        if (!player.isDead()) {
            // Update enemies
            updateEnemies();
            
            // Update player projectiles and check for hits
            updatePlayerProjectiles();
            
            // Update breadcrumbs
            updatePlayerBreadcrumbs();
        } else {
            // Player is dead - enemies should stop attacking and return to patrol
            for (Enemy enemy : enemies) {
                if (enemy.hasSeenPlayerEver()) {
                    enemy.clearPlayerMemory();
                }
            }
        }
        
        // Update camera to smoothly follow playe
        camera.cameraOnCrane(
            player.getcraneX(), 
            player.getcraneY(), 
            player.getcraneWidth(), 
            player.getcraneHeight()
        );
        
        // Update screen shake effect
        screenShake.update();
    }

    private void updatePlayerBreadcrumbs() {
        // Update breadcrumb trail based on player movement
        player.updateBreadcrumbs(enemies);
        
        // Clear breadcrumbs if any enemy is actively searching
        // (prevents exploitation of the tracking system)
        for (Enemy en : enemies) {
            if (en.isCurrentlySearching() && !player.breadcrumbs.isEmpty()) {
                player.clearBreadcrumbs();
                break;
            }
        }
    }
    
    private void updatePlayerProjectiles() {
        List<Projectile> projectilesToRemove = new ArrayList<>();
        
        for (Projectile p : player.projectiles) {
            boolean hitSomething = false;
            
            // Check collision with each enemy
            for (Enemy en : enemies) {
                if (!en.isAlive()) continue; // Skip dead enemies
                
                if (p.getBounds().intersects(en.getBodyHitbox())) {
                    // Apply damage to enemy
                    en.takeDamage(player.getcraneRangeDamage(), screenShake);
                    p.alive = false;
                    hitSomething = true;
                    break; // Projectile can only hit one target
                }
            }
            
            // Mark projectile for removal if it hit something or died
            if (hitSomething || !p.alive) {
                projectilesToRemove.add(p);
            }
        }
        
        // Safely remove dead projectiles
        player.projectiles.removeAll(projectilesToRemove);
    }
    
    private void updateEnemies() {
        // Create safe copies to prevent concurrent modification
        List<Point> safeBreadcrumbs = new ArrayList<>(player.breadcrumbs);
        List<Enemy> enemiesCopy = new ArrayList<>(enemies);
        
        for (Enemy en : enemiesCopy) {
            if (!safeBreadcrumbs.isEmpty() && en.hasSeenPlayerEver() && !en.isCurrentlySearching()) {
                en.setBreadcrumbTrail(safeBreadcrumbs);
            }
            
            // Update enemy AI, movement, and state
            en.update(this, collision, player.getcraneBodyHitbox(), player, screenShake);
            
            // Check if enemy executed a melee attack this frame
            Rectangle enemyMeleeHitbox = en.consumeMeleeHitbox();
            if (enemyMeleeHitbox != null) {
                if (enemyMeleeHitbox.intersects(player.getcraneBodyHitbox())) {
                    player.cranetakeDamage(en.getMeleeDamage(), screenShake);
                }
            }
            
            // Check if player's melee attack hit this enemy
            if (player.checkMeleeHit(en)) {
                en.takeDamage(player.getcraneMeleeDamage(), screenShake);
            }
            
            // Update enemy projectiles and check player collision
            List<EnemyProjectile> enemyProjectilesCopy = new ArrayList<>(en.projectiles);
            List<EnemyProjectile> enemyProjectilesToRemove = new ArrayList<>();
            
            for (EnemyProjectile ep : enemyProjectilesCopy) {
                if (ep.getBounds().intersects(player.getcraneBodyHitbox())) {
                    player.cranetakeDamage(en.getRangeDamage(), screenShake);
                    ep.alive = false;
                }
                if (!ep.alive) {
                    enemyProjectilesToRemove.add(ep);
                }
            }
            
            // Safely remove dead enemy projectiles
            en.projectiles.removeAll(enemyProjectilesToRemove);
        }
        
        // Update screen shake effect
        screenShake.update();
    }

    // ========================================
    // RENDERING
    // ========================================
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Get base camera position
        int baseCamX = camera.getCameraX();
        int baseCamY = camera.getCameraY();
        
        // Apply screen shake offset to camera
        int camX = baseCamX + screenShake.getOffsetX();
        int camY = baseCamY + screenShake.getOffsetY();
        
        // Render all game elements in order
        drawWorld(g2d, camX, camY);
        drawPlayerProjectiles(g2d, camX, camY);
        player.drawBreadcrumbs(g, camX, camY);
        drawEnemies(g2d, camX, camY);
        player.draw(g2d, camX, camY);
        drawObstacles(g2d, camX, camY);
        drawUI(g2d);

        // Draw death screen overlay (only shows when player is dead)
        player.drawDeathUI(g2d, screenWidth, screenHeight);
        
        g2d.dispose();
    }
    
    private void drawWorld(Graphics2D g, int camX, int camY) {
        if (map != null) {
            g.drawImage(map, -camX, -camY, worldWidth + 100, worldHeight + 100, null);
        }
    }
    
    private void drawPlayerProjectiles(Graphics2D g, int camX, int camY) {
        List<Projectile> projectileCopy = new ArrayList<>(player.projectiles);
        for (Projectile p : projectileCopy) {
            p.draw(g, camX, camY);
        }
    }
    
    private void drawObstacles(Graphics2D g, int camX, int camY) {
        // Draw house obstacle
        if (house != null) {
            g.drawImage(house, 634 - camX, 13483 - camY, 293, 280, null);
        }
        
        // Draw tree obstacle
        if (tree != null) {
            g.drawImage(tree, 334 - camX, 13483 - camY, 314, 361, null);
        }
        
        // Draw collision boxes (for debugging)
        collision.draw(g, camX, camY);
    }
    
    private void drawEnemies(Graphics2D g, int camX, int camY) {
        List<Enemy> enemiesCopy = new ArrayList<>(enemies);
        for (Enemy en : enemiesCopy) {
            en.draw(g, camX, camY);
        }
    }
    
    private void drawUI(Graphics2D g) {
        // Only draw health/mana/cooldowns if player is alive
        if (!player.isDead()) {
            // Draw health and mana bars
            player.drawHealthBar(g, healthBarX, healthBarY, barWidth, barHeight);
            player.drawManaBar(g, healthBarX, manaBarY, barWidth, barHeight);
            
            // Draw attack cooldown indicators
            drawCooldownIndicators(g);
        }
    }
    
    private void drawCooldownIndicators(Graphics g) {
        // Melee attack cooldown
        long meleeCooldownRemaining = player.getMeleeAttackRemainingCooldown();
        if (meleeCooldownRemaining > 0) {
            double seconds = meleeCooldownRemaining / 1000.0;
            g.setColor(Color.RED);
            g.drawString(String.format("Melee CD: %.2f s", seconds), 20, 100);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Melee Attack Ready!", 20, 100);
        }
        
        // Ranged attack cooldown
        long rangeCooldownRemaining = player.getRangeAttackRemainingCooldown();
        if (rangeCooldownRemaining > 0) {
            double seconds = rangeCooldownRemaining / 1000.0;
            g.setColor(Color.RED);
            g.drawString(String.format("Range CD: %.2f s", seconds), 20, 120);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Range Attack Ready!", 20, 120);
        }
    }
}