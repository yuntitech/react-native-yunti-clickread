/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunti.clickread;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.badoo.mobile.util.WeakHandler;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yt.ytdeep.client.dto.ResPlayDTO;
import com.yunti.util.ResourceUtils;

import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class PlayerManager implements AdsMediaSource.MediaSourceFactory, Player.EventListener {

    private Fragment mFragment;
    private Context mContext;
    private final DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer mPlayer;
    private final OkHttpClient mClient;
    //
    private static final int SHOW_PROGRESS = 1;
    private static final long mProgressUpdateInterval = 250;
    //
    private boolean mPlayTracks = false;
    private Long mClickReadId = 0L;
    private List<ClickReadTrackinfo> trackInfoList;
    private ClickReadTrackinfo mPlayTrackInfo;
    private EventListener mEventListener;
    private WeakHandler mProgressHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mPlayer != null) {
                onProgress(mPlayer.getCurrentPosition());
                mProgressHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, mProgressUpdateInterval);
            }
            return false;
        }
    });

    public boolean isPlayTracks() {
        return mPlayTracks;
    }

    public void togglePlayTracks() {
        mPlayTracks = !mPlayTracks;
    }

    public void setClickReadId(Long clickReadId) {
        mClickReadId = clickReadId;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    public PlayerManager(Fragment fragment, Context context) {
        mFragment = fragment;
        mClient = OkHttpClientProvider.getOkHttpClient();
        mContext = context;
        dataSourceFactory =
                new DefaultDataSourceFactory(
                        mContext, Util.getUserAgent(mContext, ""));
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(this);
    }

    public void playTracks(List<ClickReadTrackinfo> trackInfoList) {
        this.mPlayTracks = true;
        this.trackInfoList = trackInfoList;
        ClickReadTrackinfo trackInfo = mPlayTrackInfo != null
                && trackInfoList.contains(mPlayTrackInfo) ?
                mPlayTrackInfo : trackInfoList.get(0);
        play(trackInfo);
        onSwitchTrack(trackInfo);
    }

    public void stopTracks() {
        this.mPlayTracks = false;
        mPlayer.setPlayWhenReady(false);
        if (this.trackInfoList != null) {
            this.trackInfoList = null;
        }
    }

    public void playAgain() {
        if (mPlayTrackInfo != null) {
            play(mPlayTrackInfo);
        }
    }

    public void play(final ClickReadTrackinfo trackInfo) {
        //视频
        if (Integer.valueOf(1).equals(trackInfo.getType())) {
            mPlayTrackInfo = trackInfo;
            RNYtClickreadModule.showVideo(mContext, mClickReadId, trackInfo);
        } else {
            Long prevResId = mPlayTrackInfo != null ? mPlayTrackInfo.getResId() : null;
            mPlayTrackInfo = trackInfo;
            mProgressHandler.removeMessages(SHOW_PROGRESS);
            //多个track同一资源时，不需要重新加载
            if (prevResId != null && prevResId.equals(trackInfo.getResId())) {
                seekTo(trackInfo);
            }
            //需要加载
            else {
                mPlayer.setPlayWhenReady(false);
                String audioFilePath = ResourceUtils.getAudioFilePath(mClickReadId,
                        trackInfo.getResId(), FetchInfo.USER_ID, mFragment.getContext());
                if (TextUtils.isEmpty(audioFilePath)) {
                    YTApi.fetch(FetchInfo.playV3(trackInfo.getResId(), trackInfo.getResSign()),
                            new YTApi.Callback<ResPlayDTO>() {
                                @Override
                                public void onFailure(int code, String errorMsg) {
                                    mPlayTrackInfo = null;
                                }

                                @Override
                                public void onResponse(int code, ResPlayDTO response) {
                                    play(trackInfo, response.getUrl());
                                }
                            }, mFragment);
                } else {
                    play(trackInfo, audioFilePath);
                }

            }
        }
    }

    public void pause() {
        mPlayTracks = false;
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mProgressHandler.removeMessages(SHOW_PROGRESS);
    }

    // AdsMediaSource.MediaSourceFactory implementation.

    @Override
    public MediaSource createMediaSource(Uri uri) {
        return buildMediaSource(uri);
    }

    @Override
    public int[] getSupportedTypes() {
        // IMA does not support Smooth Streaming ads.
        return new int[]{C.TYPE_HLS, C.TYPE_OTHER};
    }

    private void play(ClickReadTrackinfo trackInfo, String uri) {
        MediaSource contentMediaSource = buildMediaSource(Uri.parse(uri));
        mPlayer.prepare(contentMediaSource);
        seekTo(trackInfo);
        mPlayer.setPlayWhenReady(true);
    }

    // Internal methods.

    private MediaSource buildMediaSource(Uri uri) {
        @ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private void sendRequest(ClickReadTrackinfo trackInfo, Callback apiCallback) {
        FetchInfo.FetchInfoParams fetchInfoParams = FetchInfo.playV3(trackInfo.getResId(),
                trackInfo.getResSign());
        for (int n = 0; n < fetchInfoParams.getFormBody().size(); n++) {
            String name = fetchInfoParams.getFormBody().name(n);
            String val = fetchInfoParams.getFormBody().value(n);
        }
        Request request = new Request.Builder()
                .post(fetchInfoParams.getFormBody())
                .url(fetchInfoParams.getUrl())
                .build();
        mClient.newCall(request).enqueue(apiCallback);
    }

    private void onProgress(long currentPosition) {
        if (mPlayTrackInfo != null
                && mPlayTrackInfo.getPe() != null
                && currentPosition > mPlayTrackInfo.getPe()) {
            onTrackEnd();
        }
    }

    private void onTrackEnd() {
        mProgressHandler.removeMessages(SHOW_PROGRESS);
        if (mEventListener != null) {
            mEventListener.onTrackEnd();
        }
        //连读
        if (mPlayTracks && trackInfoList != null) {
            int trackIndex = trackInfoList.indexOf(mPlayTrackInfo);
            int trackNext = trackIndex + 1;
            if (trackIndex != -1 && trackNext < trackInfoList.size()) {
                ClickReadTrackinfo nextTrack = trackInfoList.get(trackNext);
                play(nextTrack);
                onSwitchTrack(nextTrack);
            } else if (trackNext >= trackInfoList.size()) {
                if (mEventListener != null) {
                    mEventListener.onTrackListEnd();
                }
            } else {
                mPlayer.setPlayWhenReady(false);
            }
        }
        //
        else {
            mPlayer.setPlayWhenReady(false);
        }
    }

    private void seekTo(ClickReadTrackinfo trackInfo) {
        if (mPlayer != null
                && trackInfo != null
                && trackInfo.getPs() != null) {
            mPlayer.seekTo(trackInfo.getPs());
        }
    }

    private void onSwitchTrack(ClickReadTrackinfo trackInfo) {
        if (mEventListener != null) {
            mEventListener.onSwitchTrack(trackInfo);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_IDLE:
                break;
            case Player.STATE_BUFFERING:
                break;
            case Player.STATE_READY:
                mProgressHandler.sendEmptyMessage(SHOW_PROGRESS);
                break;
            case Player.STATE_ENDED:
                mProgressHandler.removeMessages(SHOW_PROGRESS);
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {
        mPlayer.setPlayWhenReady(true);
    }


    public interface EventListener {

        void onTrackListEnd();

        void onTrackEnd();

        void onSwitchTrack(ClickReadTrackinfo trackInfo);
    }

}
