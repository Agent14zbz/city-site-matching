package main;

import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
import elements.Building;
import guo_cam.CameraController;
import org.locationtech.jts.geom.Polygon;
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

    private DBManager dbManager;
    private PreProcessing preProcessing;
    private boolean drawBlock = false;

    private MatchManager matchManager;
    private boolean drawEmpty = false;
    private boolean drawResult = false;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        this.jtsRender = new JtsRender(this);

        this.dbManager = new DBManager();
        this.preProcessing = new PreProcessing(dbManager);

        this.matchManager = new MatchManager();

        textSize(20);
        long l=1;
        System.out.println(Long.toString(l));
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        gcam.drawSystem(200);

//        strokeWeight(3);
//        stroke(255, 0, 0);
//        for (int i = 0; i < preProcessing.areas.length; i++) {
//            line(i * 0.25f, (float) preProcessing.areas[i] * 0.01f, i * 0.25f, (float) preProcessing.areas[i] * 0.01f + 5);
//        }

//        fill(0);
//        for (int i = 0; i < preProcessing.minmax.length; i++) {
//            text(String.format("%.2f", preProcessing.minmax[i]), 0, (float) preProcessing.minmax[i] * 0.005f);
//        }

//        if (drawBlock) {
//            for (Block block : preProcessing.getBlockList()) {
//                jtsRender.drawGeometry(block.getShape());
//                for (Building building : block.getBuildings()) {
//                    jtsRender.drawGeometry(building.getBaseShape());
//                }
//            }
//        }

        if (drawEmpty) {
            pushStyle();
            strokeWeight(3);
            stroke(255, 0, 0);
            for (BlockEmpty e : matchManager.getBlockEmpties()) {
                jtsRender.drawGeometry(e.getShape());
                ZPoint[] polyAxesVecs = e.getShapeDescriptor().getAxes();

                pushStyle();
                stroke(255, 0, 0);
                polyAxesVecs[0].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        50,
                        5
                );
                stroke(0, 255, 0);
                polyAxesVecs[1].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        50,
                        5
                );
                popStyle();
            }
            popStyle();
        }

        if (drawResult) {
            pushStyle();
            strokeWeight(1);
            stroke(0);
            for (List<Polygon> list : matchManager.getBuildingResults()) {
                for (Polygon p : list) {
                    jtsRender.drawGeometry3D(p);
                }
            }
            strokeWeight(2);
            stroke(0, 0, 255);
            for (Polygon p : matchManager.getBlockResults()) {
                jtsRender.drawGeometry(p);
            }
            popStyle();
        }
//        // existing blocks from database
//        pushMatrix();
//        for (int i = 0; i < preProcessing.getBlockList().size(); i++) {
//            pushStyle();
//            strokeWeight(3);
//            jtsRender.drawGeometry(preProcessing.getBlockList().get(i).getShape());
//            strokeWeight(1);
//            for (Building b : preProcessing.getBlockList().get(i).getBuildings()) {
//                for (Polygon p : b.getFaces()) {
//                    jtsRender.drawPolygon3D(p);
//                }
//            }
//            translate(300, 0, 0);
//            popStyle();
//        }
//        popMatrix();
    }

    public void keyPressed() {
        if (key == '3') {
            preProcessing.initBlocks(dbManager);
            this.drawBlock = true;
        }
        if (key == '4') {
            preProcessing.updateDatabase(dbManager);
        }
        if (key == '1') {
            matchManager.load(dbManager);
            drawEmpty = true;
        }
        if (key == '2') {
            matchManager.matchTest();
            drawResult = true;
        }
    }
}