import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;

//overhead utilities
//implements deques from assignment eight
//Represents a boolean-valued question over values of type T
interface IPred<T> {
    boolean apply(T t);
}

// example of an IPred<String> used for testing
class StringLenBiggerThanThree implements IPred<String> {
    public boolean apply(String arg) {
        return arg.length() > 3;
    }
}

// another example of an IPred<String> used for testing
class StringLenThree implements IPred<String> {
    public boolean apply(String arg) {
        return arg.length() == 3;
    }
}

// class for Deque structure
class Deque<T> {
    Sentinel<T> header;

    Deque(Sentinel<T> header) {
        this.header = header;
    }

    Deque() {
        this.header = new Sentinel<T>();
    }

    // that counts the number of nodes in a list Deque, not including the header
    // node.
    int size() {
        return header.size();
    }

    // consumes a value of type T and inserts it at the front of the list.
    void addAtHead(T content) {
        header.addAtHead(content);
    }

    // consumes a value of type T and inserts it at the tail of this list
    void addAtTail(T content) {
        header.addAtTail(content);
    }

    // removes the first node from this Deque.
    T removeFromHead() {
        return header.removeFromHead();
    }

    // removes the last node from this Deque.
    T removeFromTail() {
        return header.removeFromTail();
    }

    // takes an IPred<T> and produces the first node in this
    // Deque for which the given predicate returns true.
    ANode<T> find(IPred<T> pred) {
        return header.findNode(pred);
    }

    // removes the given node from this Deque.
    // If the given node is the Sentinel header, the method does nothing.
    void removeNode(ANode<T> arg) {
        header.removeNode(arg);
    }

    // prints the deque
    String print() {
        return header.print();
    }
}

abstract class ANode<T> {
    ANode<T> next;
    ANode<T> prev;

    // no args constructor for sentinels
    ANode() {
        this.next = this;
        this.prev = this;
    }

    // constructor that takes in nodes, but makes sure neither is set to null
    ANode(ANode<T> n, ANode<T> p) {
        if (n == null || p == null) {
            throw new IllegalArgumentException("No Nulls please");
        }
        this.next = n;
        this.prev = p;
        //
        n.prev = this;
        p.next = this;
    }

    // EFFECT: adds a node
    void addNode(ANode<T> n) {
        this.next = n;
        n.prev = this;
    }

    // default size method, returns 0 for sentinels
    int countSize() {
        return 0;
    }

    // simple equivalence method to check two Nodes
    boolean sameNode(ANode<T> arg) {
        return this == arg;
    }

    // default find that simply returns this
    ANode<T> find(IPred<T> pred) {
        return this;
    }

    // EFFECT: adds a node ahead of this one
    void addAtHead(T content) {
        this.next = new Node<T>(content, this.next, this);
    }

    // EFFECT: adds a node behind this one
    void addAtTail(T content) {
        this.prev = new Node<T>(content, this, this.prev);
    }

    // abstracted out re-aligning of links
    // EFFECT: changes next and prev of this ANode
    void linkFix(ANode<T> next, ANode<T> prev) {
        this.next = next;
        this.prev = prev;
    }

    // returns next node
    ANode<T> nextNode() {
        return this.next;
    }

    // returns prev node
    ANode<T> prevNode() {
        return this.prev;
    }

    // dumby printer method for ANode<T>
    String printer() {
        return "";
    }

    // nodeRemover method to be used by Sentinel
    // since we don't want that being removed
    void nodeRemover(ANode<T> arg) {
        // necessary to keep
    }

}

class Sentinel<T> extends ANode<T> {

    Sentinel() {
        super();
    }

    // returns size of rest of list
    int size() {
        return next.countSize();
    }

    // EFFECT: removes the node closest to the head from the list
    T removeFromHead() {
        if (this == this.next) {
            throw new RuntimeException("Can't remove head of empty list");
        }
        ANode<T> oldHead = this.next;
        this.next = this.next.next;
        this.next.prev = this;
        return ((Node<T>) oldHead).data;
    }

    // EFFECT: removes the node closest to the tail from the list
    T removeFromTail() {
        if (this == this.prev) {
            throw new RuntimeException("Can't remove tail of empty list");
        }
        ANode<T> oldTail = this.prev;
        this.prev = this.prev.prev;
        this.prev.next = this;
        return ((Node<T>) oldTail).data;
    }

    // returns the first node to meet this condition, or itself
    ANode<T> findNode(IPred<T> pred) {
        return this.next.find(pred);
    }

    // EFFECT: removes arg node from list, or nothing
    void removeNode(ANode<T> arg) {
        this.next.nodeRemover(arg);
    }

    // debugging
    String print() {
        return "Sentinel".concat(this.next.printer());
    }

}

class Node<T> extends ANode<T> {
    T data;

    Node(T content, ANode<T> next, ANode<T> prev) {
        super(next, prev);
        this.data = content;
    }

    Node(T content) {
        super(null, null);
    }

    // returns size
    int countSize() {
        return 1 + next.countSize();
    }

    // finds the node
    ANode<T> find(IPred<T> pred) {
        if (pred.apply(data)) {
            return this;
        }

        else {
            return this.next.find(pred);
        }
    }

    // removes the node
    void nodeRemover(ANode<T> arg) {
        if (this.equals(arg)) {
            this.prev.next = this.next;
            this.next.prev = this.prev;
        }

        else {
            this.next.nodeRemover(arg);
        }
    }

    // prints the node
    String printer() {
        return data.toString().concat(this.next.printer());
    }

}

// maze code
// to represent a graph vertex
class Vertex {
    // this vertex's original representative
    int itself;
    // x coordinate
    int x;
    // y coordinate
    int y;
    // allows the game to be played at different sizes
    public static final int CELL_SIZE = 10;

    Vertex(int itself, int x, int y) {
        this.itself = itself;
        this.x = x;
        this.y = y;
    }

    // checks if this vertex is smaller than the int passed in
    boolean smallerThan(int other) {
        return this.itself < other;
    }

    // checks if there should be a wall here
    boolean wallCheck(int x, int y, int other) {
        return (this.x == x) && (this.y == y) && (this.itself > other);
    }

    // checks if there is a connection between the vertices
    boolean connection(int other, int width) {
        return (this.itself + width == other) || (this.itself - width == other);
    }

    // checks if there should be a horizontal wall
    boolean noRightWall(int x, int y, int width, Vertex other) {
        return (((this.x == x) && (this.y == y) && other.smallerThan(this.itself))
                || other.wallCheck(x, y, this.itself)) && other.connection(this.itself, width);
    }

    // checks if there should be a vertical wall
    boolean noDownWall(int x, int y, Vertex other) {
        return (((this.x == x) && (this.y == y) && other.smallerThan(this.itself))
                || other.wallCheck(x, y, this.itself)) && other.connection(this.itself, 1);
    }
}

// to represent an edge in the graph
class Edge implements Comparable<Edge> {
    // the weight of this edge
    double weight;
    // the first vertex in the edge
    Vertex vertex1;
    // the second vertex in the edge
    Vertex vertex2;

    Edge(Vertex vertex1, Vertex vertex2) {
        this.weight = Math.random();
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }

    // returns the weight of this vertex
    double getWeight() {
        return this.weight;
    }

    // checks if there should not be a horizontal wall
    boolean noRightWall(int x, int y, int width) {
        return this.vertex1.noRightWall(x, y, width, this.vertex2);
    }

    // checks if there should not be a vertical wall
    boolean noDownWall(int x, int y) {
        return this.vertex1.noDownWall(x, y, this.vertex2);
    }

    // used to sort the arrayList by edge weights
    public int compareTo(Edge other) {
        if (this.getWeight() < other.getWeight()) {
            return -1;
        } 
        
        else if (this.getWeight() > other.getWeight()) {
            return 1;
        } 
        
        else {
            return 0;
        }
    }

    // checks if they are the same vertices
    boolean sameVerticies(Vertex that1, Vertex that2) {
        return (this.vertex1.equals(that1) && this.vertex2.equals(that2));
    }
}

// to represent a maze
class Maze extends World {
    // current position of player 1
    int playerx = 0;
    int playery = 0;
    // current position of player 2
    int player2x = this.width - 1;
    int player2y = 0;
    // the hashmap for the representatives of the vertices
    HashMap<Integer, Integer> representatives = new HashMap<Integer, Integer>();
    // ArrayList holding all vertecies
    ArrayList<Vertex> allVertex = new ArrayList<Vertex>();
    // layout of maze by Vertex
    ArrayList<ArrayList<Vertex>> vertGrid = new ArrayList<ArrayList<Vertex>>();
    // the edges in the MST
    ArrayList<Edge> edgesInTree;
    // the edges that still need to be checked for their placement in the tree
    ArrayList<Edge> workList;
    // the length of the maze
    int length;
    // the width of the maze
    int width;
    // searching booleans
    boolean depthActivated = false;
    // trail for depth
    ArrayList<Vertex> depthTrail = new ArrayList<Vertex>();
    // counter for depth
    int depthCounter = 0;
    // searching for breadth
    boolean breadthActivated = false;
    // trail for breadth
    ArrayList<Vertex> breadthTrail = new ArrayList<Vertex>();
    // counter for breadth
    int breadthCounter = 0;
    // show solution or not
    boolean showSolution = false;
    // solution trail
    ArrayList<Vertex> solution = new ArrayList<Vertex>();
    // player 1 trail
    ArrayList<Posn> trail = new ArrayList<Posn>();
    // player 2 trail
    ArrayList<Posn> trail2 = new ArrayList<Posn>();
    // show path toggle
    boolean showPath = true;
    // player 2 present
    boolean player2 = false;

    Maze(int length, int width) {
        this.length = length;
        this.width = width;
        this.vertGrid = this.makeVertices();
        this.initWorkList();
        this.makeTree();
    }

    // Constructor only used to test methods inside real constructor
    Maze(int length, int width, String forTest) {
        this.length = length;
        this.width = width;
        this.edgesInTree = new ArrayList<Edge>();
        this.workList = new ArrayList<Edge>();
    }

    // EFFECT: creates a new Maze
    void mazeGen(int length, int width, boolean player2) {
        this.length = length;
        this.width = width;
        this.playerx = 0;
        this.playery = 0;
        this.player2x = this.width - 1;
        this.player2y = 0;
        this.trail = new ArrayList<Posn>();
        this.trail2 = new ArrayList<Posn>();
        this.player2 = player2;
        this.depthActivated = false;
        this.depthTrail = new ArrayList<Vertex>();
        this.depthCounter = 0;
        this.breadthActivated = false;
        this.breadthTrail = new ArrayList<Vertex>();
        this.breadthCounter = 0;
        this.showSolution = false;
        this.solution = new ArrayList<Vertex>();
        this.representatives = new HashMap<Integer, Integer>();
        this.allVertex = new ArrayList<Vertex>();
        this.vertGrid = new ArrayList<ArrayList<Vertex>>();
        this.vertGrid = this.makeVertices();
        this.initWorkList();
        this.makeTree();

    }

    // makes all the vertices in the graph
    //
    // EFFECT: assigns a list of all Verticies present in Graph to AllVertex
    ArrayList<ArrayList<Vertex>> makeVertices() {
        ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
        int itself = 0;
        for (int y = 0; y < this.length; y += 1) {
            ArrayList<Vertex> temp = new ArrayList<Vertex>();
            for (int x = 0; x < this.width; x += 1) {
                temp.add(new Vertex(itself, x, y));
                this.representatives.put(itself, itself);
                itself += 1;
            }
            vertices.add(temp);
            // only added code
            allVertex.addAll(temp);
        }
        return vertices;
    }

    // EFFECT: makes the list of edges in the graph
    void initWorkList() {
        this.workList = new ArrayList<Edge>();

        for (int y = 0; y < this.length; y += 1) {
            for (int x = 0; x < this.width; x += 1) {
                if (x > 0) {
                    this.workList.add(new Edge(this.vertGrid.get(y).get(x - 1), 
                            this.vertGrid.get(y).get(x)));
                }

                if (x < this.width - 1) {
                    this.workList.add(new Edge(this.vertGrid.get(y).get(x + 1), 
                            this.vertGrid.get(y).get(x)));
                }

                if (y > 0) {
                    this.workList.add(new Edge(this.vertGrid.get(y - 1).get(x), 
                            this.vertGrid.get(y).get(x)));
                }

                if (y < this.length - 1) {
                    this.workList.add(new Edge(this.vertGrid.get(y + 1).get(x), 
                            this.vertGrid.get(y).get(x)));
                }
            }
        }
        Collections.sort(this.workList);
    }

    // EFFECT: makes the spanning tree
    void makeTree() {
        this.edgesInTree = new ArrayList<Edge>();

        while (this.workList.size() > 0) {
            Edge temp = this.workList.get(0);
            this.workList.remove(0);

            if (this.find(temp.vertex1) != (this.find(temp.vertex2))) {
                this.edgesInTree.add(temp);
                this.union(temp.vertex1, temp.vertex2);
            }
        }
    }

    // EFFECT: sets the first representative to the second's representative
    void union(Vertex v1, Vertex v2) {
        this.representatives.put(this.find(v1), this.find(v2));
    }

    // returns v's representative
    int find(Vertex v) {
        int rep = v.itself;
        boolean moreReps = true;
        while (moreReps) {
            rep = this.representatives.get(rep);
            moreReps = this.representatives.get(rep) != rep;
        }
        return rep;
    }

    // returns v's father
    int findParent(Vertex v) {
        int rep = v.itself;
        return this.representatives.get(rep);
    }

    // checks if a horizontal wall should be added
    boolean addRightWall(int x, int y) {
        boolean add = true;

        for (Edge edge : this.edgesInTree) {
            if (edge.noRightWall(x, y, this.width)) {
                add = false;
            }
        }

        return add;
    }

    // checks if a vertical wall should be added
    boolean addDownWall(int x, int y) {
        boolean add = true;

        for (Edge edge : this.edgesInTree) {
            if (edge.noDownWall(x, y)) {
                add = false;
            }
        }

        return add;
    }

    // returns the arrayList of a depth first search on the graph
    ArrayList<Vertex> depthFirst() {
        HashMap<Vertex, Edge> edges = new HashMap<Vertex, Edge>();
        // used as a Stack (me thinks)
        Deque<Vertex> worklist = new Deque<Vertex>();
        // using a deque because we already built it but can be used
        // as both a stack AND a queue.
        ArrayList<Vertex> visited = new ArrayList<Vertex>();
        // adds starting node to worklist
        worklist.addAtHead(allVertex.get(0));
        // sets target to final Vertex in ArrayList of created Vertex
        Vertex target = allVertex.get(allVertex.size() - 1);
        // System.out.println("while loop begun");
        while (worklist.size() > 0) {
            Vertex next = worklist.removeFromHead();
            if (visited.contains(next)) {
                // System.out.println("Vertex was seen before");
            } 
            
            else if (next.equals(target)) {
                visited.add(next);
                // System.out.println("end was reached");
                // sets workList size to 0 to break loop
                worklist = new Deque<Vertex>();
            } 
            
            else {
                visited.add(next);

                for (Edge e : this.edgesInTree) {
                    if (e.vertex1.equals(next)) {
                        if (!visited.contains(e.vertex2)) {
                            worklist.addAtHead(e.vertex2);
                            edges.put(e.vertex2, new Edge(next, e.vertex2));
                        }
                    }

                    else if (e.vertex2.equals(next)) {
                        if (!visited.contains(e.vertex1)) {
                            worklist.addAtHead(e.vertex1);
                            edges.put(e.vertex1, new Edge(next, e.vertex1));
                        }
                    }

                }
            }
        }
        this.depthTrail = visited;
        return reconstruct(edges, target, new ArrayList<Vertex>());
    }

    // returns the arrayList of a breadth first search on the graph
    ArrayList<Vertex> breadthFirst() {
        HashMap<Vertex, Edge> edges = new HashMap<Vertex, Edge>();
        // used as a Queue (me thinks)
        Deque<Vertex> worklist = new Deque<Vertex>();
        // using a deque because we already built it but can be used
        // as both a stack AND a queue.
        ArrayList<Vertex> visited = new ArrayList<Vertex>();
        // adds starting node to worklist
        worklist.addAtHead(allVertex.get(0));
        // sets target to final Vertex in ArrayList of created Vertex
        Vertex target = allVertex.get(allVertex.size() - 1);
        // System.out.println("while loop begun");
        while (worklist.size() > 0) {
            Vertex next = worklist.removeFromTail();
            if (visited.contains(next)) {
                // System.out.println("Vertex was seen before");
            } 
            
            else if (next.equals(target)) {
                visited.add(next);
                // System.out.println("end was reached");
                // sets workList size to 0 to break loop
                worklist = new Deque<Vertex>();
            } 
            
            else {
                visited.add(next);

                for (Edge e : this.edgesInTree) {
                    if (e.vertex1.equals(next)) {
                        if (!visited.contains(e.vertex2)) {
                            worklist.addAtHead(e.vertex2);
                            edges.put(e.vertex2, new Edge(next, e.vertex2));
                        }
                    }

                    else if (e.vertex2.equals(next)) {
                        if (!visited.contains(e.vertex1)) {
                            worklist.addAtHead(e.vertex1);
                            edges.put(e.vertex1, new Edge(next, e.vertex1));
                        }
                    }

                }
            }
        }
        this.breadthTrail = visited;
        return reconstruct(edges, target, new ArrayList<Vertex>());
    }

    // returns the path from beginning to end
    ArrayList<Vertex> reconstruct(HashMap<Vertex, Edge> edges, 
            Vertex finish, ArrayList<Vertex> sol) {
        Vertex next = finish;
        if (!edges.containsKey(next)) {
            return sol;
        } 
        
        else {
            Vertex nameme = edges.get(next).vertex1;
            sol.add(next);
            edges.remove(next);
            return reconstruct(edges, nameme, sol);
        }
    }

    // draws the world
    public WorldScene makeScene() {
        WorldScene world = this.getEmptyScene();
        WorldImage player = new RectangleImage(Vertex.CELL_SIZE, Vertex.CELL_SIZE, 
                "solid", Color.BLUE);
        WorldImage start = new RectangleImage(Vertex.CELL_SIZE, Vertex.CELL_SIZE, 
                "solid", Color.GREEN);
        WorldImage end = new RectangleImage(Vertex.CELL_SIZE, Vertex.CELL_SIZE, 
                "solid", Color.MAGENTA);
        WorldImage rightLine = new LineImage(new Posn(Vertex.CELL_SIZE, 0), new Color(0, 0, 0));
        WorldImage downLine = new LineImage(new Posn(0, Vertex.CELL_SIZE), new Color(0, 0, 0));
        // searching specifics
        WorldImage vertexSearched = new RectangleImage(Vertex.CELL_SIZE, 
                Vertex.CELL_SIZE, "solid", Color.CYAN);
        WorldImage vertexSolution = new RectangleImage(Vertex.CELL_SIZE, 
                Vertex.CELL_SIZE, "solid", Color.RED);
        WorldImage oneWins = new TextImage("Player 1 wins!", Color.BLACK);
        WorldImage twoWins = new TextImage("Player 2 wins!", Color.BLACK);

        // draws trail
        if (this.showPath) {
            for (Posn spot : this.trail) {
                world.placeImageXY(vertexSearched, spot.x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                        spot.y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
            }

            if (this.player2) {
                for (Posn spot : this.trail2) {
                    world.placeImageXY(vertexSearched, 
                            spot.x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                            spot.y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
                }

                world.placeImageXY(end, Vertex.CELL_SIZE / 2, 
                        Vertex.CELL_SIZE * this.length - Vertex.CELL_SIZE / 2);

                world.placeImageXY(start, 
                        this.width * Vertex.CELL_SIZE - Vertex.CELL_SIZE / 2, Vertex.CELL_SIZE / 2);

                world.placeImageXY(player, this.player2x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                        this.player2y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);

            }
        }
        // draws trail
        // search maybe?
        // draws depth trail
        for (int i = depthCounter; i > 0; i -= 1) {
            world.placeImageXY(vertexSearched, 
                    depthTrail.get(i).x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                    depthTrail.get(i).y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
        }

        for (int i = breadthCounter; i > 0; i -= 1) {
            world.placeImageXY(vertexSearched, 
                    breadthTrail.get(i).x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                    breadthTrail.get(i).y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
        }

        if (showSolution) {
            for (Vertex v : solution) {
                world.placeImageXY(vertexSolution,
                        v.x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                        v.y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
            }
        }

        // start cell
        world.placeImageXY(start, Vertex.CELL_SIZE / 2, Vertex.CELL_SIZE / 2);

        // current player position
        world.placeImageXY(player, this.playerx * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                this.playery * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);

        // end cell
        world.placeImageXY(end, Vertex.CELL_SIZE * this.width - Vertex.CELL_SIZE / 2,
                Vertex.CELL_SIZE * this.length - Vertex.CELL_SIZE / 2);

        for (int y = 0; y < this.length; y += 1) {
            for (int x = 0; x < this.width; x += 1) {
                if (this.addRightWall(x, y)) {
                    world.placeImageXY(rightLine, 
                            x * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2, y * Vertex.CELL_SIZE);
                }

                if (this.addDownWall(x, y)) {
                    world.placeImageXY(downLine, 
                            x * Vertex.CELL_SIZE, y * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
                }
            }
        }
        
        if (this.player2) {
            if (this.playerx == this.width - 1 && this.playery == this.length - 1) {
                world = new WorldScene(this.width, this.length);
                world.placeImageXY(oneWins, 
                        this.width / 2 * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                        this.length / 2 * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
            } 
            
            else if (this.player2x == 0 && this.player2y == this.length - 1) {
                world = new WorldScene(this.width, this.length);
                world.placeImageXY(twoWins, 
                        this.width / 2 * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2,
                        this.length / 2 * Vertex.CELL_SIZE + Vertex.CELL_SIZE / 2);
            }
        }
        return world;
    }

    // EFFECT: updates the Maze and "Player" depending on key press
    public void onKeyEvent(String key) {
        if (key.equals("n")) {
            this.mazeGen(this.length, this.width, false);
        }
        // manual player movement
        if (key.equals("down")) {
            trail.add(new Posn(this.playerx, this.playery));
            if (this.playery + 1 < this.length 
                    && this.canWalk(playerx, playery, playerx, playery + 1)) {
                this.playery += 1;
            }
        }

        if (key.equals("up")) {
            trail.add(new Posn(this.playerx, this.playery));
            if (this.playery - 1 >= 0 
                    && this.canWalk(playerx, playery, playerx, playery - 1)) {
                this.playery -= 1;
            }
        }
        if (key.equals("right")) {
            trail.add(new Posn(this.playerx, this.playery));
            if (this.playerx + 1 < this.width 
                    && this.canWalk(playerx, playery, playerx + 1, playery)) {
                this.playerx += 1;
            }
        }
        if (key.equals("left")) {
            trail.add(new Posn(this.playerx, this.playery));
            if (this.playerx - 1 >= 0 
                    && this.canWalk(playerx, playery, playerx - 1, playery)) {
                this.playerx -= 1;
            }
        }
        if (key.equals("j")) {
            this.depthActivated = true;
            this.solution = this.depthFirst();

        }
        if (key.equals("k")) {
            this.breadthActivated = true;
            this.solution = this.breadthFirst();

        }
        if (key.equals("2")) {
            this.mazeGen(this.length, this.width, true);
        }
        if (key.equals("1")) {
            this.mazeGen(this.length, this.width, false);
        }
        if (this.player2) {
            if (key.equals("s")) {
                trail2.add(new Posn(this.player2x, this.player2y));
                if (this.player2y + 1 < this.length 
                        && this.canWalk(player2x, player2y, player2x, player2y + 1)) {
                    this.player2y += 1;
                }
            }

            if (key.equals("w")) {
                trail2.add(new Posn(this.player2x, this.player2y));
                if (this.player2y - 1 >= 0 
                        && this.canWalk(player2x, player2y, player2x, player2y - 1)) {
                    this.player2y -= 1;
                }
            }
            if (key.equals("d")) {
                trail2.add(new Posn(this.player2x, this.player2y));
                if (this.player2x + 1 < this.width 
                        && this.canWalk(player2x, player2y, player2x + 1, player2y)) {
                    this.player2x += 1;
                }
            }
            if (key.equals("a")) {
                trail2.add(new Posn(this.player2x, this.player2y));
                if (this.player2x - 1 >= 0 
                        && this.canWalk(player2x, player2y, player2x - 1, player2y)) {
                    this.player2x -= 1;
                }
            }
            if (key.equals("t")) {
                this.showPath = !this.showPath;
            }
        }
    }

    boolean canWalk(int x1, int y1, int x2, int y2) {
        boolean can = false;
        for (Edge edge : this.edgesInTree) {
            if (edge.sameVerticies(this.vertGrid.get(y1).get(x1), this.vertGrid.get(y2).get(x2))
                    || edge.sameVerticies(this.vertGrid.get(y2).get(x2), 
                            this.vertGrid.get(y1).get(x1))) {
                can = true;
            }
        }
        return can;
    }

    // EFFECT: updates maze every tick
    public void onTick() {
        if (playerx == this.width - 1 && playery == this.length - 1) {
            solution = depthFirst();
            showSolution = true;
        }
        if (depthCounter < depthTrail.size() - 1 && depthActivated) {
            depthCounter += 1;
        } 
        
        else if (depthActivated) {
            showSolution = true;
        }

        if (breadthCounter < breadthTrail.size() - 1 && breadthActivated) {
            breadthCounter += 1;
        } 
        
        else if (breadthActivated) {
            showSolution = true;
        }

    }
}

class ExampleMaze {
    // deque stuff
    Sentinel<String> s1 = new Sentinel<String>();
    Deque<String> deque1 = new Deque<String>(s1);
    Sentinel<String> s2 = new Sentinel<String>();
    Deque<String> deque2 = new Deque<String>(s2);
    Node<String> n21 = new Node<String>("abc", s2, s2);
    Node<String> n22 = new Node<String>("bcd", s2, n21);
    Node<String> n23 = new Node<String>("cde", s2, n22);
    Node<String> n24 = new Node<String>("def", s2, n23);
    Sentinel<String> s3 = new Sentinel<String>();
    Deque<String> deque3 = new Deque<String>(s3);
    Node<String> n31 = new Node<String>("zyx", s3, s3);
    Node<String> n32 = new Node<String>("yyz", s3, n31);
    Node<String> n33 = new Node<String>("aar", s3, n32);
    Node<String> n34 = new Node<String>("bet", s3, n33);
    Node<String> n35 = new Node<String>("sre", s3, n34);
    String result = s2.print();
    String otherResult = s2.print();
    // maze
    Maze m;
    Vertex v1 = new Vertex(10, 1, 1);
    Vertex v2 = new Vertex(20, 2, 2);
    Vertex v3 = new Vertex(11, 2, 2);
    Vertex v4 = new Vertex(12, 2, 2);
    //
    Vertex v11 = new Vertex(0, 1, 1);
    Vertex v21 = new Vertex(1, 2, 2);
    Vertex v31 = new Vertex(2, 2, 2);
    Vertex v41 = new Vertex(3, 2, 2);
    Edge e1 = new Edge(v1, v2);
    Edge e2 = new Edge(v1, v3);
    Edge e3 = new Edge(v1, v4);
    HashMap<Integer, Integer> reps = new HashMap<Integer, Integer>();

    void testGame(Tester t) {
        m = new Maze(30, 30);

        m.bigBang(m.width * Vertex.CELL_SIZE, m.length * Vertex.CELL_SIZE, .01);
    }

    void init() {
        m = new Maze(2, 2, "test");
        // deque init
        s1 = new Sentinel<String>();
        deque1 = new Deque<String>(s1);
        s2 = new Sentinel<String>();
        deque2 = new Deque<String>(s2);
        n21 = new Node<String>("abc", s2, s2);
        n22 = new Node<String>("bcd", s2, n21);
        n23 = new Node<String>("cde", s2, n22);
        n24 = new Node<String>("def", s2, n23);
        s3 = new Sentinel<String>();
        deque3 = new Deque<String>(s3);
        n31 = new Node<String>("zyx", s3, s3);
        n32 = new Node<String>("yyz", s3, n31);
        n33 = new Node<String>("aadr", s3, n32);
        n34 = new Node<String>("bet", s3, n33);
        n35 = new Node<String>("sre", s3, n34);
    }

    // deque testing
    void testConstructor(Tester t) {
        init();
        // claims class ANode<T> can't be found
        /*
         * t.checkConstructorException(new IllegalArgumentException(
         * "No Nulls please"), "ANode<T>",null, null);
         */
    }

    void testPrint(Tester t) {
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        t.checkExpect(deque3.print(), "Sentinelzyxyyzaadrbetsre");
        t.checkExpect(deque1.print(), "Sentinel");
    }

    void testSize(Tester t) {
        init();
        t.checkExpect(deque2.size(), 4);
        t.checkExpect(deque3.size(), 5);
        t.checkExpect(deque1.size(), 0);
    }

    void testAddAtHead(Tester t) {
        init();
        t.checkExpect(deque2.size(), 4);
        deque2.addAtHead("yyz");
        t.checkExpect(deque2.size(), 5);
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        deque2.addAtHead("zzz");
        t.checkExpect(deque2.print(), "Sentinelzzzabcbcdcdedef");
    }

    void testAddAtTail(Tester t) {
        init();
        t.checkExpect(deque2.size(), 4);
        deque2.addAtTail("yyz");
        t.checkExpect(deque2.size(), 5);
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        deque2.addAtTail("zzz");
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedefzzz");
    }

    void testRemoveFromHead(Tester t) {
        init();
        t.checkExpect(deque2.size(), 4);
        deque2.removeFromHead();
        t.checkExpect(deque2.size(), 3);
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        deque2.removeFromHead();
        t.checkExpect(deque2.print(), "Sentinelbcdcdedef");
        t.checkException(new RuntimeException("Can't remove head of empty list"), 
                deque1, "removeFromHead");

    }

    void testRemoveFromTail(Tester t) {
        init();
        t.checkExpect(deque2.size(), 4);
        deque2.removeFromTail();
        t.checkExpect(deque2.size(), 3);
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        deque2.removeFromTail();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcde");
        t.checkException(new RuntimeException("Can't remove tail of empty list"), 
                deque1, "removeFromTail");
    }

    void testFind(Tester t) {
        init();
        t.checkExpect(deque3.find(new StringLenBiggerThanThree()), n33);
        t.checkExpect(deque2.find(new StringLenBiggerThanThree()), s2);
        t.checkExpect(deque3.find(new StringLenThree()), n31);
    }

    void testRemoveNode(Tester t) {
        init();
        deque2.removeNode(n22);
        t.checkExpect(deque2.print(), "Sentinelabccdedef");
        init();
        t.checkExpect(deque2.print(), "Sentinelabcbcdcdedef");
        deque2.removeNode(n23);
        t.checkExpect(deque2.print(), "Sentinelabcbcddef");
        init();
        t.checkExpect(deque1.print(), "Sentinel");
        deque1.removeNode(n23);
        t.checkExpect(deque1.print(), "Sentinel");
    }

    // maze testing
    void testSmallerThan(Tester t) {
        t.checkExpect(v1.smallerThan(5), false);
        t.checkExpect(v1.smallerThan(12), true);
        t.checkExpect(v2.smallerThan(5), false);
        t.checkExpect(v1.smallerThan(50), true);
    }

    void testWallCheck(Tester t) {
        t.checkExpect(v1.wallCheck(1, 1, 1), true);
        t.checkExpect(v1.wallCheck(2, 1, 11), false);
        t.checkExpect(v1.wallCheck(1, 1, 11), false);
        t.checkExpect(v2.wallCheck(2, 2, 1), true);
        t.checkExpect(v2.wallCheck(1, 1, 11), false);
        t.checkExpect(v2.wallCheck(2, 2, 20), false);
    }

    void testConnection(Tester t) {
        t.checkExpect(v1.connection(9, 1), true);
        t.checkExpect(v1.connection(5, 5), true);
        t.checkExpect(v1.connection(9, 2), false);
        t.checkExpect(v1.connection(5, 6), false);
        t.checkExpect(v2.connection(19, 1), true);
        t.checkExpect(v2.connection(15, 5), true);
        t.checkExpect(v2.connection(19, 2), false);
        t.checkExpect(v2.connection(15, 6), false);
    }

    void testNoRightWall(Tester t) {
        t.checkExpect(v1.noRightWall(1, 1, 10, this.v2), false);
        t.checkExpect(v2.noRightWall(2, 2, 10, this.v1), true);
        t.checkExpect(v1.noRightWall(2, 2, 10, this.v2), true);
        t.checkExpect(v2.noRightWall(3, 3, 10, this.v1), false);
    }

    void testNoDownWall(Tester t) {
        t.checkExpect(v1.noDownWall(2, 2, this.v3), true);
        t.checkExpect(v1.noDownWall(2, 2, this.v2), false);
        t.checkExpect(v1.noDownWall(1, 1, this.v2), false);
        t.checkExpect(v3.noDownWall(1, 1, this.v2), false);
    }

    void testGetWeight(Tester t) {
        t.checkNumRange(e1.getWeight(), 0, 1);
        t.checkNumRange(e2.getWeight(), 0, 1);
    }

    void testNoRightWallEdge(Tester t) {
        t.checkExpect(e1.noRightWall(2, 2, 10), true);
        t.checkExpect(e1.noRightWall(1, 1, 10), false);
        t.checkExpect(e1.noRightWall(2, 3, 10), false);
        t.checkExpect(e2.noRightWall(2, 2, 1), true);
        t.checkExpect(e2.noRightWall(2, 2, 10), false);
        t.checkExpect(e2.noRightWall(3, 2, 10), false);
    }

    void testNoDownWallEdge(Tester t) {
        t.checkExpect(e1.noDownWall(1, 1), false);
        t.checkExpect(e1.noDownWall(2, 2), false);
        t.checkExpect(e2.noDownWall(1, 1), false);
        t.checkExpect(e2.noDownWall(2, 2), true);
        t.checkExpect(e2.noDownWall(3, 3), false);
    }

    void testCompareTo(Tester t) {
        t.checkNumRange(e1.compareTo(e2), -1, 2);
        t.checkNumRange(e2.compareTo(e1), -1, 2);
        t.checkNumRange(e1.compareTo(e1), -1, 2);
    }

    /*
     * void testTree(Tester t) { this.init();
     * t.checkExpect(m.edgesInTree.size(), 0); t.checkExpect(m.workList.size(),
     * 0); t.checkExpect(m.workList.size(), 8);
     * t.checkExpect(m.edgesInTree.size(), 0); m.makeTree();
     * t.checkExpect(m.workList.size(), 0);
     * //t.checkExpect(m.edgesInTree.size(), 3); }
     */

    void testUnionFind(Tester t) {
        this.init();
        m.representatives.put(10, 10);
        m.representatives.put(20, 20);
        m.representatives.put(11, 11);
        t.checkExpect(m.find(v1), 10);
        t.checkExpect(m.find(v2), 20);
        t.checkExpect(m.find(v3), 11);
        m.union(v1, v2);
        t.checkExpect(m.find(v1), 20);
        t.checkExpect(m.find(v2), 20);
        t.checkExpect(m.find(v3), 11);
        m.union(v2, v3);
        t.checkExpect(m.find(v1), 11);
        t.checkExpect(m.find(v2), 11);
        t.checkExpect(m.find(v3), 11);
    }

    void testAddRightWall(Tester t) {
        this.init();
        t.checkExpect(m.addRightWall(1, 1), true);
        t.checkExpect(m.addRightWall(2, 2), true);
        m.edgesInTree.add(e3);
        t.checkExpect(m.addRightWall(1, 1), true);
        t.checkExpect(m.addRightWall(2, 2), false);
    }

    void testAddDownWall(Tester t) {
        this.init();
        t.checkExpect(m.addDownWall(1, 1), true);
        t.checkExpect(m.addDownWall(2, 2), true);
        m.edgesInTree.add(e2);
        t.checkExpect(m.addDownWall(1, 1), true);
        t.checkExpect(m.addDownWall(2, 2), false);
    }

    // beginning of new tests
    void testSameVerticies(Tester t) {
        init();
        Edge tempEdge1 = new Edge(v1, v2);
        Edge tempEdge12 = new Edge(v1, v2);
        Edge tempEdge2 = new Edge(v2, v1);
        t.checkExpect(tempEdge1.sameVerticies(v1, v2), true);
        t.checkExpect(tempEdge12.sameVerticies(v1, v2), true);
        t.checkExpect(tempEdge2.sameVerticies(v1, v2), false);

    }

    void testUncontrolledSearches(Tester t) {
        // we'll simply test the searches against each other since each should
        // get to
        // the same answer
        // reconstruct requires too many perfect variables to test separately
        // from the search
        // engines themselves so we will test it by testing the searches against
        // each other
        init();
        m = new Maze(40, 40);
        t.checkExpect(m.depthFirst(), m.breadthFirst());
        m = new Maze(30, 30);
        t.checkExpect(m.depthFirst(), m.breadthFirst());
        m = new Maze(10, 20);
        t.checkExpect(m.depthFirst(), m.breadthFirst());
        m = new Maze(20, 10);
        t.checkExpect(m.depthFirst(), m.breadthFirst());

    }
}