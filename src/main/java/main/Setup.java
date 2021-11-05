package main;

import basicGeometry.ZFactory;
import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.Building;
import guo_cam.CameraController;
import math.ZGeoMath;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZJtsTransform;

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

        createTestBlock();
        matchTest();
    }

    private Polygon testEmptyBlock;
    private List<Polygon> testNewFaces;
    private Geometry obb1;
    private Geometry obb2;

    private void createTestBlock() {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(-70, 440),
                new Coordinate(77, 310),
                new Coordinate(120, 180),
                new Coordinate(-120, 300),
                new Coordinate(-70, 440)
        };
        testEmptyBlock = ZFactory.jtsgf.createPolygon(coordinates);
        ZPoint testCenter = new ZPoint(testEmptyBlock.getCentroid());
        ZJtsTransform jtsTransform = new ZJtsTransform();
        jtsTransform.addRotateAboutPoint2D(Math.PI * 0.5, testCenter);
        testEmptyBlock = (Polygon) jtsTransform.applyToGeometry2D(ZFactory.jtsgf.createPolygon(coordinates));
        obb1 = MinimumDiameter.getMinimumRectangle(testEmptyBlock);
    }

    private void matchTest() {
        ZPoint dir = ZGeoMath.obbDir(testEmptyBlock);

        Block b0 = preProcessing.getBlockList().get(0);
        obb2 = MinimumDiameter.getMinimumRectangle(b0.getShape());

        System.out.println(dir);
        System.out.println(b0.getDirVec());

        ZPoint testCenter = new ZPoint(testEmptyBlock.getCentroid());
        ZJtsTransform transform = new ZJtsTransform();
        System.out.println("dir angle " + b0.getDirVec().angleWith(dir));
        transform
                .addRotateAboutZ3D(PI * (b0.getDirVec().angleWith(dir) / 180))
                .addTranslate3D(testCenter.sub(new ZPoint(b0.getCentroid())))
        ;

        testNewFaces = new ArrayList<>();
        List<Building> b0Buildings = b0.getBuildings();
        for (Building b : b0Buildings) {
            testNewFaces.addAll(b.transform(transform));
        }
//        System.out.println(testNewFaces.size());
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        gcam.drawSystem(200);

        jtsRender.drawGeometry(obb1);
        jtsRender.drawGeometry(obb2);

        pushStyle();
        strokeWeight(3);
        stroke(255, 0, 0);
        jtsRender.drawGeometry(testEmptyBlock);
        popStyle();

        pushStyle();
        fill(255, 0, 0);
        for (Polygon p : testNewFaces) {
            jtsRender.drawGeometry3D(p);
        }
        popStyle();

        // existing blocks from database
        pushMatrix();
        for (int i = 0; i < preProcessing.getBlockList().size(); i++) {
            pushStyle();
            strokeWeight(3);
            jtsRender.drawGeometry(preProcessing.getBlockList().get(i).getShape());
            strokeWeight(1);
            for (Building b : preProcessing.getBlockList().get(i).getBuildings()) {
                for (Polygon p : b.getFaces()) {
                    jtsRender.drawPolygon3D(p);
                }
            }
            translate(300, 0, 0);
            popStyle();
        }
        popMatrix();
    }
}