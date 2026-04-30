package com.example.lostandfound;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfound.databinding.FragmentRemoveItemBinding;
import com.example.lostandfound.storage.LostFoundDatabase;
import com.example.lostandfound.storage.LostFoundItem;
import com.example.lostandfound.storage.LostFoundItemDao;

public class RemoveItemFragment extends Fragment {

    private FragmentRemoveItemBinding binding;
    private LostFoundItemDao itemDao;
    private long itemId = -1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRemoveItemBinding.inflate(inflater, container, false);
        itemDao = LostFoundDatabase.getInstance(requireContext()).lostFoundItemDao();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBackToList.setOnClickListener(v ->
                NavHostFragment.findNavController(RemoveItemFragment.this).navigateUp());

        Bundle args = getArguments();
        if (args != null) {
            itemId = args.getLong(ItemListFragment.ARG_ITEM_ID, -1);
        }
        renderItem();

        binding.buttonRemove.setOnClickListener(v ->
                removeItem());
    }

    private void renderItem() {
        if (itemId == -1) {
            Toast.makeText(requireContext(), R.string.item_not_found, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(RemoveItemFragment.this).navigateUp();
            return;
        }

        LostFoundItem item = itemDao.getItemById(itemId);
        if (item == null) {
            Toast.makeText(requireContext(), R.string.item_not_found, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(RemoveItemFragment.this).navigateUp();
            return;
        }

        binding.textItemTitle.setText(item.getPostType() + " " + item.getDescription());
        String recent = DateUtils.getRelativeTimeSpanString(
                item.getCreatedAt(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
        binding.textItemWhen.setText(getString(R.string.item_recent_format, recent) + " | "
                + getString(R.string.item_date_format, item.getDate()));
        binding.textItemCategory.setText(getString(R.string.item_category_format, item.getCategory()));
        binding.textItemWhere.setText(getString(R.string.item_location_format, item.getLocation()));
        binding.imageItem.setImageURI(Uri.parse(item.getImageUri()));
    }

    private void removeItem() {
        if (itemId == -1) {
            return;
        }
        int deletedRows = itemDao.deleteItemById(itemId);
        if (deletedRows > 0) {
            Toast.makeText(requireContext(), R.string.item_removed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), R.string.remove_failed, Toast.LENGTH_SHORT).show();
        }
        NavHostFragment.findNavController(RemoveItemFragment.this)
                .navigate(R.id.action_removeItemFragment_to_itemListFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
