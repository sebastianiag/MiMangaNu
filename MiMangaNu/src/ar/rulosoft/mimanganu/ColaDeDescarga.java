package ar.rulosoft.mimanganu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class ColaDeDescarga {

	public static boolean corriendo = false;
	public static int maximaPriorida = 0;
	public static ArrayList<Descarga> capitulos = new ArrayList<Descarga>();
	public static Descarga actual;
	public static String base_dir;
	public static Context context = null;
	public static int llaves = 3;
	public static ImagenDescargada imagenDescargadaListener = null;
	public static boolean hereComesANewChallenger = false;

	public static void iniciarCola(Context context_) {
		context = context_;
		if (!corriendo) {
			corriendo = true;
			llaves = 3;
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (!capitulos.isEmpty()) {
						hereComesANewChallenger = false;
						actual = capitulos.get(capitulos.size() - 1);
						final Manga m = Database.getFullManga(context, actual.capitulo.getMangaID());
						final ServerBase s = ServerBase.getServer(m.getServerId());
						int i = 0;
						String ruta = generarRutaBase(s, m, actual.capitulo);
						File storagePath = new File(ruta);
						storagePath.mkdirs();
						while ((((i = actual.getSiguiente()) > -1) || (llaves < 3)) && !actual.getCapitulo().isDescargado()) {
							if (i != -1) {
								boolean lanzada = false;
								while (!lanzada) {
									if (llaves > 0) {
										llaves--;
										descargar(actual.capitulo, ruta, s, i);
										lanzada = true;
										if (hereComesANewChallenger)
											break;
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
							} else {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							if (hereComesANewChallenger)
								break;
						}
						Log.e("MANGA DESCARGADO ", "lo mismo");
						if (!hereComesANewChallenger && actual.getSiguiente() == -1) {
							Database.UpdateCapituloDescargado(context, actual.capitulo.getId(), 1);
							capitulos.remove(actual);
						}
					}
					Log.i("MIMANGAFINALDESCARGAS", "Se han descargado");
					corriendo = false;
				}

				private void descargar(final Capitulo c, final String ruta, final ServerBase s, final int pagina) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								boolean estado = guardarImagen(s.getImagen(c, pagina), pagina + ".jpg", ruta);

								if (estado) {
									if (imagenDescargadaListener != null) {
										imagenDescargadaListener.onDescarga(c.getId(), pagina);
									}
									actual.setPaginaFinalizada(pagina);
								} else {
									actual.setPaginaError(pagina);
								}
							} catch (Exception e) {
								actual.setPaginaError(pagina);
								e.printStackTrace();
							} finally {
								ColaDeDescarga.llaves++;
							}
						}
					}).start();
				}
			}).start();
		}
	}

	public static boolean guardarImagen(String rutaWeb, String archivo, String rutaLocal) throws Exception {

		boolean finalizadaOK = true;
		File o = new File(rutaLocal, archivo);
		if (!o.exists()) {
			URL url = new URL(rutaWeb);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			int contentLenght = con.getContentLength();
			InputStream input = con.getInputStream();
			OutputStream output = new FileOutputStream(o);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead = 0;
				while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
					output.write(buffer, 0, bytesRead);
				}
			} catch (Exception e) {
				e.printStackTrace();
				finalizadaOK = false;
			} finally {
				if (contentLenght > o.length()) {
					o.delete();
					Log.e("MIMANGA DOWNLOAD", "content lenght =" + contentLenght + " tamaño =" + o.length() + " en =" + o.getPath());
					finalizadaOK = false;
				} else {
					Log.i("MIMANGA DOWNLOAD", "descarga ok =" + o.getPath());
				}
				output.close();
				input.close();
			}
		}
		return finalizadaOK;
	}

	public interface ImagenDescargada {
		void onDescarga(int cid, int pagina);
	}

	public static String generarRutaBase(ServerBase s, Manga m, Capitulo c) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/" + cleanFileName(s.getServerName()) + "/" + cleanFileName(m.getTitulo()) + "/" + cleanFileName(c.getTitulo());
	}

	public static String generarRutaBase(ServerBase s, Manga m) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/" + cleanFileName(s.getServerName()) + "/" + cleanFileName(m.getTitulo());
	}

	public static class Descarga {
		Capitulo capitulo;
		boolean[] paginasStatus;
		int progreso = 0;

		public Descarga(Capitulo capitulo) {
			this.capitulo = capitulo;
			paginasStatus = new boolean[capitulo.getPaginas()];
		}

		public int getSiguiente() {
			int j = -2;
			if (progreso < capitulo.getPaginas()) {
				for (int i = 0; i < capitulo.getPaginas(); i++) {
					if (paginasStatus[i] == false) {
						paginasStatus[i] = true;
						j = i;
						break;
					}
				}
			}
			return (j + 1);
		}

		public void setPaginaFinalizada(int pagina) {
			paginasStatus[(pagina - 1)] = true;
			progreso++;
		}

		public void setPaginaError(int pagina) {
			paginasStatus[(pagina - 1)] = false;
		}

		public void setPaginaStatus(int pagina, boolean status) {
			paginasStatus[(pagina - 1)] = status;
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

	}

	public static void add(Capitulo result) {
		capitulos.add(new Descarga(result));
		hereComesANewChallenger = true;
	}
	public static void addCola(Capitulo capitulo) {
		capitulos.add(0, new Descarga(capitulo));
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
