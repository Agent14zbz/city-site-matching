package main;

import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
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
    private boolean drawbest5 = false;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        this.jtsRender = new JtsRender(this);

        this.dbManager = new DBManager();
        this.preProcessing = new PreProcessing(dbManager);

        this.matchManager = new MatchManager();

        textSize(12);
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

        // draw empty blocks and axes
        if (drawEmpty) {
            pushStyle();
            strokeWeight(3);
            stroke(255, 0, 0);
            for (int i = 0; i < matchManager.getBlockEmpties().size(); i++) {
                BlockEmpty e = matchManager.getBlockEmpties().get(i);
                fill(0);
                text(i, (float) e.getCentroid().getX(), (float) e.getCentroid().getY());
                noFill();
                jtsRender.drawGeometry(e.getShape());
                ZPoint[] polyAxesVecs = e.getShapeDescriptor().getAxesNew();

                pushStyle();
                strokeWeight(1.5f);
                stroke(255, 0, 0);
                polyAxesVecs[0].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        30,
                        0
                );
                polyAxesVecs[0].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        -30,
                        0
                );
                stroke(0, 255, 0);
                polyAxesVecs[1].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        15,
                        0
                );
                polyAxesVecs[1].displayAsVector(
                        this,
                        new ZPoint(e.getCentroid()),
                        -15,
                        0
                );
                popStyle();
            }
            popStyle();
        }

        // draw match result to each empty block
        if (drawResult) {
            pushStyle();
            strokeWeight(1);
            stroke(0);
            fill(200);
            for (List<Polygon> list : matchManager.getBuildingResults()) {
                for (Polygon p : list) {
                    jtsRender.drawGeometry(p);
                }
            }
            strokeWeight(2);
            noFill();
            stroke(0);
            for (int i = 0; i < matchManager.getBlockResults().size(); i++) {
                Polygon p = matchManager.getBlockResults().get(i);
//                jtsRender.drawGeometry(p);
                ZPoint[] bestAxes = matchManager.getBestAxes().get(i);
                pushStyle();
                strokeWeight(1);
                stroke(0, 0, 255);
                bestAxes[0].displayAsVector(
                        this,
                        new ZPoint(p.getCentroid()),
                        30,
                        0
                );
                bestAxes[0].displayAsVector(
                        this,
                        new ZPoint(p.getCentroid()),
                        -30,
                        0
                );
                stroke(0, 255, 255);
                bestAxes[1].displayAsVector(
                        this,
                        new ZPoint(p.getCentroid()),
                        15,
                        0
                );
                bestAxes[1].displayAsVector(
                        this,
                        new ZPoint(p.getCentroid()),
                        -15,
                        0
                );
                popStyle();
            }
            popStyle();
        }

        // draw best 5 results for each empty block
//        if (drawbest5) {
//            pushStyle();
//            pushMatrix();
//            translate(0, 400);
//            strokeWeight(2);
//            for (int i = 0; i < matchManager.getEmptiesOrigin().size(); i++) {
//                stroke(255, 0, 0);
//                Polygon empty = matchManager.getEmptiesOrigin().get(i);
//                fill(0);
//                text(i, (float) empty.getCentroid().getX(), (float) empty.getCentroid().getY());
//                noFill();
//                jtsRender.drawGeometry(empty);
//                stroke(0);
//                for (int j = 0; j < matchManager.getBest5().get(i).size(); j++) {
//                    translate(150, 0);
//                    Block e = matchManager.getBest5().get(i).get(j);
//                    jtsRender.drawGeometry(matchManager.getBest5().get(i).get(j).getShape());
//                    ZPoint[] polyAxesVecs = e.getShapeDescriptor().getAxesNew();
//
//                    pushStyle();
//                    strokeWeight(1);
//                    stroke(0, 0, 255);
//                    polyAxesVecs[0].displayAsVector(
//                            this,
//                            new ZPoint(e.getCentroid()),
//                            30,
//                            0
//                    );
//                    polyAxesVecs[0].displayAsVector(
//                            this,
//                            new ZPoint(e.getCentroid()),
//                            -30,
//                            0
//                    );
//                    stroke(0, 255, 255);
//                    polyAxesVecs[1].displayAsVector(
//                            this,
//                            new ZPoint(e.getCentroid()),
//                            15,
//                            0
//                    );
//                    polyAxesVecs[1].displayAsVector(
//                            this,
//                            new ZPoint(e.getCentroid()),
//                            -15,
//                            0
//                    );
//                    popStyle();
//                }
//                translate(-750, 200);
//            }
//            popStyle();
//            popMatrix();
//        }
    }

    public void keyPressed() {
        if (key == '4') {
            preProcessing.initBlocks(dbManager);
            this.drawBlock = true;
        }
        if (key == '5') {
            preProcessing.updateDatabase(dbManager);
        }
        if (key == '6') {
            preProcessing.getBlocksToUpdate(dbManager);
        }
        if (key == '7') {
            preProcessing.updateSDAxes(dbManager);
        }
        if (key == '8') {
            preProcessing.updateAxes(dbManager);
        }


        if (key == '1') {
            matchManager.loadEmpty(dbManager);
            drawEmpty = true;
        }
        if (key == '2') {
            matchManager.matchTest2(dbManager);
            drawResult = true;
            drawbest5 = true;
        }
//        if (key == '3') {
//            matchManager.matchTest(dbManager);
//            drawResult = true;
//            drawbest5 = true;
//        }

    }
}