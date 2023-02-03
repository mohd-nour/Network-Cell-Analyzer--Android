package com.example.netcellanalyzer;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.callback.OnPieSelectListener;
import com.razerdp.widget.animatedpieview.data.IPieInfo;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    TextView operator;
    TextView snrtext;
    TextView frequency;
    TextView cell;
    TextView time;
    TextView textNetworkStrength;
    TextView NetType;
    TextView Test;
    Button sync;

    DatabaseHelper databaseHelper;
    Spinner spinner;
    Spinner spinner2;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        operator = findViewById(R.id.opEdit);
        snrtext = findViewById(R.id.snrEdit);
        frequency = findViewById(R.id.freqEdit);
        cell = findViewById(R.id.cellidEdit);
        time = findViewById(R.id.timeEdit);
        sync = findViewById(R.id.syncButton);
        textNetworkStrength = findViewById(R.id.textView11);
        NetType = findViewById(R.id.textView12);
        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        loadSpinnerData();

        sync.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String date1 = spinner.getSelectedItem().toString();
                String date2 = spinner2.getSelectedItem().toString();
                float alfaPercent = databaseHelper.alfaPercent(date1,date2);
                float touchPercent = 100 - alfaPercent;
                float twog = databaseHelper.twogPercent(date1,date2);
                float threeg = databaseHelper.threegPercent(date1,date2);
                float fourg = 100 - (threeg+twog);
                float twoSignal = databaseHelper.twogSigPower(date1,date2);
                float threeSignal = databaseHelper.threegSigPower(date1,date2);
                float fourSignal = databaseHelper.fourgSigPower(date1,date2);
                float avgsnr = databaseHelper.averageSnr(date1,date2);
                drawPie(alfaPercent,touchPercent);
                drawPie2(twog,threeg,fourg);
                drawPie3(avgsnr);
                drawPie4(twoSignal,threeSignal,fourSignal);
            }
        });


        databaseHelper = new DatabaseHelper( MainActivity.this);
        if (CheckNetworkConnectivity() && !CheckNetworkWifi()) {

            NetType.setText(GetNetworkType());
            operator.setText(GetNetworkOperator());
            GetCellInfo();

            Thread t=new Thread(){


                @Override
                public void run(){

                    while(!isInterrupted()){

                        try {
                            Thread.sleep(20000);  //1000ms = 1 sec

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    GetCellInfo();
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };

            t.start();

        } else {
            //Type you are not connected to Mobile Network
            Toast toast = Toast.makeText(getApplicationContext(),
                    "You are not connected to Mobile Network",
                    Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    public void drawPie(float p1,float p2) {
        AnimatedPieView mAnimatedPieView = findViewById(R.id.animatedPieView);
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(p1, Color.parseColor("#58508d"), "Alfa")) //Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(p2, Color.parseColor("#bc5090"), "Touch")).drawText(true)
                .duration(1200).textSize(40).pieRadius(150);// draw pie animation duration
        config.selectListener(new OnPieSelectListener<IPieInfo>() {
            @Override
            public void onSelectPie(@NonNull IPieInfo pieInfo, boolean isFloatUp) {
                Toast.makeText(MainActivity.this, pieInfo.getDesc() + " - " + pieInfo.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

// The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();

    }

    public void drawPie2(float g2 , float g3, float g4) {
        AnimatedPieView mAnimatedPieView = findViewById(R.id.animatedPieView2);
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(g4, Color.parseColor("#58508d"), "4G"))
                .addData(new SimplePieInfo(g3, Color.parseColor("#bc5090"), "3G")) //Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(g2, Color.parseColor("#ff6361"), "2G")).drawText(true)
                .duration(1200).textSize(40).pieRadius(150);// draw pie animation duration
        config.selectListener(new OnPieSelectListener<IPieInfo>() {
            @Override
            public void onSelectPie(@NonNull IPieInfo pieInfo, boolean isFloatUp) {
                Toast.makeText(MainActivity.this, pieInfo.getDesc() + " - " + pieInfo.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

// The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();

    }

    public void drawPie3(float avg) {
        AnimatedPieView mAnimatedPieView = findViewById(R.id.animatedPieView3);
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(avg, Color.parseColor("#58508d"), "4G")).drawText(true)
                .duration(1200).textSize(40).pieRadius(150);// draw pie animation duration
        config.selectListener(new OnPieSelectListener<IPieInfo>() {
            @Override
            public void onSelectPie(@NonNull IPieInfo pieInfo, boolean isFloatUp) {
                Toast.makeText(MainActivity.this, pieInfo.getDesc() + " - " + pieInfo.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

// The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();

    }
    public void drawPie4(float two, float three ,float four) {
        AnimatedPieView mAnimatedPieView = findViewById(R.id.animatedPieView4);
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(two, Color.parseColor("#58508d"), "4G"))
                .addData(new SimplePieInfo(three, Color.parseColor("#bc5090"), "3G")) //Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(four, Color.parseColor("#ff6361"), "2G")).drawText(true)
                .duration(1200).textSize(40).pieRadius(150);// draw pie animation duration
        config.selectListener(new OnPieSelectListener<IPieInfo>() {
            @Override
            public void onSelectPie(@NonNull IPieInfo pieInfo, boolean isFloatUp) {
                Toast.makeText(MainActivity.this, pieInfo.getDesc() + " - " + pieInfo.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

// The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();

    }

//Checks if connected to any network or not

    public Boolean CheckNetworkConnectivity() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        Boolean bool = Objects.requireNonNull(manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).isConnectedOrConnecting();

        return bool;
    }
    //Checks if connected to WIFI

    public Boolean CheckNetworkWifi() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        Boolean bool = Objects.requireNonNull(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).isConnectedOrConnecting();

        return bool;
    }
//retrieves Network Operator Name

    public String GetNetworkOperator() {
        TelephonyManager manager = (TelephonyManager) getSystemService(TelephonyManager.class);
        String carrierName = Objects.requireNonNull(manager.getNetworkOperatorName());
        return carrierName;
    }
    //retrieves Network Type Name
    public String GetNetworkType() {
        TelephonyManager telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
        }
        return "New type of network";
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)

//retrieves All Cell Related Information
    public void GetCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
            }
        };

        TelephonyManager.CellInfoCallback callback = null;
        //to work on Different APIs
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            callback = new TelephonyManager.CellInfoCallback() {
                @Override
                public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
                }
            };
        }
        telephonyManager.requestCellInfoUpdate(executor, callback);
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo(); //List including all CellInfo retrieved

        Log.d("List", String.valueOf(cellInfoList)); //for debugging, can be deleted

        //If list is not empty
        if (cellInfoList != null ) {

            for (int i = 0; i < cellInfoList.size(); i++) { //retrieve all cellinfolist data

                //LTE Cell Identity
                if (cellInfoList.get(i) instanceof CellInfoLte && cellInfoList.get(i).isRegistered()) {
                    CellSignalStrengthLte lte = ((CellInfoLte) cellInfoList.get(i)).getCellSignalStrength();
                    CellInfoLte cellInfoLt = (CellInfoLte) cellInfoList.get(i);
                    CellIdentityLte cellIdentityLte = cellInfoLt.getCellIdentity();

                    int mCid = cellIdentityLte.getCi();
                    int bw = cellInfoLt.getCellIdentity().getBandwidth();
                    int snr = lte.getRssnr();
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    int NetworkStrength = lte.getDbm();

                    snrtext.setText(String.valueOf(snr));
                    textNetworkStrength.setText(String.valueOf(NetworkStrength) + " dB");
                    frequency.setText(String.valueOf((bw)));
                    time.setText(date);
                    cell.setText(String.valueOf(mCid));
                    // log to database
                    databaseHelper.logData(GetNetworkOperator(),NetworkStrength,snr,GetNetworkType(),bw,mCid,date);
                    loadSpinnerData();

//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "You are connected to LTE",
//                            Toast.LENGTH_SHORT);
//                    toast.show();
//
//                    Log.d("SNR", String.valueOf(snr));
//                    Log.d("CID", String.valueOf(mCid));
//                    //debugging

                }
                //GSM Cell Identity
                else if (cellInfoList.get(i) instanceof CellInfoGsm && cellInfoList.get(i).isRegistered()) {
                    CellSignalStrengthGsm gsm = ((CellInfoGsm) cellInfoList.get(i)).getCellSignalStrength();
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfoList.get(i);

                    int NetworkStrength = gsm.getDbm(); //return signal strength in Db
                    int mCid = cellInfoGsm.getCellIdentity().getCid();

                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    snrtext.setText(String.valueOf("NONE"));
                    textNetworkStrength.setText(String.valueOf(NetworkStrength));
                    frequency.setText(String.valueOf(("NONE")));
                    time.setText(date);
                    cell.setText(String.valueOf(mCid));
                    // log to database
                    databaseHelper.logData(GetNetworkOperator(),NetworkStrength,100,GetNetworkType(),-1,mCid,date);
                    loadSpinnerData();


////debugging
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "You are connected to GSM",
//                            Toast.LENGTH_SHORT);
//                    toast.show();

                }
                //CDMA Cell Identity
                else if (cellInfoList.get(i) instanceof CellInfoCdma && cellInfoList.get(i).isRegistered()) {
                    CellSignalStrengthCdma cdma = ((CellInfoCdma) cellInfoList.get(i)).getCellSignalStrength();
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfoList.get(i);


                    int mCid = cellInfoCdma.getCellIdentity().getBasestationId();
                    int NetworkStrength = cdma.getCdmaDbm(); //return signal strength in Db
                    int snr = cdma.getEvdoSnr();
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    snrtext.setText(String.valueOf(snr));
                    textNetworkStrength.setText(String.valueOf(NetworkStrength));
                    frequency.setText(String.valueOf(("NONE")));
                    time.setText(date);
                    cell.setText(String.valueOf(mCid));
                    // log to database
                    databaseHelper.logData(GetNetworkOperator(),NetworkStrength,snr,GetNetworkType(),-1,mCid,date);
                    loadSpinnerData();
////debugging
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "You are connected to CDMA",
//                            Toast.LENGTH_SHORT);
//                    toast.show();
                }
                //WCDMA Cell Identity
                else if (cellInfoList.get(i) instanceof CellInfoWcdma && cellInfoList.get(i).isRegistered()) {
                    CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) cellInfoList.get(i)).getCellSignalStrength();
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfoList.get(i);

                    int mCid = cellInfoWcdma.getCellIdentity().getCid();
                    int NetworkStrength =  wcdma.getDbm(); //return signal strength in Db
                    //Freq Band = UARFCNx0.2 + Offset
                    //https://www.rfwireless-world.com/Terminology/UMTS-UARFCN-to-frequency-conversion.html
                    int UARFCN = cellInfoWcdma.getCellIdentity().getUarfcn();
                    float bw = (float) (UARFCN*0.2);
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    snrtext.setText(String.valueOf("None"));
                    textNetworkStrength.setText(String.valueOf(NetworkStrength));
                    frequency.setText(String.valueOf((bw)));
                    time.setText(date);
                    cell.setText(String.valueOf(mCid));
                    // log to database
                    databaseHelper.logData(GetNetworkOperator(),NetworkStrength,100,GetNetworkType(),(int)bw,mCid,date);
                    loadSpinnerData();
////debugging
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "You are connected to WCDMA",
//                            Toast.LENGTH_SHORT);
                }
                //TD-SCDMA Cell Identity
                else if (cellInfoList.get(i) instanceof CellInfoTdscdma) {
                        CellSignalStrengthTdscdma tdscdma = ((CellInfoTdscdma) cellInfoList.get(i)).getCellSignalStrength();
                        CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfoList.get(i);

                        int mCid = cellInfoTdscdma.getCellIdentity().getCid();
                        //Freq Band = UARFCNx0.2 + Offset
                        //https://www.rfwireless-world.com/Terminology/UMTS-UARFCN-to-frequency-conversion.html
                        int UARFCN = cellInfoTdscdma.getCellIdentity().getUarfcn();
                        float bw = (float) (UARFCN * 0.2); //bandwidth using UAFCN to freq method
                        int NetworkStrength = tdscdma.getDbm(); //return signal strength in Db
                        // SINR Power is not found
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        snrtext.setText(String.valueOf("NONE"));
                        textNetworkStrength.setText(String.valueOf(NetworkStrength));
                        frequency.setText(String.valueOf((bw)));
                        time.setText(date);
                        cell.setText(String.valueOf(mCid));
                        // log to database
                        databaseHelper.logData(GetNetworkOperator(),NetworkStrength,100,GetNetworkType(),(int)bw,mCid,date);
                        loadSpinnerData();
////debugging
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "You are connected to TD-SCDMA",
//                            Toast.LENGTH_SHORT);
                }
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No Data to Display",
                    Toast.LENGTH_SHORT);
            toast.show();

        }

    }
    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData() {
        // database handler
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getAllLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner2.setAdapter(dataAdapter);
    }


}
