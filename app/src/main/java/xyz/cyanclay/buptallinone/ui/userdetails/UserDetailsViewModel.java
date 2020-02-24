package xyz.cyanclay.buptallinone.ui.userdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserDetailsViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public UserDetailsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is userDetails fragment");
    }



    public LiveData<String> getText() {
        return mText;
    }
}
