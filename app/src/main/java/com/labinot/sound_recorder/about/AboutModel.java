package com.labinot.sound_recorder.about;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.labinot.sound_recorder.R;


import java.util.ArrayList;
import java.util.List;

public class AboutModel {

    private final List<AboutModel> myAboutListModel = new ArrayList<>();
    private String contentID;
    private int icon;
    private String mTitle;
    private Context context;
    private Intent intent;
    private int tintColor;

    public AboutModel(Context context) {
        this.context = context;
    }

    public AboutModel() {
    }

    static boolean isAppInstalled(Context context, String appName) {

        PackageManager packageManager = context.getPackageManager();

        boolean installed = false;

        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo1 : packageInfo) {
            if (packageInfo1.packageName.equals(appName)) {
                installed = true;
                break;
            }
        }


        return installed;
    }

    public int getTintColor() {
        return tintColor;
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }

    public List<AboutModel> getMyAboutListModel() {
        return myAboutListModel;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getContentID() {
        return contentID;
    }

    public void setContentID(String contentID) {
        this.contentID = contentID;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void addNewItem(AboutModel model) {
        myAboutListModel.add(model);
    }

    public void addFacebook(String id, String title) {

        AboutModel facebookElement = new AboutModel();

        facebookElement.setTitle(title);
        facebookElement.setIcon(R.drawable.ic_facebook);
        facebookElement.setContentID(id);
        facebookElement.setTintColor(context.getResources().getColor(R.color.facebookColor));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        if (isAppInstalled(context, "com.facebook.katana")) {

            intent.setPackage("com.facebook.katana");

            int versionCode = 0;

            try {
                versionCode = context.getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Uri uri;

            if (versionCode >= 3002850) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + "http://m.facebook.com/" + id);
            } else {
                uri = Uri.parse("fb://page/" + id);
            }

            intent.setData(uri);

        } else {
            intent.setData(Uri.parse("http://m.facebook.com/" + id));
        }

        facebookElement.setIntent(intent);

        addNewItem(facebookElement);
    }

    public void addInstagram(String id, String title) {

        AboutModel instagramElement = new AboutModel();

        instagramElement.setTitle(title);
        instagramElement.setIcon(R.drawable.ic_instagram);
        instagramElement.setContentID(id);
        instagramElement.setTintColor(context.getResources().getColor(R.color.instagramColor));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://instagram.com/_u/" + id));

        if (isAppInstalled(context, "com.instagram.android")) {
            intent.setPackage("com.instagram.android");
        }

        instagramElement.setIntent(intent);

        addNewItem(instagramElement);

    }

    public void addGitHub(String id, String title) {

        AboutModel gitHubElement = new AboutModel();

        gitHubElement.setTitle(title);
        gitHubElement.setIcon(R.drawable.ic_github);
        gitHubElement.setContentID(id);
        gitHubElement.setTintColor(context.getResources().getColor(R.color.gitHubColor));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(String.format("https://github.com/%s", id)));

        gitHubElement.setIntent(intent);
        addNewItem(gitHubElement);
    }

    public void addEmail(String email, String title) {

        AboutModel emailElement = new AboutModel();
        emailElement.setTitle(title);
        emailElement.setIcon(R.drawable.ic_email);
        emailElement.setTintColor(context.getResources().getColor(R.color.emailColor));

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailElement.setIntent(intent);

        addNewItem(emailElement);

    }

    public void addWebsite(String url, String title) {

        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        AboutModel websiteElement = new AboutModel();
        websiteElement.setTitle(title);
        websiteElement.setIcon(R.drawable.ic_internet);
        websiteElement.setContentID(url);
        websiteElement.setTintColor(context.getResources().getColor(R.color.websiteColor));

        Uri uri = Uri.parse(url);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);

        websiteElement.setIntent(browserIntent);

        addNewItem(websiteElement);
    }

    public void addYoutube(String id, String title) {

        AboutModel youtubeElement = new AboutModel();
        youtubeElement.setTitle(title);
        youtubeElement.setIcon(R.drawable.ic_youtube);
        youtubeElement.setTintColor(context.getResources().getColor(R.color.youtubeColor));
        youtubeElement.setContentID(id);

        Intent intent;

        if (isAppInstalled(context, "com.google.android.youtube")) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format("http://youtube.com/channel/%s", id)));
            intent.setPackage("com.google.android.youtube");
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/channel/" + id));
        }

        youtubeElement.setIntent(intent);

        addNewItem(youtubeElement);

    }

    public void addPlayStore(String id, String title) {
        AboutModel playStoreElement = new AboutModel();
        playStoreElement.setTitle(title);
        playStoreElement.setIcon(R.drawable.ic_playstore);

        playStoreElement.setContentID(id);
        playStoreElement.setTintColor(context.getResources().getColor(R.color.playStoreColor));

        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + id);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        playStoreElement.setIntent(goToMarket);

        addNewItem(playStoreElement);
    }

    public void addCustomItem(int icon, int youColor, String title) {

        AboutModel customElement = new AboutModel();
        customElement.setTitle(title);
        customElement.setIcon(icon);
        customElement.setTintColor(youColor);

        addNewItem(customElement);

    }

}
