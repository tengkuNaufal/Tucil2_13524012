import java.util.List;

public class OctreeNode {
    public Cube bounds;
    public int depth;
    public OctreeNode[] children;
    public boolean isLeaf;

    public OctreeNode(Cube bounds, int depth){
        this.bounds = bounds;
        this.depth = depth;
        this.children = new OctreeNode[8];
        this.isLeaf = true;
    }

    public void subdivide(){
        if (!isLeaf) return;
        double half = bounds.size/2.0;
        Vertex base = bounds.min;

        Vertex[] offset = new Vertex[] {
            new Vertex(0,0,0),
            new Vertex(half, 0, 0),
            new Vertex(0, half, 0),
            new Vertex(half, half, 0),
            new Vertex(0, 0, half),
            new Vertex(half, 0, half),
            new Vertex(0, half, half),
            new Vertex(half, half, half)
        };

        for (int i =0; i< 8; i++){
            Vertex childMin = new Vertex(base.x + offset[i].x, base.y + offset[i].y, base.z + offset[i].z);

            children[i] = new OctreeNode(new Cube(childMin, half), depth + 1);
        }

        isLeaf = false;
    }

    public static Cube createBoundingCube(List<Vertex> vertices){
        Vertex first = vertices.get(0);

        double minX = first.x, minY = first.y, minZ = first.z;
        double maxX = first.x, maxY = first.y, maxZ = first.z;

        for (Vertex v : vertices){
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);

            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;
        double size = Math.max(sizeX, Math.max(sizeY, sizeZ));

        return new Cube(new Vertex(minX, minY, minZ), size);
    }
}
