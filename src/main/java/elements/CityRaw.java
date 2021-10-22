package elements;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/22
 * @time 14:45
 */
public class CityRaw {
    private String name;
    private double ratio;

    /* ------------- constructor ------------- */

    public CityRaw(){

    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public void setName(String name) {
        this.name = name;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public String getName() {
        return name;
    }

    public double getRatio() {
        return ratio;
    }
}
