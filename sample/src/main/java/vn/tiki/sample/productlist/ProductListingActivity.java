package vn.tiki.sample.productlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import javax.inject.Inject;
import vn.tiki.daggers.ActivityInjector;
import vn.tiki.daggers.Daggers;
import vn.tiki.noadapter2.DiffCallback;
import vn.tiki.noadapter2.OnlyAdapter;
import vn.tiki.sample.R;
import vn.tiki.sample.base.BaseMvpActivity;
import vn.tiki.sample.entity.Product;

public class ProductListingActivity
    extends BaseMvpActivity<ProductListingView, ProductListingPresenter>
    implements ActivityInjector, ProductListingView {

  @BindView(android.R.id.content) View rootView;
  @BindView(R.id.rvProducts) RecyclerView rvProducts;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

  @BindString(R.string.product_listing) String textProductListing;
  @BindString(R.string.product_listing_try_again) String textTryAgain;
  @BindString(R.string.product_listing_error_occurred) String textError;

  @Inject ProductListingPresenter presenter;

  private Snackbar snackbar;

  public static Intent intent(Context context) {
    return new Intent(context, ProductListingActivity.class);
  }

  @Override public Object activityModule() {
    return new ProductListing.Module(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_product_listing);
    ButterKnife.bind(this);
    Daggers.inject(this, this);

    configureToolbar();
    configureList();

    connect(presenter, this);
  }

  private void configureToolbar() {
    setSupportActionBar(toolbar);
    final ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar == null) {
      return;
    }
    setTitle(textProductListing);
  }

  private void configureList() {
    rvProducts.setLayoutManager(new GridLayoutManager(
        this,
        2,
        LinearLayoutManager.VERTICAL,
        false));
    rvProducts.setAdapter(new OnlyAdapter.Builder()
        .viewHolderFactory((parent, type) -> ProductViewHolder.create(parent))
        .diffCallback(new DiffCallback() {
          @Override public boolean areItemsTheSame(Object oldItem, Object newItem) {
            return oldItem instanceof Product
                && newItem instanceof Product;
          }

          @Override public boolean areContentsTheSame(Object oldItem, Object newItem) {
            return oldItem.equals(newItem);
          }
        })
        .build());
  }

  @Override public void showLoading() {
    swipeRefreshLayout.setRefreshing(true);
    if (snackbar != null) {
      snackbar.dismiss();
    }
  }

  @Override public void showContent(List<Product> items) {
    swipeRefreshLayout.setRefreshing(false);
    ((OnlyAdapter) rvProducts.getAdapter()).setItems(items);
  }

  @Override public void showLoadError() {
    swipeRefreshLayout.setRefreshing(false);
    snackbar = Snackbar.make(rootView, textError, Snackbar.LENGTH_INDEFINITE)
        .setAction(textTryAgain, __ -> presenter.onRetry());
    snackbar.show();
  }
}