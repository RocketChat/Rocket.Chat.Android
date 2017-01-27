package chat.rocket.android.layouthelper.extra_action.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;

public abstract class AbstractUploadActionItem extends AbstractExtraActionItem {

  public static final int RC_UPL = 0x12;

  @Override
  public void handleItemSelectedOnActivity(Activity activity) {
    DetailItemInfo[] itemList = getDetailItemList();

    if (itemList.length >= 2) {
      showSelectionDialog(activity, itemList,
          index -> handleDetailItemInfo(activity, itemList[index]));
    } else if (itemList.length == 1) {
      handleDetailItemInfo(activity, itemList[0]);
    }
  }

  @Override
  public void handleItemSelectedOnFragment(Fragment fragment) {
    DetailItemInfo[] itemList = getDetailItemList();

    if (itemList.length >= 2) {
      showSelectionDialog(fragment.getContext(), itemList,
          index -> handleDetailItemInfo(fragment, itemList[index]));
    } else if (itemList.length == 1) {
      handleDetailItemInfo(fragment, itemList[0]);
    }
  }

  private void handleDetailItemInfo(Activity activity, DetailItemInfo info) {
    if (info != null) {
      activity.startActivityForResult(info.getIntent(), info.getReturnCode());
    }
  }

  private void handleDetailItemInfo(Fragment fragment, DetailItemInfo info) {
    if (info != null) {
      fragment.startActivityForResult(info.getIntent(), info.getReturnCode());
    }
  }

  private interface OnSelectedCallback {
    void onSelected(int index);
  }

  private void showSelectionDialog(Context context, DetailItemInfo[] itemList,
                                   OnSelectedCallback callback) {
    ArrayAdapter<DetailItemInfo> adapter = new ArrayAdapter<DetailItemInfo>(context,
        android.R.layout.simple_list_item_1,
        android.R.id.text1,
        itemList) {
      @NonNull
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        TextView text = (TextView) super.getView(position, convertView, parent);
        text.setText(itemList[position].getCaption());
        return text;
      }
    };

    // TODO: BottomSheet...
    new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
        .setAdapter(adapter, (dialogInterface, index) -> callback.onSelected(index))
        .show();
  }

  protected abstract DetailItemInfo[] getDetailItemList();

  protected interface DetailItemInfo {
    Intent getIntent();

    @StringRes int getCaption();

    /**
     * code used for param of startActivityForResult.
     */
    int getReturnCode();
  }

  @Override
  public int getBackgroundTint() {
    return R.color.colorAccent;
  }
}
