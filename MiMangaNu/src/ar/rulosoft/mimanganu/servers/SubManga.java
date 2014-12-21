package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class SubManga extends ServerBase {

	public SubManga() {
		setServerID(SUBMANGA);
		setIcon(R.drawable.submanga_icon);
		this.setServerName("SubManga");
		setBandera(R.drawable.flag_esp);
	}

	@Override
	public ArrayList<Manga> getMangas() throws Exception {
		// <td><a href="(http://submanga.com/.+?)".+?</b>(.+?)<
		ArrayList<Manga> mangas = new ArrayList<Manga>();
		Navegador nav = new Navegador();
		String source = nav.get("http://submanga.com/series/n");
		Pattern p = Pattern.compile("<td><a href=\"(http://submanga.com/.+?)\".+?</b>(.+?)<");
		Matcher m = p.matcher(source);
		while (m.find()) {
			String name = m.group(2);
			if (name.indexOf("¡") == -1 && name.indexOf("¿") == -1 && name.indexOf("ñ") == -1 && name.indexOf("Ñ") == -1) {
				mangas.add(new Manga(SUBMANGA, name, m.group(1), false));
			}
		}
		return mangas;
	}

	@Override
	public ArrayList<Manga> getBusqueda(String termino) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cargarCapitulos(Manga manga) throws Exception {
		if (manga.getCapitulos().size() == 0) {
			Pattern p;
			Matcher m;
			String data = new Navegador().get((manga.getPath() + "/completa"));
			/*
			 * // portada p = Pattern.compile(PATRON_PORTADA); m =
			 * p.matcher(data);
			 * 
			 * if (m.find()) { manga.setImages(m.group(1)); }
			 * 
			 * // sinopsis p = Pattern.compile(PATRON_SINOPSIS); m =
			 * p.matcher(data); if (m.find()) { manga.setSinopsis(m.group(1)); }
			 * else { manga.setSinopsis("Sin sinopsis."); }
			 * 
			 * /
			 */// capitulos
			p = Pattern.compile("\"s\"><a href=\"(http://submanga.com/.+?)\">(.+?)</a>");
			m = p.matcher(data);

			while (m.find()) {
				String web = "http://submanga.com/c/" + m.group(1).substring(m.group(1).lastIndexOf("/"));
				Capitulo mc = new Capitulo(Html.fromHtml(m.group(2)).toString(), web);
				manga.addCapituloFirst(mc);
			}
		}
	}

	@Override
	public void cargarPortada(Manga manga) throws Exception {
		Pattern p;
		Matcher m;
		String data = new Navegador().get((manga.getPath()));

		p = Pattern.compile("<p class=\"cb\"><img src=\"(.+?)\".+?<p>(.+?)</p>");
		m = p.matcher(data);

		if (m.find()) {
			manga.setImages(m.group(1));
			manga.setSinopsis(Html.fromHtml(m.group(2)).toString());
		} else {
			manga.setSinopsis("Sin sinopsis.");
		}
	}

	@Override
	public String getPagina(Capitulo c, int pagina) {
		if (pagina > c.getPaginas()) {
			pagina = 1;
		}
		return c.getPath() + "/" + pagina;
	}

	@Override
	public String getImagen(Capitulo c, int pagina) throws Exception {
		// <img src="(http://.+?)"
		String data;
		data = new Navegador().get(this.getPagina(c, pagina));
		return getFirstMacth("<img src=\"(http://.+?)\"", data, "Error: no se pudo obtener el enlace a la imagen");
	}

	@Override
	public void iniciarCapitulo(Capitulo c) throws Exception {
		String data;
		data = new Navegador().get(c.getPath());
		String paginas = getFirstMacth("(\\d+)</option></select>", data, "Error: no se pudo obtener el numero de paginas");
		c.setPaginas(Integer.parseInt(paginas));
	}

	@Override
	public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getCategorias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getOrdenes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tieneListado() {
		return true;
	}

	@Override
	public boolean tieneNavegacionVisual() {
		return false;
	}

}
