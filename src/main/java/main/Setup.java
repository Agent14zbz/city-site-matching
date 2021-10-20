package main;

import database.DBManager;
import guo_cam.CameraController;
import processing.core.PApplet;

import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/12
 * @time 16:40
 */
public class Setup extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private DBManager loader;
    private List<Block> blocks;

    private CameraController gcam;

    public void setup() {
        this.gcam = new CameraController(this);


        this.loader = new DBManager();

        int[] test = new int[]{1, 2, 3, 4, 5};
        this.blocks = loader.collectBlocks(test);
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println(blocks.get(i).toString());
        }
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
    }
}