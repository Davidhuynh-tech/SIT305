package com.example.lostandfound;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfound.databinding.FragmentItemListBinding;
import com.example.lostandfound.storage.LostFoundDatabase;
import com.example.lostandfound.storage.LostFoundItem;
import com.example.lostandfound.storage.LostFoundItemDao;

public class ItemListFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    private FragmentItemListBinding binding;
    private LostFoundItemDao itemDao;
    private java.util.List<String> filterCategories;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        itemDao = LostFoundDatabase.getInstance(requireContext()).lostFoundItemDao();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBackHome.setOnClickListener(v ->
                NavHostFragment.findNavController(ItemListFragment.this).navigateUp());

        binding.buttonAddNewPost.setOnClickListener(v ->
                NavHostFragment.findNavController(ItemListFragment.this)
                        .navigate(R.id.action_itemListFragment_to_addItemFragment));

        setupCategoryFilter();
        renderList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            renderList();
        }
    }

    private void renderList() {
        String selectedCategory = binding.spinnerFilterCategory.getSelectedItem() == null
                ? getString(R.string.all_categories)
                : binding.spinnerFilterCategory.getSelectedItem().toString();
        java.util.List<LostFoundItem> items = selectedCategory.equals(getString(R.string.all_categories))
                ? itemDao.getAllItems()
                : itemDao.getItemsByCategory(selectedCategory);

        if (items.isEmpty()) {
            binding.textEmptyState.setVisibility(View.VISIBLE);
            binding.listItems.setAdapter(null);
            return;
        }

        binding.textEmptyState.setVisibility(View.GONE);
        java.util.List<String> display = new java.util.ArrayList<>();
        for (LostFoundItem item : items) {
            String recent = DateUtils.getRelativeTimeSpanString(
                    item.getCreatedAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            ).toString();
            display.add(item.getPostType()
                    + " - "
                    + item.getDescription()
                    + " ("
                    + item.getCategory()
                    + ", "
                    + recent
                    + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                display
        );
        binding.listItems.setAdapter(adapter);
        binding.listItems.setOnItemClickListener((parent, view, position, id) -> {
            long itemId = items.get(position).getId();
            Bundle args = new Bundle();
            args.putLong(ARG_ITEM_ID, itemId);
            NavHostFragment.findNavController(ItemListFragment.this)
                    .navigate(R.id.action_itemListFragment_to_removeItemFragment, args);
        });
    }

    private void setupCategoryFilter() {
        filterCategories = new java.util.ArrayList<>();
        filterCategories.add(getString(R.string.all_categories));
        filterCategories.addAll(java.util.Arrays.asList(getResources().getStringArray(R.array.item_categories)));
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterCategories
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterCategory.setAdapter(filterAdapter);
        binding.spinnerFilterCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                renderList();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // no-op
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
