package com.rain.networkproxy.ui.filter;

import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.FakeResourceProvider;
import com.rain.networkproxy.helper.ResourceProvider;
import com.rain.networkproxy.storage.FakeFilterStorage;
import com.rain.networkproxy.storage.FilterItem;
import com.rain.networkproxy.storage.FilterStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class FilterDialogViewModelTest {
    private static final String SELECT_ALL = String.valueOf(R.string.network_proxy_select_all);
    private final FilterStorage filterStorage = new FakeFilterStorage();
    private final ResourceProvider resourceProvider = new FakeResourceProvider();

    private FilterDialogViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new FilterDialogViewModel(filterStorage, resourceProvider);
        viewModel.initialize();
    }

    @Test
    public void observeItems_empty() {
        filterStorage.storeItems(Collections.<FilterItem>emptyList());
        viewModel.observeItems()
                .test()
                .assertValue(Collections.<FilterItemViewModel>emptyList())
                .assertNoErrors()
                .assertNotComplete()
                .dispose();
    }

    @Test
    public void observeItems_shouldReturnSelectAndItems() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void observeItems_shouldReturnSelectAndAllItemsActive() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", true),
                new FilterItem("/comments", true)
        ));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, true),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", true),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void observeItems_shouldReturnSelectAndAllItemsNotActive() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", false),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", false)
        ));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos", false),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", false)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void selectAllItems() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Update(0, true));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, true),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", true),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void deselectAllItems() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Update(0, false));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos", false),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", false)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void removeNonExistItem() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Remove(4));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void removeItem() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Remove(2));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, true),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void updateItem() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Update(2, true));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, true),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", true),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void updateNonExistItem() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Update(4, true));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @Test
    public void addItem() {
        filterStorage.storeItems(Arrays.asList(
                new FilterItem("/todos", true),
                new FilterItem("/todos/*", false),
                new FilterItem("/comments", true)
        ));
        viewModel.post(new FilterAction.Add("/todos/*/comments"));
        viewModel.observeItems()
                .test()
                .assertValue(Arrays.asList(
                        new FilterItemViewModel(SELECT_ALL, false),
                        new FilterItemViewModel("/todos/*/comments", true),
                        new FilterItemViewModel("/todos", true),
                        new FilterItemViewModel("/todos/*", false),
                        new FilterItemViewModel("/comments", true)
                ))
                .assertNotComplete()
                .assertNoErrors()
                .dispose();
    }

    @After
    public void tearDown() {
        viewModel.dispose();
    }
}
