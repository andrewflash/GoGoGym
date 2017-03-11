package net.tudelft.xflash.gogogym;

/**
 * Created by USER on 11-Mar-17.
 */

public class Gym {
    public int id;
    public int gym_id;
    public String gym_name;
    public double longitude;
    public double latitude;


    public Gym(int id, String gym_name, double longitude, double latitude) {
        this.id = id;
        this.gym_name = gym_name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String stringify_Gym() {
        return id +  ";" + gym_name + ";" + longitude + ";" + latitude + ";";
    }
}
