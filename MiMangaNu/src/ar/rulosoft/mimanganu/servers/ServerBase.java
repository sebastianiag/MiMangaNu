package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;

public abstract class ServerBase {

	public static final int MANGAPANDA = 1;
	public static final int ESMANGAONLINE = 2;
	public static final int ESMANGAHERE = 3;
	public static final int MANGAHERE = 4;
	public static final int MANGAFOX = 5;
	public static final int SUBMANGA = 6;
	public static final int ESMANGA = 7;
	public static final int HEAVENMANGACOM = 8;
	public static final int STARKANACOM = 9;
	public static final int ESNINEMANGA = 10;
	
	private String serverName;
	private int icon;
	private int bandera;
	private int serverID;
	public boolean hayMas = true;

	public static ServerBase getServer(int id) {
		ServerBase s = null;
		switch (id) {
		case MANGAPANDA:
			s = new MangaPanda();
			break;
		case ESMANGAHERE:
			s = new EsMangaHere();
			break;
		case ESMANGAONLINE:
			s = new EsMangaOnline();
			break;
		case MANGAHERE:
			s = new MangaHere();
			break;
		case MANGAFOX:
			s = new MangaFox();
			break;
		case SUBMANGA:
			s = new SubManga();
			break;
		case ESMANGA:
			s = new EsMangaCom();
			break;
		case HEAVENMANGACOM:
			s = new HeavenMangaCom();
			break;
		case STARKANACOM:
			s = new StarkanaCom();
			break;
		case ESNINEMANGA:
			s = new EsNineMangaCom();
			break;
		default:
			break;
		}
		return s;
	}

	// server
	public abstract ArrayList<Manga> getMangas() throws Exception;
	public abstract ArrayList<Manga> getBusqueda(String termino)throws Exception;

	// capitulos
	public abstract void cargarCapitulos(Manga m) throws Exception;

	public abstract void cargarPortada(Manga m) throws Exception;

	// manga
	public abstract String getPagina(Capitulo c, int pagina);

	public abstract String getImagen(Capitulo c, int pagina) throws Exception;

	public abstract void iniciarCapitulo(Capitulo c) throws Exception;
	

	// server visual
	public abstract ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception;

	public abstract String[] getCategorias();

	public abstract String[] getOrdenes();
	
	public abstract boolean tieneListado();
	
	public int buscarNuevosCapitulos(Manga manga, Context context)throws Exception{
		int returnValue = 0;
		Manga mangaDb = Database.getFullManga(context, manga.getId());
			this.cargarCapitulos(manga);
			int diff = manga.getCapitulos().size() - mangaDb.getCapitulos().size();
			if (diff > 0) {
				int diffEncontradas = 0;
				for (int j = manga.getCapitulos().size() - 1; j > 0 ; j--) {
					boolean flag = true;
					for (int k = 0; k < mangaDb.getCapitulos().size(); k++) {
						if (manga.getCapitulos().get(j).getPath().contains(mangaDb.getCapitulos().get(k).getPath())) {
							mangaDb.getCapitulos().remove(k);
							flag = false;
							break;
						}
					}
					if (flag) {
						manga.getCapitulo(j).setEstadoLectura(Capitulo.NUEVO);
						Database.addCapitulo(context, manga.getCapitulo(j), manga.getId());
						Log.e("Nuevo Manga", manga.getCapitulo(j).getTitulo());
						diffEncontradas++;
						if (diffEncontradas == diff) {
							Database.updateMangaNuevos(context, mangaDb, diff);
							returnValue = diff;
							break;
						}
					}
				}
			} else if (diff < 0) {
				
			}
		return returnValue;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getBandera() {
		return bandera;
	}

	public void setBandera(int bandera) {
		this.bandera = bandera;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getFirstMacth(String patron, String source, String errorMsj) throws Exception {
		Pattern p = Pattern.compile(patron);
		Matcher m = p.matcher(source);
		if (m.find()) {
			return m.group(1);
		}
		throw new Exception(errorMsj);
	}
	
	public boolean tieneNavegacionVisual(){
		return true;
	}

}
