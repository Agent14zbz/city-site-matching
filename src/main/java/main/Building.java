package main;

import org.locationtech.jts.geom.LineString;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/19
 * @time 15:38
 */
public class Building {
    private long osmid;
    private LineString shape;
    private String name = "";
    private String type;
    private long storey;

    /* ------------- constructor ------------- */

    public Building() {

    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public void setOsmid(long osmid) {
        this.osmid = osmid;
    }

    public void setShape(LineString shape) {
        this.shape = shape;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStorey(long storey) {
        this.storey = storey;
    }

    /* ------------- draw ------------- */

}
