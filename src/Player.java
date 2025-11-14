import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.imageio.ImageIO;

public class Player {
    
    // ========================================
    // POSITION & DIMENSIONS
    // ========================================
    
    private int craneX;
    private int craneY;
    private int spawnX;
    private int spawnY;
    private final int craneWidth = 192;
    private final int craneHeight = 192;
    private final float craneBaseSpeed = 1.5f;
    
    // ========================================
    // MOVEMENT & DIRECTION
    // ========================================

    private String caneCurrentDirection = "down";
    private String craneState = "idle";
    
    // ========================================
    // HEALTH & MANA SYSTEM
    // ========================================

    private final int craneMaxHealth = 100;
    private int craneCurrentHealth = 100;
    private final int craneMaxMana = 100;
    private int craneCurrentMana = 100;

    // ========================================
    // DEATH & RESPAWN SYSTEM
    // ========================================
    
    private boolean isDead = false;
    private long deathTime = 0;
    private final long autoRespawnDelay = 5000; // 5 seconds
    private boolean canManualRespawn = false;
    
    // ========================================
    // HITBOXES & COLLISION
    // ========================================
    
    private Rectangle craneBodyHitbox;
    private Rectangle craneFrontSensor;
    private final int craneBodyWidthCollision = 30;
    private final int craneBodyHeightCollision = 21;
    private final int craneSensorWidthCollision = 7;
    private final int craneSensorHeightCollision = 8;
    private final int craneSensorDistanceCollision = 15;
    
    // ========================================
    // ANIMATION SYSTEM
    // ========================================

    private BufferedImage craneCurrentFrame;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private final int animationInterval = 100;
    private BufferedImage[] upMovement;
    private BufferedImage[] downMovement;
    private BufferedImage[] leftMovement;
    private BufferedImage[] rightMovement;
    private BufferedImage[] upLeftMovement;
    private BufferedImage[] upRightMovement;
    private BufferedImage[] downLeftMovement;
    private BufferedImage[] downRightMovement;
    
    // ========================================
    // MELEE ATTACK SYSTEM
    // ========================================
    
    private BufferedImage[] upMelee;
    private BufferedImage[] downMelee;
    private BufferedImage[] leftMelee;
    private BufferedImage[] rightMelee;
    private BufferedImage[] upLeftMelee;
    private BufferedImage[] upRightMelee;
    private BufferedImage[] downLeftMelee;
    private BufferedImage[] downRightMelee;
    private boolean isMeleeAttacking = false;
    private int meleeAttackFrameIndex = 0;
    private int meleeAttackFrameCounter = 0;
    private final int meleeAttackFrameDelay = 10;
    private final int meleeSpawnFrame = 3;

    private boolean meleeAttackOnCooldown = false;
    private long meleeLastAttackTime = 0;
    private final long meleeAttackCooldownDuration = 1000; // 1 second
    private Rectangle meleeHitbox = null;
    @SuppressWarnings("unused")
    private boolean meleeDamageApplied = false; // Don't removed this
    private boolean meleeSpawnedThisAttack = false;
    private final Set<Enemy> hitEnemiesThisAttack = new HashSet<>();
    
    // ========================================
    // RANGE ATTACK SYSTEM
    // ========================================

    private BufferedImage[] upRange;
    private BufferedImage[] downRange;
    private BufferedImage[] leftRange;
    private BufferedImage[] rightRange;
    private BufferedImage[] upLeftRange;
    private BufferedImage[] upRightRange;
    private BufferedImage[] downLeftRange;
    private BufferedImage[] downRightRange;
    private boolean isRangeAttacking = false;
    private int rangeAttackFrameIndex = 0;
    private int rangeAttackFrameCounter = 0;
    private final int rangeAttackFrameDelay = 10;
    private final int projectileSpawnFrame = 4;

    private boolean rangeAttackOnCooldown = false;
    private long rangeLastAttackTime = 0;
    private final long rangeAttackCooldownDuration = 1000; // 1 second
    private boolean projectileSpawnedThisAttack = false;

    public final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final int projectileSpeed = 4;
    private final int projectileRange = 200;
    private final int rangeManaCost = 5;
    
    // ========================================
    // ATTACK DAMAGE VALUES
    // ========================================

    private int craneMeleeDamage = 10;
    private int craneRangeDamage = 15;

    // ========================================
    // BREADCRUMB TRAIL (for enemy tracking)
    // ========================================
    
    public final List<Point> breadcrumbs = new ArrayList<>();
    private final int breadcrumbMax = 15;
    private long lastBreadcrumbTime = 0L;
    private final int breadcrumbSpacingMs = 200;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================

    public Player(int startX, int startY) {
        this.craneX = startX;
        this.craneY = startY;
        this.spawnX = startX;
        this.spawnY = startY;
        initHitboxes();
        try {
            loadSprites();
        } catch (IOException e) {
            System.err.println("Failed to load player sprites: " + e.getMessage());
        }
    }
    
    // ========================================
    // INITIALIZATION
    // ========================================
    
    private void initHitboxes() {
        // Create body hitbox (centered on sprite)
        craneBodyHitbox = new Rectangle(
            craneX + (craneWidth - craneBodyWidthCollision) / 2,
            craneY + (craneHeight - craneBodyHeightCollision) / 2,
            craneBodyWidthCollision, craneBodyHeightCollision
        );
        
        // Create front sensor (positioned ahead of player)
        craneFrontSensor = new Rectangle(
            craneX + (craneWidth - craneSensorWidthCollision) / 2,
            craneY + (craneHeight - craneSensorHeightCollision) / 2 - craneSensorDistanceCollision,
            craneSensorWidthCollision, craneSensorHeightCollision
        );
    }
    
    private void loadSprites() throws IOException {
        // Movement animations
        upMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Back\\mc_back", 8);
        downMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Front\\mc_front", 8);
        leftMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Left\\mc_left", 8);
        rightMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Right\\mc_right", 8);
        upLeftMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Upper_Left\\mc_ULeft", 8);
        upRightMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Upper_Right\\mc_URight", 8);
        downLeftMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Lower_Left\\mc_LLeft", 8);
        downRightMovement = loadAnimation("resources\\Characters\\Crane\\Crane_Movement\\Lower_Right\\mc_LRight", 8);
        
        // Melee attack animations
        upMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Back\\mc_back", 4);
        downMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Front\\mc_front", 4);
        leftMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Left\\mc_left", 4);
        rightMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Right\\mc_right", 4);
        upLeftMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Upper_Left\\mc_Upper_Left", 4);
        upRightMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Upper_Right\\mc_Upper_Right", 4);
        downLeftMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Lower_Left\\mc_Lower_Left", 4);
        downRightMelee = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Melee\\Lower_Right\\mc_Lower_Right", 4);
        
        // Ranged attack animations
        upRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Back\\mc_back", 5);
        downRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Front\\mc_front", 5);
        leftRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Left\\mc_left", 5);
        rightRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Right\\mc_right", 5);
        upLeftRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Upper_Left\\mc_upper_left", 5);
        upRightRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Upper_Right\\mc_upper_right", 5);
        downLeftRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Lower_Left\\mc_lower_left", 5);
        downRightRange = loadAnimation("resources\\Characters\\Crane\\Crane_Attack_Range\\Lower_Right\\mc_lower_right", 5);
        
        // Set default idle frame (facing down)
        craneCurrentFrame = downMovement[3];
    }
    
    private BufferedImage[] loadAnimation(String basePath, int frameCount) throws IOException {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = basePath + (i + 1) + ".png";
            frames[i] = ImageIO.read(new File(path));
        }
        return frames;
    }
    
    // ========================================
    // UPDATE METHODS
    // ========================================
    
    public void update(KeyHandler keyH, Collision collision, int worldWidth, int worldHeight) {
        // Check if player is dead
        if (isDead) {
            updateDeathState(keyH);
            return; // Skip all other updates while dead
        }

        // Execute state-specific update logic
        switch (craneState) {
            case "idle" -> updateIdle(keyH);
            case "moving" -> updateMovement(keyH, collision);
            case "isCraneMeleeAttacking" -> updateMeleeAttack(keyH);
            case "isCraneRangeAttacking" -> updateRangeAttack(keyH);
        }
        
        // Regenerate mana over time
        regenerateMana();
        
        // Update all active projectiles
        updateProjectiles(collision);
        
        // Update attack cooldown timers
        updateCooldowns();
        
        // Prevent player from leaving world bounds
        enforceWorldBoundaries(worldWidth, worldHeight);
        
        // Update collision detection sensor
        collision.checkCollision(craneFrontSensor);
    }
    
    private void updateIdle(KeyHandler keyH) {
        // Check for attack inputs first (attacks have priority)
        if (keyH.meleeAttackPressed) {
            if (startMeleeAttack()) return;
        }
        
        if (keyH.rangeAttackPressed) {
            if (startRangeAttack()) {
                keyH.rangeAttackPressed = false;
            }
        }
        
        // Check for movement
        if (isMovingInput(keyH)) {
            craneState = "moving";
            updateMovement(keyH, null);
        } else {
            setIdle();
        }
    }
    
    private void updateMovement(KeyHandler keyH, Collision collision) {
        // Attacks can interrupt movement
        if (keyH.meleeAttackPressed) {
            if (startMeleeAttack()) return;
        }
        
        if (keyH.rangeAttackPressed) {
            if (startRangeAttack()) {
                keyH.rangeAttackPressed = false;
            }
        }
        
        // Calculate movement direction
        int moveX = 0, moveY = 0;
        int speed = keyH.shiftPressed ? (int) craneBaseSpeed + 1 : (int) craneBaseSpeed;
        
        if (keyH.upPressed) moveY -= 1;
        if (keyH.downPressed) moveY += 1;
        if (keyH.leftPressed) moveX -= 1;
        if (keyH.rightPressed) moveX += 1;
        
        // Apply movement or transition to idle
        if (moveX != 0 || moveY != 0) {
            move(moveX, moveY, speed, collision);
        } else {
            craneState = "idle";
            setIdle();
        }
    }
    
    private void updateMeleeAttack(KeyHandler keyH) {
        updateMeleeAttackAnimation();
        
        if (!isMeleeAttacking) {
            // Clear hitbox when attack finishes
            meleeHitbox = null;
            meleeDamageApplied = false;
            craneState = isMovingInput(keyH) ? "moving" : "idle";
        }
    }
    
    private void updateRangeAttack(KeyHandler keyH) {
        updateRangeAttackAnimation();
        
        if (!isRangeAttacking) {
            craneState = isMovingInput(keyH) ? "moving" : "idle";
        }
    }

    // ========================================
    // DEATH & RESPAWN SYSTEM
    // ========================================
    
    private void updateDeathState(KeyHandler keyH) {
        long currentTime = System.currentTimeMillis();
        long timeSinceDeath = currentTime - deathTime;
        
        // Enable manual respawn after 1 second
        if (timeSinceDeath >= 1000) {
            canManualRespawn = true;
        }
        
        // Check for manual respawn (R key)
        if (canManualRespawn && keyH.respawnPressed) {
            respawn();
            keyH.respawnPressed = false; // Reset the key
            return;
        }
        
        // Auto respawn after delay
        if (timeSinceDeath >= autoRespawnDelay) {
            respawn();
        }
    }
    
    private void die() {
        if (isDead) return; // Already dead
        
        isDead = true;
        deathTime = System.currentTimeMillis();
        canManualRespawn = false;
        craneState = "dead";
        
        // Clear all active effects
        projectiles.clear();
        breadcrumbs.clear();
        meleeHitbox = null;
        hitEnemiesThisAttack.clear();
        
        // Reset attack states
        isMeleeAttacking = false;
        isRangeAttacking = false;
        meleeAttackOnCooldown = false;
        rangeAttackOnCooldown = false;
    }
    
    public void respawn() {
        // Reset position to spawn point
        craneX = spawnX;
        craneY = spawnY;
        
        // Reset health and mana
        craneCurrentHealth = craneMaxHealth;
        craneCurrentMana = craneMaxMana;
        
        // Reset state
        isDead = false;
        canManualRespawn = false;
        craneState = "idle";
        caneCurrentDirection = "down";
        
        // Reset hitboxes
        updatecraneBodyHitbox();
        updateSensorPosition();
        
        // Set idle frame
        setIdle();
    }
    
    public void setSpawnPoint(int x, int y) {
        this.spawnX = x;
        this.spawnY = y;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public long getRespawnTimeRemaining() {
        if (!isDead) return 0;
        long timeSinceDeath = System.currentTimeMillis() - deathTime;
        long remaining = autoRespawnDelay - timeSinceDeath;
        return Math.max(remaining, 0);
    }
    
    public boolean canManualRespawn() {
        return canManualRespawn;
    }

    // ========================================
    // MOVEMENT
    // ========================================
    
    private void move(int moveX, int moveY, int speed, Collision collision) {
        if (collision == null) return;
        
        // Calculate next positions
        int nextX = craneX + moveX * speed;
        int nextY = craneY + moveY * speed;
        
        // Create temporary hitbox for X-axis collision check
        Rectangle nextBodyX = new Rectangle(
            nextX + (craneWidth - craneBodyWidthCollision) / 2,
            craneY + (craneHeight - craneBodyHeightCollision) / 2,
            craneBodyWidthCollision, craneBodyHeightCollision
        );
        
        // Create temporary hitbox for Y-axis collision check
        Rectangle nextBodyY = new Rectangle(
            craneX + (craneWidth - craneBodyWidthCollision) / 2,
            nextY + (craneHeight - craneBodyHeightCollision) / 2,
            craneBodyWidthCollision, craneBodyHeightCollision
        );
        
        // Apply movement only if no collision detected
        if (!collision.checkCollision(nextBodyX)) craneX = nextX;
        if (!collision.checkCollision(nextBodyY)) craneY = nextY;
        
        // Update direction and play appropriate animation
        if (moveY < 0 && moveX == 0) setDirection("up", upMovement);
        else if (moveY > 0 && moveX == 0) setDirection("down", downMovement);
        else if (moveX < 0 && moveY == 0) setDirection("left", leftMovement);
        else if (moveX > 0 && moveY == 0) setDirection("right", rightMovement);
        else if (moveY < 0 && moveX < 0) setDirection("up-left", upLeftMovement);
        else if (moveY < 0 && moveX > 0) setDirection("up-right", upRightMovement);
        else if (moveY > 0 && moveX < 0) setDirection("down-left", downLeftMovement);
        else if (moveY > 0 && moveX > 0) setDirection("down-right", downRightMovement);
        
        // Synchronize all hitboxes with new position
        updatecraneBodyHitbox();
        updateSensorPosition();
    }
    
    private void enforceWorldBoundaries(int worldWidth, int worldHeight) {
        if (craneX < 0) craneX = 0;
        if (craneY < 0) craneY = 0;
        if (craneX > worldWidth - craneWidth) craneX = worldWidth - craneWidth;
        if (craneY > worldHeight - craneHeight) craneY = worldHeight - craneHeight;
    }
    
    // ========================================
    // ANIMATION
    // ========================================
    
    private void setDirection(String direction, BufferedImage[] frames) {
        caneCurrentDirection = direction;
        animate(frames);
    }
    
    private void animate(BufferedImage[] frames) {
        long now = System.nanoTime();
        long intervalNanos = animationInterval * 1_000_000L;
        
        if (now - lastFrameTime >= intervalNanos) {
            frameIndex = (frameIndex + 1) % frames.length;
            craneCurrentFrame = frames[frameIndex];
            lastFrameTime = now;
        }
    }
    
    private void setIdle() {
        switch (caneCurrentDirection) {
            case "up" -> craneCurrentFrame = upMovement[3];
            case "down" -> craneCurrentFrame = downMovement[3];
            case "left" -> craneCurrentFrame = leftMovement[3];
            case "right" -> craneCurrentFrame = rightMovement[3];
            case "up-left" -> craneCurrentFrame = upLeftMovement[3];
            case "up-right" -> craneCurrentFrame = upRightMovement[3];
            case "down-left" -> craneCurrentFrame = downLeftMovement[3];
            case "down-right" -> craneCurrentFrame = downRightMovement[3];
        }
    }
    
    // ========================================
    // MELEE ATTACK
    // ========================================
    
    private boolean startMeleeAttack() {
        if (isMeleeAttacking || meleeAttackOnCooldown) return false;
        
        isMeleeAttacking = true;
        meleeAttackOnCooldown = true;
        meleeAttackFrameIndex = 0;
        meleeAttackFrameCounter = 0;
        craneState = "isCraneMeleeAttacking";
        meleeLastAttackTime = System.currentTimeMillis();
        hitEnemiesThisAttack.clear();
        
        return true;
    }
    

    private void updateMeleeAttackAnimation() {
        if (!isMeleeAttacking) return;
        
        // Get animation frames for current direction
        BufferedImage[] attackFrames = switch (caneCurrentDirection) {
            case "up" -> upMelee;
            case "down" -> downMelee;
            case "left" -> leftMelee;
            case "right" -> rightMelee;
            case "up-left" -> upLeftMelee;
            case "up-right" -> upRightMelee;
            case "down-left" -> downLeftMelee;
            case "down-right" -> downRightMelee;
            default -> null;
        };
        
        if (attackFrames == null) {
            isMeleeAttacking = false;
            return;
        }
        
        // Advance animation frame
        meleeAttackFrameCounter++;
        if (meleeAttackFrameCounter >= meleeAttackFrameDelay) {
            meleeAttackFrameCounter = 0;
            meleeAttackFrameIndex++;
            
            // Spawn hitbox at designated frame
            if (!meleeSpawnedThisAttack && meleeAttackFrameIndex == meleeSpawnFrame) {
                createMeleeHitbox();
                meleeSpawnedThisAttack = true;
            }
            
            // End attack when animation completes
            if (meleeAttackFrameIndex >= attackFrames.length) {
                meleeAttackFrameIndex = 0;
                isMeleeAttacking = false;
                meleeSpawnedThisAttack = false;
                setIdle();
                return;
            }
        }
        
        // Display current attack frame
        craneCurrentFrame = attackFrames[meleeAttackFrameIndex];
    }
    
    private void createMeleeHitbox() {
        int hitboxWidth = 20;
        int hitboxHeight = 20;
        int hitboxX = craneX + craneWidth / 2 - hitboxWidth / 2;
        int hitboxY = craneY + craneHeight / 2 - hitboxHeight / 2;
        int offset = 20;
        
        // Position hitbox based on facing direction
        switch (caneCurrentDirection) {
            case "up" -> hitboxY -= offset;
            case "down" -> hitboxY += offset;
            case "left" -> hitboxX -= offset;
            case "right" -> hitboxX += offset;
            case "up-left" -> { hitboxX -= offset; hitboxY -= offset; }
            case "up-right" -> { hitboxX += offset; hitboxY -= offset; }
            case "down-left" -> { hitboxX -= offset; hitboxY += offset; }
            case "down-right" -> { hitboxX += offset; hitboxY += offset; }
        }
        
        meleeHitbox = new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }
    
    public boolean checkMeleeHit(Enemy enemy) {
        if (meleeHitbox == null) return false;
        if (hitEnemiesThisAttack.contains(enemy)) return false;

        if (meleeHitbox.intersects(enemy.getBodyHitbox())) {
            hitEnemiesThisAttack.add(enemy); 
            return true;
        }
        return false;
    }
    
    // ========================================
    // RANGE ATTACK
    // ========================================
    
    private boolean startRangeAttack() {

        // Safety check: reset stuck attack state
        if (isRangeAttacking && !rangeAttackOnCooldown) {
            isRangeAttacking = false;
            rangeAttackFrameIndex = 0;
            rangeAttackFrameCounter = 0;
            projectileSpawnedThisAttack = false;
        }
        
        // Check if attack is blocked
        if (isRangeAttacking || rangeAttackOnCooldown) {
            return false;
        }
        
        // Check mana requirement
        if (craneCurrentMana < rangeManaCost) {
            return false;
        }
        
        // Start the attack
        isRangeAttacking = true;
        rangeAttackFrameIndex = 0;
        rangeAttackFrameCounter = 0;
        craneState = "isCraneRangeAttacking";
        rangeAttackOnCooldown = true;
        rangeLastAttackTime = System.currentTimeMillis();
        projectileSpawnedThisAttack = false;

        // Deduct mana cost
        craneCurrentMana -= rangeManaCost;
        if (craneCurrentMana < 0) craneCurrentMana = 0;
        
        return true;
    }
    
    private void updateRangeAttackAnimation() {
        if (!isRangeAttacking) return;
        
        // Get animation frames for current direction
        BufferedImage[] attackFrames = switch (caneCurrentDirection) {
            case "up" -> upRange;
            case "down" -> downRange;
            case "left" -> leftRange;
            case "right" -> rightRange;
            case "up-left" -> upLeftRange;
            case "up-right" -> upRightRange;
            case "down-left" -> downLeftRange;
            case "down-right" -> downRightRange;
            default -> null;
        };
        
        if (attackFrames == null) {
            isRangeAttacking = false;
            return;
        }
        
        // Advance animation frame
        rangeAttackFrameCounter++;
        if (rangeAttackFrameCounter >= rangeAttackFrameDelay) {
            rangeAttackFrameCounter = 0;
            rangeAttackFrameIndex++;
            
            // Spawn projectile at designated frame
            if (!projectileSpawnedThisAttack && rangeAttackFrameIndex == projectileSpawnFrame) {
                spawnProjectile();
                projectileSpawnedThisAttack = true;
            }
            
            // End attack when animation completes
            if (rangeAttackFrameIndex >= attackFrames.length) {
                rangeAttackFrameIndex = 0;
                isRangeAttacking = false;
                projectileSpawnedThisAttack = false;
                setIdle();
                return;
            }
        }
        
        // Display current attack frame
        craneCurrentFrame = attackFrames[rangeAttackFrameIndex];
    }
    
    private void spawnProjectile() {
        int centerX = craneX + craneWidth / 2;
        int centerY = craneY + craneHeight / 2;
        
        float dirX = 0, dirY = 0;
        
        // Determine direction vector based on facing direction
        switch (caneCurrentDirection) {
            case "up" -> dirY = -1;
            case "down" -> dirY = 1;
            case "left" -> dirX = -1;
            case "right" -> dirX = 1;
            case "up-left" -> { dirX = -1; dirY = -1; }
            case "up-right" -> { dirX = 1; dirY = -1; }
            case "down-left" -> { dirX = -1; dirY = 1; }
            case "down-right" -> { dirX = 1; dirY = 1; }
            default -> dirY = 1;
        }
        
        // Normalize direction vector for consistent speed
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len != 0f) {
            dirX /= len;
            dirY /= len;
        }
        
        // Create and add projectile to active list
        Projectile p = new Projectile(centerX, centerY, dirX, dirY, projectileSpeed, projectileRange);
        projectiles.add(p);
    }
    
    private void updateProjectiles(Collision collision) {
        // Update all projectiles
        for (Projectile p : projectiles) {
            p.update(collision);
        }
        
        // Remove dead projectiles
        projectiles.removeIf(p -> !p.alive);
    }
    
    // ========================================
    // HEALTH & MANA
    // ========================================
    
    public void cranetakeDamage(int damage, ScreenShake screenShake) {
        if (isDead) return; // Can't take damage while dead
        
        craneCurrentHealth -= damage;
        if (craneCurrentHealth <= 0) {
            craneCurrentHealth = 0;
            die(); // Trigger death
        }

        // Trigger screen shake effect
        if (screenShake != null) {
            int shakeIntensity = Math.min(damage / 2 + 3, 12); // Scale shake with damage
            long shakeDuration = 200 + (damage * 10L); // Longer shake for big hits
            screenShake.start(shakeDuration, shakeIntensity);
        }
    }
    
    private void regenerateMana() {
        long currentTime = System.currentTimeMillis();
        if (currentTime % 1000 < 17) { // Approximately once per second
            if (craneCurrentMana < craneMaxMana) {
                craneCurrentMana += 1;
            }
        }
    }
    
    // ========================================
    // BREADCRUMB SYSTEM (for enemy AI)
    // ========================================
    
    public void updateBreadcrumbs(List<Enemy> enemies) {
        // Only create breadcrumbs when an enemy can see the player
        boolean anyEnemyCanSeePlayer = false;
        
        for (Enemy en : enemies) {
            if (en.canCurrentlySeePlayer()) {
                anyEnemyCanSeePlayer = true;
                break;
            }
        }
        
        if (!anyEnemyCanSeePlayer) return;
        
        // Create breadcrumb at regular intervals
        long now = System.currentTimeMillis();
        if (now - lastBreadcrumbTime >= breadcrumbSpacingMs) {
            breadcrumbs.add(new Point(craneBodyHitbox.x + 15, craneBodyHitbox.y));
            lastBreadcrumbTime = now;
            
            // Maintain maximum breadcrumb count
            if (breadcrumbs.size() > breadcrumbMax) {
                breadcrumbs.remove(0); // Remove oldest breadcrumb
            }
        }
    }
    
    public void clearBreadcrumbs() {
        breadcrumbs.clear();
    }
    
    // ========================================
    // COOLDOWN MANAGEMENT
    // ========================================
    
    private void updateCooldowns() {
        long currentTime = System.currentTimeMillis();
        
        // Update melee cooldown
        if (meleeAttackOnCooldown) {
            if (currentTime - meleeLastAttackTime >= meleeAttackCooldownDuration) {
                meleeAttackOnCooldown = false;
            }
        }
        
        // Update ranged attack cooldown
        if (rangeAttackOnCooldown) {
            if (currentTime - rangeLastAttackTime >= rangeAttackCooldownDuration) {
                rangeAttackOnCooldown = false;
            }
        }
    }
    
    public long getMeleeAttackRemainingCooldown() {
        if (!meleeAttackOnCooldown) return 0;
        long elapsed = System.currentTimeMillis() - meleeLastAttackTime;
        long remaining = meleeAttackCooldownDuration - elapsed;
        return Math.max(remaining, 0);
    }
    
    public long getRangeAttackRemainingCooldown() {
        if (!rangeAttackOnCooldown) return 0;
        long elapsed = System.currentTimeMillis() - rangeLastAttackTime;
        long remaining = rangeAttackCooldownDuration - elapsed;
        return Math.max(remaining, 0);
    }
    
    // ========================================
    // HITBOX UPDATES
    // ========================================
    
    private void updatecraneBodyHitbox() {
        craneBodyHitbox.setLocation(
            craneX + (craneWidth - craneBodyWidthCollision) / 2,
            craneY + (craneHeight - craneBodyHeightCollision) / 2
        );
    }
    
    private void updateSensorPosition() {
        int offsetX = 0, offsetY = 0;
        
        // Calculate offset based on facing direction
        switch (caneCurrentDirection) {
            case "up" -> offsetY = -craneSensorDistanceCollision;
            case "down" -> offsetY = craneSensorDistanceCollision;
            case "left" -> offsetX = -craneSensorDistanceCollision;
            case "right" -> offsetX = craneSensorDistanceCollision;
            case "up-left" -> { offsetX = -craneSensorDistanceCollision; offsetY = -craneSensorDistanceCollision; }
            case "up-right" -> { offsetX = craneSensorDistanceCollision; offsetY = -craneSensorDistanceCollision; }
            case "down-left" -> { offsetX = -craneSensorDistanceCollision; offsetY = craneSensorDistanceCollision; }
            case "down-right" -> { offsetX = craneSensorDistanceCollision; offsetY = craneSensorDistanceCollision; }
        }
        
        // Update sensor position
        craneFrontSensor.setLocation(
            craneX + (craneWidth - craneSensorWidthCollision) / 2 + offsetX,
            craneY + (craneHeight - craneSensorHeightCollision) / 2 + offsetY
        );
    }
    
    // ========================================
    // RENDERING
    // ========================================
    
    public void draw(Graphics2D g, int camX, int camY) {
        // Don't draw player if dead (optional: you can show a death sprite instead)
        if (isDead) {
            // Draw death overlay or skip drawing
            drawDeathScreen(g, camX, camY);
            return;
        }
        // Draw player sprite
        if (craneCurrentFrame != null) {
            g.drawImage(craneCurrentFrame, craneX - camX, craneY - camY, craneWidth, craneHeight, null);
        }
        
        // Draw body hitbox (debug visualization)
        g.setColor(Color.RED);
        g.drawRect(craneBodyHitbox.x - camX, craneBodyHitbox.y - camY, craneBodyHitbox.width, craneBodyHitbox.height);
        
        // Draw front sensor (debug visualization)
        g.setColor(Color.YELLOW);
        g.drawRect(craneFrontSensor.x - camX, craneFrontSensor.y - camY, craneFrontSensor.width, craneFrontSensor.height);
    }

    private void drawDeathScreen(Graphics2D g, int camX, int camY) {
        // Draw faded death sprite at death location
        if (craneCurrentFrame != null) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.drawImage(craneCurrentFrame, craneX - camX, craneY - camY, craneWidth, craneHeight, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    public void drawDeathUI(Graphics g, int screenWidth, int screenHeight) {
        if (!isDead) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        // Draw "YOU DIED" text
        g2d.setColor(new Color(200, 0, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        String deathText = "YOU DIED";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (screenWidth - fm.stringWidth(deathText)) / 2;
        int textY = screenHeight / 2 - 100;
        g2d.drawString(deathText, textX, textY);
        
        // Draw respawn instructions
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        
        if (canManualRespawn) {
            String respawnText = "Press R to Respawn";
            fm = g2d.getFontMetrics();
            textX = (screenWidth - fm.stringWidth(respawnText)) / 2;
            textY = screenHeight / 2;
            g2d.drawString(respawnText, textX, textY);
        } else {
            String waitText = "Please wait...";
            fm = g2d.getFontMetrics();
            textX = (screenWidth - fm.stringWidth(waitText)) / 2;
            textY = screenHeight / 2;
            g2d.drawString(waitText, textX, textY);
        }
        
        // Draw auto-respawn timer
        long remaining = getRespawnTimeRemaining();
        if (remaining > 0) {
            String timerText = "Auto-respawn in: " + (remaining / 1000 + 1) + "s";
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g2d.getFontMetrics();
            textX = (screenWidth - fm.stringWidth(timerText)) / 2;
            textY = screenHeight / 2 + 50;
            g2d.drawString(timerText, textX, textY);
        }
    }

    public void drawHealthBar(Graphics g, int barX, int barY, int barWidth, int barHeight) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int healthFillWidth = (int) ((double) craneCurrentHealth / craneMaxHealth * barWidth);
        
        // Draw max health background
        g2d.setColor(new Color(80, 0, 0)); // Dark red background
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw current health
        g2d.setColor(new Color(220, 20, 20)); // Bright red foreground
        g2d.fillRect(barX, barY, healthFillWidth, barHeight);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(barX, barY, barWidth, barHeight);
        
        // Draw health text (centered)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String healthText = craneCurrentHealth + " / " + craneMaxHealth;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = barX + (barWidth - fm.stringWidth(healthText)) / 2;
        int textY = barY + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(healthText, textX, textY);
    }
    
    public void drawManaBar(Graphics g, int barX, int barY, int barWidth, int barHeight) {
        Graphics2D g2d = (Graphics2D) g;
        
        int manaFillWidth = (int) ((double) craneCurrentMana / craneMaxMana * barWidth);
        
        // Draw max mana background
        g2d.setColor(new Color(0, 0, 80)); // Dark blue background
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw current mana
        g2d.setColor(new Color(20, 120, 220)); // Bright blue foreground
        g2d.fillRect(barX, barY, manaFillWidth, barHeight);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(barX, barY, barWidth, barHeight);
        
        // Draw mana text (centered)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String manaText = craneCurrentMana + " / " + craneMaxMana;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = barX + (barWidth - fm.stringWidth(manaText)) / 2;
        int textY = barY + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(manaText, textX, textY);
    }
    
    public void drawBreadcrumbs(Graphics g, int camX, int camY) {
        // Create safe copy to avoid ConcurrentModificationException
        List<Point> safeCopy = new ArrayList<>(breadcrumbs);
        
        // Draw breadcrumb points
        g.setColor(new Color(130, 92, 92)); // Brownish color for breadcrumbs
        for (Point p : safeCopy) {
            int drawX = p.x - camX;
            int drawY = p.y - camY;
            g.fillRect(drawX - 2, drawY - 2, 5, 5);
        }
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    private boolean isMovingInput(KeyHandler keyH) {
        return keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;
    }
    
    // ========================================
    // GETTERS & SETTERS
    // ========================================

    public int getcraneX() { return craneX; }
    public int getcraneY() { return craneY; }
    public int getcraneWidth() { return craneWidth; }
    public int getcraneHeight() { return craneHeight; }
    public Rectangle getcraneBodyHitbox() { return craneBodyHitbox; }
    public Rectangle getcraneFrontSensor() { return craneFrontSensor; }
    public Rectangle getMeleeHitbox() { return meleeHitbox; }
    public int getcraneCurrentHealth() { return craneCurrentHealth; }
    public int getcraneMaxHealth() { return craneMaxHealth; }
    public int getcraneCurrentMana() { return craneCurrentMana; }
    public int getcraneMaxMana() { return craneMaxMana; }
    public String getcaneCurrentDirection() { return caneCurrentDirection; }
    public String getcraneState() { return craneState; }
    public BufferedImage getcraneCurrentFrame() { return craneCurrentFrame; }
    public int getcraneMeleeDamage() { return craneMeleeDamage; }
    public int getcraneRangeDamage() { return craneRangeDamage; }
    
    public void setX(int x) { 
        this.craneX = x; 
        updatecraneBodyHitbox();
        updateSensorPosition();
    }

    public void setY(int y) { 
        this.craneY = y; 
        updatecraneBodyHitbox();
        updateSensorPosition();
    }
}