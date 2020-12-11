package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.GameState2;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;
import entity.Ship2;
import screen.PauseScreen;

/**
 * Implements the game screen(2Player), where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen2 extends Screen {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	private static final double POINT_EASY = 0.5;
	private static final double POINT_HARD = 1.5;

	/** Current game difficulty settings. */
	private GameSettings gameSettings;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship ship;
	private Ship2 ship2;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Current score. */
	private int score;
	private int score2;
	/** Player lives left. */
	private int lives;
	private int lives2;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	private int bulletsShot2;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	private int shipsDestroyed2;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. */
	private boolean bonusLife;
	private boolean bonusLife2;

	private String difficulty;
	
	/* count player1's bullet*/
	public int cnt = 0;
	public int pos = 0;
	/* count player2's bullet*/
	public int cnt2 = 0;
	public int pos2 = 0;
	
	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen2(final GameState gameState, final GameState2 gameState2,
			final GameSettings gameSettings, final boolean bonusLife, final boolean bonusLife2,
			final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.bonusLife2 = bonusLife2;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.score2 = gameState2.getScore();
		this.lives = gameState.getLivesRemaining();
		this.lives2 = gameState2.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		if (this.bonusLife2)
			this.lives2++;
		this.bulletsShot = gameState.getBulletsShot();
		this.bulletsShot2 = gameState2.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.shipsDestroyed2 = gameState2.getShipsDestroyed();
		this.difficulty = gameState.getDifficulty();
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this);
		this.ship = new Ship(this.width / 2 + 100, this.height - 30);
		this.ship2 = new Ship2(this.width / 2 - 100, this.height - 30);
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();
		this.score += LIFE_SCORE * (this.lives);
		this.score2 += LIFE_SCORE * (this.lives2);
		this.logger.info("Screen cleared with a score of " + this.score);
		this.logger.info("Screen cleared with a score of " + this.score2);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {
			
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)){
				try {
					Thread.sleep(10); //1ÃÊ
					PauseScreen current = new PauseScreen(448, 400, 60);
					//int rectWidth = current.getWidth();
					//int rectHeight = 620/6;
					//backBufferGraphics.fillRect(0, 620 / 2 - rectHeight / 2,rectWidth, rectHeight);
					current.run();
					if(current.run()==0) {
						this.lives=0; 
						this.lives2=0;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (!this.ship.isDestroyed()) {
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_RIGHT);
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_LEFT);

				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship.getPositionX()
						- this.ship.getSpeed() < 1;

				if (moveRight && !isRightBorder) {
					this.ship.moveRight();
				}
				if (moveLeft && !isLeftBorder) {
					this.ship.moveLeft();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
					if (this.ship.shoot(this.bullets)) {
						pos = this.ship.getPositionX();
						this.bulletsShot++;
						this.cnt += 1;
					}
			}

			if (!this.ship2.isDestroyed()) {
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_D);
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_A);

				boolean isRightBorder = this.ship2.getPositionX()
						+ this.ship2.getWidth() + this.ship2.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship2.getPositionX()
						- this.ship2.getSpeed() < 1;

				if (moveRight && !isRightBorder) {
					this.ship2.moveRight();
				}
				if (moveLeft && !isLeftBorder) {
					this.ship2.moveLeft();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_SHIFT))
					if (this.ship2.shoot(this.bullets)) {
						pos2 = this.ship2.getPositionX();
						this.bulletsShot2++;
						this.cnt2 += 1;
					}
			}
			
			if (this.enemyShipSpecial != null) {
				if (!this.enemyShipSpecial.isDestroyed())
					this.enemyShipSpecial.move(2, 0);
				else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
					this.enemyShipSpecial = null;

			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip();
				this.enemyShipSpecialCooldown.reset();
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			this.ship.update();
			this.ship2.update();
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets);
		}

		manageCollisions();
		cleanBullets();
		draw();

		if (this.lives2 == 0) {
			this.ship2.destroy();
		}
		if (this.lives == 0) {
			this.ship.destroy();
		}		
		if ((this.enemyShipFormation.isEmpty() || (this.lives2 == 0 && this.lives == 0))
				&& !this.levelFinished) {
			
				this.levelFinished = true;
				this.screenFinishedCooldown.reset();
		
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;

	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawEntity(this.ship, this.ship.getPositionX(),
				this.ship.getPositionY());
		drawManager.drawEntity(this.ship2, this.ship2.getPositionX(),
				this.ship2.getPositionY());
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());
		// Interface.
		drawManager.drawScore2(this, this.score);
		drawManager.drawScore3(this, this.score2);
		drawManager.drawdifficulty(this,this.difficulty, this.level);
		drawManager.drawLives(this, this.lives);
		drawManager.drawLives2(this, this.lives2);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship.isDestroyed()) {
						this.ship.destroy();
						this.lives--;
						this.logger.info("Hit on player ship, " + this.lives
								+ " lives remaining.");
					}
				}
				
				if (checkCollision(bullet, this.ship2) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship2.isDestroyed()) {
						this.ship2.destroy();
						this.lives2--;
						this.logger.info("Hit on player ship, " + this.lives
								+ " lives remaining.");
					}
				}
				
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip) && (Math.abs(enemyShip.getPositionX() - pos) <= 24)) {
						if (this.difficulty.equals("E")) {
							this.score += enemyShip.getPointValue() * POINT_EASY;
						}
						else if (this.difficulty.equals("H")) {
							this.score += enemyShip.getPointValue() * POINT_HARD;
						}
						else this.score += enemyShip.getPointValue();
						System.out.println("!enemyShip.getPositionX() : " + enemyShip.getPositionX());
						System.out.println("!enemyShipWidth : " + enemyShip.getWidth());
						System.out.println("!pos : " + pos);
						this.shipsDestroyed++;
						this.shipsDestroyed2++;	
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
					else if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip) && (Math.abs(enemyShip.getPositionX() - pos2) <= 24)) {
						if (this.difficulty.equals("E")) {
							this.score2 += enemyShip.getPointValue() * POINT_EASY;
						}
						else if (this.difficulty.equals("H")) {
							this.score2 += enemyShip.getPointValue() * POINT_HARD;
						}
						else this.score2 += enemyShip.getPointValue();
						System.out.println("!bulletShot2 : " + this.bulletsShot2);
						this.shipsDestroyed++;
						this.shipsDestroyed2++;	
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial) && (Math.abs(this.enemyShipSpecial.getPositionX() - pos) <= 24)) {
					if (this.difficulty.equals("E")) {
						this.score += this.enemyShipSpecial.getPointValue() * POINT_EASY;
					}
					else if (this.difficulty.equals("H")) {
						this.score += this.enemyShipSpecial.getPointValue() * POINT_HARD;
					}
					else this.score += this.enemyShipSpecial.getPointValue();
					System.out.println("!enemyShip.getPositionX() : " + this.enemyShipSpecial.getPositionX());
					System.out.println("!pos : " + pos);
					this.shipsDestroyed++;
					this.shipsDestroyed2++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
				else if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial) && (Math.abs(this.enemyShipSpecial.getPositionX() - pos2) <= 24)) {
					if (this.difficulty.equals("E")) {
						this.score2 += this.enemyShipSpecial.getPointValue() * POINT_EASY;
					}
					else if (this.difficulty.equals("H")) {
						this.score2 += this.enemyShipSpecial.getPointValue() * POINT_HARD;
					}
					else this.score2 += this.enemyShipSpecial.getPointValue();
					System.out.println("!bulletShot2 : " + this.bulletsShot2);
					this.shipsDestroyed++;
					this.shipsDestroyed2++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.difficulty, this.score, this.lives,
				this.bulletsShot, this.shipsDestroyed);
	}
	
	public final GameState2 getGameState2() {
		return new GameState2(this.level, this.difficulty, this.score2, this.lives2,
				this.bulletsShot2, this.shipsDestroyed2);
	}
}