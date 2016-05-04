package com.zzu.string.baidumap;


import android.graphics.Bitmap;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

/**
 * 历史轨迹数据
 * Created by songchao on 16/4/28.
 */
public class HistoryTrackData {
    public int status; // 状态码，0为成功
    public int size; // 返回结果条数，该页返回了几条数据
    public int total; // 符合条件结果条数，一共有几条符合条件的数据
    public String entity_name;// 返回的entity标识
    public List<Points> points;
    public String message; // 响应信息,对status的中文描述

    public class Points {
        public String loc_time;// 该track实时点的上传时间 UNIX时间戳 该时间为用户上传的时间
        public List<Double> location;// 经纬度 Array 百度加密坐标
        public String create_time;// 创建时间 格式化时间 该时间为服务端时间
        public String device_id;// 设备编号 string， 当service的type是2和4，且为该属性赋过值才会返回
        public double radius;// GPS定位精度 当service的type是1，2，3，4，且创建该track的时候输入了这个字段才会返回。
        public double realtime_poiid;
        public int direction; // GPS方向 当service的type是1，且创建该track的时候输入了这个字段才会返回。
        public double speed;// GPS速度当service的type是1，且创建该track的时候输入了这个字段才会返回。

        public String getLoc_time() {
            return loc_time;
        }

        public void setLoc_time(String loc_time) {
            this.loc_time = loc_time;
        }

        public List<Double> getLocation() {
            return location;
        }

        public void setLocation(List<Double> location) {
            this.location = location;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public double getRealtime_poiid() {
            return realtime_poiid;
        }

        public void setRealtime_poiid(double realtime_poiid) {
            this.realtime_poiid = realtime_poiid;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

    }

    /**
     * 从这里对返回的地址点进行处理
     *
     * @return
     */

    public List<LatLng> getListPoints() {
        List<LatLng> list = new ArrayList<LatLng>();

        if (points == null || points.size() == 0) {
            return null;

        }
        Iterator<Points> it = points.iterator();
        Points prePoint = null;
        List<Double> preLocation = null;

        while (it.hasNext()) {
            Boolean isOk = false;
            Points nextPoint = (Points) it.next();
            List<Double> location = nextPoint.getLocation();
            Double speed;
            if (Math.abs(location.get(0) - 0.0) >= 0.01 && Math.abs(location.get(1) - 0.0) >= 0.01) {
                LatLng latLng = new LatLng(location.get(1), location.get(0));
                if (prePoint != null) {
                    //计算开始
                    Integer preTime = Integer.valueOf(prePoint.getLoc_time());
                    Integer nextTime = Integer.valueOf(nextPoint.getLoc_time());
                    int time = preTime - nextTime;
                    preLocation = prePoint.getLocation();
                    LatLng preLatLng = new LatLng(preLocation.get(1),preLocation.get(0));

                    Double distance = DistanceUtil.getDistance(preLatLng,latLng);
                    speed = distance / time;
                    if (speed < MainActivity.SPEED) {
                        isOk = true;
                    } else {
                        isOk = false;
                    }
                }else{
                    list.add(latLng);
                    //计算完毕赋值
                    prePoint = nextPoint;
                }
                //----------------------------------------对点进行过滤，在相应的行驶速度下跑的太远的点将被抛弃
                if(isOk) {
                    list.add(latLng);
                    //计算完毕赋值
                    prePoint = nextPoint;
                }
            }
        }
        return list;

    }


    /*public static final double EARTH_RADIUS = 6378.137;//地球半径

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }*/

    /*public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }*/

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HistoryTrackData [status=" + status + ", size=" + size + ", total=" + total + ", entity_name="
                + entity_name + ", points =" + points + ", message=" + message + "]";
    }

}
