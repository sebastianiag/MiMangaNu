package ar.rulosoft.mimanganu.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DescargaCapitulo.DescargaEstado;
import ar.rulosoft.mimanganu.services.DescargaIndividual.Estados;

public class ServicioColaDeDescarga extends Service implements CambioEstado {
	public static ServicioColaDeDescarga actual = null;
	public static boolean intentPrending = false;
	public static ArrayList<DescargaCapitulo> descargas = new ArrayList<DescargaCapitulo>();
	int slots = 2;
	DescargaListener descargaListener = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		actual = this;
		intentPrending = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				iniciarCola();
			}
		}).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCambio(DescargaIndividual descargaIndividual) {
		if (descargaIndividual.estado.ordinal() > Estados.POSTERGADA.ordinal()) {
			slots++;
		}
		if (descargaListener != null) {
			descargaListener.onImagenDescargada(descargaIndividual.cid, descargaIndividual.index);
		}
	}

	public void setDescargaListener(DescargaListener descargaListener) {
		this.descargaListener = descargaListener;
	}

	public void iniciarCola() {
		Manga manga = null;
		ServerBase s = null;
		String ruta = "";
		int lcid = -1;
		while (!descargas.isEmpty()) {
			if (slots > 0) {
				slots--;
				DescargaCapitulo dc = null;
				int sig = 1;
				for (DescargaCapitulo d : descargas) {
					if (d.estado != DescargaEstado.ERROR) {
						sig = d.getSiguiente();
						if (sig > -1) {
							dc = d;
							break;
						}
					}
				}
				if (dc != null) {
					if (manga == null || manga.getId() != dc.capitulo.getMangaID()) {
						manga = Database.getManga(actual.getApplicationContext(), dc.capitulo.getMangaID());
						s = ServerBase.getServer(manga.getServerId());
					}
					if (lcid != dc.capitulo.getId()) {
						lcid = dc.capitulo.getId();
						ruta = generarRutaBase(s, manga, dc.capitulo);
						new File(ruta).mkdirs();
					}
					try {
						String origen = s.getImagen(dc.capitulo, sig);
						String destino = ruta + "/" + sig + ".jpg";
						DescargaIndividual des = new DescargaIndividual(origen, destino, sig - 1, dc.capitulo.getId());
						des.setCambioListener(dc);
						dc.setCambioListener(this);
						new Thread(des).start();
					} catch (Exception e) {
						dc.setErrorIdx(sig - 1);
						slots++;
					}
				} else if(slots == 1){
					break;
				}else{
					try {
						Thread.sleep(500);	
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					slots++;
				}
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		actual = null;
		stopSelf();
	}

	public static void agregarDescarga(Activity activity, Capitulo capitulo, boolean lectura) {
		if (!capitulo.isDescargado()) {
			if (!intentPrending && actual == null) {
				intentPrending = true;
				activity.startService(new Intent(activity, ServicioColaDeDescarga.class));
			}
			if (lectura)
				descargas.add(0, new DescargaCapitulo(capitulo));
			else
				descargas.add(new DescargaCapitulo(capitulo));
		}
	}

	public static void quitarDescarga(int index) {
		descargas.remove(index);
	}

	public static String generarRutaBase(ServerBase s, Manga m, Capitulo c) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/" + cleanFileName(s.getServerName()) + "/"
				+ cleanFileName(m.getTitulo()) + "/" + cleanFileName(c.getTitulo());
	}

	public static String generarRutaBase(ServerBase s, Manga m) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/" + cleanFileName(s.getServerName()) + "/"
				+ cleanFileName(m.getTitulo());
	}

	final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
			28, 29, 30, 31, 58, 42, 63, 92, 47 };

	static {
		Arrays.sort(illegalChars);
	}

	public static String cleanFileName(String badFileName) {
		StringBuilder cleanName = new StringBuilder();
		for (int i = 0; i < badFileName.length(); i++) {
			int c = (int) badFileName.charAt(i);
			if (Arrays.binarySearch(illegalChars, c) < 0) {
				cleanName.append((char) c);
			}
		}
		return cleanName.toString();
	}
}
