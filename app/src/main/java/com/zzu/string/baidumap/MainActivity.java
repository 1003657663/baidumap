package com.zzu.string.baidumap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;
import com.baidu.trace.OnGeoFenceListener;
import com.baidu.trace.OnEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    final long SERVICE_ID = 115498;//-百度地图开发者的服务号id
    LBSTraceClient client;
    Trace trace;
    TextView state;
    EditText speedEdit;
    MapView mMapView;//view，主视图显示，和textview，imageView差不多
    BaiduMap mBaiduMap;
    OnTrackListener onTrackListener;
    String entityName;//不同路线的唯一标识符
    AsyncTask queryHisTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //实例化地图必须在setContentView之前
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        state = (TextView) findViewById(R.id.start_state);
        speedEdit = (EditText) findViewById(R.id.start_speed);

        //实例化轨迹服务客户端
        client = new LBSTraceClient(getApplicationContext());
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //鹰眼服务ID
        long serviceId = SERVICE_ID;
        //entity标识
        entityName = "test2";
        //轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
        final int traceType = 2;
        //实例化轨迹服务
        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);
        //实例化开启轨迹服务回调接口
        final OnStartTraceListener startTraceListener = new OnStartTraceListener() {
            //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {

            }

            //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {

            }
        };

        onTrackListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {

            }

            @Override
            public Map onTrackAttrCallback() {
                //设置自定信息，比如汽车油量等等
                return super.onTrackAttrCallback();
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                System.out.println(s);
                showHistoryTrack(s);
            }
        };

        //开启轨迹服务
        client.startTrace(trace, startTraceListener);
        client.setOnTrackListener(onTrackListener);
        //位置采集周期
        int gatherInterval = 10;
        //打包周期
        int packInterval = 60;
        //设置位置采集和打包周期
        client.setInterval(gatherInterval, packInterval);
        // 设置协议类型，0为http，1为https
        int protocoType = 0;
        client.setProtocolType(protocoType);


        queryHisTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                while (true){
                    publishProgress();
                    try {
                        Thread.sleep(10000);//10秒钟获取一次位置信息
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                String sp = speedEdit.getText().toString();
                if(sp.equals("")){
                    sp = "5";
                }
                SPEED = Float.valueOf(sp);
                queryProcessedHistoryTrack();
            }
        };
        queryHisTask.execute();
    }


    public static float SPEED = 5;



    private void searchLocation() {
        //鹰眼服务ID
        long serviceId =SERVICE_ID;
        //entity标识列表（多个entityName，以英文逗号"," 分割）
        String entityNames = "test2";
        //检索条件（格式为 : "key1=value1,key2=value2,....."）
        String columnKey = "car_team=1";
        //返回结果的类型（0 : 返回全部结果，1 : 只返回entityName的列表）
        int returnType = 0;
        //活跃时间，UNIX时间戳（指定该字段时，返回从该时间点之后仍有位置变动的entity的实时点集合）
        int activeTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        //Entity监听器
        OnEntityListener entityListener = new OnEntityListener() {
            // 查询失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                System.out.println("entity请求失败回调接口消息 : " + arg0);
            }

            // 查询entity回调接口，返回查询结果列表
            @Override
            public void onQueryEntityListCallback(String arg0) {
                //System.out.println("entity回调接口消息 : " + arg0);
                state.setText(arg0);
            }
        };

        //查询实时轨迹
        client.queryEntityList(serviceId, entityNames, columnKey, returnType, activeTime, pageSize,
                pageIndex, entityListener);
    }



    /**
     * 显示历史轨迹
     *
     * @param
     */
    public void showHistoryTrack(String historyTrack) {
        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack,
                HistoryTrackData.class);
        List<LatLng> latLngList = new ArrayList<LatLng>();

        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }
            // 绘制历史轨迹
            drawHistoryTrack(latLngList);

        }

    }


    /**
     * 查询历史轨迹
     */
    public void queryHistoryTrack() {

        // entity标识
        String entityName = this.entityName;
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 开始时间
        int startTime = 0;
        int endTime = 0;
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        client.queryHistoryTrack(SERVICE_ID, entityName, simpleReturn, startTime, endTime, pageSize, pageIndex, onTrackListener);
    }

    /**
     * 查询纠偏后的轨迹
     */
    public void queryProcessedHistoryTrack(){
        String entityName = this.entityName;
        int simpleReturn = 0;
        int isProcessed = 1;
        int startTime = (int) (System.currentTimeMillis() / 1000 - 24 * 60 * 60);
        int endTime = (int) (System.currentTimeMillis() / 1000);
        int pageSize = 1000;
        int pageIndex = 1;
        client.queryProcessedHistoryTrack(SERVICE_ID,entityName,simpleReturn,isProcessed,startTime,endTime,pageSize,pageIndex,onTrackListener);
    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    MapStatusUpdate msUpdate;
    MapStatusUpdate nextUpdate;
    PolylineOptions polyline;
    BitmapDescriptor bmStart,bmEnd;
    MarkerOptions startMarker,endMarker;
    public void drawHistoryTrack(final List<LatLng> points) {
        // 绘制新覆盖物前，清空之前的覆盖物
        mBaiduMap.clear();
        MapStatus preMapStatus = mBaiduMap.getMapStatus();
        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(this, "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
            Looper.loop();
            //resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);

            //起点和终点图标
            if(bmStart ==null) {
                Bitmap bitmap = getMapToastBitmap("起点",getResources().getDrawable(R.mipmap.storehouse));
                bmStart = BitmapDescriptorFactory.fromBitmap(bitmap);
            }
            if(bmEnd == null){
                Bitmap bitmap = getMapToastBitmap("终点",getResources().getDrawable(R.mipmap.temphouse));
                bmEnd = BitmapDescriptorFactory.fromBitmap(bitmap);
            }

            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加终点图标
            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.BLUE).points(points);

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.overlook(preMapStatus.overlook);
            builder.rotate(preMapStatus.rotate);
            builder.target(preMapStatus.target);
            builder.targetScreen(preMapStatus.targetScreen);
            builder.zoom(preMapStatus.zoom);
            MapStatus mapStatus = builder.build();
            //new MapStatus(preMapStatus.rotate,preMapStatus.target,preMapStatus.overlook,preMapStatus.zoom,preMapStatus.targetScreen,bounds);
            nextUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);

            addMarker();

        }

    }

    /**
     * 获取起始地点的view转换的图片
     */
    private Bitmap getMapToastBitmap(String text, Drawable image){
        View view = View.inflate(this,R.layout.map_toast,null);
        ((TextView)view.findViewById(R.id.map_toast_text)).setText(text);
        ((ImageView)view.findViewById(R.id.map_toast_image)).setImageDrawable(image);
        return MapToastView.getViewBitmap(view);
    }



    /**
     * 添加覆盖物
     */
    Boolean hasSetBound = false;
    public void addMarker() {
        if (null != msUpdate) {
            if(!hasSetBound) {
                mBaiduMap.setMapStatus(msUpdate);
                hasSetBound = true;
            }else {
                mBaiduMap.setMapStatus(nextUpdate);
            }
        }

        if (null != startMarker) {
            mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        //实例化停止轨迹服务回调接口
        OnStopTraceListener stopTraceListener = new OnStopTraceListener() {
            // 轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
            }

            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
            }
        };

        //停止轨迹服务
        client.stopTrace(trace, stopTraceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(queryHisTask.getStatus() == AsyncTask.Status.FINISHED) {
            queryHisTask.execute();
        }
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(queryHisTask !=null && queryHisTask.getStatus() != AsyncTask.Status.FINISHED) {
            queryHisTask.cancel(true);
        }
        mMapView.onPause();

    }
}
