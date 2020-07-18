package com.example.nzse.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nzse.R;
import com.example.nzse.drawable.FavoriteDrawable;
import com.example.nzse.model.Channel;
import com.example.nzse.util.Callbacks;
import com.example.nzse.util.Lists;
import com.example.nzse.viewmodel.MainViewModel;
import com.example.nzse.widget.ActionButton;
import com.example.nzse.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.nzse.util.Callbacks.forwardOnClick;

public class ChannelsFragment extends Fragment {
    private MainViewModel mViewModel;

    private Toolbar mToolbar;
    private ObjectAnimator mAnimatorRefresh;

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.fragment_channels, container, false);

        mToolbar = root.findViewById(R.id.toolbar);
        mRecyclerView = root.findViewById(R.id.recyclerView);

        mToolbar.getActionButton().setIcon(R.drawable.ic_search);
        mToolbar.getActionButton().setOnClickListener(forwardOnClick(this, "onClickActionButton"));

        mViewModel.getTooManyFavoriteChannels().observe(requireActivity(), Callbacks.<Void>forwardObserver(this, "onTooManyFavoriteChannels"));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new ChannelsAdapter(requireActivity(), mViewModel));

        return root;
    }

    @SuppressWarnings("unused")
    public void onClickActionButton(View v) {
        Toast.makeText(getContext(), R.string.search_not_impl, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("unused")
    public void onTooManyFavoriteChannels(Void v) {
        Toast.makeText(getContext(), R.string.too_many_favorites, Toast.LENGTH_LONG).show();
    }

    private static class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {
        private LifecycleOwner mLifecycleOwner;
        private MainViewModel mViewModel;

        private List<Channel> mChannels = new ArrayList<>();
        private List<String> mFavoriteChannels = new ArrayList<>();
        private String mSelectedChannel;

        private Map<String, ChannelViewHolder> mViewHolderMap = new HashMap<>();

        public ChannelsAdapter(LifecycleOwner lifecycleOwner, MainViewModel mainViewModel) {
            mLifecycleOwner = lifecycleOwner;
            mViewModel = mainViewModel;
            initObservers();
        }

        @NonNull
        @Override
        public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fragment_channels_item,
                    parent,
                    false
            );
            return new ChannelViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
            Channel channel = mChannels.get(position);

            boolean selected = channel.id.equals(mSelectedChannel);
            boolean favorite = mFavoriteChannels.contains(channel.id);
            holder.bindChannel(channel, mViewModel, selected, favorite);

            mViewHolderMap.put(channel.id, holder);
        }

        @Override
        public int getItemCount() {
            return mChannels.size();
        }

        private void initObservers() {
            mViewModel.getChannels().observe(mLifecycleOwner, Callbacks.<List<Channel>>forwardObserver(this, "onChannelsChanged"));
            mViewModel.getFavoriteChannels().observe(mLifecycleOwner, Callbacks.<List<String>>forwardObserver(this, "onFavoriteChannelsChanged"));
            mViewModel.getSelectedChannel().observe(mLifecycleOwner, Callbacks.<String>forwardObserver(this, "onSelectedChannelChanged"));
        }

        @SuppressWarnings("unused")
        public void onChannelsChanged(List<Channel> channels) {
            mChannels = channels;
            notifyDataSetChanged();
        }

        @SuppressWarnings("unused")
        public void onFavoriteChannelsChanged(List<String> favorites) {
            List<String> removedFavs = Lists.difference(mFavoriteChannels, favorites);
            List<String> addedFavs = Lists.difference(favorites, mFavoriteChannels);

            // Kopie erzeugen
            mFavoriteChannels = new ArrayList<>(favorites);

            for(String removedFav : removedFavs){
                ChannelViewHolder holder = mViewHolderMap.get(removedFav);
                if(holder != null) {
                    holder.setFavorite(false);
                }
            }
            for(String addedFav : addedFavs) {
                ChannelViewHolder holder = mViewHolderMap.get(addedFav);
                if(holder != null) {
                    holder.setFavorite(true);
                }
            }
        }

        @SuppressWarnings("unused")
        public void onSelectedChannelChanged(String selectedChannel) {
            if(!selectedChannel.equals(mSelectedChannel)) {
                ChannelViewHolder holder = mViewHolderMap.get(mSelectedChannel);
                if(holder != null) {
                    holder.setSelected(false);
                }

                mSelectedChannel = selectedChannel;

                holder = mViewHolderMap.get(mSelectedChannel);
                if(holder != null) {
                    holder.setSelected(true);
                }
            }
        }

        static class ChannelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private static final float BUTTON_MINIMUM_SCALE = 0.2f;
            private static final float BUTTON_DEFAULT_SCALE = 1f;

            private Channel mChannel;
            private MainViewModel mViewModel;

            private TextView mTextViewProvider;
            private TextView mTextViewProgram;
            private ActionButton mActionButton;
            private FavoriteDrawable mFavoriteDrawable;

            private ObjectAnimator mObjectAnimator;

            ChannelViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                mTextViewProvider = itemView.findViewById(R.id.textView_provider);
                mTextViewProgram = itemView.findViewById(R.id.textView_program);
                mActionButton = itemView.findViewById(R.id.actionButton);

                mFavoriteDrawable = (FavoriteDrawable) mActionButton.getIcon();
                mActionButton.setOnClickListener(forwardOnClick(this, "onClickFavorite"));
            }

            public void bindChannel(Channel channel, MainViewModel viewModel, boolean selected, boolean favorite) {
                mChannel = channel;
                mViewModel = viewModel;

                mTextViewProvider.setText(channel.provider);
                mTextViewProgram.setText(channel.program);
                setInitialSelected(selected);
                setInitialFavorite(favorite);
            }

            public void setInitialSelected(boolean selected) {
                if(mObjectAnimator != null) mObjectAnimator.cancel();

                itemView.setSelected(selected);
                mActionButton.setVisibility(selected ? View.VISIBLE : View.GONE);
                mActionButton.setScaleX(selected ? BUTTON_DEFAULT_SCALE : BUTTON_MINIMUM_SCALE);
                mActionButton.setScaleY(selected ? BUTTON_DEFAULT_SCALE : BUTTON_MINIMUM_SCALE);
                mActionButton.setAlpha(selected ? 1f : 0f);
            }

            public void setSelected(final boolean selected) {
                if(mObjectAnimator != null) mObjectAnimator.cancel();

                itemView.setSelected(selected);

                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", mActionButton.getScaleX(), selected ? BUTTON_DEFAULT_SCALE : BUTTON_MINIMUM_SCALE);
                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", mActionButton.getScaleY(), selected ? BUTTON_DEFAULT_SCALE : BUTTON_MINIMUM_SCALE);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", mActionButton.getAlpha(), selected ? 1f : 0f);
                mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionButton, scaleX, scaleY, alpha);
                mObjectAnimator.setDuration(200);
                mObjectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                mObjectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mActionButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!selected) {
                            mActionButton.setVisibility(View.GONE);
                        }
                    }
                });
                mObjectAnimator.start();
            }

            private void setInitialFavorite(boolean favorite) {
                mFavoriteDrawable.setProgress(favorite ? 1 : 0);
            }

            public void setFavorite(boolean favorite) {
                mFavoriteDrawable.setEnabled(favorite);
            }

            @Override
            public void onClick(View v) {
                mViewModel.selectChannel(mChannel.id);
            }

            @SuppressWarnings("unused")
            public void onClickFavorite(View v) {
                mViewModel.toggleFavoriteChannel(mChannel.id);
            }
        };
    }
}
