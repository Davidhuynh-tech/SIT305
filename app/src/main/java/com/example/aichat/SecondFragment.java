package com.example.aichat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aichat.data.AppDatabase;
import com.example.aichat.data.ChatMessage;
import com.example.aichat.data.ChatMessageDao;
import com.example.aichat.databinding.FragmentSecondBinding;
import com.example.aichat.network.ChatbotService;
import com.example.aichat.ui.ChatAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ChatAdapter chatAdapter;
    private ChatMessageDao chatMessageDao;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final ChatbotService chatbotService = new ChatbotService();
    private String username = "";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        username = resolveUsername();
        binding.chatTitle.setText(getString(R.string.chat_title_with_name, username));

        chatMessageDao = AppDatabase.getInstance(requireContext()).chatMessageDao();
        chatAdapter = new ChatAdapter();
        binding.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.messagesRecyclerView.setAdapter(chatAdapter);

        loadChatHistory();
        binding.sendButton.setOnClickListener(v -> sendMessage());
    }

    private String resolveUsername() {
        String fromArgs = getArguments() != null ? getArguments().getString("username", "") : "";
        if (!fromArgs.isEmpty()) {
            return fromArgs;
        }
        return requireContext()
                .getSharedPreferences("user_session_prefs", android.content.Context.MODE_PRIVATE)
                .getString("username", "");
    }

    private void loadChatHistory() {
        databaseExecutor.execute(() -> {
            List<ChatMessage> storedMessages = chatMessageDao.getAllMessages();
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                chatAdapter.setMessages(storedMessages);
                if (!storedMessages.isEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(storedMessages.size() - 1);
                }
            });
        });
    }

    private void sendMessage() {
        String message = binding.messageInput.getText() != null
                ? binding.messageInput.getText().toString().trim()
                : "";
        if (message.isEmpty()) {
            return;
        }

        binding.messageInput.setText("");
        ChatMessage userMessage = new ChatMessage(username, message, true, System.currentTimeMillis());
        saveAndRenderMessage(userMessage);

        chatbotService.getReply(username, message, new ChatbotService.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    ChatMessage botMessage = new ChatMessage("Bot", reply, false, System.currentTimeMillis());
                    saveAndRenderMessage(botMessage);
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    ChatMessage error = new ChatMessage("Bot", errorMessage, false, System.currentTimeMillis());
                    saveAndRenderMessage(error);
                });
            }
        });
    }

    private void saveAndRenderMessage(ChatMessage message) {
        chatAdapter.addMessage(message);
        binding.messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        databaseExecutor.execute(() -> chatMessageDao.insert(message));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

}