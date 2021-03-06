package com.satchlapp.adapter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.satchlapp.PaletteTransformation;
import com.satchlapp.R;
import com.satchlapp.activity.StoryPreviewActivity;
import com.satchlapp.model.Category;
import com.satchlapp.model.Story;
import com.satchlapp.model.User;
import com.satchlapp.util.CustomAnimation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Sara on 3/9/2017.
 */
public class StoryCardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Category category;
    private Context context;

    public StoryCardViewAdapter(Category category){
        this.category = category;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView;
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType){
            case Category.DISPLAY_TYPE_DETAILED:
                itemView = inflater.inflate(R.layout.list_item_detailed_story, parent, false);
                viewHolder = new DetailedStoryViewHolder(itemView);
                break;
            case Category.DISPLAY_TYPE_SIMPLE:
                itemView = inflater.inflate(R.layout.list_item_simple_story_front, parent, false);
                viewHolder = new SimpleStoryViewHolder(itemView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        final Story story = category.getStoryList().get(position);

        if(viewHolder instanceof DetailedStoryViewHolder){
            final DetailedStoryViewHolder castedViewHolder = (DetailedStoryViewHolder) viewHolder;

            User author = story.getAuthor();
            String name = author.getFirstName() + " " + author.getLastName();
            castedViewHolder.textViewAuthorsName.setText(name);
            Picasso.with(context)
                    .load(author.getProfilePictureUrl())
                    .into(castedViewHolder.imageViewAuthorProfile);

            final int[] swatchRgb = new int[1];
            Picasso.with(context)
                    .load(story.getCoverImageUrl())
                    .fit().centerCrop()
                    .transform(PaletteTransformation.getInstance())
                    .into(castedViewHolder.imageViewCoverImage, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) castedViewHolder.imageViewCoverImage.getDrawable()).getBitmap();
                            Palette palette = PaletteTransformation.getPalette(bitmap);
                            Palette.Swatch swatch = getColorSwatch(palette);
                            if (swatch != null) {
                                castedViewHolder.textViewTitle.setBackgroundColor(ColorUtils.setAlphaComponent(swatch.getRgb(), 166));
                                castedViewHolder.textViewTitle.setTextColor(ColorUtils.setAlphaComponent(swatch.getTitleTextColor(), 255));
                                swatchRgb[0] = swatch.getRgb();
                            }
                        }
                    });
            castedViewHolder.textViewTitle.setText(story.getTitle());
            castedViewHolder.textViewDescriptionShort.setText(story.getDescription());

            castedViewHolder.buttonExpandDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ObjectAnimator raiseElevation = ObjectAnimator.ofFloat(
                            castedViewHolder.cardViewButtonContainer,
                            "cardElevation",
                            30f);
                    raiseElevation.setDuration(125);

                    ObjectAnimator increaseMaxLines = ObjectAnimator.ofInt(
                            castedViewHolder.textViewDescriptionShort,
                            "maxLines",
                            15);
                    increaseMaxLines.setDuration(300);
                    increaseMaxLines.setInterpolator(new DecelerateInterpolator());

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(raiseElevation)
                            .with(increaseMaxLines);
                    animatorSet.start();

                    float cardWidth = castedViewHolder.cardView.getWidth();
                    float buttonWidth = castedViewHolder.linearLayoutButton.getWidth();
                    final float distance = (cardWidth - buttonWidth)/2;
                    castedViewHolder.linearLayoutButton.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    castedViewHolder.linearLayoutButton.setTranslationX(-distance);
                                    castedViewHolder.textViewButtonLabel.setText("Back");
                                    castedViewHolder.buttonExpandDescription.setRotation(90f);
                                    castedViewHolder.linearLayoutButton.animate()
                                            .alpha(1f)
                                            .setDuration(150);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });

                    castedViewHolder.buttonRead.setRotation(270f);
                    castedViewHolder.linearLayoutContinue.setAlpha(0f);
                    castedViewHolder.linearLayoutContinue.setVisibility(View.VISIBLE);
                    castedViewHolder.linearLayoutContinue.animate()
                            .alpha(1f)
                            .setDuration(300);


                    /*View parentView = (View)view.getParent().getParent();
                    View titleView = parentView.findViewById(R.id.listItemDetailedStoryTextViewTitle);
                    View descriptionView = parentView.findViewById(R.id.listItemDetailedStoryTextViewDescriptionShort);
                    int[] titleScreenLocation = new int[2];
                    int[] descriptionScreenLocation = new int[2];
                    titleView.getLocationOnScreen(titleScreenLocation);
                    descriptionView.getLocationOnScreen(descriptionScreenLocation);
                    Intent subActivity = new Intent(context, StoryPreviewActivity.class);
                    subActivity.
                            putExtra("titleLeft",titleScreenLocation[0]).
                            putExtra("titleTop", titleScreenLocation[1]).
                            putExtra("descriptionLeft", descriptionScreenLocation[0]).
                            putExtra("descriptionTop", descriptionScreenLocation[1]).
                            putExtra("titleWidth", titleView.getWidth()).
                            putExtra("titleHeight", titleView.getHeight()).
                            putExtra("descriptionWidth",descriptionView.getWidth()).
                            putExtra("descriptionHeight",descriptionView.getHeight()).
                            putExtra("title", story.getTitle()).
                            putExtra("description", story.getDescription()).
                            putExtra("titleColor", ((ColorDrawable)titleView.getBackground()).getColor());
                    context.startActivity(subActivity);

                    ((Activity)context).overridePendingTransition(0,0);*/
                }
            });
        } else if(viewHolder instanceof SimpleStoryViewHolder){
            final SimpleStoryViewHolder castedViewHolder = (SimpleStoryViewHolder) viewHolder;
            castedViewHolder.container.setOnClickListener(new View.OnClickListener() {
                private boolean cardFrontIsShowing = true;

                @Override
                public void onClick(View view) {
                    if(cardFrontIsShowing){
                        CustomAnimation.crossFadeViews(castedViewHolder.linearLayoutCardFront,
                                castedViewHolder.linearLayoutCardBack);

                        cardFrontIsShowing = false;
                    } else{
                        CustomAnimation.crossFadeViews(castedViewHolder.linearLayoutCardBack,
                                castedViewHolder.linearLayoutCardFront);

                        cardFrontIsShowing = true;
                    }
                }
            });
            Picasso.with(context)
                    .load(story.getCoverImageUrl())
                    .fit().centerCrop()
                    .transform(PaletteTransformation.getInstance())
                    .into(castedViewHolder.imageViewCoverImage, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) castedViewHolder.imageViewCoverImage.getDrawable()).getBitmap();
                            Palette palette = PaletteTransformation.getPalette(bitmap);
                            Palette.Swatch swatch = getColorSwatch(palette);
                            if (swatch != null) {
                                castedViewHolder.textViewTitle.setBackgroundColor(ColorUtils.setAlphaComponent(swatch.getRgb(), 166));
                                castedViewHolder.textViewTitle.setTextColor(ColorUtils.setAlphaComponent(swatch.getTitleTextColor(), 255));
                            }
                        }
                    });
            castedViewHolder.textViewTitle.setText(story.getTitle());
            castedViewHolder.textViewDescription.setText(story.getDescription());
        }
    }

    @Override
    public int getItemCount(){
        return category.getStoryList().size();
    }

    @Override
    public int getItemViewType(int position){
        return category.getDisplayType();
    }

    class DetailedStoryViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public TextView textViewTitle;
        public TextView textViewAuthorsName;
        public ImageView imageViewAuthorProfile;
        public TextView textViewDescriptionShort;
        public ImageView imageViewCoverImage;
        public CardView cardViewButtonContainer;
        public TextView textViewButtonLabel;
        public ImageButton buttonExpandDescription;
        public LinearLayout linearLayoutButton;
        public LinearLayout linearLayoutContinue;
        public ImageButton buttonRead;

        public DetailedStoryViewHolder(View itemView){
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.listItemDetailedStoryCardView);
            textViewTitle = (TextView) itemView.findViewById(R.id.listItemDetailedStoryTextViewTitle);
            textViewAuthorsName = (TextView) itemView.findViewById(R.id.listItemDetailedStoryTextViewAuthor);
            imageViewAuthorProfile = (ImageView) itemView.findViewById(R.id.listItemDetailedStoryImageViewAuthorProfile);
            textViewDescriptionShort = (TextView) itemView.findViewById(R.id.listItemDetailedStoryTextViewDescriptionShort);
            imageViewCoverImage = (ImageView) itemView.findViewById(R.id.listItemDetailedStoryImageViewCoverImage);
            textViewButtonLabel = (TextView) itemView.findViewById(R.id.listItemDetailedStoryTextViewButtonLabel);
            cardViewButtonContainer = (CardView) itemView.findViewById(R.id.listItemDetailedStoryButtonContainer);
            linearLayoutButton = (LinearLayout) itemView.findViewById(R.id.listItemDetailedStoryLinearLayoutButton);
            linearLayoutContinue = (LinearLayout) itemView.findViewById(R.id.listItemDetailedStoryLinearLayoutContinue);
            buttonRead = (ImageButton) itemView.findViewById(R.id.listItemDetailedStoryButtonRead);
            buttonExpandDescription = (ImageButton) itemView.findViewById(R.id.listItemDetailedStoryButtonExpandDescription);
        }
    }

    class SimpleStoryViewHolder extends RecyclerView.ViewHolder{
        private CardView container;
        private LinearLayout linearLayoutCardFront;
        private LinearLayout linearLayoutCardBack;
        private ImageView imageViewCoverImage;
        private TextView textViewTitle;
        private TextView textViewDescription;
        private Button buttonStoryTag1;
        private Button buttonStoryTag2;

        public SimpleStoryViewHolder(View itemView){
            super(itemView);
            container = (CardView) itemView.findViewById(R.id.listItemSimpleStoryCardView);
            linearLayoutCardFront = (LinearLayout) itemView.findViewById(R.id.listItemSimpleStoryCardFront);
            linearLayoutCardBack = (LinearLayout) itemView.findViewById(R.id.listItemSimpleStoryCardBack);
            imageViewCoverImage = (ImageView) itemView.findViewById(R.id.listItemSimpleStoryImageViewCoverImage);
            textViewTitle = (TextView) itemView.findViewById(R.id.listItemSimpleStoryTextViewTitle);
            textViewDescription = (TextView) itemView.findViewById(R.id.listItemSimpleStoryTextViewDescription);
            buttonStoryTag1 = (Button) itemView.findViewById(R.id.listItemSimpleStoryButtonStoryTag1);
            buttonStoryTag2 = (Button) itemView.findViewById(R.id.listItemSimpleStoryButtonStoryTag2);
        }
    }

    private Palette.Swatch getColorSwatch(Palette palette){
        //We want the vibrant swatch by default. Sometimes this fails, so we'll take any swatch
        //at that point. And if no swatch exists, we'll return a grey one.
        Palette.Swatch swatch = palette.getVibrantSwatch();

        //We also want to make sure the swatch selected is not the dominate color in the image.
        //It just looks bad.

        //It's set to false out of the if statement so that I can use it later.
        boolean swatchIsDominateColor = false;
        if(swatch != null) {
            swatchIsDominateColor = true;
            for (Palette.Swatch s : palette.getSwatches()) {
                if (s.getPopulation() > swatch.getPopulation()) {
                    swatchIsDominateColor = false;
                    break;
                }
            }
        }
        if(swatch == null || swatchIsDominateColor){
            for(Target target: palette.getTargets()){
                if(!target.equals(Target.VIBRANT)){
                    if(palette.getSwatchForTarget(target) != null){
                        swatch = palette.getSwatchForTarget(target);
                        break;
                    }
                }
            }
        }

        if(swatch == null){
            swatch = new Palette.Swatch(Color.GRAY,0);
        }

        return swatch;
    }
}
