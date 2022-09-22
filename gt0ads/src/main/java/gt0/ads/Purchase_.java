package gt0.ads;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class Purchase_ {
    private final String SKU = "premium";
    private Activity activity;
    private OnPriceReceived onPriceReceived;
    private DB db;
    private AnalyticsCallback analyticsCallback;

    public void setAnalyticsCallback(AnalyticsCallback analyticsCallback) {
        this.analyticsCallback = analyticsCallback;
    }

    interface AnalyticsCallback {
        void OnPremiumPurchased();

        void OnPremiumPurchasedSuccess();

        void OnPremiumRestored();
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (analyticsCallback != null)
                    analyticsCallback.OnPremiumPurchased();
                db.setPremium(true);
                Toast.makeText(activity, "Premium Purchased", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    PackageManager packageManager = activity.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    activity.startActivity(mainIntent);
                    Runtime.getRuntime().exit(0);
                }, 600);
            }
        }

    };
    private BillingClient bc;
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                handlePremPurchases(list);
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Purchase.PurchasesResult queryAlreadyPurchasesResult = bc.queryPurchases(INAPP);
                List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if (alreadyPurchases != null) {
                    handlePremPurchases(alreadyPurchases);
                }
            }
        }
    };

    public Purchase_(Activity activity, OnPriceReceived onPriceReceived) {
        this.activity = activity;
        db = new DB(activity);
        this.onPriceReceived = onPriceReceived;
        bc = BillingClient.newBuilder(activity)
                .enablePendingPurchases().setListener(purchasesUpdatedListener).build();
        bc.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Purchase.PurchasesResult queryPurchase = bc.queryPurchases(INAPP);
                    List<Purchase> qPurchase = queryPurchase.getPurchasesList();
                    if (qPurchase != null && qPurchase.size() > 0) {
                        handlePremPurchases(qPurchase);
                    }
                    getPremiumPrice();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    void handlePremPurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.getSkus().contains(SKU) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    bc.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                } else {
                    if (analyticsCallback != null) {
                        analyticsCallback.OnPremiumPurchasedSuccess();
                    }
                    db.setPremium(true);
                    Toast.makeText(activity, "Premium Purchased", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(this::restartApp, 500);

                }
            } else if (purchase.getSkus().contains(SKU) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {

            } else if (purchase.getSkus().contains(SKU) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                db.setPremium(false);
            }
        }
    }

    private void initPurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(SKU);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        bc.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList != null && skuDetailsList.size() > 0) {
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetailsList.get(0))
                                    .build();
                            SkuDetails skuDetails = skuDetailsList.get(0);
                            if (skuDetails != null) {
                                String resultStr = skuDetails.getOriginalPrice();
                                onPriceReceived.onPriceString(resultStr);
                            }
                            bc.launchBillingFlow(activity, flowParams);
                        } else {
                            Toast.makeText(activity, "Purchase not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getPremiumPrice() {
        List<String> skuList = new ArrayList<>();
        skuList.add(SKU);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        bc.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList != null && skuDetailsList.size() > 0) {
                            SkuDetails skuDetails = skuDetailsList.get(0);
                            if (skuDetails != null) {
                                String resultStr = skuDetails.getOriginalPrice();
                                onPriceReceived.onPriceString(resultStr);
                            }
                        } else {
                            Toast.makeText(activity, "Purchase not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity,
                                " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void restartApp() {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        activity.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    public void purchase() {
        if (bc.isReady()) {
            initPurchase();
        } else {
            bc = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(purchasesUpdatedListener).build();
            bc.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initPurchase();
                    } else {
                        Toast.makeText(activity, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    public void restorePurchase() {
        Purchase.PurchasesResult queryPurchase = bc.queryPurchases(INAPP);
        List<Purchase> qPurchase = queryPurchase.getPurchasesList();
        if (qPurchase != null && qPurchase.size() > 0) {
            for (Purchase purchase : qPurchase) {
                if (purchase.getSkus().contains(SKU) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (analyticsCallback != null) {
                        analyticsCallback.OnPremiumRestored();
                    }
                    db.setPremium(true);
                    Toast.makeText(activity, "Purchase Restored", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(this::restartApp, 600);
                } else {
                    db.setPremium(false);
                    Toast.makeText(activity, "Premium not purchased, sorry", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public interface OnPriceReceived {
        void onPriceString(String price);
    }


}
