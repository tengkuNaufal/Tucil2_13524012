import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Exporter {
    public static String buildOutputPath(String inputPath) {
        int dotIndex = inputPath.lastIndexOf('.');
        if (dotIndex == -1) {
            return inputPath + "_voxelized.obj";
        }
        return inputPath.substring(0, dotIndex) + "_voxelized.obj";
    }

    public static void exportVoxelOBJ(String path, List<Voxel> voxels) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            int vertexOffset = 1;

            for (Voxel voxel : voxels) {
                List<Vertex> verts = cubeVertices(voxel.bounds);

                for (Vertex v : verts) {
                    bw.write(String.format("v %f %f %f%n", v.x, v.y, v.z));
                }

                int[][] faces = {
                    {0, 1, 2}, {0, 2, 3},
                    {4, 5, 6}, {4, 6, 7},
                    {0, 1, 5}, {0, 5, 4},
                    {2, 3, 7}, {2, 7, 6},
                    {0, 3, 7}, {0, 7, 4},
                    {1, 2, 6}, {1, 6, 5}
                };

                for (int[] f : faces) {
                    bw.write(String.format(
                        "f %d %d %d%n",
                        vertexOffset + f[0],
                        vertexOffset + f[1],
                        vertexOffset + f[2]
                    ));
                }

                vertexOffset += 8;
            }
        }
    }

    public static List<Vertex> cubeVertices(Cube c) {
        double x = c.min.x;
        double y = c.min.y;
        double z = c.min.z;
        double s = c.size;

        return List.of(
            new Vertex(x, y, z),
            new Vertex(x + s, y, z),
            new Vertex(x + s, y + s, z),
            new Vertex(x, y + s, z),
            new Vertex(x, y, z + s),
            new Vertex(x + s, y, z + s),
            new Vertex(x + s, y + s, z + s),
            new Vertex(x, y + s, z + s)
        );
    }
}