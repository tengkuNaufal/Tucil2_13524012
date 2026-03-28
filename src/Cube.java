public class Cube {
    public Vertex min;
    public double size;

    public Cube(Vertex min, double size){
        this.min = min;
        this.size = size;
    }

    public Vertex getMax(){
        return new Vertex(min.x + size, min.y + size, min.z + size);
    }
}
