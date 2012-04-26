package gis_project;

public class Node {

    private DataPoint dp;
    private Node leftChild;
    private Node rightChild;
    private Node parent;
    
    public Node(DataPoint dp) {
        this.dp = dp;
    }
    
    public DataPoint getDp() {
        return dp;
    }
    
    public Node getLeftChild() {
        return leftChild;
    }
    
    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }
    
    public Node getRightChild() {
        return rightChild;
    }
    
    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }
}