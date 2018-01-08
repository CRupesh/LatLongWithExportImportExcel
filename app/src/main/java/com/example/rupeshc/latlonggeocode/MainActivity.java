package com.example.rupeshc.latlonggeocode;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Button btnGetLatLng,btnExportExcel,btnReadExcel;
    EditText edt_address;
    TextView txt_Lat,txt_Long;
    String asyncFlag="";

    Observable<ArrayList<String>> obsLatLong;
    Observer<ArrayList<String>> mObserver;

    HashMap<String,String> mCellValue;
    ArrayList<String> mColvalue;
    ArrayList<String> arrLatLong = new ArrayList<String>();

    private ProgressDialog dialog;

    public static final int PERMISSIONS_CODE = 1;
    private DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DBHandler.getInstance(MainActivity.this);
        db.open();



        btnGetLatLng = (Button) findViewById(R.id.btnGetLatLng);
        btnExportExcel = (Button) findViewById(R.id.btnExportExcel);
        btnReadExcel = (Button) findViewById(R.id.btnReadExcel);
        edt_address = (EditText) findViewById(R.id.edt_addrs);
        txt_Lat = (TextView) findViewById(R.id.txt_Lat);
        txt_Long = (TextView) findViewById(R.id.txt_Long);

        askForPermission();
        edt_address.setText("IIT Main Gate Powai Mumbai 400076");
        btnExportExcel.setVisibility(View.GONE);

        mCellValue =new HashMap<String, String>();
        mColvalue = new ArrayList<String>();
        dialog = new ProgressDialog(MainActivity.this);

        btnGetLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncFlag = "latlang";
                //new GetLatLong().execute();
                getLatGolgRx();
            }
        });

        btnExportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncFlag = "export";
//                new GetLatLong().execute();
//                exportExcelFile(MainActivity.this,"Doc/ExportLatLongUser.xls");

                exportExcelWithLatLong();
            }
        });

        btnReadExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncFlag = "import";
//                btnExportExcel.setVisibility(View.VISIBLE);
//                new GetLatLong().execute();
//                if (db.GetAllLatLong().getCount()==0)
//                readExcelFile(MainActivity.this,"Doc/GeoCode.xls");
                readExcelWithLatLong();
            }
        });
    }

    private void doWork(){

//        readExcelFile(MainActivity.this,"Doc/LatLongUser.xls")

    }

    private void readExcelWithLatLong(){
        readExcelFile(MainActivity.this,"Doc/LatLongUser.xls")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ArrayList<String>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        dialog.show();
                    }

                    @Override
                    public void onNext(final ArrayList<String> strings) {
                        arrLatLong.clear();
                        arrLatLong=strings;

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        /*if (dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (arrLatLong.size()>0)
                                {
                                    txt_Lat.setText(arrLatLong.get(0));
                                    txt_Long.setText(arrLatLong.get(1));
                                }
                            }
                        });*/
                        exportExcelWithLatLong();
                    }
                });
    }

    private void exportExcelWithLatLong(){
        exportExcelFile(MainActivity.this,"Doc/ExportLatLongUser.xls")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ArrayList<String>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(final ArrayList<String> strings) {
                        arrLatLong.clear();
                        arrLatLong=strings;

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (arrLatLong.size()>0)
                                {
                                    txt_Lat.setText(arrLatLong.get(0));
                                    txt_Long.setText(arrLatLong.get(1));
                                }
                            }
                        });
                    }
                });
    }

    public void getLatGolgRx(){
        getLocationFromAddress(MainActivity.this, edt_address.getText().toString())
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<String>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        dialog.setMessage("Fetching LatLong....");
                        dialog.setCancelable(false);
                        dialog.show();
                    }

                    @Override
                    public void onNext(ArrayList<String> strings) {
                        arrLatLong.clear();
                        arrLatLong=strings;

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (arrLatLong.size()>0)
                        {
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt_Lat.setText(arrLatLong.get(0));
                                        txt_Long.setText(arrLatLong.get(1));
                                    }
                                });
                            }
                            catch (Exception E){
                                Log.e("","Error="+E.toString());
                            }

                        }

                    }
                });
    }

    private void askForPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_CODE);
    }

    public Observable<ArrayList<String>> getLocationFromAddress(Context context, String strAddress) {

        ArrayList<String> arrLstLatLong= new ArrayList<String>();
        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size()==0) {
                return Observable.just(arrLstLatLong);
            }
            Address location = address.get(0);
            arrLstLatLong.add("Latitude: "+location.getLatitude());
            arrLstLatLong.add("Longitude: "+location.getLongitude());
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return Observable.just(arrLstLatLong);
    }

    public ArrayList<String> arr_getLocationFromAddress(Context context, String strAddress) {

        ArrayList<String> arrLstLatLong= new ArrayList<String>();
        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size()==0) {
                return arrLstLatLong;
            }
            Address location = address.get(0);
            arrLstLatLong.add("Latitude: "+location.getLatitude());
            arrLstLatLong.add("Longitude: "+location.getLongitude());
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return arrLstLatLong;
    }


    private Observable<ArrayList<String>> readExcelFile(Context context, final String filename) {

        ArrayList<String> str = new ArrayList<String>();
        int i = 0;
        try {
            // Creating Input Stream
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            str.add("File Name: "+filename);
            str.add("File path: "+Environment.getExternalStorageDirectory().getAbsolutePath()+filename);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setMessage("Reading the entries of "+filename+", \nPlease wait...");
                }
            });

//            txt_Lat.setText("File Name: "+filename);
//            txt_Long.setText("File path: "+path+filename);
            File file = new File(path, filename);
            if (file.exists()){
                FileInputStream myInput = new FileInputStream(file);

                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

                // Create a workbook using the File System
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

                // Get the first sheet from workbook
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);

                /** We now need something to iterate through the cells.**/
                Iterator rowIter = mySheet.rowIterator();

                mCellValue =new HashMap<String, String>();
                mColvalue = new ArrayList<String>();
                ArrayList<String> arrayListLatLong = new ArrayList<String>();
                mCellValue.clear();
                int j = 0;

                db.FlushDatabase();
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator cellIter = myRow.cellIterator();
                    j=0;
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (i==0){
                            mColvalue.add(myCell.toString());

                        }

                        if (i>0){
                            mCellValue.put(mColvalue.get(j),myCell.toString());
                        }
                        j++;
                    }

                    if (i>0){

//                        arrayListLatLong = arr_getLocationFromAddress(context,mCellValue.get("CUST_ADDRESS"));
                        if (arrayListLatLong.size()>0)
                            db.insertUserMaster(
                                    mCellValue.get("GEOCODE_ID"),
                                    mCellValue.get("CUST_ID"),
                                    mCellValue.get("CUST_ADDRESS"),
                                    ""+arrayListLatLong.get(0),
                                    ""+arrayListLatLong.get(1),
                                    mCellValue.get("PINCODE"),
                                    mCellValue.get("CITY"),
                                    mCellValue.get("STATE"),
                                    mCellValue.get("LASTUPDATED_DTS"));
                        else
                        {
                            db.insertUserMaster(
                                    mCellValue.get("GEOCODE_ID"),
                                    mCellValue.get("CUST_ID"),
                                    mCellValue.get("CUST_ADDRESS"),
                                    "0",
                                    "0",
                                    mCellValue.get("PINCODE"),
                                    mCellValue.get("CITY"),
                                    mCellValue.get("STATE"),
                                    mCellValue.get("LASTUPDATED_DTS"));
                        }
                    }

                    i++;
                }
            }
            else
            {
                Log.e("","File Not Found");
            }
            Log.e("","");

        } catch (Exception e) {
            Log.e("","i="+i);
            e.printStackTrace();
        }

        return Observable.just(str);
    }

    private Observable<ArrayList<String>> exportExcelFile(Context context, final String fileName) {

        ArrayList<String> str = new ArrayList<String>();
        String path="";


        boolean success = false;
        int counter=0;
        final Cursor cur = db.GetAllLatLong();
        try
        {
            //New Workbook
            Workbook wb = new HSSFWorkbook();
            Cell c = null;

            //Cell style for header row
            CellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.LIME.index);
            cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            //New Sheet
            Sheet sheet1 = null;
            sheet1 = wb.createSheet("myOrder");

            // Generate column headings
            Row row = sheet1.createRow(0);

            for (int i = 0; i<mColvalue.size(); i++){
                c = row.createCell(i);
                c.setCellValue(mColvalue.get(i));
                c.setCellStyle(cs);
            }

            ArrayList<String> arr = new ArrayList<String>();
            for (cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
                mCellValue.clear();
                arr.clear();
                counter++;
//                arr = getLocationFromAddress(context,cur.getString(1).toString());

                mCellValue.put(mColvalue.get(0),cur.getString(0));
                mCellValue.put(mColvalue.get(1),cur.getString(1));
                mCellValue.put(mColvalue.get(2),cur.getString(2));
                mCellValue.put(mColvalue.get(3),cur.getString(3));
                mCellValue.put(mColvalue.get(4),cur.getString(4));
                mCellValue.put(mColvalue.get(5),cur.getString(5));
                mCellValue.put(mColvalue.get(6),cur.getString(6));
                mCellValue.put(mColvalue.get(7),cur.getString(7));
                mCellValue.put(mColvalue.get(8),cur.getString(8));

                row = sheet1.createRow(counter);
                for (int i = 0; i<mColvalue.size(); i++){
                    c = row.createCell(i);
                    c.setCellValue(mCellValue.get(mColvalue.get(i)));
                }
                sheet1.setColumnWidth(0, (15 * 500));
                sheet1.setColumnWidth(1, (15 * 500));
                sheet1.setColumnWidth(2, (15 * 500));

                final int finalCounter = counter;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage("Exporting excel file in sd card\n..."+ finalCounter +"/"+cur.getCount()+", \n\nPlease wait...");
                    }
                });
                // Create a path where we will place our List of objects on external storage
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
                File file = new File(path, fileName);
                FileOutputStream os = null;

                try {
                    os = new FileOutputStream(file);
                    wb.write(os);
                    success = true;
                } catch (IOException e) {
                    Log.w("FileUtils", "Error writing " + file, e);
                } catch (Exception e) {
                    Log.w("FileUtils", "Failed to save file", e);
                } finally {
                    try {
                        if (null != os)
                            os.close();
                    } catch (Exception ex) {
                        Log.e("", "Error found at record="+counter);
                    }
                }
            }
//            Toast.makeText(context,"Exported Successfully",Toast.LENGTH_LONG).show();
//            txt_Lat.setText("File Name: "+fileName);
//            txt_Long.setText("File path: "+Environment.getExternalStorageDirectory().getAbsolutePath()+fileName);
        }
        catch (Exception E){
            Log.e("", "Error found at record="+counter);
//            Toast.makeText(context,"Error found at record="+counter,Toast.LENGTH_LONG).show();
        }

        str.add("File Name: "+fileName);
        str.add("File path: "+Environment.getExternalStorageDirectory().getAbsolutePath()+fileName);


        return Observable.just(str);
    }



    /*public class GetLatLong extends AsyncTask<Void,Void, ArrayList<String>>{

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Fetching LatLong....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> arr = new ArrayList<String>();
//            if (asyncFlag.equals("latlang")){
//                arr = getLocationFromAddress(MainActivity.this,edt_address.getText().toString());
//            }
//            else
                if (asyncFlag.equals("import")){
                arr = readExcelFile(MainActivity.this,"Doc/GeoCode.xls");
            }
            else if (asyncFlag.equals("export")){
                arr = exportExcelFile(MainActivity.this,"Doc/ExportLatLongUser.xls");
            }
            return arr;

        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (strings.size()>0)
            {
                txt_Lat.setText("Lattitude: "+strings.get(0));
                txt_Long.setText("Longitude: "+strings.get(1));
            }

        }
    }*/



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)   {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                        || permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                        || permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    if (grantResult == PackageManager.PERMISSION_GRANTED) {

                    }
                }
            }
        }
    }





}
