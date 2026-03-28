import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Gunakan: java Main <input.obj> <kedalaman_maksimum>");
            return;
        }

        String inputPath = args[0];
        int maxDepth;

        try {
            maxDepth = Integer.parseInt(args[1]);
            if (maxDepth < 0) {
                System.out.println("Error: kedalaman_maksimum harus >= 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: kedalaman_maksimum harus berupa integer");
            return;
        }

        Instant start = Instant.now();

        try {
            Mesh mesh = Parser.parseOBJ(inputPath);
            Parser.validateMesh(mesh);

            Cube rootCube = OctreeNode.createBoundingCube(mesh.getVertices());
            OctreeNode root = new OctreeNode(rootCube, 0);

            Statistics stats = new Statistics();
            List<Voxel> voxels = Voxelizer.voxelize(mesh, root, maxDepth, stats);

            String outputPath = Exporter.buildOutputPath(inputPath);
            Exporter.exportVoxelOBJ(outputPath, voxels);

            Duration elapsed = Duration.between(start, Instant.now());

            System.out.println("[HASIL VOXELISASI]");
            System.out.println("Input file              : " + inputPath);
            System.out.println("Output file             : " + outputPath);
            System.out.println("Kedalaman Maksimum      : " + maxDepth);
            System.out.println("Jumlah Voxel            : " + voxels.size());
            System.out.println("Jumlah vertex terbentuk : " + (voxels.size() * 8));
            System.out.println("Jumlah face terbentuk   : " + (voxels.size() * 12));
            System.out.println("Kedalaman Octree        : " + stats.getMaxObservedDepth());
            System.out.println("Lama waktu eksekusi     : " + elapsed.toMillis() + " ms");

            System.out.println("\nJumlah node tiap kedalaman:");
            for (Map.Entry<Integer, Integer> entry : stats.getNodesPerDepth().entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

            System.out.println("\nJumlah node yang tidak diakses tiap kedalaman:");
            for (Map.Entry<Integer, Integer> entry : stats.getPrunedPerDepth().entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}