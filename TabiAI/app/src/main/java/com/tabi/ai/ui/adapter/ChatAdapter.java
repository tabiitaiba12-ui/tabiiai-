package com.tabi.ai.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tabi.ai.R;
import com.tabi.ai.data.local.ChatMessageEntity;
import com.tabi.ai.utils.Constants;
import com.tabi.ai.utils.DateTimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter rendering user messages and Tabi's replies as
 * distinct chat bubbles.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI = 1;

    private final List<ChatMessageEntity> messages = new ArrayList<>();

    public void submitList(List<ChatMessageEntity> newMessages) {
        ChatDiffCallback diffCallback = new ChatDiffCallback(this.messages, newMessages);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.messages.clear();
        this.messages.addAll(newMessages);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSender() == Constants.SENDER_USER ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageEntity message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTimestamp;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(ChatMessageEntity message) {
            tvMessage.setText(message.getMessage());
            tvTimestamp.setText(DateTimeHelper.getBubbleTimestamp(message.getTimestamp()));
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTimestamp;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(ChatMessageEntity message) {
            tvMessage.setText(message.getMessage());
            tvTimestamp.setText(DateTimeHelper.getBubbleTimestamp(message.getTimestamp()));
        }
    }

    private static class ChatDiffCallback extends DiffUtil.Callback {
        private final List<ChatMessageEntity> oldList;
        private final List<ChatMessageEntity> newList;

        ChatDiffCallback(List<ChatMessageEntity> oldList, List<ChatMessageEntity> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ChatMessageEntity oldItem = oldList.get(oldItemPosition);
            ChatMessageEntity newItem = newList.get(newItemPosition);
            return oldItem.getMessage().equals(newItem.getMessage())
                    && oldItem.getSender() == newItem.getSender()
                    && oldItem.getTimestamp() == newItem.getTimestamp();
        }
    }
}
