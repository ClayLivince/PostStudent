package xyz.cyanclay.poststudent.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItems;

public class ItemListViewModel extends ViewModel {

    public InfoItems notices;

    private MutableLiveData<String> mText;

    public ItemListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}