package xyz.cyanclay.poststudent.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class WebViewViewModel extends ViewModel {

    // TODO: Implement the ViewModel
    private final MutableLiveData<String> url = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, String>> cookies = new MutableLiveData<>(new HashMap<String, String>());

    public LiveData<String> getURL() {
        return url;
    }

    public void setUrl(String item) {
        this.url.setValue(item);
    }

    public void putCookies(Map<String, String> cookies) {
        this.cookies.getValue().putAll(cookies);
    }

}
