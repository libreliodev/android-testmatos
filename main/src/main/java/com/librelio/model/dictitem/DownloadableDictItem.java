package com.librelio.model.dictitem;

import android.content.Context;

import com.librelio.storage.DownloadsManager;

public abstract class DownloadableDictItem extends DictItem {

//    protected int downloadStatus = DownloadStatusCode.NOT_DOWNLOADED;

    public int getDownloadStatus() {
        return new DownloadsManager(context).getDownloadStatus(getFilePath());
    }

//    public void setDownloadStatus(int downloadStatus) {
//        this.downloadStatus = downloadStatus;
//    }

    public abstract void onDownloadButtonClick(Context context);

}
