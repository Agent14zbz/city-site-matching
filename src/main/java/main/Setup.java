package main;

import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
import elements.Building;
import guo_cam.CameraController;
import igeo.IG;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import render.ZRender;
import transform.ZTransform;

import java.util.ArrayList;
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
        size(1920, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private DBManager dbManager;
    private PreProcessing preProcessing;
    private boolean drawBlock = false;

    private MatchManager matchManager;
    private boolean drawTestPCA = false;
    private boolean drawEmpty = false;
    private boolean drawSite = false;
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
//        gcam.top();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        gcam.drawSystem(200);

        // draw PCA test
        if (drawTestPCA) {
            BlockEmpty e = matchManager.getTestBlockPCA();
            pushStyle();
            strokeWeight(1);
            stroke(0);
            jtsRender.drawGeometry(e.getShape());
            ZPoint[] polyAxesPCA1 = e.getShapeDescriptor().getAxes();
            pushStyle();
            strokeWeight(2f);
            stroke(255, 0, 0);
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA1[0],
                    new ZPoint(e.getCentroid()),
                    30,
                    0
            );
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA1[0],
                    new ZPoint(e.getCentroid()),
                    -30,
                    0
            );
            stroke(0, 255, 0);
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA1[1],
                    new ZPoint(e.getCentroid()),
                    15,
                    0
            );
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA1[1],
                    new ZPoint(e.getCentroid()),
                    -15,
                    0
            );
            popStyle();
            popStyle();

            translate(200, 0);
            pushStyle();
            strokeWeight(1);
            stroke(0);
            jtsRender.drawGeometry(e.getShape());
            ZPoint[] polyAxesPCA2 = e.getShapeDescriptor().getAxesNew();
            pushStyle();
            strokeWeight(2f);
            stroke(255, 0, 0);
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA2[0],
                    new ZPoint(e.getCentroid()),
                    30,
                    0
            );
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA2[0],
                    new ZPoint(e.getCentroid()),
                    -30,
                    0
            );
            stroke(0, 255, 0);
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA2[1],
                    new ZPoint(e.getCentroid()),
                    15,
                    0
            );
            ZRender.drawZPointAsVec2D(
                    this,
                    polyAxesPCA2[1],
                    new ZPoint(e.getCentroid()),
                    -15,
                    0
            );
            popStyle();
            popStyle();
        }

        // draw empty blocks and axes
        if (drawEmpty) {
            // draw obb axis
            pushStyle();
            strokeWeight(1);
            stroke(0);
            for (int i = 0; i < matchManager.getBlockEmpties().size(); i++) {
                BlockEmpty e = matchManager.getBlockEmpties().get(i);
                fill(255, 0, 0);
//                text(i, (float) e.getCentroid().getX(), (float) e.getCentroid().getY());
                fill(200);
                jtsRender.drawGeometry(e.getShape());

                ZPoint[] polyAxesVecs = e.getShapeDescriptor().getAxesOBB();
                pushStyle();
                strokeWeight(2f);
                stroke(255, 0, 0);
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[0],
                        new ZPoint(e.getCentroid()),
                        30,
                        0
                );
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[0],
                        new ZPoint(e.getCentroid()),
                        -30,
                        0
                );
                stroke(0, 255, 0);
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[1],
                        new ZPoint(e.getCentroid()),
                        15,
                        0
                );
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[1],
                        new ZPoint(e.getCentroid()),
                        -15,
                        0
                );
                popStyle();
            }
            popStyle();

            // draw pca axis
            translate(1200, 0);
            pushStyle();
            strokeWeight(1);
            stroke(0);
            for (int i = 0; i < matchManager.getBlockEmpties().size(); i++) {
                BlockEmpty e = matchManager.getBlockEmpties().get(i);
                fill(255, 0, 0);
//                text(i, (float) e.getCentroid().getX(), (float) e.getCentroid().getY());
                fill(200);
                jtsRender.drawGeometry(e.getShape());

                ZPoint[] polyAxesVecs = e.getShapeDescriptor().getAxesNew();
                pushStyle();
                strokeWeight(2f);
                stroke(255, 0, 0);
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[0],
                        new ZPoint(e.getCentroid()),
                        30,
                        0
                );
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[0],
                        new ZPoint(e.getCentroid()),
                        -30,
                        0
                );
                stroke(0, 255, 0);
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[1],
                        new ZPoint(e.getCentroid()),
                        15,
                        0
                );
                ZRender.drawZPointAsVec2D(
                        this,
                        polyAxesVecs[1],
                        new ZPoint(e.getCentroid()),
                        -15,
                        0
                );
                popStyle();
            }
            popStyle();
        }

        // draw site blocks and buildings
        if (drawSite) {
            pushStyle();
            noFill();
            for (Polygon block : matchManager.getSite_test_block()) {
                jtsRender.drawGeometry(block);
            }
            for (Polygon building : matchManager.getSite_test_buildingBase()) {
                jtsRender.drawGeometry(building);
            }
            popStyle();
        }

        // draw match result to each empty block
        if (drawResult) {
            pushStyle();
            strokeWeight(1);
            stroke(255);
            fill(60);
            for (Block blockMatch : matchManager.getBlockMatches()) {
                for (Building bu : blockMatch.getBuildings()) {
                    for (Polygon face : bu.getFaces()) {
                        jtsRender.drawGeometry3D(face);
                    }
                }
            }
//            strokeWeight(2);
//            noFill();
//            stroke(0, 0, 255);
//            for (int i = 0; i < matchManager.getBlockResults().size(); i++) {
//                Polygon p = matchManager.getBlockResults().get(i);
//                jtsRender.drawGeometry(p);
//                ZPoint[] bestAxes = matchManager.getBestAxes().get(i);
//                pushStyle();
//                strokeWeight(1);
//                stroke(0, 0, 255);
//                bestAxes[0].displayAsVector(
//                        this,
//                        new ZPoint(p.getCentroid()),
//                        30,
//                        0
//                );
//                bestAxes[0].displayAsVector(
//                        this,
//                        new ZPoint(p.getCentroid()),
//                        -30,
//                        0
//                );
//                stroke(0, 255, 255);
//                bestAxes[1].displayAsVector(
//                        this,
//                        new ZPoint(p.getCentroid()),
//                        15,
//                        0
//                );
//                bestAxes[1].displayAsVector(
//                        this,
//                        new ZPoint(p.getCentroid()),
//                        -15,
//                        0
//                );
//                popStyle();
//            }
            popStyle();
        }

        // draw best 5 for each empty block
        if (drawbest5) {
            pushStyle();
            pushMatrix();
            translate(0, 1000);
            strokeWeight(2);
            for (int i = 0; i < matchManager.getEmptiesOrigin().size(); i++) {
                stroke(255, 0, 0);
                Polygon empty = matchManager.getEmptiesOrigin().get(i);
                fill(0);
                text(i, (float) empty.getCentroid().getX(), (float) empty.getCentroid().getY());
                noFill();
                jtsRender.drawGeometry(empty);
                stroke(0);
                for (int j = 0; j < matchManager.getBestMatch().get(i).size(); j++) {
                    translate(300, 0);
                    Block e = matchManager.getBestMatch().get(i).get(j);
                    jtsRender.drawGeometry(e.getShape());
                    for (Building bu : e.getBuildings()) {
                        jtsRender.drawGeometry3D(bu.getBaseShape());
                    }
                }
                translate(-1500, 400);
            }
            popStyle();
            popMatrix();
        }
    }

    public void keyPressed() {
        // pre-processing
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
        if (key == '9') {
            preProcessing.getBlocksToUpdate2(dbManager);
        }
        if (key == '0') {
            preProcessing.updateShapeAxesOBB(dbManager);
        }

        // match tests
        if (key == '`') {
            matchManager.loadEmptyTXT();
            drawEmpty = true;
        }
        if (key == '1') {
            matchManager.loadEmpty();
            drawEmpty = true;
        }
        if (key == '2') {
            matchManager.matchTest(dbManager);
            drawResult = true;
            drawbest5 = true;
        }
//        if (key == '3') {
//            matchManager.matchTest(dbManager);
//            drawResult = true;
//            drawbest5 = true;
//        }

        // display controls
        if (key == 'q') {
            drawEmpty = !drawEmpty;
        }
        if (key == 'w') {
            drawResult = !drawBlock;
        }
        if (key == 'e') {
            matchManager.loadEmpty();
            drawTestPCA = !drawTestPCA;
        }
        if (key == 'r') {
            matchManager.loadSiteTest("./src/main/resources/20220110sitetest1.3dm");
            matchManager.matchTest(dbManager);
            drawEmpty = true;
            drawSite = true;
            drawResult = true;
            drawbest5 = true;
            for (int i = 0; i < matchManager.getBestMatch().size(); i++) {
                List<Block> bests = matchManager.getBestMatch().get(i);
                List<Double> bestDists = matchManager.getBestMatchDists().get(i);
                List<Double> dists = new ArrayList<>();
                List<Double> gsis = new ArrayList<>();
                List<Double> fsis = new ArrayList<>();
                for (int j = 0; j < bests.size(); j++) {
                    Block best = bests.get(j);
                    gsis.add(best.getGSI());
                    fsis.add(best.getFSI());
                    dists.add(bestDists.get(j));
                }
                System.out.println("block " + i + " matched gsi: " + gsis);
                System.out.println("block " + i + " matched fsi: " + fsis);
                System.out.println("block " + i + " matched distance: " + dists);
            }
        }

        // output 3dm
        if (key == '+') {
            IG.init();
            // best 5
            for (int i = 0; i < matchManager.getBestMatch().size(); i++) {
                List<Block> bests = matchManager.getBestMatch().get(i);
                for (int j = 0; j < bests.size(); j++) {
                    Block best = bests.get(j);
                    ZTransform.PolygonToICurve(best.getShape()).layer("bestMatchBlock" + i + "_" + j);
                    for (Building bu : best.getBuildings()) {
                        ZTransform.PolygonToICurve(bu.getBaseShape()).layer("bestMatchBlock" + i + "_" + j);
                    }
                }
            }
            // generate result
            for (Block match : matchManager.getBlockMatches()) {
                ZTransform.PolygonToICurve(match.getShape()).layer("matchResult");
                for (Building building : match.getBuildings()) {
                    for (Polygon p : building.getFaces()) {
                        ZTransform.PolygonToICurve(p).layer("matchResult");
                    }
                }
            }
            IG.save("./src/main/resources/match_hamburg2.3dm");
        }
    }
}