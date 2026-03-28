import java.util.ArrayList;
import java.util.List;

public class Voxelizer {
    public enum StatusCube {
        OUTSIDE,
        INTERSECTING,
    }

    public static List<Voxel> voxelize(Mesh mesh, OctreeNode root, int maxDepth, Statistics stats){
        List<Voxel> voxels = new ArrayList<>();
        voxelizeRecursive(mesh, root, maxDepth, stats, voxels);
        return voxels;
    }

    public static void voxelizeRecursive(Mesh mesh, OctreeNode node, int maxDepth, Statistics stats, List<Voxel> voxels){
        stats.incrementNode(node.depth);

        StatusCube status = klasifikasiCube(mesh, node.bounds);

        switch(status){
            case OUTSIDE:
                stats.incrementPruned(node.depth);
                return;

            case INTERSECTING:
                if (node.depth == maxDepth) {
                    voxels.add(new Voxel(node.bounds));
                    return;
                }

                node.subdivide();
                for (OctreeNode child : node.children) {
                    voxelizeRecursive(mesh, child, maxDepth, stats, voxels);
                }
                break;
        }
    }

    public static StatusCube klasifikasiCube(Mesh mesh, Cube cube){
        for (Face face : mesh.getFaces()){
            Vertex v0 = mesh.getVertices().get(face.a);
            Vertex v1 = mesh.getVertices().get(face.b);
            Vertex v2 = mesh.getVertices().get(face.c);

            if (triangleIntersectsCube(v0, v1, v2, cube)){
                return StatusCube.INTERSECTING;
            }
        }

        return StatusCube.OUTSIDE;
    }

    public static boolean triangleIntersectsCube(Vertex a, Vertex b, Vertex c, Cube cube){
        Vertex triangleMin = new Vertex(
            Math.min(a.x, Math.min(b.x, c.x)),
            Math.min(a.y, Math.min(b.y, c.y)),
            Math.min(a.z, Math.min(b.z, c.z))
        );

        Vertex triangleMax = new Vertex(
            Math.max(a.x, Math.max(b.x, c.x)),
            Math.max(a.y, Math.max(b.y, c.y)),
            Math.max(a.z, Math.max(b.z, c.z))
        );

        Vertex cubeMax = cube.getMax();

        return aabbOverlap(triangleMin, triangleMax, cube.min, cubeMax);

    }

    public static boolean aabbOverlap(Vertex min1, Vertex max1, Vertex min2, Vertex max2){
        return min1.x <= max2.x && max1.x >= min2.x &&
               min1.y <= max2.y && max1.y >= min2.y &&
               min1.z <= max2.z && max1.z >= min2.z;
    }
}
