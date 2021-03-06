package com.blockchain.store.playmarket.ui.main_list_screen;

import android.content.Context;
import android.util.Log;

import com.blockchain.store.playmarket.Application;
import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.check_transation_status_beta.CheckUpdateWorker;
import com.blockchain.store.playmarket.check_transation_status_beta.JobUtils;
import com.blockchain.store.playmarket.data.entities.ChangellyCurrenciesResponse;
import com.blockchain.store.playmarket.data.entities.Category;
import com.blockchain.store.playmarket.data.entities.SearchResponse;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.blockchain.store.playmarket.ui.main_list_screen.MainMenuContract.*;

/**
 * Created by Crypton04 on 26.01.2018.
 */

public class MainMenuPresenter implements Presenter {
    private static final String TAG = "MainMenuPresenter";

    View view;

    @Override
    public void init(View view) {
        this.view = view;
    }

    @Override
    public void loadCategories() {
        RestApi.getServerApi().getCagories().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.setProgress(true))
                .doOnTerminate(() -> view.setProgress(false))
                .subscribe(this::onCategoriesLoaded, this::onCategoriesLoadFail);
    }

    @Override
    public void searchQuery(String text) {
//        RestApi.getServerApi().getSearchResult(text)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::onSearchResultReady, this::onSearchResultFail);
    }

    @Override
    public void requestUpdateListener(Context context) {
//        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(CheckUpdateWorker.class,15, TimeUnit.MINUTES).build();
//        WorkManager.getInstance().enqueueUniquePeriodicWork("playmarketupdate", ExistingPeriodicWorkPolicy.KEEP, workRequest);
        JobUtils.scheduleCheckUpdateJob(context,false);
    }

    private void onSearchResultReady(SearchResponse searchResponse) {
        if (searchResponse.apps != null) {
            view.onSearchResultReady(searchResponse.apps);
            String[] strings = new String[searchResponse.apps.size()];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = searchResponse.apps.get(i).nameApp;
            }
            view.onSearchSuggestionReady(strings);
        }
    }

    private void onSearchResultFail(Throwable throwable) {
        view.onSearchResultFail(throwable);
    }

    private void onBalanceOk(ChangellyCurrenciesResponse baseInfuraResponse) {
        Log.d(TAG, "onBalanceOk() called with: baseInfuraResponse = [" + baseInfuraResponse + "]");
    }

    private void onBalanceFail(Throwable throwable) {
        Log.d(TAG, "onBalanceFail() called with: throwable = [" + throwable + "]");
    }


    private void onCategoriesLoaded(ArrayList<Category> categories) {
        view.onCategoryLoaded(categories);
    }

    private void onCategoriesLoadFail(Throwable throwable) {
        view.onCategoryLoadFailed(throwable);
    }
}
