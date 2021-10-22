package main;

import database.DBManager;
import database.PreProcessing;
import elements.BlockRaw;
import elements.Building;
import guo_cam.CameraController;
import processing.core.PApplet;
import render.JtsRender;

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

    private DBManager manager;
    private PreProcessing preProcessing;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        this.jtsRender = new JtsRender(this);

        this.manager = new DBManager();
        this.preProcessing = new PreProcessing(manager);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        gcam.drawSystem(200);
        pushMatrix();
        for (int i = 0; i < preProcessing.getBlockList().size(); i++) {
            jtsRender.drawGeometry(preProcessing.getBlockList().get(i).getShape());
            for (Building b : preProcessing.getBlockList().get(i).getBuildings()) {
                jtsRender.drawGeometry(b.getBaseShape());
            }
            translate(300, 0, 0);
        }
        popMatrix();
    }
}