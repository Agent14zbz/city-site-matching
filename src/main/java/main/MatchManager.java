package main;

import basicGeometry.ZPoint;
import database.DBManager;
import elements.Block;
import elements.BlockEmpty;
import elements.Building;
import igeo.ICurve;
import igeo.IG;
import math.ZMath;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import transform.ZJtsTransform;
import transform.ZTransform;

import java.util.ArrayList;
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

    private List<BlockEmpty> blockEmpties;
    private List<Block> blockSamples;

    private List<Polygon> blockResults;
    private List<List<Polygon>> buildingResults;

    /* ------------- constructor ------------- */

    public MatchManager() {

    }

    /* ------------- member function ------------- */

    public void load(DBManager dbManager) {
        this.blockEmpties = new ArrayList<>();
        this.blockSamples = new ArrayList<>();

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
        System.out.println("empty blocks: " + blockEmpties.size());

        // load block samples
//        List<Integer> idList = new ArrayList<>();
//        try {
//            File f = new File("./src/main/resources/validBlocks.txt");
//            BufferedReader reader = new BufferedReader(new FileReader(f));
//            String line = reader.readLine();
//            System.out.println(">>>> start collecting block samples");
//            double cnt = 0;
//
//            while (line != null) {
//                idList.add(Integer.parseInt(line));
//                line = reader.readLine();
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int[] ids = new int[600];
//        for (int i = 0; i < ids.length; i++) {
//            ids[i] = idList.get(i);
//        }
//        this.blockSamples = dbManager.collectBlocks(ids);
//        CityRaw cityRaw = dbManager.collectCity();
//        for (Block block : blockSamples) {
//            block.setCityName(cityRaw.getName());
//
//            long id = block.getID();
//            List<Building> buildingList = dbManager.collectBuildingInBlock((int) id);
//            for (Building building : buildingList) {
//                building.generateAbsShape(block.getCentroidLatLon(), cityRaw.getRatio());
//                building.generateVolume();
//            }
//            block.setBuildings(buildingList);
//
//            block.generateAbsShape(cityRaw.getRatio());
//            block.initProperties();
//        }
//        System.out.println("valid block samples: " + blockSamples.size());
    }

    public void matchTest() {
        this.blockResults = new ArrayList<>();
        this.buildingResults = new ArrayList<>();
        // get all sample feature vectors
        double[][] sampleFeatureVectors = new double[blockSamples.size()][];
        for (int i = 0; i < blockSamples.size(); i++) {
            sampleFeatureVectors[i] = blockSamples.get(i).getShapeDescriptor().getDescriptorAsDouble();
        }

        // match and find best vectors for each empty block
        for (int i = 0; i < blockEmpties.size(); i++) {
            BlockEmpty empty = blockEmpties.get(i);
            double[] shapeFeatureVector = empty.getShapeDescriptor().getDescriptorAsDouble();

            double[] vectorDists = new double[sampleFeatureVectors.length];
            for (int j = 0; j < sampleFeatureVectors.length; j++) {
                vectorDists[j] = ZMath.distanceEuclidean(shapeFeatureVector, sampleFeatureVectors[j]);
            }
            int min = ZMath.getMinIndex(vectorDists);
            Block best = blockSamples.get(min);

            // generate buildings
            // compare 4 cases: default, rotate 180, mirror, mirror & rotate 180
            double emptyArea = empty.getArea();
            double bestArea = best.getArea();
            double scaleRatio = Math.sqrt(emptyArea / bestArea);

            ZPoint emptyCentroid = new ZPoint(empty.getCentroid());
            ZPoint bestCentroid = new ZPoint(best.getCentroid());
            ZPoint moveVec = emptyCentroid.sub(bestCentroid);

            ZPoint emptyAxes = empty.getShapeDescriptor().getAxes()[0];
            ZPoint bestAxes = best.getShapeDescriptor().getAxes()[0];
            double angle = bestAxes.angleWith(emptyAxes);

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
            double[] interArea = new double[]{
                    inter1.getArea(), inter2.getArea(), inter3.getArea(), inter4.getArea()
            };
            int maxInter = ZMath.getMaxIndex(interArea);

            ZJtsTransform bestMatchTrans = null;
            switch (maxInter) {
                case 1:
                    bestMatchTrans = transform2;
                    break;
                case 2:
                    bestMatchTrans = transform3;
                    break;
                case 3:
                    bestMatchTrans = transform4;
                    break;
                default:
                    bestMatchTrans = transform1;
                    break;
            }

            blockResults.add((Polygon) bestMatchTrans.applyToGeometry2D(best.getShape()));
            for (Building b : best.getBuildings()) {
//                List<Polygon> faces = b.transform(bestMatchTrans.duplicate2DTo3D());
//                buildingResults.add(faces);
            }

            System.out.println("matched success for empty block " + i);

        }
    }

    public List<Building> generateBuildings(long bestBlockID, DBManager manager) {
        // extract buildings data from the best-match case
        List<Building> buildings = new ArrayList<>();

        return buildings;
    }

    /* ------------- setter & getter ------------- */

    public List<BlockEmpty> getBlockEmpties() {
        return blockEmpties;
    }

    public List<Polygon> getBlockResults() {
        return blockResults;
    }

    public List<List<Polygon>> getBuildingResults() {
        return buildingResults;
    }

    /* ------------- draw ------------- */
}
