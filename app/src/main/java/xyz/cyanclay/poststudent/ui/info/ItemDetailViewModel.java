package xyz.cyanclay.poststudent.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import xyz.cyanclay.poststudent.network.info.InfoManager;

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
