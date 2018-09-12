package com.chart.chartline.model;

import android.graphics.PointF;

/**
 * 低价图
 * <p>
 * Created by yakui.liu on 2018/7/21
 */
public class LowPriceResp {

    //PriceList里面的内容
    //日期 格式 MM-dd
    public String date;

    //价格", example = ￥890
    public String price;

    public PointF pointF;

}
