import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private final List<Vertex> vertices;
    private final List<Face> faces;

    public Mesh() {
        this.vertices = new ArrayList<>();
        this.faces = new ArrayList<>();
    }

    public List<Vertex> getVertices(){
        return vertices;
    }

    public List<Face> getFaces(){
        return faces;
    }

    public void addVertex(Vertex v){
        vertices.add(v);
    }

    public void addFace(Face f){
        faces.add(f);
    }
}
