package com.blockchain.store.playmarket.ui.transfer_screen;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.types.EthereumPrice;
import com.blockchain.store.playmarket.ui.transfer_screen.transfer_confirm_screen.TransferConfirmFragment;
import com.blockchain.store.playmarket.ui.transfer_screen.transfer_info_screen.TransferInfoFragment;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.NonSwipeableViewPager;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.blockchain.store.playmarket.utilities.ViewPagerAdapter;
import com.mtramin.rxfingerprint.RxFingerprint;
import com.orhanobut.hawk.Hawk;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransferActivity extends AppCompatActivity implements TransferContract.View, LifecycleOwner {

    public static String RECIPIENT_ARG = "recipient_address";
    public static String PRICE_ARG = "price_address";

    private TransferViewModel transferViewModel;

    private TransferPresenter presenter;

    private String password;
    private String recipientAddress;
    private String transferAmount;
    private boolean isEth;
    private boolean isBlockEth;

    @BindView(R.id.transfer_viewPager) NonSwipeableViewPager transferViewPager;
    @BindView(R.id.continue_transfer_button) Button continueButton;
    ViewPagerAdapter transferAdapter;

    public static void start(Context context, String recipientAddress, String priceInEth) {
        Intent starter = new Intent(context, TransferActivity.class);
        starter.putExtra(RECIPIENT_ARG, recipientAddress);
        starter.putExtra(PRICE_ARG, priceInEth);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        ButterKnife.bind(this);

        getDataFromViewModel();

        recipientAddress = getIntent().getStringExtra(RECIPIENT_ARG);
        if (recipientAddress != null) {
            transferViewModel.recipientAddress.setValue(recipientAddress);
        }
        transferAmount = getIntent().getStringExtra(PRICE_ARG);
        if (transferAmount != null) {
            transferViewModel.isBlockEthIcon.setValue(true);
            transferViewModel.transferAmount.setValue(transferAmount);
        }

        presenter = new TransferPresenter();
        presenter.init(this, getApplicationContext());

        transferAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        transferAdapter.addFragment(new TransferInfoFragment());
        transferAdapter.addFragment(new TransferConfirmFragment());
        transferViewPager.setAdapter(transferAdapter);
    }

    @OnClick(R.id.continue_transfer_button)
    public void continueButtonClicked() {
        TransferInfoFragment transferInfoFragment = (TransferInfoFragment) transferAdapter.getItem(0);
        TransferConfirmFragment transferConfirmFragment = (TransferConfirmFragment) transferAdapter.getItem(1);

        if (transferViewPager.getCurrentItem() == 0
                && transferInfoFragment.isHasNoError()) {
            goToConfirmTransfer();
            if (checkFingerprint()) {
                transferConfirmFragment.initFingerprint();
                setContinueButtonVisibility(View.INVISIBLE);
            } else {
                transferConfirmFragment.initPassword();
                setContinueButtonVisibility(View.VISIBLE);
            }
        } else if (transferViewPager.getCurrentItem() == 1) {
            getDataFromViewModel();
            if (presenter.passwordCheck(password)) {
                if (isEth)
                    transferAmount = new EthereumPrice(transferAmount, EthereumPrice.Currency.ETHER).inLongToString();
                else
                    transferAmount = new EthereumPrice(transferAmount, EthereumPrice.Currency.WEI).inLongToString();
                presenter.createTransaction(transferAmount, recipientAddress);
            } else {
                transferConfirmFragment.showError();
            }
        }
    }

    @OnClick(R.id.cancel_transfer_button)
    void cancelButtonClicked() {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void goToConfirmTransfer() {
        transferViewPager.setCurrentItem(1, true);
    }

    private void goToTransferInfo() {
        transferViewPager.setCurrentItem(0, true);
    }

    private void back() {
        if (transferViewPager.getCurrentItem() == 1) {
            TransferConfirmFragment transferConfirmFragment = (TransferConfirmFragment) transferAdapter.getItem(1);
            transferConfirmFragment.disposedFingerprint();
            setContinueButtonVisibility(View.VISIBLE);
            goToTransferInfo();
        } else {
            this.finish();
        }
    }

    @Override
    public void closeTransferActivity() {
        finish();
    }

    @Override
    public void showToast(String message) {
        ToastUtil.showToast(message);
    }

    private void getDataFromViewModel() {
        transferViewModel = ViewModelProviders.of(this).get(TransferViewModel.class);

        transferViewModel.recipientAddress.observe(this, s -> recipientAddress = s);
        transferViewModel.transferAmount.observe(this, s -> transferAmount = s);
        transferViewModel.senderPassword.observe(this, s -> password = s);
        transferViewModel.isEth.observe(this, aBoolean -> isEth = aBoolean);
        transferViewModel.isBlockEthIcon.observe(this, aBoolean -> isBlockEth = aBoolean);
    }

    private boolean checkFingerprint() {
        return RxFingerprint.isAvailable(this) && Hawk.contains(Constants.ENCRYPTED_PASSWORD);
    }

    public void setContinueButtonVisibility(int type) {
        continueButton.setVisibility(type);
    }
}
