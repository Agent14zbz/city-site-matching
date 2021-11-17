package main;

import advancedGeometry.ZShapeDescriptor;
import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
import elements.BlockMatch;
import elements.Building;
import igeo.ICurve;
import igeo.IG;
import math.ZMath;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import transform.ZJtsTransform;
import transform.ZTransform;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<BlockEmpty> blockEmpties; // empty blocks to match

    private List<Polygon> blockResults; // match result: block polygon
    private List<ZPoint[]> bestAxes; // match result: block axes
    private List<List<Polygon>> buildingResults; // match result: building bases

    private List<Polygon> emptiesOrigin;
    private List<List<Block>> best5;

    /* ------------- constructor ------------- */

    public MatchManager() {

    }

    /* ------------- member function ------------- */

    public void loadEmpty(DBManager dbManager) {
        this.blockEmpties = new ArrayList<>();

        // load empty blocks
        System.out.println(">>>> start collecting empty blocks");
        IG.init();
        IG.open("./src/main/resources/20211107test.3dm");
        ICurve[] polyLines = IG.layer("emptyBlock").curves();
        for (int i = 0; i < polyLines.length; i++) {
            Polygon e = (Polygon) ZTransform.ICurveToJts(polyLines[i]);
            if (e != null) {
                blockEmpties.add(new BlockEmpty(e));
            }

        }
        System.out.println(">>> empty blocks: " + blockEmpties.size());
    }

    /**
     * match test
     * load indicators only for the first
     * request full data after matching for the second time
     *
     * @param dbManager database manager
     */
    public void matchTest2(DBManager dbManager) {
        long startTime = System.currentTimeMillis();

        this.blockResults = new ArrayList<>();
        this.bestAxes = new ArrayList<>();
        this.buildingResults = new ArrayList<>();

        this.emptiesOrigin = new ArrayList<>();
        this.best5 = new ArrayList<>();
        // load block match samples refer to empty block properties
        for (int i = 0; i < blockEmpties.size(); i++) {
            BlockEmpty empty = blockEmpties.get(i);
            double[] areaRange = new double[]{empty.getArea() * 0.8, empty.getArea() * 1.2};
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
                int[] min = ZMath.getMinIndices(vectorDists, 5);
                int random = min[0];
                BlockMatch bestMatch = blockMatchList.get(random);
                long id = bestMatch.getId();
                Block best = dbManager.collectBlockResult(id);

                List<Block> bests = new ArrayList<>();
                for (int j = 0; j < min.length; j++) {
                    BlockMatch best5s = blockMatchList.get(min[j]);
                    long best5id = best5s.getId();
                    Block result = dbManager.collectBlockResult(best5id);
                    bests.add(result);
                }
                best5.add(bests);

                // compare 4 cases: 1.default  2.rotate 180  3.mirror  4.mirror & rotate 180
                double emptyArea = empty.getArea();
                double bestArea = best.getArea();
                double scaleRatio = Math.sqrt(emptyArea / bestArea);

                ZPoint emptyCentroid = new ZPoint(empty.getCentroid());
                ZPoint bestCentroid = new ZPoint(best.getCentroid());
                ZPoint moveVec = emptyCentroid.sub(bestCentroid);

                ZPoint emptyAxes = empty.getShapeDescriptor().getAxesNew()[0];
                ZPoint bestAxes = best.getShapeDescriptor().getAxesNew()[0];
                double angle = bestAxes.angleWith(emptyAxes);
                this.bestAxes.add(new ZPoint[]{
                        bestAxes.rotate2D(Math.PI * (angle / 180)),
                        best.getShapeDescriptor().getAxesNew()[1].rotate2D(Math.PI * (angle / 180))
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
                transform4.addRotateAboutOrigin2D(Math.PI);
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
                System.out.println(">>> empty block " + i + " intersection ratio: ");
                int maxInter = ZMath.getMaxIndex(interRatio);

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
                if (maxInterRatio < 0.9) {
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
                }

                // get building bases
                List<Long> buildingIDs = best.getBuildingIDs();
                List<Building> buildings = dbManager.collectBuildingInBlockPre(buildingIDs);
                for (Building building : buildings) {
                    building.setBlockID(best.getID());
                    building.generateAbsShape(best.getCentroidLatLon(), best.getCityRatio());
                }
                best.setBuildings(buildings);

                // transform the result to the empty site
                Polygon bestMatchResult = (Polygon) bestMatchTransform.applyToGeometry2D(best.getShape());
                blockResults.add(bestMatchResult);
                List<Polygon> bases = new ArrayList<>();
                for (Building building : buildings) {
                    Polygon baseShape = building.getBaseShape();
                    bases.add((Polygon) bestMatchTransform.applyToGeometry2D(baseShape));
                }
                buildingResults.add(bases);

                System.out.println("matched success for empty block " + i);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + " ms");
    }


//    /**
//     * match test
//     * load all possible sample at one time
//     *
//     * @param dbManager database manager
//     */
//    @Deprecated
//    public void matchTest(DBManager dbManager) {
//        long startTime = System.currentTimeMillis();
//
//        this.blockResults = new ArrayList<>();
//        this.bestAxes = new ArrayList<>();
//        this.buildingResults = new ArrayList<>();
//
//        this.emptiesOrigin = new ArrayList<>();
//        this.best5 = new ArrayList<>();
//
//        // load block match samples refer to empty block properties
//        for (int i = 0; i < blockEmpties.size(); i++) {
//            BlockEmpty empty = blockEmpties.get(i);
//            double[] areaRange = new double[]{empty.getArea() * 0.8, empty.getArea() * 1.2};
//            double[] gsiRange = new double[]{empty.getTargetGSI() * 0.75, empty.getTargetGSI() * 1.25};
//            double[] fsiRange = new double[]{empty.getTargetFSI() * 0.75, empty.getTargetFSI() * 1.25};
//            List<Block> blockMatchList = dbManager.collectBlocksConstrain(
//                    areaRange, gsiRange, fsiRange
//            );
//
//            ZJtsTransform transform = new ZJtsTransform();
//            transform.addTranslate2D(new ZPoint(empty.getCentroid()).scaleTo(-1));
//            emptiesOrigin.add((Polygon) transform.applyToGeometry2D(empty.getShape()));
//
//            if (blockMatchList.size() > 0) {
//                // match the closest 5 cases, pick one randomly
//                double[] vectorDists = new double[blockMatchList.size()];
//                for (int j = 0; j < blockMatchList.size(); j++) {
//                    vectorDists[j] = ZMath.distanceEuclidean(
//                            blockMatchList.get(j).getShapeDescriptor().getDescriptorAsDouble(),
//                            empty.getShapeDescriptor().getDescriptorAsDouble()
//                    );
//                }
//                int[] min = ZMath.getMinIndices(vectorDists, 5);
//                int random = min[0];
//                Block best = blockMatchList.get(random);
//                long id = best.getID();
//
//                List<Block> bests = new ArrayList<>();
//                for (int j = 0; j < min.length; j++) {
//                    Block best5s = blockMatchList.get(min[j]);
//                    bests.add(best5s);
//                }
//                best5.add(bests);
//
//                // compare 4 cases: 1.default  2.rotate 180  3.mirror  4.mirror & rotate 180
//                double emptyArea = empty.getArea();
//                double bestArea = best.getArea();
//                double scaleRatio = Math.sqrt(emptyArea / bestArea);
//
//                ZPoint emptyCentroid = new ZPoint(empty.getCentroid());
//                ZPoint bestCentroid = new ZPoint(best.getCentroid());
//                ZPoint moveVec = emptyCentroid.sub(bestCentroid);
//
//                ZPoint emptyAxes = empty.getShapeDescriptor().getAxesNew()[0];
//                ZPoint bestAxes = best.getShapeDescriptor().getAxesNew()[0];
//                double angle = bestAxes.angleWith(emptyAxes);
//                this.bestAxes.add(new ZPoint[]{
//                        bestAxes.rotate2D(Math.PI * (angle / 180)),
//                        best.getShapeDescriptor().getAxesNew()[1].rotate2D(Math.PI * (angle / 180))
//                });
//
//                ZJtsTransform transform1 = new ZJtsTransform();
//                transform1.addScale2D(scaleRatio);
//                transform1.addRotateAboutOrigin2D(Math.PI * (angle / 180));
//                transform1.addTranslate2D(moveVec);
//                Polygon default1 = (Polygon) transform1.applyToGeometry2D(best.getShape());
//
//                ZJtsTransform transform2 = new ZJtsTransform();
//                transform2.addScale2D(scaleRatio);
//                transform2.addRotateAboutOrigin2D(Math.PI * (angle / 180));
//                transform2.addRotateAboutOrigin2D(Math.PI);
//                transform2.addTranslate2D(moveVec);
//                Polygon rotate2 = (Polygon) transform2.applyToGeometry2D(best.getShape());
//
//                ZJtsTransform transform3 = new ZJtsTransform();
//                transform3.addScale2D(scaleRatio);
//                transform3.addRotateAboutOrigin2D(Math.PI * (angle / 180));
//                transform3.addReflect2D(new ZPoint(0, 0), bestAxes);
//                transform3.addTranslate2D(moveVec);
//                Polygon mirror3 = (Polygon) transform3.applyToGeometry2D(best.getShape());
//
//                ZJtsTransform transform4 = new ZJtsTransform();
//                transform4.addScale2D(scaleRatio);
//                transform4.addRotateAboutOrigin2D(Math.PI * (angle / 180));
//                transform4.addRotateAboutOrigin2D(Math.PI);
//                transform4.addRotateAboutOrigin2D(Math.PI);
//                transform4.addTranslate2D(moveVec);
//                Polygon rotateMirror4 = (Polygon) transform4.applyToGeometry2D(best.getShape());
//
//                // compare the most intersection case
//                Polygon emptyShape = empty.getShape();
//                Geometry inter1 = emptyShape.intersection(default1);
//                Geometry inter2 = emptyShape.intersection(rotate2);
//                Geometry inter3 = emptyShape.intersection(mirror3);
//                Geometry inter4 = emptyShape.intersection(rotateMirror4);
//                double[] interRatio = new double[]{
//                        inter1.getArea() / emptyArea,
//                        inter2.getArea() / emptyArea,
//                        inter3.getArea() / emptyArea,
//                        inter4.getArea() / emptyArea
//                };
//                System.out.println(">>> empty block " + i + " intersection ratio: ");
//                System.out.println(Arrays.toString(interRatio));
//                int maxInter = ZMath.getMaxIndex(interRatio);
//
//                ZJtsTransform bestMatchTrans = null;
//                switch (maxInter) {
//                    case 1:
//                        bestMatchTrans = transform2;
//                        break;
//                    case 2:
//                        bestMatchTrans = transform3;
//                        break;
//                    case 3:
//                        bestMatchTrans = transform4;
//                        break;
//                    default:
//                        bestMatchTrans = transform1;
//                        break;
//                }
//
//                Polygon bestMatchResult = (Polygon) bestMatchTrans.applyToGeometry2D(best.getShape());
//                // if the intersection ratio is still lower than standard, rotate to find the best direction
//
//                blockResults.add(bestMatchResult);
//                // transform the building bases
//
//                System.out.println("matched success for empty block " + i);
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("程序运行时间：" + (endTime - startTime) + " ms");
//    }



    /* ------------- setter & getter ------------- */

    public List<BlockEmpty> getBlockEmpties() {
        return blockEmpties;
    }

    public List<Polygon> getBlockResults() {
        return blockResults;
    }

    public List<ZPoint[]> getBestAxes() {
        return bestAxes;
    }

    public List<List<Polygon>> getBuildingResults() {
        return buildingResults;
    }

    public List<Polygon> getEmptiesOrigin() {
        return emptiesOrigin;
    }

    public List<List<Block>> getBest5() {
        return best5;
    }

    /* ------------- draw ------------- */
}
