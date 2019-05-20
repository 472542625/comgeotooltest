package geotooltest;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import java.util.ArrayList;
import java.util.List;



/**
 * Hello world!
 *
 */
public class App 
{
    private static GeometryFactory geometryFactory= JTSFactoryFinder.getGeometryFactory(null);

    public static  Point createPoint(String lon, String lat){
        Coordinate coord = new Coordinate(Double.parseDouble(lon), Double.parseDouble(lat));
        Point point = geometryFactory.createPoint( coord );
        return point;
    }
    public static Geometry lonlat2WebMactor(Geometry geom){
        try{
            //这里是以OGC WKT形式定义的是World Mercator投影，网页地图一般使用该投影

//			CoordinateReferenceSystem crsTarget = CRS.parseWKT(strWKTMercator);
            CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:3857");

            // 投影转换
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crsTarget);
            return JTS.transform(geom, transform);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static  Geometry WebMactor2lonlat(Geometry geom){
        try{
            //这里是以OGC WKT形式定义的是World Mercator投影，网页地图一般使用该投影

//			CoordinateReferenceSystem crsTarget = CRS.parseWKT(strWKTMercator);
            CoordinateReferenceSystem crsTarget4326 = CRS.decode("EPSG:4326");
            CoordinateReferenceSystem crsTarget3857 = CRS.decode("EPSG:3857");
            // 投影转换
            MathTransform transform = CRS.findMathTransform(crsTarget3857, DefaultGeographicCRS.WGS84);
            return JTS.transform(geom, transform);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static class XJPoint{
        Double x;
        public XJPoint() {

        }

        public XJPoint(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        Double y;

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }
    }

    /**
     * 输入巡检点数组
     * 首先进行数组中点的缓冲区分析，再对缓冲区结果进行联合分析
     * 输出面积，也可以输出分析后点图形点集合，也可以输出OGC-WKT格式数据
     */
    List<XJPoint> inputPointArray = new ArrayList();
    public  static  Double  point_buffer_union( List<XJPoint> inputPointArray){

        Geometry pointMactor ;
        Geometry buffer_geom ;
        Geometry union_geom =createPoint(inputPointArray.get(0).getX().toString(),inputPointArray.get(0).getY().toString());
        for (int i=0;i<inputPointArray.size();i++){
            Point point = createPoint(inputPointArray.get(i).getX().toString(),inputPointArray.get(i).getY().toString());
            pointMactor = lonlat2WebMactor(point);
            buffer_geom =  pointMactor.buffer(10);
            if(i==0){
                union_geom = buffer_geom;
            }
            if(i>=1){

                union_geom = union_geom.union(buffer_geom);
            }


        }

        List<XJPoint> outputPointArray = new ArrayList();///输出联合分析后的图形点坐标
        XJPoint inputPoint = new XJPoint();
        for(Coordinate coor :WebMactor2lonlat(union_geom).getCoordinates()){
            inputPoint.setX(coor.x);
            inputPoint.setY(coor.y);
            outputPointArray.add(inputPoint);
        }

        Double area = union_geom.getArea();




        return  area;
    }

    /**
     * 测试
     * @param args
     */
    public static void main( String[] args )
    {

      App a1 = new App();
        Point point = a1.createPoint("100.123456","20.123456");
        Geometry pointMactor = a1.lonlat2WebMactor(point);///米单位坐标
        Geometry buffer_geom =  pointMactor.buffer(10);///米单位坐标进行缓冲区分析


        Point point2 = a1.createPoint("100.123486","20.123456");
        Geometry pointMactor2 = a1.lonlat2WebMactor(point2);///米单位坐标
        Geometry buffer_geom2 =  pointMactor2.buffer(10);///米单位坐标进行缓冲区分析


        Geometry union_geom = buffer_geom.union(buffer_geom2);

        System.out.println("墨卡托3857坐标系："+pointMactor.toString());

        System.out.println("WGS84-4326坐标系："+point.toString());

        System.out.println("墨卡托3857坐标系的缓冲区分析结果："+buffer_geom.toString());

        System.out.println("WGS84坐标系的缓冲区分析结果："+a1.WebMactor2lonlat(buffer_geom).toString());

        System.out.println("缓冲区面积"+buffer_geom.getArea());

        System.out.println("WGS84坐标系的union分析结果："+ a1.WebMactor2lonlat(union_geom).toString());


        List<XJPoint> inputPointArray = new ArrayList();
        inputPointArray.add(new XJPoint(100.123456,20.123456));
        inputPointArray.add(new XJPoint(100.123486,20.123456));
//        List<XJPoint> outputPointArray = point_buffer_union(inputPointArray);
//        for (XJPoint p: outputPointArray){
//            System.out.print(p.getX()+" "+p.getY()+" ");
//        }

       // System.out.println("WGS84坐标系的union函数分析结果："+ WebMactor2lonlat(point_buffer_union(inputPointArray)).toString());
        System.out.println("计算面积"+point_buffer_union(inputPointArray).toString());
    }
}
