package in.lazymanstudios.uri_to_file;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import in.lazymanstudios.uri_to_file.model.MethodResultWrapper;
import in.lazymanstudios.uri_to_file.model.UriToFile;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.io.File;

public class UriToFileMethodHandler implements MethodChannel.MethodCallHandler {
  private final UriToFile model;

  public UriToFileMethodHandler(Context context) {
    model = new UriToFile(context);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call,
                           @NonNull MethodChannel.Result result) {
    switch (call.method) {
    case "fromUri": {
      String uriString = call.argument("uriString");
      model.fromUri(new MethodResultWrapper(result), uriString);
      break;
    }
    case "clearTemporaryFiles": {
      model.clearTemporaryFiles(new MethodResultWrapper(result));
      break;
    }
    case "toContentUri": {
      String path = call.argument("path");
      Uri contentUri = getContentUriFromPath(path);
      result.success(contentUri != null ? contentUri.toString() : null);
      break;
    }
    default: {
      result.notImplemented();
      break;
    }
    }
  }

  private Uri getContentUriFromPath(String absolutePath) {
    // 处理外部存储目录
    if (absolutePath.startsWith(
            Environment.getExternalStorageDirectory().getPath())) {
      String relativePath = absolutePath.substring(
          Environment.getExternalStorageDirectory().getPath().length() + 1);
      return DocumentsContract.buildDocumentUri(
          "com.android.externalstorage.documents", "primary:" + relativePath);
    }

    // 处理应用内部存储目录
    if (absolutePath.startsWith(context.getFilesDir().getPath())) {
      String relativePath =
          absolutePath.substring(context.getFilesDir().getPath().length() + 1);
      return DocumentsContract.buildDocumentUri(
          context.getPackageName() + ".fileprovider", "files/" + relativePath);
    }

    // 其他情况尝试使用FileProvider
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return FileProvider.getUriForFile(
            context, context.getPackageName() + ".fileprovider",
            new File(absolutePath));
      } else {
        return Uri.fromFile(new File(absolutePath));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
