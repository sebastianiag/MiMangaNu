package ar.rulosoft.mimanganu.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga.DescargaIndividual.Estados;

public class ServicioColaDeDescarga extends Service implements Observer {
	public static ServicioColaDeDescarga actual = null;
	public static boolean intentPrending = false;
	public static ArrayList<DescargaCapitulo> descargas = new ArrayList<ServicioColaDeDescarga.DescargaCapitulo>();
	int slots = 2;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		actual = this;
		intentPrending = false;
		iniciarCola();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (((DescargaIndividual) data).estado.ordinal() > Estados.POSTERGADA.ordinal()) {
			slots++;
		}
	}

	public void iniciarCola() {
		Manga manga = null;
		ServerBase s = null;
		String ruta = "";
		while (!descargas.isEmpty()) {
			if (slots > 0) {
				DescargaCapitulo dc = null;
				int sig = 1;
				for (DescargaCapitulo d : descargas) {
					sig = d.getSiguiente();
					if (sig > -1) {
						dc = d;
						break;
					}
				}
				if (dc != null) {
					if (manga == null || manga.getId() != dc.capitulo.getMangaID()) {
						manga = Database.getManga(getApplicationContext(), dc.capitulo.getMangaID());
						s = ServerBase.getServer(manga.getServerId());
						ruta = generarRutaBase(s, manga, dc.capitulo);
					}
					try {
						String origen = s.getImagen(dc.capitulo, sig);
						String destino = ruta + sig + ".jpg";
						DescargaIndividual des = new DescargaIndividual(origen, destino, sig);
						des.addObserver(dc);
						des.addObserver(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					break;
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
		if (!intentPrending && actual == null) {
			intentPrending = true;
			activity.startService(new Intent(activity, ServicioColaDeDescarga.class));
		}
		if (lectura)
			descargas.add(0, new DescargaCapitulo(capitulo));
		else
			descargas.add(new DescargaCapitulo(capitulo));
	}

	public static class DescargaCapitulo implements Observer {
		public static enum DescargaEstado {
			EN_COLA, DESCARGANDO, DESCARGADO, ERROR
		};

		static final int MAX_ERRORS = 5;
		public DescargaEstado estado;
		Capitulo capitulo;
		Estados[] paginasStatus;
		int progreso = 0;

		public DescargaCapitulo(Capitulo capitulo) {
			this.capitulo = capitulo;
			paginasStatus = new Estados[capitulo.getPaginas()];
			for (Estados es : paginasStatus) {
				es = Estados.EN_COLA;
			}
			estado = DescargaEstado.EN_COLA;
		}

		public void cambiarEstado(DescargaEstado nuevoEstado) {
			this.estado = nuevoEstado;
		}

		public int getSiguiente() {
			int j = -2;
			if (estado.ordinal() < DescargaEstado.DESCARGADO.ordinal()) {
				if (estado == DescargaEstado.EN_COLA)
					cambiarEstado(DescargaEstado.DESCARGANDO);
				if (hayErrores()) {
					j = -11;
				} else if (progreso < capitulo.getPaginas()) {
					for (int i = 0; i < capitulo.getPaginas(); i++) {
						if (paginasStatus[i] == Estados.EN_COLA || paginasStatus[i] == Estados.POSTERGADA) {
							paginasStatus[i] = Estados.INICIADA;
							j = i;
							break;
						}
					}
				}
			}
			return (j + 1);
		}

		public boolean hayErrores() {
			int errors = 0;
			for (Estados e : paginasStatus) {
				if (e.ordinal() > Estados.DESCARGA_OK.ordinal()) {
					errors++;
					if (errors > MAX_ERRORS) {
						cambiarEstado(DescargaEstado.ERROR);
						break;
					}
				}
			}
			return errors > MAX_ERRORS;
		}

		public boolean estaDescargando() {
			boolean ret = false;
			for (Estados e : paginasStatus) {
				if (e.ordinal() < Estados.POSTERGADA.ordinal()) {
					ret = true;
					break;
				}
			}
			if (!ret)
				cambiarEstado(DescargaEstado.DESCARGADO);
			return ret;
		}

		public void setPaginaFinalizada(int pagina, Estados estado) {
			paginasStatus[(pagina - 1)] = estado;
			progreso++;
		}

		public void setPaginaStatus(int pagina, Estados estado) {
			paginasStatus[(pagina - 1)] = estado;
		}

		public int getProgreso() {
			return progreso;
		}

		public void setProgreso(int progreso) {
			this.progreso = progreso;
		}

		public int getFaltantes() {
			return capitulo.getPaginas() - progreso;
		}

		public Capitulo getCapitulo() {
			return capitulo;
		}

		public void setCapitulo(Capitulo capitulo) {
			this.capitulo = capitulo;
		}

		@Override
		public void update(Observable observable, Object data) {
			// TODO Auto-generated method stub

		}
	}

	public static class DescargaIndividual extends Observable implements Runnable {
		String origen, destino;
		int index;

		static enum Estados {
			EN_COLA, INICIADA, DESCARGANDO, REINTENTANDO, POSTERGADA, DESCARGA_OK, ERROR_CONECCION, ERROR_404, ERROR_TIMEOUT, ERROR_SUBIDA, ERROR_URL_INVALIDA, ERROR_ESCRIBIR_ARCHIVO, ERROR_ABRIR_ARCHIVO
		};

		Estados estado = Estados.EN_COLA;
		int reintentos = 3;

		public DescargaIndividual(String origen, String destino, int index) {
			super();
			this.origen = origen;
			this.destino = destino;
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public void run() {
			changeStatus(Estados.INICIADA);
			while (estado != Estados.DESCARGA_OK && reintentos > 0) {
				File o = new File(destino);
				if (o.length() == 0) {
					InputStream input;
					OutputStream output;
					int contentLenght;
					try {
						URL url = new URL(origen);
						HttpURLConnection con = (HttpURLConnection) url.openConnection();
						con.setConnectTimeout(3000);
						con.setReadTimeout(3000);
						int code = con.getResponseCode();
						if (code != 200) {
							if (code == 404) {
								changeStatus(Estados.ERROR_404);
								// TODO write dummy images
							} else {
								changeStatus(Estados.ERROR_CONECCION);
							}
							reintentos = 0;
							break;
						}
						contentLenght = con.getContentLength();
						input = con.getInputStream();
						output = new FileOutputStream(o);
					} catch (MalformedURLException e) {
						changeStatus(Estados.ERROR_URL_INVALIDA);
						reintentos = 0;
						break;
					} catch (FileNotFoundException e) {
						changeStatus(Estados.ERROR_ESCRIBIR_ARCHIVO);
						reintentos = 0;
						break;
					} catch (IOException e) {
						changeStatus(Estados.ERROR_ABRIR_ARCHIVO);
						reintentos = 0;
						break;
					}
					try {
						changeStatus(Estados.DESCARGANDO);
						byte[] buffer = new byte[4096];
						int bytesRead = 0;
						while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
							output.write(buffer, 0, bytesRead);
						}
					} catch (Exception e) {
						reintentos--;
						if (reintentos > 0) {
							changeStatus(Estados.REINTENTANDO);
						} else {
							changeStatus(Estados.ERROR_TIMEOUT);
						}
					} finally {
						if (estado != Estados.REINTENTANDO) {
							if (contentLenght > o.length()) {
								Log.e("MIMANGA DOWNLOAD", "content lenght =" + contentLenght + " tamaño =" + o.length() + " en =" + o.getPath());
								o.delete();
								reintentos--;
								changeStatus(Estados.REINTENTANDO);
							} else {
								Log.i("MIMANGA DOWNLOAD", "descarga ok =" + o.getPath());
								changeStatus(Estados.DESCARGA_OK);
							}
						}
						try {
							output.close();
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}
		}

		void changeStatus(Estados estado) {
			this.estado = estado;
			notifyObservers(this);
		}

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
