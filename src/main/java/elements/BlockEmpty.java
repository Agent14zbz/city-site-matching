package elements;

import advancedGeometry.ZShapeDescriptor;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/11/3
 * @time 10:53
 */
public class BlockEmpty {

    private ZShapeDescriptor shapeDescriptor;

    /* ------------- constructor ------------- */

    public BlockEmpty(Polygon polygon) {
        this.shapeDescriptor = new ZShapeDescriptor(polygon);
    }

    public BlockEmpty(WB_Polygon polygon) {
        this.shapeDescriptor = new ZShapeDescriptor(polygon);
    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
