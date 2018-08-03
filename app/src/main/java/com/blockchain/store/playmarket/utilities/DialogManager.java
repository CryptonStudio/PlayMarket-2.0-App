package com.blockchain.store.playmarket.utilities;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockchain.store.playmarket.Application;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.AppInfo;
import com.blockchain.store.playmarket.data.entities.UserReview;
import com.blockchain.store.playmarket.data.types.EthereumPrice;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mtramin.rxfingerprint.RxFingerprint;
import com.orhanobut.hawk.Hawk;

import java.math.BigDecimal;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class DialogManager {
    private static final String TAG = "DialogManager";

    private EditText folderNamedText;
    private EditText passwordText;

    public void showPurchaseDialog(App app, Context context, PurchaseDialogCallback callback) {
        String accountBalanceInWei = AccountManager.getUserBalance();

        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.purchase_confirm_dialog);

        SimpleDraweeView appIcon = dialog.findViewById(R.id.appIcon);
        TextView appTitleText = dialog.findViewById(R.id.appTitleText);
        TextView priceText = dialog.findViewById(R.id.priceText);
        TextView balanceText = dialog.findViewById(R.id.balanceText);
        Button continueButton = dialog.findViewById(R.id.continue_button);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        EditText passwordText = dialog.findViewById(R.id.password_editText);

        View fingerPrintHolder = dialog.findViewById(R.id.fingerprint_holder);


        Disposable fingerprintDisposable = Disposables.empty();
        if (FingerprintUtils.isFingerprintAvailibility(context)) {
            fingerPrintHolder.setVisibility(View.VISIBLE);
            fingerprintDisposable = RxFingerprint.decrypt(context, Hawk.get(Constants.ENCRYPTED_PASSWORD))
                    .subscribe(fingerprintDecryptionResult -> {
                        switch (fingerprintDecryptionResult.getResult()) {
                            case FAILED:
                                passwordText.setError(context.getResources().getString(R.string.fingerprint_not_recognized));
                                passwordText.requestFocus();
                                break;
                            case HELP:
                                break;
                            case AUTHENTICATED:
                                if (new BigDecimal(accountBalanceInWei).compareTo(new BigDecimal(app.price)) == 1) {
                                    try {
                                        passwordText.setText(fingerprintDecryptionResult.getDecrypted());
                                        Application.keyManager.getKeystore().unlock(Application.keyManager.getAccounts().get(0), passwordText.getText().toString());
                                        dialog.dismiss();
                                        callback.onPurchaseClicked();
                                    } catch (Exception e) {
                                        passwordText.setError(context.getString(R.string.wrong_password));
                                    }


                                } else {
                                    balanceText.setError(context.getString(R.string.not_enought_balance));
                                    balanceText.requestFocus();
                                }
                                break;
                        }
                    }, throwable -> {
                        //noinspection StatementWithEmptyBody
                        if (RxFingerprint.keyInvalidated(throwable)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the fingerprints stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "decrypt", throwable);
                    });
        }


        appIcon.setImageURI(app.getIconUrl());
        appTitleText.setText(app.nameApp);
        balanceText.setText(new EthereumPrice(accountBalanceInWei).inEther().toString());
        priceText.setText(new EthereumPrice(app.price).inEther().toString());

        continueButton.setOnClickListener(v -> {
            if (new BigDecimal(accountBalanceInWei).compareTo(new BigDecimal(app.price)) == 1) {
                try {
                    Application.keyManager.getKeystore().unlock(Application.keyManager.getAccounts().get(0), passwordText.getText().toString());
                    dialog.dismiss();
                    callback.onPurchaseClicked();
                } catch (Exception e) {
                    passwordText.setError(context.getString(R.string.wrong_password));
                }


            } else {
                balanceText.setError(context.getString(R.string.not_enought_balance));
                balanceText.requestFocus();
            }
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showReviewDialog(UserReview userReview, Context context, CreateReviewCallback callback) {
        String accountBalanceInWei = AccountManager.getUserBalance();
        String accountBalanceAsString = new EthereumPrice(accountBalanceInWei).inEther().toString();
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.invest_amount_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
        TextView userName = dialog.findViewById(R.id.user_name);
        TextView userCommentary = dialog.findViewById(R.id.user_commentary);
        TextView readMore = dialog.findViewById(R.id.read_more);
        LinearLayout replayReviewHolder = dialog.findViewById(R.id.replay_review_holder);
        EditText commentary = dialog.findViewById(R.id.commentary);
        MaterialRatingBar ratingBar = dialog.findViewById(R.id.user_rating_bar);
        TextView yourBalanceText = dialog.findViewById(R.id.your_balance);
        EditText passwordField = dialog.findViewById(R.id.password_field);
        TextView fingerPrintTitle = dialog.findViewById(R.id.fingerprint_info_textView);
        Button continueButton = dialog.findViewById(R.id.continue_button);
        Button closeButton = dialog.findViewById(R.id.cancelButton);


        Disposable fingerprintDisposable = Disposables.empty();

        if (userReview == null) {
            replayReviewHolder.setVisibility(View.GONE);
        } else {

            userName.setText(userReview.voter);
            userCommentary.setText(userReview.description);
            ratingBar.setRating(Float.parseFloat(userReview.vote));

        }

        yourBalanceText.setText(String.format(context.getString(R.string.your_balance_is), accountBalanceAsString));

        if (FingerprintUtils.isFingerprintAvailibility(context)) {
            fingerPrintTitle.setVisibility(View.VISIBLE);
            fingerprintDisposable = RxFingerprint.decrypt(context, Hawk.get(Constants.ENCRYPTED_PASSWORD))
                    .subscribe(fingerprintDecryptionResult -> {
                        switch (fingerprintDecryptionResult.getResult()) {
                            case FAILED:
                                passwordField.setError(context.getResources().getString(R.string.fingerprint_not_recognized));
                                passwordField.requestFocus();
                                break;
                            case HELP:
                                break;
                            case AUTHENTICATED:
                                //todo add check if comment isn't empty
                                if (new BigDecimal(accountBalanceInWei).compareTo(new BigDecimal("0")) == 1) {
                                    try {
                                        passwordField.setText(fingerprintDecryptionResult.getDecrypted());
                                        Application.keyManager.getKeystore().unlock(Application.keyManager.getAccounts().get(0), passwordField.getText().toString());
                                        dialog.dismiss();
                                        callback.onReviewInfoReady(commentary.getText().toString(), String.valueOf(ratingBar.getRating()));
                                    } catch (Exception e) {
                                        passwordField.setError(context.getString(R.string.wrong_password));
                                    }

                                } else {
                                    passwordField.setError(context.getString(R.string.not_enought_balance));
                                    passwordField.requestFocus();
                                }
                                break;
                        }
                    }, throwable -> {
                        //noinspection StatementWithEmptyBody
                        if (RxFingerprint.keyInvalidated(throwable)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the fingerprints stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "decrypt", throwable);
                    });
        }


        closeButton.setOnClickListener(v -> dialog.dismiss());

        continueButton.setOnClickListener(v -> {

            if (new BigDecimal(accountBalanceInWei).compareTo(new BigDecimal("0")) == 1) {
                try {
                    Application.keyManager.getKeystore().unlock(Application.keyManager.getAccounts().get(0), passwordField.getText().toString());
                    dialog.dismiss();
                    callback.onReviewInfoReady(commentary.getText().toString(), String.valueOf(ratingBar.getRating()));
                } catch (Exception e) {
                    passwordField.setError(context.getString(R.string.wrong_password));
                }

            } else {
                passwordField.setError(context.getString(R.string.not_enought_balance));
                passwordField.requestFocus();
            }

        });
        dialog.show();
    }

//    private void setReadMoreLogic(TextView userCommentary) {
//        if (userCommentary.getMaxLines() == 2) {
//            textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 10);
//            readMore.setText(itemView.getContext().getString(R.string.read_less));
//        } else if (userCommentary.getMaxLines() == 10) {
//            textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 2);
//            readMore.setText(itemView.getContext().getString(R.string.read_more));
//        }
//
//        if (textDescriptionAnimator != null && !textDescriptionAnimator.isStarted()) {
//            textDescriptionAnimator.setDuration(Constants.USER_REVIEW_EXPAND_ANIMATION_MILLIS).start();
//        }
//    }

    public AlertDialog showCreateFolderDialog(Context context, String folderName, CreateFolderDialogCallback callback) {

        AlertDialog createFolderDialog = new AlertDialog.Builder(context)
                .setView(R.layout.create_folder_dialog)
                .setCancelable(false)
                .create();
        createFolderDialog.show();

        folderNamedText = (EditText) createFolderDialog.findViewById(R.id.folder_editText);
        Button confirmButton = (Button) createFolderDialog.findViewById(R.id.confirm_create_button);
        Button cancelButton = (Button) createFolderDialog.findViewById(R.id.cancel_create_button);
        TextInputLayout passwordLayout = (TextInputLayout) createFolderDialog.findViewById(R.id.folder_inputLayout);

        folderNamedText.setText(folderName);

        confirmButton.setOnClickListener(v -> {
            if (folderNamedText.getText().toString().equals("")) {
                passwordLayout.setErrorEnabled(true);
                passwordLayout.setError(context.getResources().getString(R.string.empty_field));
            } else {
                callback.createFolderClicked(folderNamedText.getText().toString());
                createFolderDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> {
            createFolderDialog.dismiss();
        });
        return createFolderDialog;
    }

    public String getFolderNameText() {
        return folderNamedText.getText().toString();
    }


    public String getPasswordText() {
        return passwordText.getText().toString();
    }


    public interface PurchaseDialogCallback {
        void onPurchaseClicked();
    }

    public interface InvestDialogCallback {
        void onInvestClicked(String investAmount);
    }

    public interface CreateFolderDialogCallback {
        void createFolderClicked(String folderName);
    }

    public interface ConfirmImportDialogCallback {
        void onImportSuccessful();
    }

    public interface CreateReviewCallback {
        void onReviewInfoReady(String review, String rating);
    }

    public enum DialogNames {
        CREATE_FOLDER_DIALOG,
        CONFIRM_IMPORT_DIALOG
    }
}
