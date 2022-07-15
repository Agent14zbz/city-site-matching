package main;

import basicGeometry.ZFactory;
import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
import elements.BlockMatch;
import elements.Building;
import igeo.ICurve;
import igeo.IG;
import math.ZMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import transform.ZJtsTransform;
import transform.ZTransform;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/11/1
 * @time 14:47
 */
public class MatchManager {
    // empty blocks to match
    private List<BlockEmpty> blockEmpties;
    private BlockEmpty testBlockPCA;

    // match result: blocks
    private List<Block> blockMatches;

    // match result: block polygon
    private List<Polygon> blockResults;
    // match result: block axes
    private List<ZPoint[]> bestAxes;

    private List<Polygon> emptiesOrigin;
    private List<List<Block>> bestMatch;
    private List<List<Double>> bestMatchDists;

    // test sites
    private List<Polygon> site_test_block;
    private List<Polygon> site_test_buildingBase;

    /* ------------- constructor ------------- */

    public MatchManager() {

    }

    /* ------------- site input ------------- */

    /**
     * load empty block from .3dm
     */
    public void loadEmpty() {
        this.blockEmpties = new ArrayList<>();
        List<Double> areas = new ArrayList<>();
        // load empty blocks
        System.out.println(">>>> start collecting empty blocks");
        IG.init();
        IG.open("./src/main/resources/20211121test.3dm");
        ICurve[] polyLines = IG.layer("emptyBlock").curves();
        for (int i = 0; i < polyLines.length; i++) {
            Polygon e = (Polygon) ZTransform.ICurveToJts(polyLines[i]);
            if (e != null) {
                BlockEmpty bm = new BlockEmpty(e);
                blockEmpties.add(bm);
                areas.add(bm.getArea());
            }
        }
        ICurve[] testBlock = IG.layer("testBlock").curves();
        this.testBlockPCA = new BlockEmpty((Polygon) ZTransform.ICurveToJts(testBlock[0]));
        System.out.println(">>> empty blocks: " + blockEmpties.size());
        System.out.println(Collections.max(areas));
        System.out.println(Collections.min(areas));
    }

    /**
     * test for experimental site
     *
     * @param path test file path
     */
    public void loadSiteTest(String path) {
        // load from 3dm
        System.out.println(">>>> start collecting empty blocks");
        this.blockEmpties = new ArrayList<>();
        IG.init();
        IG.open(path);
        ICurve[] targetBlock = IG.layer("target").curves();
        for (int i = 0; i < targetBlock.length; i++) {
            Polygon e = (Polygon) ZTransform.ICurveToJts(targetBlock[i]);
            if (e != null) {
                BlockEmpty be = new BlockEmpty(e);
                this.blockEmpties.add(be);
            }
        }

        ICurve[] siteBlocks = IG.layer("block").curves();
        this.site_test_block = new ArrayList<>();
        for (int i = 0; i < siteBlocks.length; i++) {
            Polygon b = (Polygon) ZTransform.ICurveToJts(siteBlocks[i]);
            site_test_block.add(b);
        }

        ICurve[] siteBuildings = IG.layer("buildingBase").curves();
        this.site_test_buildingBase = new ArrayList<>();
        for (int i = 0; i < siteBuildings.length; i++) {
            Polygon b = (Polygon) ZTransform.ICurveToJts(siteBuildings[i]);
            site_test_buildingBase.add(b);
        }
        System.out.println(">>> test site collected");
    }

    /**
     * load empty block from .txt
     */
    public void loadEmptyTXT() {
        this.blockEmpties = new ArrayList<>();
        List<Double> areas = new ArrayList<>();
        try {
            File f = new File("./src/main/resources/polygons.txt");
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();

            while (line != null) {
                String[] spString = line.split("\\s+");
                Coordinate[] coords = new Coordinate[Math.round(spString.length * 0.5f) + 1];
                for (int i = 0; i < coords.length - 1; i++) {
                    coords[i] = new Coordinate(
                            Double.parseDouble(spString[i * 2]) * 2.4,
                            Double.parseDouble(spString[i * 2 + 1]) * 2.4
                    );
                }
                coords[coords.length - 1] = coords[0];
                BlockEmpty bm = new BlockEmpty(ZFactory.jtsgf.createPolygon(coords));
                areas.add(bm.getArea());
                blockEmpties.add(bm);
                line = reader.readLine();
            }
            System.out.println(">>> empty blocks: " + blockEmpties.size());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[] as = new double[areas.size()];
        for (int i = 0; i < as.length; i++) {
            as[i] = areas.get(i);
        }
        System.out.println(ZMath.average(as));
        System.out.println(Collections.max(areas));
        System.out.println(Collections.min(areas));
    }

    /* ------------- match and generate ------------- */

    /**
     * match test
     * load indicators only for the first
     * request full data after matching for the second time
     *
     * @param dbManager database manager
     */
    public void matchTest(DBManager dbManager) {
        long startTime = System.currentTimeMillis();

        this.blockResults = new ArrayList<>();
        this.bestAxes = new ArrayList<>();
        this.blockMatches = new ArrayList<>();

        this.emptiesOrigin = new ArrayList<>();
        this.bestMatch = new ArrayList<>();
        this.bestMatchDists = new ArrayList<>();
        // load block match samples refer to empty block properties
        int count = 0;
        for (int i = 0; i < blockEmpties.size(); i++) {
            BlockEmpty empty = blockEmpties.get(i);
            double[] areaRange = new double[]{empty.getArea() * 0.9, empty.getArea() * 1.1};
            double[] gsiRange = new double[]{empty.getTargetGSI() * 0.75, empty.getTargetGSI() * 1.25};
            double[] fsiRange = new double[]{empty.getTargetFSI() * 0.75, empty.getTargetFSI() * 1.25};
            List<BlockMatch> blockMatchList = dbManager.collectBlocksInfo(
                    areaRange, gsiRange, fsiRange
            );

            ZJtsTransform transform = new ZJtsTransform();
            transform.addTranslate2D(new ZPoint(empty.getCentroid()).scaleTo(-1));
            emptiesOrigin.add((Polygon) transform.applyToGeometry2D(empty.getShape()));

            if (blockMatchList.size() > 0) {
                // match the closest 5 cases, pick one randomly
                double[] vectorDists = new double[blockMatchList.size()];
                for (int j = 0; j < blockMatchList.size(); j++) {
                    vectorDists[j] = ZMath.distanceEuclidean(
                            blockMatchList.get(j).getShapeDescriptor(),
                            empty.getShapeDescriptor().getDescriptorAsDouble()
                    );
                }
                int[] min = ZMath.getMinIndices(vectorDists, 10);
                List<Block> bests = new ArrayList<>();
                List<Double> bestDists = new ArrayList<>();
                for (int j = 0; j < min.length; j++) {
                    BlockMatch best5s = blockMatchList.get(min[j]);
                    long best5id = best5s.getId();
                    Block result = dbManager.collectBlockResult(best5id);
                    List<Long> buildingIDs = result.getBuildingIDs();
                    List<Building> buildings = dbManager.collectBuildingInBlockPre(buildingIDs);
                    result.setBuildings(buildings);
                    for (Building building : buildings) {
                        building.setBlockID(result.getID());
                        building.setBlock(result);
                        building.generateAbsShape(result.getCentroidLatLon(), result.getCityRatio());
                        building.cal3DInfo();
                        double area = building.getBuildingArea();
                    }
                    bests.add(result);
                    bestDists.add(vectorDists[min[j]]);
                }
                bestMatch.add(bests);
                bestMatchDists.add(bestDists);

                int random = 0;
                if (i == 0) {
                    random = min[0];
                } else if (i == 1) {
                    random = min[2];
                } else if (i == 2) {
                    random = min[4];
                } else if (i == 3) {
                    random = min[7];
                } else if (i == 4) {
                    random = min[0];
                }

                BlockMatch bestMatch = blockMatchList.get(random);
                long id = bestMatch.getId();
                Block best = dbManager.collectBlockResult(id);

                // compare 4 cases: 1.default  2.rotate 180  3.mirror  4.mirror & rotate 180
                double emptyArea = empty.getArea();
                double bestArea = best.getArea();
                double scaleRatio = Math.sqrt(emptyArea / bestArea);

                ZPoint emptyCentroid = new ZPoint(empty.getCentroid());
                ZPoint bestCentroid = new ZPoint(0, 0);
                ZPoint moveVec = emptyCentroid.sub(bestCentroid);

//                ZPoint emptyAxes = empty.getShapeDescriptor().getAxesNew()[0];
//                ZPoint bestAxes = best.getShapeDescriptor().getAxesNew()[0];
//                double angle = bestAxes.angleWith(emptyAxes);
//                this.bestAxes.add(new ZPoint[]{
//                        bestAxes.rotate2D(Math.PI * (angle / 180)),
//                        best.getShapeDescriptor().getAxesNew()[1].rotate2D(Math.PI * (angle / 180))
//                });

                ZPoint emptyAxes = empty.getShapeDescriptor().getAxesOBB()[0];
                ZPoint bestAxes = best.getShapeDescriptor().getAxesOBB()[0];
                double angle = bestAxes.angleWith(emptyAxes);
                this.bestAxes.add(new ZPoint[]{
                        bestAxes.rotate2D(Math.PI * (angle / 180)),
                        best.getShapeDescriptor().getAxesOBB()[1].rotate2D(Math.PI * (angle / 180))
                });

                ZJtsTransform transform1 = new ZJtsTransform();
                transform1.addScale2D(scaleRatio);
                transform1.addRotateAboutOrigin2D(Math.PI * (angle / 180));
                transform1.addTranslate2D(moveVec);
                Polygon default1 = (Polygon) transform1.applyToGeometry2D(best.getShape());

                ZJtsTransform transform2 = new ZJtsTransform();
                transform2.addScale2D(scaleRatio);
                transform2.addRotateAboutOrigin2D(Math.PI * (angle / 180));
                transform2.addRotateAboutOrigin2D(Math.PI);
                transform2.addTranslate2D(moveVec);
                Polygon rotate2 = (Polygon) transform2.applyToGeometry2D(best.getShape());

                ZJtsTransform transform3 = new ZJtsTransform();
                transform3.addScale2D(scaleRatio);
                transform3.addRotateAboutOrigin2D(Math.PI * (angle / 180));
                transform3.addReflect2D(new ZPoint(0, 0), bestAxes);
                transform3.addTranslate2D(moveVec);
                Polygon mirror3 = (Polygon) transform3.applyToGeometry2D(best.getShape());

                ZJtsTransform transform4 = new ZJtsTransform();
                transform4.addScale2D(scaleRatio);
                transform4.addRotateAboutOrigin2D(Math.PI * (angle / 180));
                transform4.addRotateAboutOrigin2D(Math.PI);
                transform4.addReflect2D(new ZPoint(0, 0), bestAxes);
                transform4.addTranslate2D(moveVec);
                Polygon rotateMirror4 = (Polygon) transform4.applyToGeometry2D(best.getShape());

                // compare the most intersection case
                Polygon emptyShape = empty.getShape();
                Geometry inter1 = emptyShape.intersection(default1);
                Geometry inter2 = emptyShape.intersection(rotate2);
                Geometry inter3 = emptyShape.intersection(mirror3);
                Geometry inter4 = emptyShape.intersection(rotateMirror4);
                double[] interRatio = new double[]{
                        inter1.getArea() / emptyArea,
                        inter2.getArea() / emptyArea,
                        inter3.getArea() / emptyArea,
                        inter4.getArea() / emptyArea
                };
                int maxInter = ZMath.getMaxIndex(interRatio);
                System.out.println(">>> empty block " + i + " intersection ratio: " + interRatio[maxInter]);

                ZJtsTransform bestMatchTransform = null;
                switch (maxInter) {
                    case 1:
                        bestMatchTransform = transform2;
                        break;
                    case 2:
                        bestMatchTransform = transform3;
                        break;
                    case 3:
                        bestMatchTransform = transform4;
                        break;
                    default:
                        bestMatchTransform = transform1;
                        break;
                }

                // if the intersection ratio is still lower than standard, rotate to find the best direction
                double maxInterRatio = interRatio[maxInter];
                if (maxInterRatio < 0.94) {
                    double[] ratioAfterRotate = new double[50];
                    double angleStep = Math.PI / 25;
                    for (int j = 0; j < 50; j++) {
                        bestMatchTransform.addRotateAboutPoint2D(angleStep, emptyCentroid);
                        Polygon rotateTemp = (Polygon) bestMatchTransform.applyToGeometry2D(best.getShape());
                        ratioAfterRotate[j] = rotateTemp.intersection(emptyShape).getArea() / emptyArea;
                    }
                    int maxRatioAfterRotate = ZMath.getMaxIndex(ratioAfterRotate);
                    bestMatchTransform.addRotateAboutPoint2D(maxRatioAfterRotate * angleStep, emptyCentroid);
                    System.out.println(">>>> rotate optimized for block " + i + "   before: " + maxInterRatio + "  after: " + ratioAfterRotate[maxRatioAfterRotate]);
                    count++;
                }

                // get building bases, calculate 3d info
                List<Long> buildingIDs = best.getBuildingIDs();
                List<Building> buildings = dbManager.collectBuildingInBlockPre(buildingIDs);
                best.setBuildings(buildings);
                double buildingAreaAll = 0;
                int building3DNum = 0;
                for (Building building : buildings) {
                    building.setBlockID(best.getID());
                    building.setBlock(best);
                    building.generateAbsShape(best.getCentroidLatLon(), best.getCityRatio());
                    building.cal3DInfo();
                    double area = building.getBuildingArea();
                    if (area > 0) {
                        buildingAreaAll += area;
                        building3DNum++;
                    }
                }

                // transform the result to the empty site
                Polygon bestMatchResult = (Polygon) bestMatchTransform.applyToGeometry2D(best.getShape());
                blockResults.add(bestMatchResult);

                List<Polygon> bases = new ArrayList<>();
                for (Building building : buildings) {
                    building.transformBase(bestMatchTransform);
                    building.generateVolume(buildingAreaAll, building3DNum);
                }
                blockMatches.add(best);

                System.out.println(">> matched success for empty block " + i);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + " ms");
        System.out.println(count);
    }

    /* ------------- setter & getter ------------- */

    public List<BlockEmpty> getBlockEmpties() {
        return blockEmpties;
    }

    public List<Polygon> getBlockResults() {
        return blockResults;
    }

    public List<Block> getBlockMatches() {
        return blockMatches;
    }

    public List<ZPoint[]> getBestAxes() {
        return bestAxes;
    }

    public List<Polygon> getEmptiesOrigin() {
        return emptiesOrigin;
    }

    public List<List<Block>> getBestMatch() {
        return bestMatch;
    }

    public List<List<Double>> getBestMatchDists() {
        return bestMatchDists;
    }

    public BlockEmpty getTestBlockPCA() {
        return testBlockPCA;
    }

    public List<Polygon> getSite_test_block() {
        return site_test_block;
    }

    public List<Polygon> getSite_test_buildingBase() {
        return site_test_buildingBase;
    }

}
