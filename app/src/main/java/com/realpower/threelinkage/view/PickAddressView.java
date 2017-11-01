package com.realpower.threelinkage.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.realpower.threelinkage.R;
import com.realpower.threelinkage.bean.CityInfoModel;
import com.realpower.threelinkage.bean.DistrictInfoModel;
import com.realpower.threelinkage.bean.ProvinceInfoModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Administrator on 2017/10/30.
 */

public class PickAddressView extends LinearLayout {
    /**
     * 与选择地址相关
     */
    protected ArrayList<String> mProvinceDatas;
    protected Map<String, ArrayList<String>> mCitisDatasMap = new HashMap<String, ArrayList<String>>();
    protected Map<String, ArrayList<String>> mDistrictDatasMap = new HashMap<String, ArrayList<String>>();

    private WheelView mProvincePicker;
    private WheelView mCityPicker;
    private WheelView mCountyPicker;

    protected String mCurrentProviceName;
    protected String mCurrentCityName;
    protected String mCurrentDistrictName;
    private TextView cancel, ok;
    protected boolean isDataLoaded = false;
    private Context context;
    private PickAddressInterface pickAddressInterface;

    public PickAddressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        LayoutInflater.from(context).inflate(R.layout.address, this);
        this.context = context;
        initData();
    }

    public PickAddressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void initData() {
        mProvincePicker = (WheelView) findViewById(R.id.province);
        mCityPicker = (WheelView) findViewById(R.id.city);
        mCountyPicker = (WheelView) findViewById(R.id.county);
        cancel = (TextView) findViewById(R.id.box_cancel);
        ok = (TextView) findViewById(R.id.box_ok);


        mProvincePicker.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                String provinceText = mProvinceDatas.get(id);
                if (!mCurrentProviceName.equals(provinceText)) {
                    mCurrentProviceName = provinceText;
                    ArrayList<String> mCityData = mCitisDatasMap.get(mCurrentProviceName);
                    mCityPicker.resetData(mCityData);
                    mCityPicker.setDefault(0);
                    mCurrentCityName = mCityData.get(0);

                    ArrayList<String> mDistrictData = mDistrictDatasMap.get(mCurrentCityName);
                    mCountyPicker.resetData(mDistrictData);
                    mCountyPicker.setDefault(0);
                    mCurrentDistrictName = mDistrictData.get(0);
                }
            }

            @Override
            public void selecting(int id, String text) {
            }
        });

        mCityPicker.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                ArrayList<String> mCityData = mCitisDatasMap.get(mCurrentProviceName);
                String cityText = mCityData.get(id);
                if (!mCurrentCityName.equals(cityText)) {
                    mCurrentCityName = cityText;
                    ArrayList<String> mCountyData = mDistrictDatasMap.get(mCurrentCityName);
                    mCountyPicker.resetData(mCountyData);
                    mCountyPicker.setDefault(0);
                    mCurrentDistrictName = mCountyData.get(0);
                }
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        mCountyPicker.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                ArrayList<String> mDistrictData = mDistrictDatasMap.get(mCurrentCityName);
                String districtText = mDistrictData.get(id);
                if (!mCurrentDistrictName.equals(districtText)) {
                    mCurrentDistrictName = districtText;
                }
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pickAddressInterface != null) {
                    pickAddressInterface.onOkClick(mProvinceDatas.get(mProvincePicker.getSelected())
                            + mCityPicker.getData().get(mCityPicker.getSelected())
                            + mCountyPicker.getData().get(mCountyPicker.getSelected()));
                }
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pickAddressInterface != null) {
                    pickAddressInterface.onCancelClick();
                }
            }
        });

        // 启动线程读取数据
        new Thread() {
            @Override
            public void run() {
                // 读取数据
                isDataLoaded = readAddrDatas();
                // 通知界面线程
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mProvincePicker.setData(mProvinceDatas);
                        mProvincePicker.setDefault(0);
                        mCurrentProviceName = mProvinceDatas.get(0);

                        ArrayList<String> mCityData = mCitisDatasMap.get(mCurrentProviceName);
                        mCityPicker.setData(mCityData);
                        mCityPicker.setDefault(0);
                        mCurrentCityName = mCityData.get(0);

                        ArrayList<String> mDistrictData = mDistrictDatasMap.get(mCurrentCityName);
                        mCountyPicker.setData(mDistrictData);
                        mCountyPicker.setDefault(0);
                        mCurrentDistrictName = mDistrictData.get(0);
                    }
                }, 200);
            }
        }.start();

    }

    /**
     * 读取地址数据，请使用线程进行调用
     *
     * @return
     */
    protected boolean readAddrDatas() {
        List<ProvinceInfoModel> provinceList = null;
        AssetManager asset = context.getAssets();
        try {
            InputStream input = asset.open("province_data.xml");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser parser = spf.newSAXParser();
            AddrXmlParser handler = new AddrXmlParser();
            Log.e("aaa", "开始解析时间 " + System.currentTimeMillis());
            parser.parse(input, handler);
            input.close();
            Log.e("aaa", "结束解析时间 " + System.currentTimeMillis());
            provinceList = handler.getDataList();
            if (provinceList != null && !provinceList.isEmpty()) {
                mCurrentProviceName = provinceList.get(0).getName();
                List<CityInfoModel> cityList = provinceList.get(0).getCityList();
                if (cityList != null && !cityList.isEmpty()) {
                    mCurrentCityName = cityList.get(0).getName();
                    List<DistrictInfoModel> districtList = cityList.get(0).getDistrictList();
                    mCurrentDistrictName = districtList.get(0).getName();
                }
            }
            mProvinceDatas = new ArrayList<String>();
            for (int i = 0; i < provinceList.size(); i++) {
                mProvinceDatas.add(provinceList.get(i).getName());
                List<CityInfoModel> cityList = provinceList.get(i).getCityList();
                ArrayList<String> cityNames = new ArrayList<String>();
                for (int j = 0; j < cityList.size(); j++) {
                    cityNames.add(cityList.get(j).getName());
                    List<DistrictInfoModel> districtList = cityList.get(j).getDistrictList();
                    ArrayList<String> distrinctNameArray = new ArrayList<String>();
                    DistrictInfoModel[] distrinctArray = new DistrictInfoModel[districtList.size()];
                    for (int k = 0; k < districtList.size(); k++) {
                        DistrictInfoModel districtModel = new DistrictInfoModel(districtList.get(k).getName(), districtList.get(k).getZipcode());
                        distrinctArray[k] = districtModel;
                        distrinctNameArray.add(districtModel.getName());
                    }
                    mDistrictDatasMap.put(cityNames.get(j), distrinctNameArray);
                }
                mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
            }
            Toast.makeText(context, "读取结束", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setOnTopClicklistener(PickAddressInterface pickAddressInterface) {
        this.pickAddressInterface = pickAddressInterface;
    }

}
