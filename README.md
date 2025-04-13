# Virtual Maze Solver — BFS Visualization


A Java Swing application that generates a random maze and visually demonstrates how **Breadth-First Search (BFS)** explores and solves it in real time!

## Features

- **Random Maze Generation:** Creates a new 10x10 maze with a 30% chance of a cell being a wall.
- **BFS Animation:** Visually illustrates the exploration process using a Swing Timer.
- **Visual Markers:**
  - **Green:** Start cell
  - **Red:** End cell
  - **Black:** Walls
  - **Light Blue:** Visited cells
  - **Yellow:** Final shortest path (if found)
- **Control Buttons:**
  - **Solve Maze:** Begin the BFS animation.
  - **Reset Maze:** Clear visited and solution markers.
  - **Random Maze:** Generate a new maze layout.

## How It Works

1. **Maze Setup:**  
   A grid of 10 rows and 10 columns is initialized. Walls are randomly placed in the grid except for the start (top-left) and end (bottom-right) cells.

2. **Breadth-First Search (BFS):**  
   The algorithm uses a FIFO queue to explore the maze cell by cell. It tracks visited cells and records the path using a previous cell array. Once the destination is reached, the solution path is constructed by backtracking.

3. **Visualization:**  
   The maze and the BFS progress are rendered on a custom Swing panel, with updates made via a Swing Timer to animate the search process.

## Requirements

- **Java 8** or higher
