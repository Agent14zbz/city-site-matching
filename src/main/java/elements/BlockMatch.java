package elements;

import org.apache.commons.lang3.ArrayUtils;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/11/15
 * @time 15:29
 */
public class BlockMatch {
    private long id;
    private double[] shapeDescriptor;
    private double[] axes;

    public BlockMatch() {

    }

    public void setId(long id) {
        this.id = id;
    }

    public void setShapeDescriptor(Double[] shapeDescriptor) {
        this.shapeDescriptor = ArrayUtils.toPrimitive(shapeDescriptor);
    }

    public void setAxes(Double[] axes) {
        this.axes = ArrayUtils.toPrimitive(axes);
    }

    public long getId() {
        return id;
    }

    public double[] getShapeDescriptor() {
        return shapeDescriptor;
    }

    public double[] getAxes() {
        return axes;
    }
}
