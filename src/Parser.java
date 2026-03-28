import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static class ParseResult {
        public final Mesh mesh;
        public final List<String> warnings;
        public final int skippedLines;
        
        public ParseResult(Mesh mesh, List<String> warnings, int skippedLines) {
            this.mesh = mesh;
            this.warnings = warnings;
            this.skippedLines = skippedLines;
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public void printWarnings() {
            if (!warnings.isEmpty()) {
                System.err.println("\n[Parser Warnings] " + warnings.size() + " warning(s):");
                for (String w : warnings) {
                    System.err.println("  - " + w);
                }
                if (skippedLines > 0) {
                    System.err.println("  Total skipped lines: " + skippedLines);
                }
                System.err.println();
            }
        }
    }
    
    /**
     * Parse file OBJ dengan penanganan error yang lebih robust.
     * Mendukung:
     * - Face dengan format v/vt/vn (texture coords & normals diabaikan)
     * - Quad faces (otomatis di-triangulasi)
     * - N-gon faces (otomatis di-triangulasi dengan fan triangulation)
     * - Skip baris yang tidak dikenal dengan warning (bukan error)
     * - Handling NumberFormatException dengan pesan yang jelas
     * - Negative vertex indices (relative indexing)
     */
    public static Mesh parseOBJ(String path) throws IOException {
        ParseResult result = parseOBJWithWarnings(path);
        result.printWarnings();
        return result.mesh;
    }
    
    /**
     * Parse OBJ dengan mengembalikan warnings secara terpisah.
     */
    public static ParseResult parseOBJWithWarnings(String path) throws IOException {
        // Validasi file sebelum parsing
        validateFile(path);
        
        Mesh mesh = new Mesh();
        List<String> warnings = new ArrayList<>();
        int skippedLines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines dan komentar
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;

                try {
                    switch (parts[0].toLowerCase()) {
                        case "v":
                            parseVertex(parts, mesh, lineNumber);
                            break;

                        case "f":
                            parseFace(parts, mesh, lineNumber, warnings);
                            break;

                        // Command OBJ yang dikenal tapi tidak digunakan - skip dengan warning ringan
                        case "vt": // texture coordinates
                        case "vn": // vertex normals
                        case "vp": // parameter space vertices
                        case "g":  // group
                        case "o":  // object name
                        case "s":  // smooth shading
                        case "mtllib": // material library
                        case "usemtl": // use material
                        case "l":  // line element
                            // Skip secara diam-diam, ini normal dalam file OBJ
                            break;

                        default:
                            // Command tidak dikenal - catat warning tapi lanjutkan
                            warnings.add("Line " + lineNumber + ": Unknown command '" + parts[0] + "' (skipped)");
                            skippedLines++;
                            break;
                    }
                } catch (OBJParseException e) {
                    // Error spesifik parsing - bisa fatal atau warning
                    if (e.isFatal()) {
                        throw new IllegalArgumentException(e.getMessage());
                    } else {
                        warnings.add(e.getMessage());
                        skippedLines++;
                    }
                }
            }
        }

        return new ParseResult(mesh, warnings, skippedLines);
    }
    
    /**
     * Validasi file sebelum parsing.
     */
    private static void validateFile(String path) throws IOException {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }
        
        if (!file.isFile()) {
            throw new IOException("Path is not a file: " + path);
        }
        
        if (!file.canRead()) {
            throw new IOException("Cannot read file (permission denied): " + path);
        }
        
        if (file.length() == 0) {
            throw new IOException("File is empty: " + path);
        }
        
        // Validasi ekstensi file
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".obj")) {
            System.err.println("[Warning] File does not have .obj extension: " + path);
        }
    }
    
    /**
     * Parse vertex line: v x y z [w]
     */
    private static void parseVertex(String[] parts, Mesh mesh, int lineNumber) throws OBJParseException {
        // Minimal 4 parts (v x y z), maksimal 5 (v x y z w)
        if (parts.length < 4) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid vertex - expected at least 3 coordinates (v x y z), got " + (parts.length - 1),
                true
            );
        }
        
        try {
            double x = parseDouble(parts[1], lineNumber, "x coordinate");
            double y = parseDouble(parts[2], lineNumber, "y coordinate");
            double z = parseDouble(parts[3], lineNumber, "z coordinate");
            
            // Validasi nilai koordinat
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new OBJParseException(
                    "Line " + lineNumber + ": Vertex contains invalid value (NaN or Infinity)",
                    true
                );
            }
            
            mesh.addVertex(new Vertex(x, y, z));
            
        } catch (NumberFormatException e) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid vertex coordinate - " + e.getMessage(),
                true
            );
        }
    }
    
    /**
     * Parse face line dengan support untuk berbagai format:
     * - f v1 v2 v3 (basic triangle)
     * - f v1 v2 v3 v4 (quad - akan di-triangulasi)
     * - f v1 v2 v3 ... vn (n-gon - akan di-triangulasi)
     * - f v1/vt1 v2/vt2 v3/vt3 (with texture coords)
     * - f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 (with texture and normals)
     * - f v1//vn1 v2//vn2 v3//vn3 (with normals only)
     */
    private static void parseFace(String[] parts, Mesh mesh, int lineNumber, List<String> warnings) 
            throws OBJParseException {
        
        if (parts.length < 4) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid face - need at least 3 vertices, got " + (parts.length - 1),
                true
            );
        }
        
        int vertexCount = mesh.getVertices().size();
        int[] indices = new int[parts.length - 1];
        
        // Parse semua vertex indices
        for (int i = 1; i < parts.length; i++) {
            try {
                indices[i - 1] = parseFaceIndex(parts[i], vertexCount, lineNumber);
            } catch (NumberFormatException e) {
                throw new OBJParseException(
                    "Line " + lineNumber + ": Invalid face vertex index '" + parts[i] + "' - " + e.getMessage(),
                    true
                );
            }
        }
        
        // Triangulasi face
        if (indices.length == 3) {
            // Triangle - langsung tambahkan
            mesh.addFace(new Face(indices[0], indices[1], indices[2]));
        } else if (indices.length == 4) {
            // Quad - split menjadi 2 triangles
            mesh.addFace(new Face(indices[0], indices[1], indices[2]));
            mesh.addFace(new Face(indices[0], indices[2], indices[3]));
        } else {
            // N-gon - fan triangulation dari vertex pertama
            warnings.add("Line " + lineNumber + ": N-gon with " + indices.length + " vertices (triangulated)");
            for (int i = 1; i < indices.length - 1; i++) {
                mesh.addFace(new Face(indices[0], indices[i], indices[i + 1]));
            }
        }
    }
    
    /**
     * Parse face vertex index dengan support untuk format v/vt/vn.
     * Mendukung negative indices (relative indexing).
     */
    private static int parseFaceIndex(String token, int currentVertexCount, int lineNumber) 
            throws OBJParseException {
        
        // Handle format v/vt/vn - ambil hanya vertex index (bagian pertama)
        String vertexPart = token;
        if (token.contains("/")) {
            String[] parts = token.split("/");
            vertexPart = parts[0];
        }
        
        if (vertexPart.isEmpty()) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Empty vertex index in face",
                true
            );
        }
        
        int idx;
        try {
            idx = Integer.parseInt(vertexPart);
        } catch (NumberFormatException e) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid vertex index '" + vertexPart + "' - not a number",
                true
            );
        }
        
        // Handle negative indices (relative indexing dari akhir)
        if (idx < 0) {
            idx = currentVertexCount + idx + 1;
        }
        
        // Validasi index (OBJ uses 1-based indexing)
        if (idx <= 0) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid vertex index " + idx + " (must be >= 1 or valid negative index)",
                true
            );
        }
        
        // Convert ke 0-based index
        return idx - 1;
    }
    
    /**
     * Parse double dengan error message yang informatif.
     */
    private static double parseDouble(String value, int lineNumber, String fieldName) throws OBJParseException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new OBJParseException(
                "Line " + lineNumber + ": Invalid " + fieldName + " '" + value + "' - not a valid number",
                true
            );
        }
    }

    /**
     * Validasi mesh dengan pesan error yang lebih detail.
     */
    public static void validateMesh(Mesh mesh) {
        if (mesh == null) {
            throw new IllegalArgumentException("Mesh is null");
        }
        
        if (mesh.getVertices() == null || mesh.getVertices().isEmpty()) {
            throw new IllegalArgumentException("Mesh has no vertices - file may be corrupted or not a valid OBJ");
        }

        if (mesh.getFaces() == null || mesh.getFaces().isEmpty()) {
            throw new IllegalArgumentException("Mesh has no faces - file may contain only vertices without face definitions");
        }

        int vertexCount = mesh.getVertices().size();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < mesh.getFaces().size(); i++) {
            Face f = mesh.getFaces().get(i);
            
            // Check for null face
            if (f == null) {
                errors.add("Face " + i + " is null");
                continue;
            }

            // Validate vertex indices
            if (f.a < 0 || f.a >= vertexCount) {
                errors.add("Face " + i + ": vertex A index " + f.a + " out of range [0, " + (vertexCount - 1) + "]");
            }
            if (f.b < 0 || f.b >= vertexCount) {
                errors.add("Face " + i + ": vertex B index " + f.b + " out of range [0, " + (vertexCount - 1) + "]");
            }
            if (f.c < 0 || f.c >= vertexCount) {
                errors.add("Face " + i + ": vertex C index " + f.c + " out of range [0, " + (vertexCount - 1) + "]");
            }
            
            // Check for degenerate faces (duplicate indices)
            if (f.a == f.b || f.b == f.c || f.a == f.c) {
                errors.add("Face " + i + ": degenerate face with duplicate vertex indices (" + f.a + ", " + f.b + ", " + f.c + ")");
            }
            
            // Limit error output
            if (errors.size() >= 10) {
                errors.add("... and more errors (showing first 10 only)");
                break;
            }
        }
        
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Mesh validation failed with " + errors.size() + " error(s):\n");
            for (String error : errors) {
                sb.append("  - ").append(error).append("\n");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }
    
    /**
     * Custom exception untuk parsing OBJ dengan flag fatal/non-fatal.
     */
    private static class OBJParseException extends Exception {
        private final boolean fatal;
        
        public OBJParseException(String message, boolean fatal) {
            super(message);
            this.fatal = fatal;
        }
        
        public boolean isFatal() {
            return fatal;
        }
    }
}