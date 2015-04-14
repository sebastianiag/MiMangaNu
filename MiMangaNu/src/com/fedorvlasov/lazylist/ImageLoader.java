package com.fedorvlasov.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Handler;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Imaginable;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private static Map<Imaginable, String> imageViews = Collections.synchronizedMap(new WeakHashMap<Imaginable, String>());
	ExecutorService executorService;
	Handler handler = new Handler();// handler to display images in UI thread

	public ImageLoader(Context context) {
		imageViews.clear();
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(3);
	}

	final int stub_id = R.drawable.stub;
	
	public void DisplayImage(String url, Imaginable imageView) {
		if (alreadyDownloading(imageView, url)) {
			imageViews.put(imageView, url);
			Bitmap bitmap = memoryCache.get(url);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				queuePhoto(url, imageView);
				imageView.setImageResource(stub_id);
			}
		}
	}

	private boolean alreadyDownloading(Imaginable imageView, String url) {
		String tag = imageViews.get(imageView);
		if (tag == null || !tag.equals(url))
			return true;
		return false;
	}

	private void queuePhoto(String url, Imaginable imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl;
			String host = null;
			{
				int idx;
				if ((idx = url.indexOf("|")) > 0) {
					host = url.substring(idx + 1);
					url = url.substring(0, idx);
				}
			}
			imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setInstanceFollowRedirects(true);
			if (host != null) {
				conn.addRequestProperty("Host", host);
			}
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Throwable ex) {
			//ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		// try {
		// decode image size

		// return BitmapFactory.decodeFile(f.getPath());

		return convertBitmap(f.getPath());

		/*
		 * BitmapFactory.Options o = new BitmapFactory.Options();
		 * o.inJustDecodeBounds = true; FileInputStream stream1=new
		 * FileInputStream(f); BitmapFactory.decodeStream(stream1,null,o);
		 * stream1.close();
		 * 
		 * //Find the correct scale value. It should be the power of 2. final
		 * int REQUIRED_SIZE=70; int width_tmp=o.outWidth,
		 * height_tmp=o.outHeight; int scale=1; while(true){
		 * if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE) break;
		 * width_tmp/=2; height_tmp/=2; scale*=2; }
		 * 
		 * //decode with inSampleSize BitmapFactory.Options o2 = new
		 * BitmapFactory.Options(); o2.inSampleSize=scale; FileInputStream
		 * stream2=new FileInputStream(f); Bitmap
		 * bitmap=BitmapFactory.decodeStream(stream2, null, o2);
		 * stream2.close(); return bitmap;/ } catch (FileNotFoundException e) {
		 * } catch (IOException e) { e.printStackTrace(); } return null;
		 */
		//
	}

	@SuppressWarnings("deprecation")
	// Android lollipop automaticamente ignora estas lineas para verciones
	// anteriores es realmente necesario
	public static Bitmap convertBitmap(String path) {

		Bitmap bitmap = null;
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
										// memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be
											// used to recover the Bitmap data
											// after being clear, when it will
											// be used in the future
		bfOptions.inPreferredConfig = Config.RGB_565;
		bfOptions.inTempStorage = new byte[32 * 1024];

		File file = new File(path);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}

		try {
			if (fs != null) {
				bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public Imaginable imageView;

		public PhotoToLoad(String u, Imaginable i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				memoryCache.put(photoToLoad.url, bmp);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			} catch (Throwable th) {
				//th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
			imageViews.remove(photoToLoad.imageView);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

}
