package com.phamhuuhoang.newfeedapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phamhuuhoang.newfeedapp.Api.ApiClient;
import com.phamhuuhoang.newfeedapp.Api.ApiInterface;
import com.phamhuuhoang.newfeedapp.models.Article;
import com.phamhuuhoang.newfeedapp.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String API_KEY="2d1b943cc8fa45179c7176740e763afa";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager  mLayoutManager;
    private List<Article> articles=new ArrayList<>();
    private  Adapter mAdapter;
    private  String TAG=MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;

    private RelativeLayout errorLayout;
    private  TextView errorTitle,subErrorTitle;
    private ImageView errorImg;
    private   Button errorRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);



        recyclerView=findViewById(R.id.recyclerview);
        mLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        errorLayout=findViewById(R.id.error_layout);
        errorTitle=findViewById(R.id.errorTitle);
        subErrorTitle=findViewById(R.id.errorSubTitle);
        errorImg=findViewById(R.id.no_result);
        errorRetry=findViewById(R.id.btn_Retry);




        //loadJson("");
        onLoadingSwipeRefresh("");
    }


    public void loadJson(final String key){
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        final ApiInterface apiInterface= ApiClient.getApiClient().create(ApiInterface.class);
        String country=Utils.getCountry();
        String language=Utils.getLanguage();
        Call<News> call;

        if(key.length()>0){
            call=apiInterface.getNewSearch(key,language,"publishedAt",API_KEY);
        }else{
            call=apiInterface.getNews(country,API_KEY);
        }


        //TODO: convert các đối tượng JSON về danh sách các đối tượng Java
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful()&&response.body().getArticles()!=null){
                    if(articles.isEmpty()){
                        articles.clear();
                    }

                    articles=response.body().getArticles();
                    mAdapter=new Adapter(articles,MainActivity.this);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();


                    initListener();

                    swipeRefreshLayout.setRefreshing(false);
                }else{

                    //TODO: xử lý khi đường link sai hoặc ko đọc đc đối tượng
                    swipeRefreshLayout.setRefreshing(false);

                    switch (response.code()){
                        case 404:
                            showError(R.drawable.no_result,"No Result","404 not found");
                            break;
                        case 500:
                            showError(R.drawable.no_result,"No Result","505 not found");
                            break;
                        default:
                            showError(R.drawable.no_result,"No Result","unknown");
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                //TODO: xử lý khi ko có mạng
                swipeRefreshLayout.setRefreshing(false);
                showError(R.drawable.no_result,"Oops...","Network failure, Please try again..\n"+t.toString());
            }
        });


    }
    private  void initListener(){

        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ImageView imageView=view.findViewById(R.id.img);
                Intent intent=new Intent(MainActivity.this,NewsDetailActivity.class);

                Article article=articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());

                //TODO: tao hiệu ứng hiện new activity
//               Pair<View,String> pair= Pair.create((View)imageView,ViewCompat.getTransitionName(imageView));
//                ActivityOptions optionsCompat=ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pair);
//
//                  if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
//                      startActivity(intent,optionsCompat.toBundle());
//                  }else{
//                      startActivity(intent);
//                  }


                startActivity(intent);



            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater  inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        SearchManager searchManager= (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView= (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem=menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest New");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>2){
                    onLoadingSwipeRefresh(query);
                    //loadJson(query);
                }else{
                    Toast.makeText(MainActivity.this,"Type more than 2 letters",Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onLoadingSwipeRefresh(newText);
               // loadJson(newText);
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false,false);


        return true;
    }

    @Override
    public void onRefresh() {
        loadJson("");
    }
    private  void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadJson(keyword);
            }
        });
    }

    private void showError(int img,String title,String subTitle){
        if(errorLayout.getVisibility()==View.GONE){
            errorLayout.setVisibility(View.VISIBLE);
        }
        errorTitle.setText(title);
        subErrorTitle.setText(subTitle);
        errorImg.setImageResource(img);

        errorRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button","click");
                onLoadingSwipeRefresh("");

            }
        });
    }
}
