package shooter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

class Canvas extends JPanel {
    private final int WIDTH = 700;
    private final int HEIGHT = 700;

    private final Color NUMBER_COLOR = new Color(94, 150, 210);
    private final Color TARGET_COLOR1 = Color.BLACK;
    private final Color TARGET_COLOR2 = Color.WHITE;

    private final Point gunSightPosition;
    private final List<Point> bulletHoles = new ArrayList<>();
    private static final int MOVE_DISTANCE = 2;
    private static final int MIN_POSITION = 40;
    private static final int MAX_POSITION = 660;

    private GameState gameState = GameState.START;
    private int numberOfAttempts = 12;
    private int score = 0;
    private int lastScore = 0;

    private int[] accelerationSequence = {1, 2, 4, 2, 1};
    private int lastDirection = 0;  // 0: None, 1: Up, 2: Down, 3: Left, 4: Right


    public Canvas() {
        setName("Canvas");
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        Color BACKGROUND_COLOR = Color.DARK_GRAY;
        setBackground(BACKGROUND_COLOR);
        gunSightPosition = new Point(WIDTH / 2, HEIGHT / 2);
        setFocusable(true);
        requestFocusInWindow();
        setupKeyBindings();
        updateStatusBar();
    }


    private void setupKeyBindings() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (gameState == GameState.START && key == KeyEvent.VK_SPACE) {
                    gameState = GameState.GAME;
                    updateStatusBar();
                    return;
                }
                if (gameState == GameState.GAME) {
                    switch (key) {
                        case KeyEvent.VK_UP:
                            if (lastDirection != 1) {
                                lastDirection = 1;
                                resetAcceleration();
                            }
                            applyAcceleration(-MOVE_DISTANCE, 0);  // Negative for moving up
                            break;
                        case KeyEvent.VK_DOWN:
                            if (lastDirection != 2) {
                                lastDirection = 2;
                                resetAcceleration();
                            }
                            applyAcceleration(MOVE_DISTANCE, 0);  // Positive for moving down
                            break;
                        case KeyEvent.VK_LEFT:
                            if (lastDirection != 3) {
                                lastDirection = 3;
                                resetAcceleration();
                            }
                            applyAcceleration(0, -MOVE_DISTANCE);  // Negative for moving left
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (lastDirection != 4) {
                                lastDirection = 4;
                                resetAcceleration();
                            }
                            applyAcceleration(0, MOVE_DISTANCE);  // Positive for moving right
                            break;
                        case KeyEvent.VK_SPACE:
                            if (numberOfAttempts > 0) {

                                int deviationX = (int) (Math.random() * 20 - 10);
                                int deviationY = (int) (Math.random() * 20 - 10);

                                bulletHoles.add(new Point(gunSightPosition.x + deviationX, gunSightPosition.y + deviationY));

                                lastScore = calculateScore(gunSightPosition);
                                score += lastScore;
                                numberOfAttempts--;
                                if (numberOfAttempts == 0) {
                                    gameState = GameState.END;
                                }
                                updateStatusBar();
                            }
                            break;
                    }
                    repaint(); // Redraw the canvas whenever a key is pressed
                }
            }
        });
    }

    private void applyAcceleration(int dy, int dx) {
        for (int factor : accelerationSequence) {
            gunSightPosition.y = Math.max(MIN_POSITION, Math.min(MAX_POSITION, gunSightPosition.y + dy * factor));
            gunSightPosition.x = Math.max(MIN_POSITION, Math.min(MAX_POSITION, gunSightPosition.x + dx * factor));
            repaint();
            try {
                // Add a delay to visually see the acceleration effect. Adjust as necessary.
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetAcceleration() {
        // Reset the acceleration sequence if direction changes
        accelerationSequence = new int[]{1, 2, 4, 2, 1};
    }


    private int calculateScore(Point bulletHolePosition) {
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;
        int initialRadius = 30;

        double distanceFromCenter = bulletHolePosition.distance(centerX, centerY);
        for (int i = 0; i < 10; i++) {
            int radius = initialRadius + (i * 30);
            if (distanceFromCenter <= radius) {
                return 10 - i;
            }
        }

        return 0;
    }

    private void updateStatusBar() {
        switch (gameState) {
            case START:
                SharpShooter.setStatusBarText("Press the SPACE bar to start the game");
                break;
            case GAME:
                SharpShooter.setStatusBarText(String.format("Bullets left: %d, your score: %d (%d)", numberOfAttempts, score, lastScore));
                break;
            case END:
                SharpShooter.setStatusBarText(String.format("Game over, your score: %d", score));
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (g instanceof Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        drawTarget(g);


        for (Point bulletHole : bulletHoles) {
            drawBulletHole(g, bulletHole.x, bulletHole.y);
        }
        drawGunSight(g, gunSightPosition.x, gunSightPosition.y);
    }

    private void drawTarget(Graphics g) {
        int centerX = 350;
        int centerY = 350;
        int initialRadius = 30;

        // Set bigger font for numbers
        g.setFont(new Font("Arial", Font.BOLD, 20));

        for (int i = 9; i >= 0; i--) {
            int radius = initialRadius + (i * 30);

            if (i > 3 || i == 0) {
                g.setColor(TARGET_COLOR1);
                drawCircleWithBorder(g, centerX, centerY, radius, TARGET_COLOR1, TARGET_COLOR1);
            } else {
                g.setColor(TARGET_COLOR2);
                drawCircleWithBorder(g, centerX, centerY, radius, TARGET_COLOR2, TARGET_COLOR2);
            }

            if (i != 0) {
                g.setColor(NUMBER_COLOR);
                String number = String.valueOf(10 - i);
                FontMetrics fm = g.getFontMetrics();
                int stringWidth = fm.stringWidth(number);
                int stringHeight = fm.getAscent();

                // Adjust position to be slightly inside the circle and not on the border
                int offset = 15;

                int xH1 = centerX - radius + offset - stringWidth / 2;
                int yH = centerY + stringHeight / 2;
                int xH2 = centerX + radius - offset - stringWidth / 2;

                int yV1 = centerY - radius + offset + stringHeight / 2;  // top number
                int yV2 = centerY + radius - stringHeight / 2; // bottom number

                // Draw numbers
                g.drawString(number, xH1, yH);
                g.drawString(number, xH2, yH);
                g.drawString(number, centerX - stringWidth / 2, yV1);
                g.drawString(number, centerX - stringWidth / 2, yV2);
            }
        }
    }

    private void drawCircleWithBorder(Graphics g, int x, int y, int r, Color fillColor, Color borderColor) {
        g.setColor(fillColor);
        g.fillOval(x - r, y - r, 2 * r, 2 * r);
        g.setColor(borderColor);
        g.drawOval(x - r, y - r, 2 * r, 2 * r);
    }

    private void drawBulletHole(Graphics g, int x, int y) {
        g.setColor(Color.GREEN);
        g.fillOval(x - 5, y - 5, 10, 10);
    }

    private void drawGunSight(Graphics g, int x, int y) {
        if (g instanceof Graphics2D g2d) {
            g2d.setColor(Color.RED);
            int sightSize = 60;
            int crossThick = 1;

            float thickness = 4.0f;  // Adjust this value for the circle's thickness
            g2d.setStroke(new BasicStroke(thickness));

            g2d.drawOval(x - sightSize / 2, y - sightSize / 2, sightSize, sightSize);

            // Calculate the arms of the sight
            int armLength = sightSize / 2;
            int gap = 8;  // Adjust this value for the size of the gap

            // Drawing the 4 arms of the sight
            g2d.fillRect(x - armLength, y - crossThick, armLength - gap, 2 * crossThick); // Left arm
            g2d.fillRect(x + gap, y - crossThick, armLength - gap, 2 * crossThick);       // Right arm
            g2d.fillRect(x - crossThick, y - armLength, 2 * crossThick, armLength - gap); // Top arm
            g2d.fillRect(x - crossThick, y + gap, 2 * crossThick, armLength - gap);       // Bottom arm
        }
    }
}
