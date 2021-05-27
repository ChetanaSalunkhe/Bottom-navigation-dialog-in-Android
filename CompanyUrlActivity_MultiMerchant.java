package com.vritti.freshmart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.vritti.freshmart.classes.URL_Company_Domain;
import com.vritti.freshmart.data.AnyMartData;
import com.vritti.freshmart.data.AnyMartDatabaseConstants;
import com.vritti.freshmart.database.DatabaseHelper;
import com.vritti.freshmart.database.DatabaseHelper_URLStore;
import com.vritti.freshmart.interfaces.CallbackInterface;
import com.vritti.freshmart.utils.NetworkUtils;
import com.vritti.freshmart.utils.StartSession;
import com.vritti.freshmart.utils.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.vritti.freshmart.data.AnyMartDatabaseConstants.TABLE_URL_COMPANYDOMAIN;

public class CompanyUrlActivity_MultiMerchant extends AppCompatActivity {
    private Context parent;
    LinearLayout layicons, layurl;
    ImageView img1, img2, img3, img4;
    EditText enterurl;
    CheckBox chkboxsecure;
    //ImageView submiturl;
    Button submiturl;
    LinearLayout layoutsecure;
    static ProgressHUD progress;

    String CompanyURL, Cdomainname;
    String imageURL;
    private String json;
    //private DatabaseHelper_URLStore db_store_companydomain;
    boolean loginStatus, URLExists;
    SharedPreferences sharedpreferences;
    public static final String COMPANYURL_PREF = "CompanyURLPref";
    URL_Company_Domain bean;
    ArrayList<URL_Company_Domain> URL_list;
    //final String DATABASE__NAME_URL = "URL_CompanyDomain";
    String DATABASE__NAME_URL;
    String res = "";
    ProgressDialog pdialogue;
    String compInstance = "";

    private DatabaseHelper databaseHelper;
    private DatabaseHelper_URLStore dbstore_helper;
    SQLiteDatabase sql_db;
    SQLiteDatabase sql_urldb;
    int apiCallLimit = 2;
    int callCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_url);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        parent = CompanyUrlActivity_MultiMerchant.this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.btncolordark));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.btncolordark));
        }

        initialize();

        enterurl.setText(AnyMartData.MARKETPLACE_CODE);

        //show language dialogue if language not selected
        /*if(AnyMartData.LANGUAGE.equalsIgnoreCase("")){
            //show dialogue box to select language
            openDialogueBox();
        }else {
            Utility.setLocale(AnyMartData.LANGUAGE, CompanyUrlActivity_MultiMerchant.this);
        }*/

        btnGoCall();

        //setListeners();
    }

    public void initialize(){

        layurl = findViewById(R.id.layurl);
        layurl.setVisibility(View.GONE);
        layicons = findViewById(R.id.layicons);
        layicons.setVisibility(View.VISIBLE);

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);

        enterurl = (EditText)findViewById(R.id.enterurl);
        layoutsecure = (LinearLayout)findViewById(R.id.layoutsecure);
        layoutsecure.setVisibility(View.GONE);

        chkboxsecure = (CheckBox)findViewById(R.id.chkboxsecure);
        chkboxsecure.setVisibility(View.VISIBLE);

        submiturl = findViewById(R.id.submiturl);
        URL_list = new ArrayList<URL_Company_Domain>();
        CompanyURL = enterurl.getText().toString().trim();
        Cdomainname = enterurl.getText().toString().trim();

        Animation hrtbeat = AnimationUtils.loadAnimation(this, R.anim.heartbeat);
        try {
            img1.startAnimation(hrtbeat);
            img2.startAnimation(hrtbeat);
            img3.startAnimation(hrtbeat);
            img4.startAnimation(hrtbeat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DATABASE__NAME_URL = enterurl.getText().toString().trim();
        AnyMartDatabaseConstants.DATABASE__NAME_URL = DATABASE__NAME_URL;

        //databaseHelper = new DatabaseHelper(parent,AnyMartDatabaseConstants.DATABASE__NAME_URL);
        // sharedpreferences = getSharedPreferences(COMPANYURL_PREF,Context.MODE_PRIVATE);
        sharedpreferences = getSharedPreferences(SplashActivity.MyPREFERENCES,Context.MODE_PRIVATE);
        AnyMartData.LANGUAGE = sharedpreferences.getString("Language","");
        dbstore_helper = new DatabaseHelper_URLStore(parent);
        sql_urldb = dbstore_helper.getWritableDatabase();

        pdialogue = new ProgressDialog(parent);
    }

    public void setListeners(){
        chkboxsecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = ((CheckBox) v).isChecked();
                chkboxsecure.setChecked(isSelected);
            }
        });

        submiturl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompanyURL = enterurl.getText().toString().trim();
                Cdomainname = enterurl.getText().toString().trim();

                //check url is already present in table or not
                getDataFromDataBase_checkURLExistence(CompanyURL);

                databaseHelper = new DatabaseHelper(parent, AnyMartDatabaseConstants.DATABASE__NAME_URL);
                sql_db = databaseHelper.getWritableDatabase();

                if (databaseHelper.getusercount() > 0) {
                    loginStatus = true;
                    //Toast.makeText(parent,"User is Logged in", Toast.LENGTH_SHORT).show();
                } else {
                    loginStatus = false;
                    //Toast.makeText(parent,"User is not Logged in, Login first", Toast.LENGTH_SHORT).show();
                }

                if(CompanyURL.equalsIgnoreCase("") || CompanyURL.equalsIgnoreCase(null)) {
                    //submiturl.setClickable(false);

                    Toast.makeText(parent,""+getResources().getString(R.string.enter_url_toast), Toast.LENGTH_SHORT).show();

                }else if(CompanyURL != "" || CompanyURL != null) {
                    //submiturl.setClickable(true);

                    AnyMartData.MAIN_URL = getcompanyURL(AnyMartData.instance);

                    //get env and plant details here
                    try{
                        if (NetworkUtils.isNetworkAvailable(CompanyUrlActivity_MultiMerchant.this)) {
                            //new GetEnvId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            new GetEnvId().execute();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                     //   Toast.makeText(parent,"Error occurs at "+e.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    private boolean getDataFromDataBase_checkURLExistence(String CompURL) {
        // TODO Auto-generated method stub
        URL_list.clear();
      //  CompanyURL = CompURL;
        AnyMartDatabaseConstants.DATABASE__NAME_URL = "AnyMart_"+CompURL;
        //sql_db = dbstore_helper.getWritableDatabase();
        sql_urldb = dbstore_helper.getWritableDatabase();

        /*DatabaseHelper_URLStore db1 = new DatabaseHelper_URLStore(parent,
                AnyMartDatabaseConstants.DATABASE__NAME_URL,AnyMartDatabaseConstants.DATABASE_VERSION);
*/
        Cursor c = sql_urldb.rawQuery("Select distinct Url,CustVendorMasterId, LoginId from "
                + TABLE_URL_COMPANYDOMAIN +
                " WHERE Url = '"+ Cdomainname +"'", null);

        if (c.getCount() > 0 ) {
            //urlpresent of this name
            c.moveToFirst();
            do {

                String url_present = c.getString(c.getColumnIndex("Url"));
                String custvendID = c.getString(c.getColumnIndex("CustVendorMasterId"));

                try{
                    if(url_present.equalsIgnoreCase("") || url_present.equalsIgnoreCase(null)){
                        //no url present
                        URLExists = false;
                    }else {
                        if(custvendID.equalsIgnoreCase("") || custvendID.equalsIgnoreCase(null)){
                            //no customer present
                            URLExists = false;
                        }else {
                            URLExists = true;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

               /* if((url_present != null) && (!custvendID.equals(""))){
                    URLExists = true;
                }else {
                    URLExists = false;
                }*/
            } while (c.moveToNext());

           // return true;

        } else {
            //url of this name not present
            URLExists = false;
           // return false;
        }
        return URLExists;
    }

    class GetEnvId extends AsyncTask<String, Void, List<String>> {
        String res;
        List<String> EnvName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pdialogue.show();
            pdialogue.setTitle(""+getResources().getString(R.string.verifying_url));*/
           /* try{
                progress = ProgressHUD.show(parent,""+parent.getResources().getString(R.string.verifying_url),
                        false,true, null);
                progress.setCanceledOnTouchOutside(true);
            }catch (Exception e){
                e.printStackTrace();
            }*/

        }

        @Override
        protected List<String> doInBackground(String... params) {
            String url = AnyMartData.CompanyURL + AnyMartData.api_getEnv;
            try {
                res = Utility.OpenConnection_ekatm(url, CompanyUrlActivity_MultiMerchant.this);
                res = res.replaceAll("\\\\", "");
                res = res.substring(1, res.length() - 1);

            } catch (Exception e) {
                e.printStackTrace();
                res = "error";
            }
            /*List<String> result = new ArrayList<>();
            result.add(res);
            result.add(params[0]);*/
            return null;
        }

        @Override
        protected void onPostExecute(List<String> str) {
            super.onPostExecute(str);

           /* try{
                //pdialogue.dismiss();
                progress.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }*/

            if (res.contains("AppEnvMasterId")) {

                SharedPreferences.Editor editor = sharedpreferences.edit();

                try {
                    JSONArray jResults = new JSONArray(res);
                    EnvName = new ArrayList<String>();
                    for (int index = 0; index < jResults.length(); index++) {

                        JSONObject jorder = jResults.getJSONObject(index);
                        EnvName.add(jorder.getString("AppEnvMasterId"));
                        String isChatApplicable = jorder.getString("IsChatApplicable");
                        String isGPSLocation = jorder.getString("IsGPSLocation");
                        String appCode = jorder.getString("AppCode");
                        String environment = jorder.getString("Environment");

                        AnyMartData.EnvMasterId = jorder.getString("AppEnvMasterId");
                        AnyMartData.isChatApplicable = isChatApplicable;
                        AnyMartData.isGPSLocation = isGPSLocation;
                        AnyMartData.AppCode = appCode;
                        AnyMartData.Environment = environment;
                        AnyMartData.SHOP_CODE = enterurl.getText().toString().trim();

                        editor.putString("isChatApplicable", isChatApplicable);
                        editor.putString("isGPSLocation",isGPSLocation);
                        editor.putString("AppEnvMasterId",  AnyMartData.EnvMasterId );
                        editor.putString("AppCode",  AnyMartData.AppCode);
                        editor.putString("Environment",  AnyMartData.Environment);
                        editor.putString("CompanyURL", AnyMartData.MAIN_URL);
                        editor.putString("companyurlmain", AnyMartData.CompanyURL);
                        editor.putString("companyURL_LOGO",enterurl.getText().toString().trim());
                        editor.putString("DatabaseName", AnyMartDatabaseConstants.DATABASE__NAME_URL);
                        editor.putString("SHOPCODE", AnyMartData.SHOP_CODE);
                        editor.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Invalid : ","fail parsing res in envis ");
                            Toast.makeText(parent,""+parent.getResources().getString(R.string.invalid_shopcode),Toast.LENGTH_LONG).show();
                        }
                    });
                }

                editor.commit();
                EnvName.add(getResources().getString(R.string.instruction_Spinner_Change_Company));
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CompanyUrlActivity_MultiMerchant.this,
                        android.R.layout.simple_spinner_item, EnvName);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                if (NetworkUtils.isNetworkAvailable(parent)) {
                   // new GetPlantDtls().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new GetPlantDtls().execute();
                }
            } else if(res.equalsIgnoreCase("error")){

                callCnt++;

                if(callCnt < apiCallLimit){
                    AnyMartData.MAIN_URL = getcompanyURL(AnyMartData.instance2);
                    new GetEnvId().execute();
                }else {
                    callCnt = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Invalid : ","no data from response of envis ");
                            Toast.makeText(parent,""+parent.getResources().getString(R.string.invalid_shopcode),Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }
    }

    class GetPlantDtls extends AsyncTask<String, Void, List<String>> {
        String res;
        List<String> EnvName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*try{
                progress = ProgressHUD.show(parent,""+parent.getResources().getString(R.string.verifying_url), false,true, null);
                progress.setCanceledOnTouchOutside(true);
            }catch (Exception e){
                e.printStackTrace();
            }*/
        }

        @Override
        protected List<String> doInBackground(String... params) {
            String url = "";
            try {
                url = AnyMartData.CompanyURL + AnyMartData.api_getPlants + "?AppEnvMasterId=" +
                        URLEncoder.encode(AnyMartData.EnvMasterId, "UTF-8") + "&PlantId=";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                res = Utility.OpenConnection_ekatm(url, CompanyUrlActivity_MultiMerchant.this);
                res = res.replaceAll("\\\\", "");
                res = res.substring(1, res.length() - 1);

            } catch (Exception e) {
                e.printStackTrace();
                res = "error";
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> str) {
            super.onPostExecute(str);

           /* try{
                //pdialogue.dismiss();
                progress.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }*/

            if (res.contains("PlantMasterId")) {

                SharedPreferences.Editor editor = sharedpreferences.edit();

                try {
                    JSONArray jResults = new JSONArray(res);
                    EnvName = new ArrayList<String>();
                    for (int index = 0; index < jResults.length(); index++) {

                        JSONObject jorder = jResults.getJSONObject(index);
                        String plantMsterid = jorder.getString("PlantMasterId");
                        String plantname = jorder.getString("PlantName");

                        AnyMartData.PlantMasterId = plantMsterid;
                        AnyMartData.PlantName = plantname;

                        editor.putString("PlantMasterId", plantMsterid);
                        editor.putString("PlantName",plantname);
                        editor.commit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(getDataFromDataBase_checkURLExistence(Cdomainname)){
                    //true
                    URLExists = true;
                }else {
                    //false
                    URLExists = false;
                    //add url, loginid, custvendormasterid
                    dbstore_helper.addURL_old(Cdomainname,"","", AnyMartDatabaseConstants.DATABASE__NAME_URL,
                            AnyMartData.EnvMasterId,AnyMartData.PlantMasterId,AnyMartData.Password,compInstance);
                }

                if(URLExists == true){ /*if(loginStatus == true)*/

                    //authenticate method pass url to server get data of that url
                    Intent intent = new Intent(parent,UserRegistrationActivity.class);
                    intent.putExtra("callFrom","MainCall");
                    startActivity(intent);
                    Toast.makeText(parent,""+getResources().getString(R.string.exist_company), Toast.LENGTH_SHORT).show();

                }else {

                    editor = sharedpreferences.edit();
                    editor.putString("companyURL_LOGO",enterurl.getText().toString().trim());
                    editor.commit();

                    //go to loginvalidationscreen
                    // Toast.makeText(parent,"User is not Logged in, Login first", Toast.LENGTH_SHORT).show();
                   // Toast.makeText(parent,""+getResources().getString(R.string.new_company), Toast.LENGTH_SHORT).show();
                   // Intent intent = new Intent(parent, LoginValidationScreen.class);
                    Intent intent = new Intent(parent, ChoseAccOptionActivity.class);
                    intent.putExtra("CompanyURL", AnyMartData.MAIN_URL);
                    intent.putExtra("companyURL_LOGO",enterurl.getText().toString().trim());
                    intent.putExtra("callFrom","URLScreen");
                    //intent.putExtra("logopath",imageURL);
                    overridePendingTransition(R.anim.enter_slide_in_down, R.anim.enter_slide_out_down);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            } else {
                Log.e("Invalid : ","no resp from plantdtls ");
                Toast.makeText(parent,""+getResources().getString(R.string.invalid_shopcode),Toast.LENGTH_LONG).show();
				/*mEturl.setText("");
				lin_compcode.setVisibility(View.VISIBLE);
				lin_login.setVisibility(View.GONE);
				//ut.displayToast(getApplicationContext(), "Enter valid Url");
				MySnackbar("Enter valid URL");*/
            }
        }
    }

    class GetCompanyLogo extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        String responseString = "";
        String compURL_logo = "https://"+ enterurl.getText().toString().trim();
        String url = "https://Bakery"+AnyMartData.instance;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* progressDialog = new ProgressDialog(parent);
            progressDialog.setMessage("Authenticating User...");
            progressDialog.show();*/
            /*progress = ProgressHUD.show(parent,
                    "Authenticating ...", false, true, null);*/
        }

        @Override
        protected Void doInBackground(Void... params) {//h207.ekatm.com/Ekatm/GetCompanyLogo
            String url_logo = AnyMartData.MAIN_URL /*compURL_logo*/ + AnyMartData.LOGO_URL +
                    "?companyUrl="+url;

            //   url_authentication = url_authentication.replaceAll(" ","%20");

            String auth = url_logo.replaceAll(" ","");
            URLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            try {
                res = Utility.OpenconnectionOrferbilling(url_logo, parent);
                int a = res.getBytes().length;
                res = res.replaceAll("\\\\", "");
                // res = res.replaceAll("\"", "");
                // res = res.replaceAll(" ", "");
                responseString = res.toString().replaceAll("^\"|\"$", "");
                Log.e("Response", responseString);

                res = responseString;
                Log.e("logopath", responseString);


            } catch (Exception e) {
                responseString = "error";
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

           // progress.dismiss();

            if (responseString.equalsIgnoreCase("Session Expired")) {

                //Toast.makeText(parent,"The page cannot be displayed because an internal server error has occurred.",Toast.LENGTH_SHORT).show();
                if (NetworkUtils.isNetworkAvailable(parent)) {
                    new StartSession(parent, new CallbackInterface() {

                        @Override
                        public void callMethod() {
                            new GetCompanyLogo().execute();
                        }

                        @Override
                        public void callfailMethod(String s) {

                        }
                    });
                }

            } else if (!responseString.equalsIgnoreCase("error")) {

                //call another api to get logo path
                json = responseString;
                imageURL = "http://"+ compURL_logo +"/images/"+ responseString;
                // parseJson(json);

                /*Picasso.with(parent)
                        .load(imageURL)//Your image link url
                        .into(compoanylogo);*/

                //new GetLogoImage().execute();


            //    Toast.makeText(parent,"Get Logo name"+ responseString, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(parent, ""+getResources().getString(R.string.servererror), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public void openDialogueBox(){
        final BottomSheetDialog btmsheetdialog = new BottomSheetDialog(parent);
        View sheetview = getLayoutInflater().inflate(R.layout.dialogue_select_language, null);
        btmsheetdialog.setContentView(sheetview);
        btmsheetdialog.show();
        btmsheetdialog.setCancelable(false);
        btmsheetdialog.setCanceledOnTouchOutside(false);

        RadioGroup radgrp =  btmsheetdialog.findViewById(R.id.radgrp_lang);
        final RadioButton radbtnhindi =  btmsheetdialog.findViewById(R.id.radbtnhindi);
        final RadioButton radbtnenglish =  btmsheetdialog.findViewById(R.id.radbtnenglish);

        radbtnhindi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    radbtnhindi.setChecked(true);
                    radbtnenglish.setChecked(false);
                    Toast.makeText(parent,""+getResources().getString(R.string.changelanguage),Toast.LENGTH_SHORT).show();
                    Utility.setLocale("hi", CompanyUrlActivity_MultiMerchant.this);

                    recreate();

                    btmsheetdialog.dismiss();

                }else {
                }
            }
        });

        radbtnenglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    radbtnenglish.setChecked(true);
                    radbtnhindi.setChecked(false);
                    Toast.makeText(parent,""+getResources().getString(R.string.changelanguage),Toast.LENGTH_SHORT).show();
                    Utility.setLocale("en", CompanyUrlActivity_MultiMerchant.this);

                    recreate();

                    btmsheetdialog.dismiss();

                }else {
                }
            }
        });
    }

    public String getcompanyURL(String instance){
        compInstance = instance;
        if (chkboxsecure.isChecked()) {

            //https
            CompanyURL = "https://" + enterurl.getText().toString().trim()+instance+"/api/OrderBillingAPI/";
            AnyMartData.CompanyURL = "https://" + enterurl.getText().toString().trim()+instance;

        } else {

            //http
            CompanyURL = "http://" + enterurl.getText().toString().trim()+instance+"/api/OrderBillingAPI/";
            AnyMartData.CompanyURL = "http://" + enterurl.getText().toString().trim()+instance;
        }

        return CompanyURL;
    }

    public void btnGoCall(){
        CompanyURL = enterurl.getText().toString().trim();
        Cdomainname = enterurl.getText().toString().trim();

        //check url is already present in table or not
        getDataFromDataBase_checkURLExistence(CompanyURL);

        databaseHelper = new DatabaseHelper(parent, AnyMartDatabaseConstants.DATABASE__NAME_URL);
        sql_db = databaseHelper.getWritableDatabase();

        if (databaseHelper.getusercount() > 0) {
            loginStatus = true;
            //Toast.makeText(parent,"User is Logged in", Toast.LENGTH_SHORT).show();
        } else {
            loginStatus = false;
            //Toast.makeText(parent,"User is not Logged in, Login first", Toast.LENGTH_SHORT).show();
        }

        if(CompanyURL.equalsIgnoreCase("") || CompanyURL.equalsIgnoreCase(null)) {
            //submiturl.setClickable(false);

            Toast.makeText(parent,""+getResources().getString(R.string.enter_url_toast), Toast.LENGTH_SHORT).show();

        }else if(CompanyURL != "" || CompanyURL != null) {
            //submiturl.setClickable(true);

            AnyMartData.MAIN_URL = getcompanyURL(AnyMartData.instance);

            //get env and plant details here
            try{
                if (NetworkUtils.isNetworkAvailable(CompanyUrlActivity_MultiMerchant.this)) {
                    //new GetEnvId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new GetEnvId().execute();
                }
            }catch (Exception e){
                e.printStackTrace();
                //   Toast.makeText(parent,"Error occurs at "+e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }

        }
    }

}
