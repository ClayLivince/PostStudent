package xyz.cyanclay.buptallinone.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import xyz.cyanclay.buptallinone.network.InfoManager;

public class ItemListViewModel extends ViewModel {

    public InfoManager.InfoItems notices;

    private MutableLiveData<String> mText;

    public ItemListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}