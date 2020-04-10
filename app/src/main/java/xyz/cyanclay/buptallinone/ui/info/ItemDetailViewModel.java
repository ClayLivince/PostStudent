package xyz.cyanclay.buptallinone.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import xyz.cyanclay.buptallinone.network.info.InfoManager;

public class ItemDetailViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private final MutableLiveData<InfoManager.InfoItem> item = new MutableLiveData<>();

    public LiveData<InfoManager.InfoItem> getItem() {
        return item;
    }

    public void setItem(InfoManager.InfoItem item) {
        this.item.setValue(item);
    }
}
