package com.example.xie.mockrecyclerview.mock;


import java.util.ArrayList;
import java.util.List;

public class MockAdapterHelper {
    final ArrayList<UpdateOp> mPendingUpdates = new ArrayList<UpdateOp>();
    final ArrayList<UpdateOp> mPostponedList = new ArrayList<UpdateOp>();

    private int mExistingUpdateTypes = 0;
    final Callback mCallback;
    final boolean mDisableRecycler;

    MockAdapterHelper(Callback callback) {
        this(callback, false);
    }

    MockAdapterHelper(Callback callback, boolean disableRecycler) {
        mCallback = callback;
        mDisableRecycler = disableRecycler;
//        mOpReorderer = new OpReorderer(this);
    }

    boolean onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        if (itemCount < 1) {
            return false;
        }
        mPendingUpdates.add(new UpdateOp(UpdateOp.UPDATE, positionStart, itemCount, payload));
        mExistingUpdateTypes |= UpdateOp.UPDATE;
        return mPendingUpdates.size() == 1;
    }

    void consumeUpdatesInOnePass() {
        consumePostponedUpdates();
    }

    void consumePostponedUpdates() {
        final int count = mPostponedList.size();
        for (int i = 0; i < count; i++) {
            mCallback.onDispatchSecondPass(mPostponedList.get(i));
        }
        recycleUpdateOpsAndClearList(mPostponedList);
        mExistingUpdateTypes = 0;
    }

    void recycleUpdateOpsAndClearList(List<UpdateOp> ops) {
        ops.clear();
    }
    void preProcess() {
        final int count = mPendingUpdates.size();
        for (int i = 0; i < count; i++) {
            UpdateOp op = mPendingUpdates.get(i);
            switch (op.cmd) {
                case UpdateOp.UPDATE:
                    applyUpdate(op);
                    break;
            }
        }
        mPendingUpdates.clear();
    }

    private void applyUpdate(UpdateOp op) {
        postponeAndUpdateViewHolders(op);
    }

    private void postponeAndUpdateViewHolders(UpdateOp op) {
        mPostponedList.add(op);
        switch (op.cmd) {
            case UpdateOp.UPDATE:
                mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
                break;
        }
    }

    public int findPositionOffset(int position) {
        return position;
    }

    static class UpdateOp {
        static final int UPDATE = 1 << 2;
        int cmd;
        int positionStart;
        Object payload;
        int itemCount;

        public UpdateOp(int cmd, int positionStart, int itemCount, Object payload) {
            this.cmd = cmd;
            this.positionStart = positionStart;
            this.itemCount = itemCount;
            this.payload = payload;
        }
    }

    interface Callback {

        MockRecyclerView.ViewHolder findViewHolder(int position);

        void offsetPositionsForRemovingInvisible(int positionStart, int itemCount);

        void offsetPositionsForRemovingLaidOutOrNewView(int positionStart, int itemCount);

        void markViewHoldersUpdated(int positionStart, int itemCount, Object payloads);

        void onDispatchFirstPass(UpdateOp updateOp);

        void onDispatchSecondPass(UpdateOp updateOp);

        void offsetPositionsForAdd(int positionStart, int itemCount);

        void offsetPositionsForMove(int from, int to);
    }
}
