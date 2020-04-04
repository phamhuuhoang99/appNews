package com.phamhuuhoang.newfeedapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ToolbarWidgetWrapper;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class NewsDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private ImageView imageView;
    private TextView appbarTitle,appbarSubTitle,date,time,title;
    private boolean isHideToolBarView=false;
    private FrameLayout date_behavior;
    private LinearLayout titleAppbar;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private String mUrl,mImg,mTitle,mDate,mSource,mAuthor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");

        appBarLayout=findViewById(R.id.appbar);
        appbarSubTitle=findViewById(R.id.subtitle_on_appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        date_behavior=findViewById(R.id.date_behavior);
        titleAppbar=findViewById(R.id.title_appbar);
        imageView=findViewById(R.id.backdrop);
        appbarTitle=findViewById(R.id.title_on_appbar);
        title=findViewById(R.id.title);
        date=findViewById(R.id.date);
        time=findViewById(R.id.time);

        Intent intent=getIntent();
        mUrl=intent.getStringExtra("url");

        mTitle=intent.getStringExtra("title");

        mImg=intent.getStringExtra("img");
        mDate=intent.getStringExtra("date");
        mSource=intent.getStringExtra("source");
        mAuthor=intent.getStringExtra("author");

        RequestOptions requestOptions=new RequestOptions();
        requestOptions.error(Utils.getRandomDrawbleColor());
        requestOptions.centerCrop();

        Glide.with(this)
                .load(mImg)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);

        appbarTitle.setText(mSource);

        appbarSubTitle.setText(mUrl);
        date.setText(Utils.DateFormat(mDate));
        title.setText(mTitle);

        String author=null;
        if(mAuthor!=null||mAuthor!=""){
            mAuthor=" \u2020 "+mAuthor;
        }else{
            author="";
        }
        time.setText(mSource+ author +" \u2022 "+ Utils.DateToTimeFormat(mDate));

        initWebView(mUrl);

    }
    private  void initWebView(String url){
        WebView webView=findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int maxScroll=appBarLayout.getTotalScrollRange();
        float percentage=Math.abs(i)/(float)maxScroll;
        if(percentage==1f&&isHideToolBarView){
            date_behavior.setVisibility(View.GONE);
            titleAppbar.setVisibility(View.VISIBLE);
            isHideToolBarView=!isHideToolBarView;
        }
        else if(percentage<1f&&isHideToolBarView){
            date_behavior.setVisibility(View.VISIBLE);
            titleAppbar.setVisibility(View.GONE);
            isHideToolBarView=!isHideToolBarView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_news,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
         if(id==R.id.share){
             try {
                //TODO: share with email
                 Intent i = new Intent(Intent.ACTION_SEND);
                 i.setType("text/plan");
                 i.putExtra(Intent.EXTRA_SUBJECT, mSource);
                 String body = mTitle + "\n" + mUrl + "\n" + "Share from the News App" + "\n";
                 i.putExtra(Intent.EXTRA_TEXT, body);
                 startActivity(Intent.createChooser(i, "Share with: "));
             }catch(Exception e){
                 Toast.makeText(NewsDetailActivity.this,"Can not be share",Toast.LENGTH_SHORT).show();
             }

         }else if(id==R.id.open_with_browser){
             //TODO: open with browser
             Intent i=new Intent(Intent.ACTION_VIEW);
             i.setData(Uri.parse(mUrl));
             startActivity(i);
         }

        return super.onOptionsItemSelected(item);
    }
}
