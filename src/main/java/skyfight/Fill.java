package skyfight;

import org.bukkit.Location;
import org.bukkit.Material;


public class Fill {
    public Location pos1;
    public Location pos2;
    public Material material;

    public Fill(Location pos1, Location pos2, Material material) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.material = material;
    }

    public Location getPos1() {return pos1;}

    public Location getPos2() {return pos2;}

    public Material getMaterial() {return material;}
}