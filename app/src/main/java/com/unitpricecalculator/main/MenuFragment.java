package com.unitpricecalculator.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.view.DragLinearLayout;

public final class MenuFragment extends BaseFragment {

    private DragLinearLayout mDragLinearLayout;
    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        } else {
            throw new IllegalArgumentException(context + " must implement CallbackClass interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        view.findViewById(R.id.btn_new).setOnClickListener(new MenuEventClickListener(MenuEvent.NEW));
        view.findViewById(R.id.btn_saved).setOnClickListener(new MenuEventClickListener(MenuEvent.SAVED));
        view.findViewById(R.id.btn_feedback).setOnClickListener(new MenuEventClickListener(MenuEvent.FEEDBACK));
        view.findViewById(R.id.btn_rate).setOnClickListener(new MenuEventClickListener(MenuEvent.RATE));
        view.findViewById(R.id.btn_share).setOnClickListener(new MenuEventClickListener(MenuEvent.SHARE));
        view.findViewById(R.id.btn_settings).setOnClickListener(new MenuEventClickListener(MenuEvent.SETTINGS));

        return view;
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

    private class MenuEventClickListener implements View.OnClickListener {

        private final MenuEvent menuEvent;

        public MenuEventClickListener(MenuEvent menuEvent) {
            this.menuEvent = menuEvent;
        }

        @Override
        public void onClick(View v) {
            mCallback.onMenuEvent(menuEvent);
        }
    }
}
