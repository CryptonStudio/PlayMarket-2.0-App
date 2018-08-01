package com.blockchain.store.playmarket.adapters;

import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.entities.UserReview;
import com.blockchain.store.playmarket.utilities.Blockies;
import com.blockchain.store.playmarket.utilities.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class UserReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "UserReviewAdapter";
    private static final int TYPE_USER_REPLY = 0;
    private static final int TYPE_USER_REVIEW = 1;
    ArrayList<UserReview> userReviews;
    int expandedItem = -1;
    UserReviewCallback callback;

    public UserReviewAdapter(ArrayList<UserReview> userReviews, UserReviewCallback callback) {
        this.callback = callback;
        this.userReviews = userReviews;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= userReviews.size()) {
            return TYPE_USER_REPLY;
        } else {
            return TYPE_USER_REVIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER_REVIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_review_item, parent, false);
            return new UserReviewViewHolder(view);
        } else if (viewType == TYPE_USER_REPLY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_replay_item, parent, false);
            return new UserReplyViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserReviewViewHolder) {
            ((UserReviewViewHolder) holder).bind(userReviews.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return userReviews.size() + 1;
    }

    class UserReplyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rating_bar) MaterialRatingBar ratingBar;
        @BindView(R.id.rating) TextView ratingView;
        @BindView(R.id.action_btn) Button replyButton;
        @BindView(R.id.review_description) EditText reviewDescription;

        public UserReplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ratingBar.setOnRatingChangeListener((ratingBar, rating) -> ratingView.setText(String.valueOf(((int) rating))));
            replyButton.setOnClickListener(view -> callback.onReplyClicked(reviewDescription.getText().toString(), ratingView.getText().toString()));
        }

    }

    class UserReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_name) TextView userName;
        @BindView(R.id.user_rating_bar) MaterialRatingBar userRatingBar;
        @BindView(R.id.user_commentary) TextView userCommentary;
        @BindView(R.id.read_more) TextView readMore;
        @BindView(R.id.root_view) ConstraintLayout constraintLayout;

        private ObjectAnimator textDescriptionAnimator;
        private View itemView;

        public UserReviewViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            userCommentary.post(() -> setupViewAfterOnMeasure(userCommentary));
        }

        public void bind(UserReview userReview, int position) {
            userCommentary.setText(userReview.description);
            userName.setText(userReview.voter);
            try {
                userRatingBar.setRating(Float.parseFloat(userReview.vote));
            } catch (Exception e) {

            }
        }

        private void setupViewAfterOnMeasure(TextView userCommentary) {
            Layout userCommentaryLayout = userCommentary.getLayout();
            if (userCommentaryLayout != null) {
                int lineCount = userCommentaryLayout.getLineCount();
                int ellipsisCount = userCommentaryLayout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    readMore.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "UserReviewViewHolder: ellipsisCount " + ellipsisCount);
            }
            readMore.setOnClickListener(v -> setReadMoreLogic(userCommentary));
        }

        private void setReadMoreLogic(TextView userCommentary) {
            if (userCommentary.getMaxLines() == 2) {
                textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 10);
                readMore.setText(itemView.getContext().getString(R.string.read_less));
            } else if (userCommentary.getMaxLines() == 10) {
                textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 2);
                readMore.setText(itemView.getContext().getString(R.string.read_more));
            }

            if (textDescriptionAnimator != null && !textDescriptionAnimator.isStarted()) {
                textDescriptionAnimator.setDuration(Constants.USER_REVIEW_EXPAND_ANIMATION_MILLIS).start();
            }
        }


    }

    public interface UserReviewCallback {
        void onReplyClicked(String message, String vote);

        void onReplyOnReviewClicked(UserReview userReview, String message);
    }
}
