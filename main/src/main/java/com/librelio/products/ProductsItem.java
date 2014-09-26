package com.librelio.products;

import android.content.Context;

import com.librelio.LibrelioApplication;
import com.librelio.model.DownloadStatusCode;
import com.librelio.model.dictitem.DownloadableDictItem;
import com.librelio.model.interfaces.DisplayableAsGridItem;
import com.librelio.utils.StorageUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductsItem extends DownloadableDictItem implements DisplayableAsGridItem {
	
	private String subtitle;

	private int downloadStatus = DownloadStatusCode.NOT_DOWNLOADED;
	private Context context;

	public ProductsItem(Context context, String title, String subtitle, String fileName) {
		this.context = context;
		this.subtitle = subtitle;
		this.title = title;
		initValues(fileName);
	}
	
	private void initValues(String filePath) {
        String filePathWithoutQueriesAtEnd;
        Pattern actualFileNamePattern = Pattern.compile("(?=.*\\?)[^\\?]+");
        Matcher actualFileNameMatcher = actualFileNamePattern.matcher(filePath);
        if (actualFileNameMatcher.find()) {
            filePathWithoutQueriesAtEnd = actualFileNameMatcher.group();
        } else {
            filePathWithoutQueriesAtEnd = filePath;
        }
        this.filePath = filePathWithoutQueriesAtEnd;
        this.itemFilename = FilenameUtils.getName(filePathWithoutQueriesAtEnd);

        // may need to check for stuff like this - ?wabbar=yes
//        Pattern updateFrequencyPattern = Pattern.compile("waupdate=([0-9]+)");
//        Matcher updateFrequencyMatcher = updateFrequencyPattern.matcher(filePathFromPlist);
//        if (updateFrequencyMatcher.find()) {
//            parsedItem.setUpdateFrequency(Integer.parseInt(updateFrequencyMatcher.group(1)));
//        }

    }
	
	public String getItemStorageDir() {
		return StorageUtils.getStoragePath(context)
				+ FilenameUtils.getPath(filePath);
	}

	@Override
	protected void initOtherValues() {
		super.initOtherValues();
	}
	
	public boolean isPaid() {
		return filePath.contains("_.");
	}

	public String getSubtitle() {
		return subtitle;
	}
	
	public boolean isDownloaded() {
		return getLocalPathIfAvailable() == null ? false : true;
	}
	
	public String getLocalPathIfAvailable() {
		// check in assets
		String pngPath = filePath;
		try {
			InputStream file = context.getAssets().open(pngPath + ".zip");
			file.close();
			return "file:///android_asset/" + pngPath;
		} catch (IOException e) {
//			e.printStackTrace();
		}

		// check in local database folder
		File localDatabaseFile = new File(getDatabaseStoragePath());
		if (localDatabaseFile.exists()) {
			return getDatabaseStoragePath();
		}
		return null;
	}

	@Override
	public String getPngUri() {

		// TODO deal with _. in paid items
		
		// check in assets
		String pngPath = getFilePath().replace("sqlite", "png");
		try {
			InputStream file = context.getAssets().open(pngPath);
			file.close();
			return "file:///android_asset/" + pngPath;
		} catch (IOException e) {
//			e.printStackTrace();
		}

		// check in local file
		String localPngPath = getItemStorageDir() + FilenameUtils.getBaseName(filePath) + ".png";
		File localPngFile = new File(localPngPath);
		if (localPngFile.exists()) {
			return localPngPath;
		}

		// else return server url
		if (isPaid()) {
			return getItemUrl().replace("_.sqlite", ".png");
		} else {
			return getItemUrl().replace(".sqlite", ".png");
		}
	}

	@Override
	public void onThumbnailClick(Context context) {
		if (isDownloaded()) {
			ProductsActivity.startActivity(context, this);
		}
	}

    public String getItemFilePath() {
        return getItemStorageDir(context) + FilenameUtils.getName(filePath);
    }

	@Override
	public String getItemUrl() {
		return LibrelioApplication.getAmazonServerUrl() + filePath;
	}

    public String getDatabaseStoragePath() {
        return "/data/data/" + context.getPackageName()
                + "/databases/" + itemFilename + ".sqlite";
    }

	@Override
	public void onDownloadButtonClick(Context context) {
		ProductsDownloadService.startProductsDownload(context, this, false);
	}

	public void onReadButtonClicked(Context context) {
		ProductsActivity.startActivity(context, this);
	}
}