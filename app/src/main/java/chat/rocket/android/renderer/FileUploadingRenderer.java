package chat.rocket.android.renderer;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import chat.rocket.persistence.realm.models.internal.FileUploading;

/**
 * rendering FileUploading status.
 */
public class FileUploadingRenderer extends AbstractRenderer<FileUploading> {
  public FileUploadingRenderer(Context context, FileUploading object) {
    super(context, object);
  }

  public FileUploadingRenderer progressInto(ProgressBar progressBar) {
    if (!shouldHandle(progressBar)) {
      return this;
    }

    if (object.getFilesize() >= Integer.MAX_VALUE) {
      int max = 1000;
      int progress = (int) (max * object.getUploadedSize() / object.getFilesize());
      progressBar.setProgress(progress);
      progressBar.setMax(max);
    } else {
      progressBar.setProgress((int) object.getUploadedSize());
      progressBar.setMax((int) object.getFilesize());
    }

    return this;
  }

  public FileUploadingRenderer progressTextInto(TextView uploadedSizeText, TextView totalSizeText) {
    if (!shouldHandle(uploadedSizeText) || !shouldHandle(totalSizeText)) {
      return this;
    }

    long uploaded = object.getUploadedSize();
    long total = object.getFilesize();

    if (total < 50 * 1024) { //<50KB
      uploadedSizeText.setText(String.format("%,d", uploaded));
      totalSizeText.setText(String.format("%,d", total));
    } else if (total < 8 * 1048576) { //<8MB
      uploadedSizeText.setText(String.format("%,d", uploaded / 1024));
      totalSizeText.setText(String.format("%,d KB", total / 1024));
    } else {
      uploadedSizeText.setText(String.format("%,d", uploaded / 1048576));
      totalSizeText.setText(String.format("%,d MB", total / 1048576));
    }

    return this;
  }
}
