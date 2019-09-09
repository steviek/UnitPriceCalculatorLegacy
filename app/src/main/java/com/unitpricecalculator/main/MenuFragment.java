package com.unitpricecalculator.main;

import android.os.Bundle;
import androidx.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.inject.FragmentScoped;
import dagger.android.ContributesAndroidInjector;
import javax.inject.Inject;

public final class MenuFragment extends BaseFragment {

    @dagger.Module
    public interface Module {
        @ContributesAndroidInjector
        @FragmentScoped
        MenuFragment contributeAndroidInjector();
    }

    @Inject Callback callback;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, /* attachToRoot= */ false);
        bindView(view, R.id.btn_new, MenuEvent.NEW);
        bindView(view, R.id.btn_saved, MenuEvent.SAVED);
        bindView(view, R.id.btn_feedback, MenuEvent.FEEDBACK);
        bindView(view, R.id.btn_rate, MenuEvent.RATE);
        bindView(view, R.id.btn_share, MenuEvent.SHARE);
        bindView(view, R.id.btn_settings, MenuEvent.SETTINGS);

        view.findViewById(R.id.btn_share).setVisibility(View.GONE);

        return view;
    }

    private void bindView(View view, @IdRes int buttonId, MenuEvent menuEvent) {
        view.findViewById(buttonId).setOnClickListener(v -> callback.onMenuEvent(menuEvent));
    }

    public enum MenuEvent {
        NEW,
        FEEDBACK,
        RATE,
        SAVED,
        SETTINGS,
        SHARE
    }

    public interface Callback {
        void onMenuEvent(MenuEvent event);
    }
}
