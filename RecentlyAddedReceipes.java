package com.example.chetana.kitchenmantra;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chetana.kitchenmantra.Adapters.IngredientsAdapter;
import com.example.chetana.kitchenmantra.Classes.Bean_RecentAddedRecps;
import com.example.chetana.kitchenmantra.Database.DatabaseHandler;
import com.example.chetana.kitchenmantra.Utilities.Utility;

import java.util.ArrayList;
import java.util.List;

public class RecentlyAddedReceipes extends AppCompatActivity {
    private Context parent;
    TextView txtrec_upldname, txtrecname, txt_addeddate, txtingredients,txtpreparations ;
    EditText edtfeedback;
    Button btn_close, submtfeedback,btncancel;
    ListView lstingredients;
    ImageView imgbtnfeedback, img_vegnonveg, img_fvrts, img_prof;

    View sheetview;
    int imgFavCnt = 0;
    String imgpath;

    IngredientsAdapter adapteringrd;
    Bean_RecentAddedRecps beanIngrd;
    ArrayList<Bean_RecentAddedRecps> arrListIngrds;

    DatabaseHandler db;
    SQLiteDatabase sqldb;
    Utility ut;
    String RECPHDRID, RECPUPLOADER, RECPCATEGORY, RECPADDATE,RECPNAME;
    String recpuplodername, recpName, recpAddedDt, recpCategory, recpPreparation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_added_receipes);

        getSupportActionBar().setTitle("Recent Added Receipes");

        init();
        
        txt_addeddate.setText(RECPADDATE);
        txtrec_upldname.setText(RECPUPLOADER);
        txtrecname.setText(RECPNAME);
        
        getReceipeFromDatabase();

        setListener();
    }

    public void init(){
        parent = RecentlyAddedReceipes.this;

        txtrec_upldname = (TextView)findViewById(R.id.txtrec_upldname);
        txtrecname = (TextView)findViewById(R.id.txtrecname);
        txt_addeddate = (TextView)findViewById(R.id.txt_addeddate);
        txtingredients = (TextView)findViewById(R.id.txtingredients);
        txtpreparations = (TextView)findViewById(R.id.txtpreparations);
        btn_close = (Button)findViewById(R.id.btn_close);
        imgbtnfeedback = (ImageView)findViewById(R.id.imgbtnfeedback);
        img_vegnonveg = (ImageView)findViewById(R.id.img_vegnonveg);
        img_fvrts = (ImageView)findViewById(R.id.img_fvrts);
        img_prof = (ImageView)findViewById(R.id.img_prof);
        lstingredients = (ListView)findViewById(R.id.lstingredients);
        
        Intent intent = getIntent();
        RECPHDRID = intent.getStringExtra("ReceipeHeaderId");
        RECPADDATE = intent.getStringExtra("ReceipeAddedDate");
        RECPCATEGORY = intent.getStringExtra("ReceipeCategory");
        RECPUPLOADER = intent.getStringExtra("ReceipeUploadeBy");
        RECPNAME = intent.getStringExtra("ReceipeName");
        
        if(RECPCATEGORY.equalsIgnoreCase("Veg")){
            img_vegnonveg.setImageResource(R.drawable.veg);
            
        }else {
            img_vegnonveg.setImageResource(R.drawable.nonveg);
        }

        db = new DatabaseHandler(parent);
        sqldb = db.getWritableDatabase();
        ut = new Utility();

        arrListIngrds = new ArrayList<Bean_RecentAddedRecps>();
        beanIngrd = new Bean_RecentAddedRecps();
        
    }

    public void setListener(){

        img_fvrts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgFavCnt == 0){
                    imgFavCnt = 1;
                    img_fvrts.setImageResource(R.drawable.favourites_sele);
                }else {
                    imgFavCnt = 0;
                    img_fvrts.setImageResource(R.drawable.favourite_black);
                }
            }
        });

        imgbtnfeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final BottomSheetDialog btmsheetdialog = new BottomSheetDialog(parent);
                sheetview = getLayoutInflater().inflate(R.layout.feedback_bottom_layout, null);
                btmsheetdialog.setContentView(sheetview);
                btmsheetdialog.show();
                btmsheetdialog.setCancelable(false);
                btmsheetdialog.setCanceledOnTouchOutside(false);

                edtfeedback = sheetview.findViewById(R.id.edtfeedback);
                submtfeedback = sheetview.findViewById(R.id.submtfeedback);
                btncancel = sheetview.findViewById(R.id.btncancel);

                submtfeedback.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        //store in table
                        btmsheetdialog.dismiss();
                    }
                });

                btncancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btmsheetdialog.dismiss();
                    }
                });
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.enter_left_to_right,R.anim.exit_right_to_left);
    }
    
    public void getReceipeFromDatabase(){
        sqldb = db.getReadableDatabase();
        String preparation = "", IMG = "";
        String ingrdDesc, ingrdqty, ingrdunit;
        
        String query = "select * from recpeheader where Recp_headerid = '"+RECPHDRID +"'";

        Cursor crecp = sqldb.rawQuery(query,null);
        Log.e("crecp", String.valueOf(crecp.getCount()));
        
        if(crecp.getCount() > 0){
            crecp.moveToFirst();
            do{
                preparation = crecp.getString(crecp.getColumnIndex("Recp_prep_desc"));
                IMG = crecp.getString(crecp.getColumnIndex("Recp_imgpath"));
                
            }while (crecp.moveToNext());
            
        }else {
            
        }

        String query_ingrd = "select * from recpedetail where Recp_headerid = '"+RECPHDRID +"'";
        Cursor cingrd = sqldb.rawQuery(query_ingrd,null);
        Log.e("crecp", String.valueOf(cingrd.getCount()));

        if(cingrd.getCount() > 0){
            cingrd.moveToFirst();
            do{
                ingrdDesc = cingrd.getString(cingrd.getColumnIndex("ingrd_itemdesc"));
                ingrdqty = cingrd.getString(cingrd.getColumnIndex("ingrd_itemqty"));
                ingrdunit = cingrd.getString(cingrd.getColumnIndex("ingrd_itemunit"));

                beanIngrd = new Bean_RecentAddedRecps();
                beanIngrd.setIngrd_desc(ingrdDesc);
                beanIngrd.setIngrd_qty(ingrdqty);
                beanIngrd.setIngrd_unit(ingrdunit);

                arrListIngrds.add(beanIngrd);

            }while (cingrd.moveToNext());

            adapteringrd = new IngredientsAdapter(parent,arrListIngrds);
            lstingredients.setAdapter(adapteringrd);

        }else {

        }

        if(IMG.equalsIgnoreCase("vegpulao")) {
            img_prof.setImageResource(R.drawable.veg_pulao);
        }else if(IMG.equalsIgnoreCase("gajarhalwa")) {
            img_prof.setImageResource(R.drawable.gajrhalwa);
        }else if(IMG.equalsIgnoreCase("eggmaggie")) {
            img_prof.setImageResource(R.drawable.eggmagg);
        }else {
                byte [] encodeByte=Base64.decode(IMG,Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                img_prof.setImageBitmap(bitmap);
            }

       /* byte [] encodeByte= Base64.decode(IMG,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        img_prof.setImageBitmap(bitmap);*/
        txtpreparations.setText(preparation);
        
    }
}
