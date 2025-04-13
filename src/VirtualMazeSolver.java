import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class VirtualMazeSolver extends JFrame {
    
    // Maze settings/layout
    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int CELL_SIZE = 40; // in pixels
    private int[][] maze = new int[ROWS][COLS];
    private final double WALL_PROBABILITY = 0.3; // for random generation , 30% chance for a cell to be a wall

    // start and end positions 
    private final int startRow = 0, startCol = 0;
    private final int endRow = ROWS - 1, endCol = COLS - 1;

    // Panel for drawing
    private MazePanel mazePanel;

    // For animated BFS pathfinding using Swing Timer
    private javax.swing.Timer animationTimer;
    private Queue<int[]> queue; // using a FIFO queue for BFS
    private boolean[][] visited;
    private int[][] prev; // for backtracking
    private boolean animating = false;
    private java.util.List<int[]> solutionPath = null;
    private java.util.List<int[]> visitedOrder = new ArrayList<>(); // for visualization

    // Animation delay in milliseconds to visualize BFS
    private int animationDelay = 100;

    public VirtualMazeSolver() {
        setTitle("Virtual Maze Solver - BFS");
        setSize(COLS * CELL_SIZE + 20, ROWS * CELL_SIZE + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the maze randomly
        initMaze();

      
        mazePanel = new MazePanel();

        // Create control buttons
        JButton solveBtn = new JButton("Solve Maze");
        JButton resetBtn = new JButton("Reset Maze");
        JButton randomBtn = new JButton("Random Maze");

        solveBtn.addActionListener(e -> {
            if (!animating) {
                resetAlgorithmState();
                startAnimation();
            }
        });

        resetBtn.addActionListener(e -> {
            // Clear solution and visited markings while keeping the current maze
            solutionPath = null;
            visitedOrder.clear();
            repaint();
        });
        
        randomBtn.addActionListener(e -> {
            // Generate a new random maze
            initMaze();
            solutionPath = null;
            visitedOrder.clear();
            repaint();
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(solveBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(randomBtn);

        add(mazePanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // Generate a random maze.
    private void initMaze() {
        Random rand = new Random();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if ((i == startRow && j == startCol) || (i == endRow && j == endCol)) {
                    maze[i][j] = 0;
                } else {
                    maze[i][j] = rand.nextDouble() < WALL_PROBABILITY ? 1 : 0;
                }
            }
        }
    }

    // Reset algorithm state before beginning animation using BFS to ensure no collisions occur
    private void resetAlgorithmState() {
        visited = new boolean[ROWS][COLS];
        prev = new int[ROWS * COLS][2];
        for (int i = 0; i < ROWS * COLS; i++) {
            prev[i][0] = -1;
            prev[i][1] = -1;
        }
        queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visitedOrder.clear();
    }

    // Start the animated solving using a Swing Timer with BFS
    private void startAnimation() {
        animating = true;
        animationTimer = new javax.swing.Timer(animationDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!queue.isEmpty()) {
                    int[] cur = queue.poll();
                    int r = cur[0], c = cur[1];
                    
                    // If this cell is already visited, skip further processing.
                    if (visited[r][c]) return;
                    
                    visited[r][c] = true;
                    visitedOrder.add(new int[]{r, c});
                    
                    // Check if we've reached the destination.
                    if (r == endRow && c == endCol) {
                        buildSolutionPath();
                        animationTimer.stop();
                        animating = false;
                        repaint();
                        return;
                    }
                    
                    // Explore neighbors (up, down, left, right)
                    int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
                    for (int[] dir : directions) {
                        int nr = r + dir[0], nc = c + dir[1];
                        if (isValid(nr, nc) && maze[nr][nc] == 0 && !visited[nr][nc]) {
                            // Add neighbor to the queue for further exploration.
                            queue.add(new int[]{nr, nc});
                            // Record the previous cell to be able to reconstruct the solution path.
                            prev[nr * COLS + nc][0] = r; 
                            prev[nr * COLS + nc][1] = c;
                        }
                    }
                } else {
                    // if No solution found; stop the animation.
                    animationTimer.stop();
                    animating = false;
                    JOptionPane.showMessageDialog(VirtualMazeSolver.this, "No path found!");
                }
                mazePanel.repaint();
            }
        });
        animationTimer.start();
    }

    // Build the solution path by backtracking from the destination to the start
    private void buildSolutionPath() {
        // If the destination was never reached, show no solution.
        if (!visited[endRow][endCol]) {
            solutionPath = null;
            return;
        }
        solutionPath = new ArrayList<>();
        int cr = endRow, cc = endCol;
        while (!(cr == startRow && cc == startCol)) {
            solutionPath.add(new int[]{cr, cc});
            int pr = prev[cr * COLS + cc][0];
            int pc = prev[cr * COLS + cc][1];
            cr = pr;
            cc = pc;
        }
        solutionPath.add(new int[]{startRow, startCol});
        Collections.reverse(solutionPath);
    }

    // Check if cell (r, c) is within maze bounds.
    private boolean isValid(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    // Custom panel for drawing the maze, visited cells, and solution path.
    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw maze cells
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    int x = j * CELL_SIZE;
                    int y = i * CELL_SIZE;
                    
                    // Set color for start and end.
                    if (i == startRow && j == startCol) {
                        g.setColor(Color.GREEN);
                    } else if (i == endRow && j == endCol) {
                        g.setColor(Color.RED);
                    }
                    // Walls versus open paths.
                    else if (maze[i][j] == 1) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }

            // Draw visited cells in light blue.
            for (int[] cell : visitedOrder) {
                int r = cell[0], c = cell[1];
                // Skip red/green start and end markers.
                if ((r == startRow && c == startCol) || (r == endRow && c == endCol))
                    continue;
                g.setColor(new Color(173, 216, 230)); //RGB code for light blue
                g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            // Draw the solution path in yellow if available.
            if (solutionPath != null) {
                for (int[] cell : solutionPath) {
                    int r = cell[0], c = cell[1];
                    // Skip drawing over the start/end cells.
                    if ((r == startRow && c == startCol) || (r == endRow && c == endCol))
                        continue;
                    g.setColor(Color.YELLOW);
                    g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.GRAY);
                    g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VirtualMazeSolver().setVisible(true);
        });
    }
}
