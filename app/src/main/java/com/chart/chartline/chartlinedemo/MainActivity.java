package com.chart.chartline.chartlinedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.chart.chartline.model.LowPriceResp;
import com.chart.chartline.utils.Utils;
import com.chart.chartline.widget.DrawBezierView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements DrawBezierView.OnTouchSelectListener {
    private DrawBezierView mDrawBezierView;
    private TextView show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        show = findViewById(R.id.show);
        mDrawBezierView = findViewById(R.id.bezier_view);
        mDrawBezierView.setOnTouchSelectListener(this);
        init();
    }

    private void init() {
        final ArrayList<LowPriceResp> resps = builderData();
        mDrawBezierView.post(new Runnable() {
            @Override
            public void run() {
                mDrawBezierView.setData(resps, getRealMinPrice(getMinPrice(resps)), getRealMaxPrice(getMaxPrice(resps)), 5);
                mDrawBezierView.startAnimation(2000);
            }
        });
    }

    private ArrayList<LowPriceResp> builderData() {
        final ArrayList<LowPriceResp> resps = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            final LowPriceResp resp = new LowPriceResp();
            final Random random = new Random();
            int r = random.nextInt(500);
            resp.price = String.valueOf(Math.max(120, r));
            resps.add(resp);
        }
        return resps;
    }

    //获得最大价格（小于等于0的数不参与计算）
    protected int getMaxPrice(ArrayList<LowPriceResp> items) {
        int price = 0;
        for (LowPriceResp t : items) {
            int dataPrice = Utils.parseInt(t.price, 0);
            if (dataPrice > price) {
                price = dataPrice;
            }
        }
        return price;
    }

    //获得最小价格（小于等于0的数不参与计算）
    protected int getMinPrice(ArrayList<LowPriceResp> items) {
        int price = 0;
        for (LowPriceResp t : items) {
            int tPrice = Utils.parseInt(t.price, 0);
            if (price == 0 && tPrice > 0) {
                price = tPrice;
            }
            if (tPrice < price && tPrice > 0) {
                price = tPrice;
            }
        }
        return price;
    }

    //最小值向下取整50的倍数
    private int getRealMinPrice(int price) {
        int t = 50;
        if (price % t == 0) {
            return price;
        }
        int p = price / t;
        return p * t;
    }

    //最大值向上取整100的倍数
    private int getRealMaxPrice(int price) {
        int t = 100;
        if (price % t == 0) {
            return price;
        }
        int p = price / t + 1;
        return p * t;
    }

    @Override
    public void clickPoint(LowPriceResp resp) {
        show.setText(resp.price);
    }
}
