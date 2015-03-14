package ar.rulosoft.mimanganu.servers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.navegadores.Navegador;

public class EsMangaCom extends ServerBase {

	public static String[] generos = new String[] { "Todos", "Acción", "Artes Marciales", "Aventura", "Ciencia Ficción", "Comedia", "Deportes", "Drama",
			"Ecchi", "Escolar", "Fantasía", "Harem", "Hentai", "Histórico", "Horror", "Josei", "Mecha", "Misterio", "Oneshot", "Psicológico", "Romance",
			"Seinen", "Shojo", "Shounen", "Sobrenatural", "Tragedia", "Vida Cotidiana", "Yuri" };
	public static String[] generosV = new String[] { "/lista-mangas", "/genero/accion", "/genero/artes-marciales", "/genero/aventura",
			"/genero/ciencia-ficcion", "/genero/comedia", "/genero/deportes", "/genero/drama", "/genero/ecchi", "/genero/escolar", "/genero/fantasia",
			"/genero/harem", "/genero/hentai", "/genero/historico", "/genero/horror", "/genero/josei", "/genero/mecha", "/genero/misterio", "/genero/oneshot",
			"/genero/psicologico", "/genero/romance", "/genero/seinen", "/genero/shojo", "/genero/shounen", "/genero/sobrenatural", "/genero/tragedia",
			"/genero/vida-cotidiana", "/genero/yuri" };

	public EsMangaCom() {
		this.setBandera(R.drawable.flag_esp);
		this.setIcon(R.drawable.esmanga);
		this.setServerName("EsManga");
		setServerID(ServerBase.ESMANGA);
	}

	@Override
	public ArrayList<Manga> getMangas() throws Exception {
		String source = new Navegador().get("http://esmanga.com");
		ArrayList<Manga> mangas = new ArrayList<Manga>();
		Pattern pre = Pattern.compile("<div class=\"blk-hd\"><span>Todas las series Manga</span></div>[\\s\\S]+");
		Matcher preMatcher = pre.matcher(source);
		if (preMatcher.find()) {
			source = preMatcher.group();
		}
		Pattern p = Pattern.compile("<li>[^<]+<article>[\\S\\s]+?<h2><a href=\"(.+?)\">(.+?)<");
		Matcher m = p.matcher(source);
		while (m.find()) {
			mangas.add(new Manga(ESMANGA, m.group(2), m.group(1), false));
		}
		return mangas;
	}

	@Override
	public ArrayList<Manga> getBusqueda(String termino) throws Exception {
		String web = "http://esmanga.com/search/results?q=" + URLEncoder.encode(termino,"UTF-8");
		return getMangasWeb(web);
	}

	@Override
	public void cargarCapitulos(Manga m) throws Exception {
		if (m.getCapitulos() == null || m.getCapitulos().size() == 0)
			cargarPortada(m);
	}

	@Override
	public void cargarPortada(Manga m) throws Exception {
		Navegador nav = new Navegador();
		String source = nav.get(m.getPath());
		//sinopsis
		m.setSinopsis(getFirstMacthDefault("<div>Sinopsis:</div></td>[\\s\\S]+?<td class=\"contxt\">([\\s\\S]+?)<", source, "Sin Sinopsis"));
		//imagen
		m.setImages(getFirstMacthDefault("<div class=\"img-anim\">\\s+?<img src=\"(.+?)\"", source, ""));
		//status
		m.setFinalizado(getFirstMacthDefault("Estado.+?<td><strong>([^<]+)", source, "En desarrollo").length() == 9);
		// capitulos
		ArrayList<Capitulo> capitulos = new ArrayList<Capitulo>();
		Pattern p = Pattern.compile("<li>	<a href=\"(.+?)\".+?>(.+?)<strong>(.+?)<");
		Matcher ma = p.matcher(source);
		while (ma.find()) {
			capitulos.add(0,new Capitulo(ma.group(2).trim() + " " + ma.group(3).trim(), ma.group(1)));
		}
		m.setCapitulos(capitulos);
	}

	@Override
	public String getPagina(Capitulo c, int pagina) {
		return c.getPath() + "/" + pagina;
	}

	@Override
	public String getImagen(Capitulo c, int pagina) throws Exception {
		Navegador nav = new Navegador();
		String source = nav.get(this.getPagina(c, pagina));
		return getFirstMacth("src=\"([^\"]+\\d.(jpg|png|bmp))", source, "Error en plugin (obtener imager)");
	}

	@Override
	public void iniciarCapitulo(Capitulo c) throws Exception {
		Navegador nav = new Navegador();
		String source = nav.get(c.getPath());
		String textNum = getFirstMacth("option value=\"(\\d+)[^=]+</option></select>", source, "Error en plugin (obtener p�ginas)");
		c.setPaginas(Integer.parseInt(textNum));
	}

	@Override
	public ArrayList<Manga> getMangasFiltered(int categoria, int ordentipo, int pagina) throws Exception {
		String web = "http://esmanga.com" + generosV[categoria] + "?page=" + pagina ;
		return  getMangasWeb(web);
	}
	
	public ArrayList<Manga> getMangasWeb(String web) throws Exception{
		String source = new Navegador().get(web);
		Pattern p = Pattern.compile("<article class=\"pli-lg\">.+?href=\"(.+?)\".+?src=\"(.+?)\".alt=\"(.+?)\"");
		Matcher m = p.matcher(source);
		ArrayList<Manga> mangas = new ArrayList<Manga>();
		while(m.find()){
			Manga manga = new Manga(ESMANGA, m.group(3), m.group(1), false);
			manga.setImages(m.group(2));
			mangas.add(0,manga);
		}	
		return mangas;
	}

	@Override
	public String[] getCategorias() {
		return generos;
	}

	@Override
	public String[] getOrdenes() {
		return new String[] { "a-z" };//, "Ranking"
	}

	@Override
	public boolean tieneListado() {
		return true;
	}
	
	

}
