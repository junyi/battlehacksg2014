package com.arunana.socialcharity.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Adapter;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class LinearListView extends LinearLayout {

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    private Adapter adapter;
    private Observer observer;
    private int choiceMode;
    private OnItemLongClickListener listener;
    private SparseBooleanArray checkStates;

    {
        observer = new Observer(this);
        checkStates = new SparseBooleanArray();
        choiceMode = CHOICE_MODE_NONE;
    }

    public LinearListView(Context context) {
        super(context);
    }

    public LinearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
    }


    public void setAdapter(Adapter adapter) {
        if (this.adapter != null)
            this.adapter.unregisterDataSetObserver(observer);
        this.adapter = adapter;
        adapter.registerDataSetObserver(observer);
        observer.onChanged();
    }

    public Adapter getAdapter() {
        return this.adapter;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position, long id);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.listener = listener;
    }

    public void onItemLongClick(View view) {
        if (listener != null) {
            int position = getChildIndex(view);
            long id = adapter.getItemId(position);
            listener.onItemLongClick(view, position, id);
        }
    }


    public void onItemCheckedChanged(View view, boolean checked) {
        if (choiceMode == CHOICE_MODE_NONE)
            return;

        int position = getChildIndex(view);

        if (choiceMode == CHOICE_MODE_MULTIPLE) {
            checkStates.put(position, checked);

        } else if (choiceMode == CHOICE_MODE_SINGLE) {
            checkStates.clear();
            if (checked) {
                checkStates.put(position, true);
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    if (v != view && v instanceof Checkable)
                        ((Checkable) v).setChecked(false);
                }
            }
        }

    }


    public void setItemChecked(int position, boolean checked) {

        if (choiceMode == CHOICE_MODE_NONE)
            return;

        if (choiceMode == CHOICE_MODE_MULTIPLE) {
            checkStates.put(position, checked);
            View view = getChildAt(position);
            if (view instanceof Checkable)
                ((Checkable) view).setChecked(checked);

        } else if (choiceMode == CHOICE_MODE_SINGLE) {
            checkStates.clear();
            if (checked) {
                checkStates.put(position, true);
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = getChildAt(i);
                    if (view instanceof Checkable)
                        ((Checkable) view).setChecked(i == position);
                }
            }
        }
    }

    public boolean isAnyItemChecked() {

        if (choiceMode == CHOICE_MODE_NONE)
            return false;

        int size = checkStates.size();
        for (int i = 0; i < size; i++)
            if (checkStates.get(i))
                return true;

        return false;
    }

    public SparseBooleanArray getCheckStates() {
        return checkStates;
    }


    private int getChildIndex(View view) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) == view)
                return i;
        }
        return -1;
    }



    private class Observer extends DataSetObserver {

        private LinearListView context;

        public Observer(LinearListView context) {
            this.context = context;
        }

        @Override
        public void onChanged() {

            int childCount = context.getChildCount();
            List<View> oldViews = new ArrayList<View>(childCount);
            for (int i = 0; i < childCount; i++)
                oldViews.add(context.getChildAt(i));
            Iterator<View> iterator = oldViews.iterator();

            //context.removeAllViews();

            int adapterCount = context.adapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                View convertView = (iterator.hasNext() ? iterator.next() : null);
                View view = context.adapter.getView(i, convertView, context);
                if (convertView == null)
                    context.addView(view);
                if (choiceMode != CHOICE_MODE_NONE && view instanceof Checkable)
                    ((Checkable) view).setChecked(checkStates.get(i));
            }

            for (int i = adapterCount; i < childCount; i++)
                context.removeViewAt(i);
        }

        @Override
        public void onInvalidated() {
            //context.removeAllViews();
        }
    }

}