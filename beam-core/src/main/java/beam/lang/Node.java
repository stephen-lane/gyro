package beam.lang;

import beam.core.BeamCore;

public abstract class Node {

    private transient Node parentNode;
    private transient int line;
    private transient int column;
    private BeamCore core;
    private BeamFile fileNode;

    public abstract boolean resolve();

    public Node parentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public BeamCore core() {
        return core;
    }

    public void setCore(BeamCore core) {
        this.core = core;
    }

    public BeamFile fileNode() {
        if (fileNode == null) {
            Node parent = parentNode();

            while (parent != null && !(parent instanceof BeamFile)) {
                parent = parent.parentNode();
            }

            if (parent instanceof BeamFile) {
                fileNode = (BeamFile) parent;
            }
        }

        return fileNode;
    }

}
