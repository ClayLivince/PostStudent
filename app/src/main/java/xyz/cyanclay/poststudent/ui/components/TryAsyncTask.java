package xyz.cyanclay.poststudent.ui.components;

import android.os.AsyncTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class TryAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private static final Logger logger = LogManager.getLogger(TryAsyncTask.class);

    public TryAsyncTask() {
        super();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        try {
            postExecute(result);
        } catch (Exception e) {
            logger.error("Error Happened on Handling Post Execution Progress: ", e);
        }
    }

    protected abstract void postExecute(Result result) throws Exception;

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled();
        try {
            cancelled(result);
        } catch (Exception e) {
            logger.error("Error Happened on Handling Cancelled Progress With Params: " + result, e);
        }
    }

    protected void cancelled(Result result) throws Exception {
    }
}
